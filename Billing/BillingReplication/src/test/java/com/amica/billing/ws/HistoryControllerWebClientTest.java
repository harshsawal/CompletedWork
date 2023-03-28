package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

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
public class HistoryControllerWebClientTest extends WebClientTestBase {
	
	protected String getResourceName() {
		return "history";
	}
	
	private LocalDateTime before;
	private LocalDateTime after;

	/**
	 * Request URLs for this class aren't relative to the base URL in  
	 * the prepared client, so we install a WebClient with no base URL.
	 */
	@Override
	@BeforeEach
	public void setUp() {
		client = WebClient.create();
	}
	
	private List<Update> getHistory(LocalDateTime when) {
		return getMany(service() + "?since=" + when.toString(), Update.class);
	}
	
	private void checkHistory(LocalDateTime when, boolean partial) {
		assertCorrectHistory(getHistory(when).stream(), partial, before, after);
	}
	
	@Test
	@SneakyThrows
	public void testGetHistory() {
		
		before = LocalDateTime.now();
		Thread.sleep(10);
		
		put(root() + "customers", new Customer(NEW_CUSTOMER_FIRST_NAME,
				NEW_CUSTOMER_LAST_NAME, NEW_CUSTOMER_TERMS), Customer.class);
		
		LocalDateTime during = LocalDateTime.now();
		Thread.sleep(10);
		Thread.sleep(10);
		
		post(root() + "invoices",
			new NewInvoice(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT),
			NewInvoice.class, Invoice.class);
		String paymentURL =  String.format("%s%s/%d?pay", 
				root(), "invoices", PAID_INVOICE_NUMBER);
		patch(paymentURL, Invoice.class);
		
		Thread.sleep(10);
		after = LocalDateTime.now();
		
		checkHistory(before, false);
		checkHistory(during, true);
		assertThat(getHistory(after), hasSize(0));
	}
}
