package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amica.billing.Invoice;
import com.amica.billing.TestUtility.IntegrationConfig;
import com.amica.billing.db.Migration;
import com.amica.billing.db.mongo.MongoPersistence;
import com.amica.billing.ws.Exceptions.CustomerNotFoundException;
import com.amica.billing.ws.Exceptions.InvoiceAlreadyPaidException;
import com.amica.billing.ws.Exceptions.InvoiceNotFoundException;
import com.amica.billing.ws.InvoiceController.InvoiceWithoutCustomer;
import com.amica.billing.ws.InvoiceController.NewInvoice;

/**
 * Integration test for the {@link InvoiceController}.
 * We use the test database, and reset the data before each test.
 *
 * @author Will Provost
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=IntegrationConfig.class)
public class InvoiceControllerIntegrationTest {

	@Autowired
	private InvoiceController controller;

	@Autowired
	private Migration migration;
	
	@Autowired
	private MongoPersistence persistence;

    @BeforeEach
    public void setUp() {
    	migration.migrate();
    	persistence.load();
    }

	@Test
	public void testGetInvoice() {
		Invoice invoice = controller.getInvoice(101);
		assertThat(invoice.getAmount(), closeTo(1000.0, 0.001));
	}

	@Test
	public void testGetInvoiceNonExistent() {
		assertThrows(InvoiceNotFoundException.class,
				() -> controller.getInvoice(9999));
	}

	@Test
	public void testCreateInvoice() {
		controller.createInvoice(new NewInvoice
				(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT));
		assertThat(controller.getInvoice(NEW_INVOICE_NUMBER), isNewInvoice());
	}

	@Test
	public void testCreateInvoiceWithNonExistentCustomer() {
		assertThrows(CustomerNotFoundException.class,
				() -> controller.createInvoice(new NewInvoice
					("Forest Whitaker", 100)));
	}

	@Test
	public void testPayInvoice() {
		assertThat(controller.payInvoice(PAID_INVOICE_NUMBER), isPaidInvoice());
		assertThat(controller.getInvoice(PAID_INVOICE_NUMBER), isPaidInvoice());
	}

	@Test
	public void testPayInvoiceNonExistent() {
		assertThrows(InvoiceNotFoundException.class,
				() -> controller.payInvoice(9999));
	}

	@Test
	public void testPayInvoiceAlreadyPaid() {
		assertThrows(InvoiceAlreadyPaidException.class,
				() -> controller.payInvoice(101));
	}

	@Test
	public void testGetInvoicesForCustomer() {
		List<InvoiceWithoutCustomer> invoices =
				controller.getInvoicesForCustomer("Chet Atkins");
		assertThat(invoices, hasSize(2));
		assertThat(invoices.get(0).getNumber(), equalTo(104));
	}

	@Test
	public void testGetInvoicesForCustomerNonExistent() {
		assertThrows(CustomerNotFoundException.class,
				() -> controller.getInvoicesForCustomer("First Last"));
	}

	@Test
	public void testGetInvoicesByCustomer() {
		Map<String,List<InvoiceWithoutCustomer>> invoices =
				controller.getInvoicesByCustomer();
		assertThat(invoices.keySet(), hasSize(13));
		assertThat(invoices, hasEntry
				(equalTo("Chet Atkins"), hasSize(2)));
	}

	@Test
	public void testGetOverdueInvoices() {
		List<Invoice> invoices =
				controller.getOverdueInvoices(AS_OF_DATE).toList();
		assertThat(invoices, hasSize(9));
		assertThat(invoices.get(0), hasProperty("number", equalTo(102)));
	}

	@Test
	public void testAddInvoiceAndGetInvoicesForCustomer() {
		final String customerName = "Chet Atkins";
		controller.createInvoice(new NewInvoice(customerName, 100));
		List<InvoiceWithoutCustomer> invoices =
				controller.getInvoicesForCustomer(customerName);
		assertThat(invoices, hasSize(3));
		assertThat(invoices.get(2).getNumber(), equalTo(125));
	}
}
