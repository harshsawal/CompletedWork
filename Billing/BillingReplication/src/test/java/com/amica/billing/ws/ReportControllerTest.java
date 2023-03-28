package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.billing.Reporter;

import lombok.SneakyThrows;

/**
 * Unit test for the {@link ReportController}.
 * 
 * @author Will Provost
 */
public class ReportControllerTest {
	
	public static final String EXPECTED_FOLDER = 
			"src/test/resources/expected/unit_test";
	
	/**
	 * Utility method that compares an actual file -- to be found in our
	 * output folder -- with an expected file -- to be found in our 
	 * expected folder. 
	 */
	@SneakyThrows
	public static void assertCorrectOutput(String actual, String expectedFilename) {
		try ( Stream<String> lines = Files.lines(Paths.get
				(EXPECTED_FOLDER).resolve(expectedFilename)); ) {
			String expected = lines.collect(Collectors.joining("\n"));
			assertThat(actual, equalTo(expected));
		}
	}
	
	/**
	 * Applies "substream" logic using dropWhile()/takeWhile() to derive
	 * a string representing the customer heading and associated invoices
	 * for a single customer.
	 */
	public static String getCustomerGroup
			(Stream<String> lines, String customerName) {
		return lines
		.dropWhile(line -> !line.startsWith(customerName))
		.takeWhile(line -> !line.trim().isEmpty())
		.collect(Collectors.joining("\n"));	
	}
	
	/**
	 * Because the invoices-by-customer report has no predictable order,
	 * we test by seeking the customer heading and group of invoices for
	 * a single customer, wherever that may be found in each of the actual
	 * and expected files, and assert that they are identical. 
	 */
	public void assertCorrectInvoicesByCustomer(String actual, String filenameFilename) {
		final String CUSTOMER = GOOD_CUSTOMERS.get(1).getName();
		try (
			Stream<String> expectedLines = Files.lines(Paths.get
					(EXPECTED_FOLDER, Reporter.FILENAME_INVOICES_BY_CUSTOMER));
		) {
			String actualGroup = getCustomerGroup(Stream.of(actual.split("\n")), CUSTOMER);
			String expectedGroup = getCustomerGroup(expectedLines, CUSTOMER);
			assertThat("Couldn't find matching customer group", 
					actualGroup, equalTo(expectedGroup));
					
		} catch (IOException ex) {
			fail("Couldn't open actual and expected file content.", ex);
		}
	}
	
	private ReportController controller;

	@BeforeEach
	public void setUp() {
		controller = new ReportController();
		controller.setReportsFolder((EXPECTED_FOLDER));
	}
	
	@Test
	public void testReportInvoicesOrderedByNumber() {
		assertCorrectOutput(controller.getInvoicesByNumber(),
					Reporter.FILENAME_INVOICES_BY_NUMBER);
	}
	
	@Test
	public void testReportInvoicesGroupedByCustomer() {
		assertCorrectInvoicesByCustomer(controller.getInvoicesByCustomer(),
				Reporter.FILENAME_INVOICES_BY_CUSTOMER);
	}
	
	@Test
	public void testReportOverdueInvoices() {
		assertCorrectOutput(controller.getOverdueInvoices(),
				Reporter.FILENAME_OVERDUE_INVOICES);
	}
	
	@Test
	public void testReportCustomersAndVolume() {
		assertCorrectOutput(controller.getCustomersAndVolume(),
				Reporter.FILENAME_CUSTOMERS_AND_VOLUME);
	}
}
