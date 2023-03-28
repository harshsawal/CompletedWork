package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.billing.Billing;

/**
 * Unit test for the {@link HistoryController}.
 *
 * @author Will Provost
 */
public class HistoryControllerTest {

	private Billing mockBilling;
	private HistoryController controller;
	
	@BeforeEach
	public void setUp() {
		mockBilling = createMockBilling();
		when(mockBilling.getHistory()).thenReturn(createMockHistory());		
		controller = new HistoryController(mockBilling);
	}

	@Test
	public void testGetHistory() {
		assertCorrectHistory(controller.getHistory(UPDATES_START),
				false, UPDATES_START, updatesEnd);
	}


	@Test
	public void testGetHistorySince() {
		assertCorrectHistory
			(controller.getHistory(UPDATES_START.plus(1, ChronoUnit.SECONDS)),
				true, UPDATES_START, updatesEnd);
	}
	@Test
	public void testGetHistoryEmpty() {
		assertThat(controller.getHistory(updatesEnd).count(), equalTo(0L));
	}
}
