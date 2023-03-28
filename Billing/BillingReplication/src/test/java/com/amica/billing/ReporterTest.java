package com.amica.billing;

import static com.amica.billing.TestUtility.*;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit test for the {@link Reporter}.
 * We set up a mock {@link Billing} object as a source for data,
 * and then compare the reporter's produced files to prepared ones with
 * the expected content for each report. The mock Billing object also uses
 * argument captors to get references to the reporter's listener methods,
 * and calls them to prove that the reporter generates certain reports 
 * in respnose to those simulated events (even though, in this case,
 * no data actually changes). 
 * 
 * @author Will Provost
 */
public class ReporterTest {
	
	public static final String EXPECTED_FOLDER = 
			"src/test/resources/expected/unit_test";
	
	/**
	 * Utility method that compares an actual file -- to be found in our
	 * output folder -- with an expected file -- to be found in our 
	 * expected folder. 
	 */
	public static void assertCorrectOutput(String filename) {
		Path actualPath = Paths.get(OUTPUT_FOLDER, filename);
		Path expectedPath = Paths.get(EXPECTED_FOLDER, filename);
		TestUtility.assertCorrectOutput(actualPath, expectedPath);
	}
	
	private Billing mockBilling;
	private Reporter reporter;
	private Consumer<Customer> customerListener;
	private Consumer<Invoice> invoiceListener;

	/**
	 * Create necessary folders, and remove any reports from prior tests,
	 * to avoid false positives caused by an expected file hanging around.
	 * Build (relatively) simple data sets and mock a Billing object that 
	 * will return them from its query methods. Create the reporter based on
	 * this mock Billing object, and capture the listeners that it passes
	 * in calls to addXXXListener() methods. 
	 */
	@BeforeEach
	@SuppressWarnings("unchecked") // argument captors
	public void setUp() throws IOException {
		Files.createDirectories(Paths.get(OUTPUT_FOLDER));
		Stream.of(Reporter.FILENAME_INVOICES_BY_NUMBER,
			Reporter.FILENAME_INVOICES_BY_CUSTOMER,
			Reporter.FILENAME_OVERDUE_INVOICES,
			Reporter.FILENAME_CUSTOMERS_AND_VOLUME)
				.forEach(f -> new File(OUTPUT_FOLDER, f).delete());
		
		mockBilling = createMockBilling();
		
		reporter = new Reporter(mockBilling);
		reporter.setOutputFolder(Paths.get(OUTPUT_FOLDER));
		reporter.setAsOf(AS_OF_DATE);
		
		ArgumentCaptor<Consumer<Customer>> customerCaptor = 
				ArgumentCaptor.forClass(Consumer.class);
		ArgumentCaptor<Consumer<Invoice>> invoiceCaptor = 
				ArgumentCaptor.forClass(Consumer.class);
		verify(mockBilling).addCustomerListener(customerCaptor.capture());
		verify(mockBilling).addInvoiceListener(invoiceCaptor.capture());
		customerListener = customerCaptor.getValue();
		invoiceListener = invoiceCaptor.getValue();
	}
	
	@Test
	public void testReportInvoicesOrderedByNumber() {
		reporter.reportInvoicesOrderedByNumber();
		assertCorrectOutput(Reporter.FILENAME_INVOICES_BY_NUMBER);
	}
	
	@Test
	public void testReportInvoicesGroupedByCustomer() {
		reporter.reportInvoicesGroupedByCustomer();
		assertCorrectInvoicesByCustomer(EXPECTED_FOLDER,
				Reporter.FILENAME_INVOICES_BY_CUSTOMER, 
				GOOD_CUSTOMERS.get(1).getName());
	}
	
	@Test
	public void testReportOverdueInvoices() {
		reporter.reportOverdueInvoices();
		assertCorrectOutput(Reporter.FILENAME_OVERDUE_INVOICES);
	}
	
	@Test
	public void testReportCustomersAndVolume() {
		reporter.reportCustomersAndVolume();
		assertCorrectOutput(Reporter.FILENAME_CUSTOMERS_AND_VOLUME);
	}
	
	@Test
	public void testOnCustomerChanged() {
		customerListener.accept(null);
		assertCorrectOutput(Reporter.FILENAME_CUSTOMERS_AND_VOLUME);
	}
	
	@Test
	public void testOnInvoiceChanged() {
		invoiceListener.accept(null);
		assertCorrectOutput(Reporter.FILENAME_INVOICES_BY_NUMBER);
		assertCorrectInvoicesByCustomer(EXPECTED_FOLDER,
				Reporter.FILENAME_INVOICES_BY_CUSTOMER, 
				GOOD_CUSTOMERS.get(1).getName());
		assertCorrectOutput(Reporter.FILENAME_OVERDUE_INVOICES);
		assertCorrectOutput(Reporter.FILENAME_CUSTOMERS_AND_VOLUME);
	}
}
