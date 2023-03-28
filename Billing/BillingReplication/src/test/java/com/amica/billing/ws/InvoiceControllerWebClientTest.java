package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException.*;

import com.amica.billing.Invoice;
import com.amica.billing.ws.InvoiceController.InvoiceWithoutCustomer;
import com.amica.billing.ws.InvoiceController.NewInvoice;

/**
 * Functional test for the Billing web service, focused on the 
 * /invoices root URL. This test is not repeatable, because
 * unlike the integration tests it can't reset the database
 * underneath the running service (which caches data in memory).
 *  
 * @author Will Provost
 */
public class InvoiceControllerWebClientTest extends WebClientTestBase {

	protected String getResourceName() {
		return "invoices";
	}
	
	@Test
	public void testGetInvoice() {
		Invoice invoice = getOne("/101", Invoice.class);
		assertThat(invoice.getAmount(), closeTo(1000.0, 0.001));
	}

	@Test
	public void testGetInvoiceNonExistent() {
		assertThrows(NotFound.class, () -> getOne("/9999", Invoice.class));
	}

	@Test
	public void testCreateInvoice() {
		Invoice newInvoice = post(new NewInvoice
				(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT), 
				NewInvoice.class, Invoice.class);
		assertThat(newInvoice, isNewInvoice());

		Invoice invoice = getOne(("/" + newInvoice.getNumber()),
				Invoice.class);
		assertThat(invoice, isNewInvoice());
	}

	@Test
	public void testCreateInvoiceWithNonExistentCustomer() {
		assertThrows(NotFound.class,
			() -> post(new NewInvoice("Forest Whitaker", 100), 
				NewInvoice.class, Invoice.class));
	}

	@Test
	public void testPayInvoice() {
		assertThat(patch("/106?pay", Invoice.class).getPaidDate(), notNullValue());
		assertThat(getOne("/106?pay", Invoice.class).getPaidDate(), notNullValue());
	}

	@Test
	public void testPayInvoiceNonExistent() {
		assertThrows(NotFound.class,
				() -> patch("/999?pay", Invoice.class));
	}

	@Test
	public void testPayInvoiceAlreadyPaid() {
		assertThrows(Conflict.class,
				() -> patch("/101?pay", Invoice.class));
	}

	@Test
	public void testGetInvoicesForCustomer() {
		List<InvoiceWithoutCustomer> invoices = 
				getMany("?customerName=Chet Atkins", InvoiceWithoutCustomer.class);
		assertThat(invoices, hasSize(2));
		assertThat(invoices.get(0).getNumber(), equalTo(104));
	}

	@Test
	public void testGetInvoicesForCustomerNonExistent() {
		assertThrows(NotFound.class,
			() -> getMany("?customerName=X Y", InvoiceWithoutCustomer.class));
	}

	@SuppressWarnings("unchecked") // Generic response body type
	@Test
	public void testGetInvoicesByCustomer() {
		Map<String,List<InvoiceWithoutCustomer>> invoices =
				getOne("?byCustomer", Map.class);
		assertThat(invoices.keySet(), hasSize(13));
		assertThat(invoices, hasEntry
				(equalTo("Chet Atkins"), hasSize(2)));
	}

	@Test
	public void testGetOverdueInvoices() {
		List<Invoice> invoices = 
				getMany("?overdueAsOf=2022-01-08", Invoice.class);
		assertThat(invoices, hasSize(9));
		assertThat(invoices.get(0), hasProperty("number", equalTo(102)));
	}

	@Test
	public void testCreateInvoice_Invalid() {
		assertThrows(BadRequest.class,
			() -> post(new NewInvoice("June Carter", 0), 
				NewInvoice.class, Invoice.class));
	}
}
