package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Update;
import com.amica.billing.ws.InvoiceController.NewInvoice;

import lombok.SneakyThrows;

/**
 * Functional test for the Billing web service, focused on the 
 * /history root URL. This test is not repeatable, because
 * unlike the integration tests it can't reset the database
 * underneath the running service (which caches data in memory).
 *  
 * @author Will Provost
 */
public class HistoryControllerRestTemplateTest extends RestTemplateTestBase {
	
	protected String getResourceName() {
		return "history";
	}
	
	private LocalDateTime before;
	private LocalDateTime after;

	private Update[] getHistory(LocalDateTime when) {
		return template.getForObject
				(queryParam("since=" + when.toString()), Update[].class);
	}
	
	private void checkHistory(LocalDateTime when, boolean partial) {
		assertCorrectHistory(Arrays.stream(getHistory(when)), 
				partial, before, after);
	}
	
	@Test
	@SneakyThrows
	public void testGetHistory() {
		
		before = LocalDateTime.now();
		Thread.sleep(10);
		
		template.put(root() + "customers", new Customer(NEW_CUSTOMER_FIRST_NAME,
				NEW_CUSTOMER_LAST_NAME, NEW_CUSTOMER_TERMS));
		
		LocalDateTime during = LocalDateTime.now();
		Thread.sleep(10);
		Thread.sleep(10);
		
		template.postForObject(root() + "invoices",
			new NewInvoice(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT),
				Invoice.class);
		String paymentURL =  String.format("%s%s/%d?pay", 
				root(), "invoices", PAID_INVOICE_NUMBER);
		template.patchForObject(paymentURL, "", Invoice.class);
		
		Thread.sleep(10);
		after = LocalDateTime.now();
		
		checkHistory(before, false);
		checkHistory(during, true);
		assertThat(getHistory(after).length, equalTo(0));
	}
}
