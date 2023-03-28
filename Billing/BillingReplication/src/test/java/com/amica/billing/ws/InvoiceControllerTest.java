package com.amica.billing.ws;

import static com.amica.HasKeys.hasKeys;
import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.billing.Billing;
import com.amica.billing.ws.Exceptions.CustomerNotFoundException;
import com.amica.billing.ws.Exceptions.InvoiceAlreadyPaidException;
import com.amica.billing.ws.Exceptions.InvoiceNotFoundException;
import com.amica.billing.ws.InvoiceController.InvoiceWithoutCustomer;
import com.amica.billing.ws.InvoiceController.NewInvoice;

/**
 * Unit test for the {@link InvoiceController}.
 *
 * @author Will Provost
 */
public class InvoiceControllerTest {

	/**
	 * Helper method to get just the invoice numbers from a query result,
	 * as a list, for easier assertion/matching.
	 */
	public static Matcher<Stream<? extends InvoiceWithoutCustomer>>
			hasNumbersWOC(Integer... numbers) {
		return hasKeys(InvoiceWithoutCustomer::getNumber, numbers);
	}
	
	private Billing	 mockBilling;
	private InvoiceController controller;

	@BeforeEach
	public void setUp() {
		mockBilling = createMockBilling();
		controller = new InvoiceController(mockBilling);
	}

	@Test
	public void testGetInvoice() {
		assertThat(controller.getInvoice(1), 
				samePropertyValuesAs(GOOD_INVOICES.get(0)));
	}

	@Test
	public void testGetInvoiceNonExistent() {
		assertThrows(InvoiceNotFoundException.class, 
				() -> controller.getInvoice(9999));
	}

	@Test
	public void testCreateInvoice() {
		controller.createInvoice(new NewInvoice("First Last", 100));
		verify(mockBilling).createInvoice("First Last", 100);
	}

	@Test
	public void testCreateInvoiceWithNonExistentCustomer() {
		doThrow(new IllegalArgumentException())
				.when(mockBilling).createInvoice(any(), anyDouble());
		assertThrows(CustomerNotFoundException.class,
				() -> controller.createInvoice(new NewInvoice("A B", 100)));
	}

	@Test
	public void testPayInvoice() {
		controller.payInvoice(1);
		verify(mockBilling).payInvoice(1);
	}

	@Test
	public void testPayInvoiceNonExistent() {
		doThrow(new IllegalArgumentException())
				.when(mockBilling).payInvoice(anyInt());
		assertThrows(InvoiceNotFoundException.class, 
				() -> controller.payInvoice(1));
	}

	@Test
	public void testPayInvoiceAlreadyPaid() {
		doThrow(new IllegalStateException())
				.when(mockBilling).payInvoice(anyInt());
		assertThrows(InvoiceAlreadyPaidException.class,
				() -> controller.payInvoice(1));
	}

	@Test
	public void testGetInvoicesForCustomer() {
		List<InvoiceWithoutCustomer> results =
				controller.getInvoicesForCustomer(GOOD_CUSTOMERS.get(1).getName());
		assertThat(results.stream(), hasNumbersWOC(2, 3, 4));
	}

	@Test
	public void testGetInvoicesForCustomerNonExistent() {
		assertThrows(CustomerNotFoundException.class,
				() -> controller.getInvoicesForCustomer("First Last"));
	}

	@Test
	public void testGetInvoicesByCustomer() {
		Map<String,List<InvoiceWithoutCustomer>> map =
				controller.getInvoicesByCustomer();
		assertThat(map.get(GOOD_CUSTOMERS.get(0).getName()).stream(), 
				hasNumbersWOC(1));
		assertThat(map.get(GOOD_CUSTOMERS.get(1).getName()).stream(), 
				hasNumbersWOC(2, 3, 4));
		assertThat(map.get(GOOD_CUSTOMERS.get(2).getName()).stream(), 
				hasNumbersWOC(5, 6));
	}

	@Test
	public void testGetOverdueInvoices() {
		assertThat(controller.getOverdueInvoices(AS_OF_DATE), hasNumbers(4, 6, 1));
	}
	
}
