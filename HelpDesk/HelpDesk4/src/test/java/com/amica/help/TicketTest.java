package com.amica.help;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.comparesEqualTo;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.help.Ticket.Priority;
import com.amica.help.Ticket.Status;

/**
 * Unit test for the {@link Ticket} class.
 * 
 * @author Will Provost
 */
public class TicketTest {

	public static final String TECHNICIAN1_ID = "TECHNICIAN1_ID";
	public static final String TECHNICIAN1_NAME = "TECHNICIAN1_NAME";
	public static final int TECHNICIAN1_EXT = 12345;

	public static final String TECHNICIAN2_ID = "TECHNICIAN2_ID";
	public static final String TECHNICIAN2_NAME = "TECHNICIAN2_NAME";
	public static final int TECHNICIAN2_EXT = 56789;

	public static final int ID = 1;
	public static final String ORIGINATOR = "ORIGINATOR";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final Priority PRIORITY = Priority.HIGH;
	public static final String RESOLVE_REASON = "RESOLVE_REASON";
	public static final String WAIT_REASON = "WAIT_REASON";
	public static final String RESUME_REASON = "RESUME_REASON";
	public static final String NOTE = "NOTE";
	public static final Tag TAG1 = new Tag("TAG1");
	public static final Tag TAG2 = new Tag("TAG2");

	public static final String START_TIME = "1/3/22 13:37";

	protected Ticket ticket;
	protected Technician tech1;
	protected Technician tech2;
	protected Technician tech3;

	/**
	 * Custom matcher that assures that an {@link Event} added to a ticket has the
	 * expected ticket ID, timestamp, status, and note.
	 */
	protected Matcher<Event> eventWith(Status status, String note) {
		return allOf(instanceOf(Event.class), hasProperty("ticketID", equalTo(ID)),
				hasProperty("timestamp", equalTo(Clock.getTime())), hasProperty("newStatus", equalTo(status)),
				hasProperty("note", equalTo(note)));
	}

	/**
	 * Helper method to assert that the Nth (0-based) event on the target ticket has
	 * the expected ID, timestamp, status, and note.
	 */
	protected void assertHasEvent(int index, Status status, String note) {
		assertThat(ticket.getHistory().count(), equalTo(index + 1L));
		assertThat(ticket.getHistory().skip(index).findFirst().get(), eventWith(status, note));
	}

	protected Matcher<Ticket> ticketWith(int ID, String Originator, String description, Priority priority) {
		return allOf(instanceOf(Ticket.class), hasProperty("ID", equalTo(ID)),
				hasProperty("originator", equalTo(Originator)), hasProperty("description", equalTo(description)),
				hasProperty("priority", equalTo(priority)), hasProperty("technician", equalTo(null)));
	}

	/**
	 * Call init() to set the clock and create technicians Create the test target.
	 */

	protected void passOneMinute() {
		Clock.setTime(Clock.getTime() + 60000);
	}

	protected void assign() {
		passOneMinute();
		ticket.assign(tech1);
	}

	protected void reassign() {
		passOneMinute();
		ticket.assign(tech2);
	}

	protected void suspend() {
		passOneMinute();
		ticket.suspend(WAIT_REASON);
	}

	protected void resume() {
		passOneMinute();
		ticket.resume(RESUME_REASON);
	}

	protected void addNote() {
		passOneMinute();
		ticket.addNote(NOTE);
	}

	protected void init() {
		Clock.setTime(START_TIME);

		tech1 = mock(Technician.class);
		when(tech1.getID()).thenReturn(TECHNICIAN1_ID);
		when(tech1.getName()).thenReturn(TECHNICIAN1_NAME);
		when(tech1.toString()).thenReturn("Technician " + TECHNICIAN1_ID + ", " + TECHNICIAN1_NAME);

		tech2 = mock(Technician.class);
		when(tech2.getID()).thenReturn(TECHNICIAN2_ID);
		when(tech2.getName()).thenReturn(TECHNICIAN2_NAME);
		when(tech2.toString()).thenReturn("Technician " + TECHNICIAN2_ID + ", " + TECHNICIAN2_NAME);
	}

	@BeforeEach
	public void setUp() {
		init();
		ticket = new Ticket(ID, ORIGINATOR, DESCRIPTION, PRIORITY);
	}

//  @Test
//  public void test1_TicketInitialized() {
//	  assertThat(ticket, ticketWith(ID, ORIGINATOR, DESCRIPTION, PRIORITY));
//	  assertHasEvent(0, Status.CREATED, "Created ticket.");
//  }

	@Test
	public void test1_TicketInitialized() {
		assertThat(ticket.getID(), equalTo(ID));
		assertThat(ticket.getOriginator(), equalTo(ORIGINATOR));
		assertThat(ticket.getDescription(), equalTo(DESCRIPTION));
		assertThat(ticket.getPriority(), equalTo(PRIORITY));

		assertThat(ticket.getStatus(), equalTo(Status.CREATED));
		assertThat(ticket.getTechnician(), nullValue());
		assertThat(ticket.getTags().count(), equalTo(0L));

		assertHasEvent(0, Status.CREATED, "Created ticket.");
	}

	@Test
	public void test2_TicketPriorityCompare() {
		Ticket ticket1_lowPriority = new Ticket(2, ORIGINATOR, DESCRIPTION, Priority.LOW);
		Ticket ticket2_UrgentPriority = new Ticket(3, ORIGINATOR, DESCRIPTION, Priority.URGENT);
		Ticket ticket3_SamePriority = new Ticket(4, ORIGINATOR, DESCRIPTION, Priority.HIGH);

		assertThat(ticket, both(lessThan(ticket1_lowPriority)).and(greaterThan(ticket2_UrgentPriority)));
		assertThat(ticket, lessThanOrEqualTo(ticket3_SamePriority));
		// assertThat(ticket, comparesEqualTo(ticket3_SamePriority)); //this is to check
		// if they follow ascending order by ID
	}

	@Test
	public void test3_Assignment() {
		assign();

		assertThat(ticket.getStatus(), equalTo(Status.ASSIGNED));
		assertThat(ticket.getTechnician(), equalTo(tech1));
		assertHasEvent(1, Status.ASSIGNED, String.format("Assigned to Technician %s, %s.", TECHNICIAN1_ID, TECHNICIAN1_NAME));
		verify(tech1).assignTicket(ticket);
	}

}
