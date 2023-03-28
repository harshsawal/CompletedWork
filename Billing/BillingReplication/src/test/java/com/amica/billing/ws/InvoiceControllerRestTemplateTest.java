package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

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
public class InvoiceControllerRestTemplateTest extends RestTemplateTestBase {

	protected String getResourceName() {
		return "invoices";
	}
	
	@Test
	public void testGetInvoice() {
		Invoice invoice = template.getForObject
				(subResource("101"), Invoice.class);
		assertThat(invoice.getAmount(), closeTo(1000.0, 0.001));
	}

	@Test
	public void testGetInvoiceNonExistent() {
		assertThrows(HttpClientErrorException.NotFound.class,
				() -> template.getForObject(subResource("9999"), Invoice.class));
	}

	@Test
	public void testCreateInvoice() {
		Invoice newInvoice = template.postForObject(service(),
			new NewInvoice(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT),
				Invoice.class);
		assertThat(newInvoice, isNewInvoice());
		
		Invoice invoice = template.getForObject
			(subResource(Integer.toString(newInvoice.getNumber())),
				Invoice.class);
		assertThat(invoice, isNewInvoice());
	}

	@Test
	public void testCreateInvoiceWithNonExistentCustomer() {
		assertThrows(HttpClientErrorException.NotFound.class,
			() -> template.postForObject(service(),
				new NewInvoice("Forest Whitaker", 100), Invoice.class));
	}

	@Test
	public void testPayInvoice() {
		Invoice paid = template.patchForObject(subResource
				("" + PAID_INVOICE_NUMBER + "?pay"), "", Invoice.class);
		assertThat(paid, isPaidInvoice());
		
		Invoice gotten = template.getForObject
			(subResource("" + PAID_INVOICE_NUMBER), Invoice.class);
		assertThat(gotten, isPaidInvoice());
	}

	@Test
	public void testPayInvoiceNonExistent() {
		assertThrows(HttpClientErrorException.NotFound.class,
				() -> template.patchForObject(subResource("9999?pay"), "", String.class));
	}

	@Test
	public void testPayInvoiceAlreadyPaid() {
		assertThrows(HttpClientErrorException.Conflict.class,
				() -> template.patchForObject(subResource("101?pay"), "", String.class));
	}

	@Test
	public void testGetInvoicesForCustomer() {
		InvoiceWithoutCustomer[] invoices = template.getForObject
			(queryParam("customerName=Chet Atkins"),
				InvoiceWithoutCustomer[].class);
		assertThat(invoices.length, equalTo(2));
		assertThat(invoices[0].getNumber(), equalTo(104));
	}

	@Test
	public void testGetInvoicesForCustomerNonExistent() {
		assertThrows(HttpClientErrorException.NotFound.class,
			() -> template.getForObject(queryParam("customerName=X Y"),
				InvoiceWithoutCustomer[].class));
	}

	@SuppressWarnings("unchecked") // Generic response body type
	@Test
	public void testGetInvoicesByCustomer() {
		Map<String,List<InvoiceWithoutCustomer>> invoices =
				template.getForObject(queryParam("byCustomer"), Map.class);
		assertThat(invoices.keySet(), hasSize(13));
		assertThat(invoices, hasEntry
				(equalTo("Chet Atkins"), hasSize(2)));
	}

	@Test
	public void testGetOverdueInvoices() {
		Invoice[] invoices =
			template.getForObject(queryParam("overdueAsOf=2022-01-08"),
						Invoice[].class);
		assertThat(invoices.length, equalTo(9));
		assertThat(invoices[0], hasProperty("number", equalTo(102)));
	}

	@Test
	public void testCreateInvoice_Invalid() {
		assertThrows(HttpClientErrorException.BadRequest.class,
			() -> template.postForObject(service(),
				new NewInvoice("June Carter", 0), Invoice.class));
	}
}
