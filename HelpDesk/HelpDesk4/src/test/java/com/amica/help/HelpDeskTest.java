package com.amica.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.help.Ticket.Priority;
import com.amica.help.Ticket.Status;

/**
 * Unit test for the {@link HelpDesk} class.
 * 
 * @author Will Provost
 */
public class HelpDeskTest {

	public static final String TECH1 = "TECH1";
	public static final String TECH2 = "TECH2";
	public static final String TECH3 = "TECH3";

	public static final int TICKET1_ID = 1;
	public static final String TICKET1_ORIGINATOR = "TICKET1_ORIGINATOR";
	public static final String TICKET1_DESCRIPTION = "TICKET1_DESCRIPTION";
	public static final Priority TICKET1_PRIORITY = Priority.LOW;
	public static final int TICKET2_ID = 2;
	public static final String TICKET2_ORIGINATOR = "TICKET2_ORIGINATOR";
	public static final String TICKET2_DESCRIPTION = "TICKET2_DESCRIPTION";
	public static final Priority TICKET2_PRIORITY = Priority.HIGH;
	
	public static final String TAG1 = "TAG1";
	public static final String TAG2 = "TAG2";
	public static final String TAG3 = "TAG3";
	
	private HelpDesk helpDesk = new HelpDesk();
	private Technician tech1;
	private Technician tech2;
	private Technician tech3;

	/**
	 * Custom matcher that checks the contents of a stream of tickets
	 * against expected IDs, in exact order;
	 */
	public static class HasIDs extends TypeSafeMatcher<Stream<? extends Ticket>> {

		private String expected;
		private String was;
		
		public HasIDs(int... IDs) {
			int[] expectedIDs = IDs;
			expected = Arrays.stream(expectedIDs)
					.mapToObj(Integer::toString)
					.collect(Collectors.joining(", ", "[ ", " ]"));		
		}
		
		public void describeTo(Description description) {
			
			description.appendText("tickets with IDs ");
			description.appendText(expected);
		}
		
		@Override
		public void describeMismatchSafely
				(Stream<? extends Ticket> tickets, Description description) {
			description.appendText("was: tickets with IDs ");
			description.appendText(was);
		}

		protected boolean matchesSafely(Stream<? extends Ticket> tickets) {
			was = tickets.mapToInt(Ticket::getID)
					.mapToObj(Integer::toString)
					.collect(Collectors.joining(", ", "[ ", " ]"));
			return expected.equals(was);
		}
		
	}
	public static Matcher<Stream<? extends Ticket>> hasIDs(int... IDs) {
		return new HasIDs(IDs);
	}
// Step5 uses a generic stream matcher:
//	public static Matcher<Stream<? extends Ticket>> hasIDs(Integer... IDs) {
//		return HasKeys.hasKeys(Ticket::getID, IDs);
//	}
	
	public void createTicket1() {
		helpDesk.createTicket(TICKET1_ORIGINATOR, TICKET1_DESCRIPTION, TICKET1_PRIORITY);
	}
	
	public void createTicket2() {
		helpDesk.createTicket(TICKET2_ORIGINATOR, TICKET2_DESCRIPTION, TICKET2_PRIORITY);
	}
	
	@BeforeEach
	public void createTechnician() {
		helpDesk.addTechnician(TECH1, TECH1, 1);
		helpDesk.addTechnician(TECH2, TECH2, 2);
		helpDesk.addTechnician(TECH3, TECH3, 3);
		Clock.setTime(100);
		Iterator<Technician> iterator = helpDesk.getTechnicians().iterator();
		tech1 = iterator.next();
		tech2 = iterator.next();
		tech3 = iterator.next();
	}
	
	@Test
	public void test1_GetTicketById() throws Exception{
		createTicket1();
		createTicket2();
		assertEquals(TICKET2_DESCRIPTION, helpDesk.getTicketByID(TICKET2_ID).getDescription());
	}
	
	@Test
	public void test2_GetTicketByIdBeforeCreation() throws Exception{
		assertNull(helpDesk.getTicketByID(TICKET2_ID));
		createTicket1();
		createTicket2();
	}
	
	@Test
	public void test3_CreateTicketsBeforeTechnician() {
		HelpDesk help = new HelpDesk();
		assertThrows(IllegalStateException.class, () -> help.createTicket(TICKET1_ORIGINATOR, TICKET1_DESCRIPTION, TICKET1_PRIORITY));
	}
	
	@Test
	public void test4_AssignTicketToTechnician() {
		createTicket1();
		assertEquals(tech1, helpDesk.getTicketByID(TICKET1_ID).getTechnician());
		assertEquals(1L, tech1.getActiveTickets().count());
	}
	
	@Test
	public void test5_DirtyTicketAssignment() {
		createTicket1();
		Ticket ticket1 = helpDesk.getTicketByID(TICKET1_ID);
		tech2.assignTicket(ticket1); //this is bad
		
		createTicket2();
		assertEquals(tech3, helpDesk.getTicketByID(TICKET2_ID).getTechnician());
	}
	
	@Test
	public void test6_GetTicketStatus() {
		createTicket1();
		createTicket2();
		helpDesk.getTicketByID(TICKET2_ID).resolve("Resolved for testing!");
		assertEquals(1L, helpDesk.getTicketsByStatus(Status.ASSIGNED).count());
		assertEquals(1L, helpDesk.getTicketsByStatus(Status.RESOLVED).count());
	}
	
	@Test
	public void test7_GetTicketByNotStatus() {
		createTicket1();
		createTicket2();
		Stream<Ticket> result = helpDesk.getTicketsByNotStatus(Status.WAITING);//.sorted(Comparator.comparing(Ticket::getPriority).reversed());
		assertThat(result, hasIDs(2,1));
	}
	
	@Test
	public void test8_GetTicketByTags() {
		createTicket1();
		createTicket2();
		//Tags tags = new Tags();
		helpDesk.addTags(2, TAG1, TAG2);
		helpDesk.addTags(1, TAG1, TAG3);
		assertEquals(1L, helpDesk.getTicketsWithAnyTag(TAG2).count());
		assertEquals(2L, helpDesk.getTicketsWithAnyTag(TAG1).count());
	}
	
	@Test
	public void test9_GetTicketsByText() {
		createTicket1();
		createTicket2();
		Ticket ticket1 = helpDesk.getTicketByID(TICKET1_ID);
		Ticket ticket2 = helpDesk.getTicketByID(TICKET2_ID);
		ticket1.addNote("Testing some ticket for text extraction!");
		ticket2.addNote("Testing some tickets for text extraction!");
		assertEquals(2L, helpDesk.getTicketsByText("ticket").count());
		assertEquals(1L, helpDesk.getTicketsByText("tickets").count());
	}
	
	@Test
	public void test10_GetTicketsByTechnician() {
		createTicket1();
		createTicket2();
		Ticket ticket1 = helpDesk.getTicketByID(TICKET1_ID);
		Ticket ticket2 = helpDesk.getTicketByID(TICKET2_ID);
		tech1.assignTicket(ticket1);
		tech2.assignTicket(ticket2);
		
		assertEquals(1L, helpDesk.getTicketsByTechnician(TECH1).count());
		assertEquals(0, helpDesk.getTicketsByTechnician(TECH3).count());
	}
	
	@Test
	public void test11_GetLatestActivity() {
		createTicket1();
		Ticket ticket1 = helpDesk.getTicketByID(TICKET1_ID);
		
	}
}

