package com.amica.help;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.amica.help.Ticket.Priority;

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
}

