package com.amica.billing;

import static com.amica.HasKeys.hasKeys;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.amica.billing.Billing.CustomerAndVolume;

/**
 * This class gathers various constants, data sets, and utility methods 
 * common to multiple tests.
 * 
 * @author Will Provost
 */
public class TestUtility {

	public static final String TEMP_FOLDER = "temp";
	public static final String OUTPUT_FOLDER = "reports";
	public static final String CUSTOMERS_FILENAME = "customers.csv";
	public static final String INVOICES_FILENAME = "invoices.csv";

	public static final LocalDate AS_OF_DATE = LocalDate.of(2022, 1, 8);
	
	////////////////////////////////////////////////////////////////////////
	// These collections provide a small set of data for unit tests: 
	
	public static final List<Customer> GOOD_CUSTOMERS = Stream.of
		(new Customer("Customer", "One", Terms.CASH),
		 new Customer("Customer", "Two", Terms.CREDIT_45),
		 new Customer("Customer", "Three", Terms.CREDIT_30)).toList();
	
	public static final Map<String,Customer> GOOD_CUSTOMERS_MAP =
		GOOD_CUSTOMERS.stream().collect(Collectors.toConcurrentMap
			(Customer::getName, Function.identity()));

	public static final List<Customer> BAD_CUSTOMERS = GOOD_CUSTOMERS.subList(2, 3);

	public static final List<Invoice> GOOD_INVOICES = Stream.of
		(new Invoice(1, GOOD_CUSTOMERS.get(0), 100, LocalDate.of(2022,  1,  4)),
		 new Invoice(2, GOOD_CUSTOMERS.get(1), 200, LocalDate.of(2022,  1,  4), LocalDate.of(2022, 1, 5)),
		 new Invoice(3, GOOD_CUSTOMERS.get(1), 300, LocalDate.of(2022,  1,  6)),
		 new Invoice(4, GOOD_CUSTOMERS.get(1), 400, LocalDate.of(2021, 11, 11)),
		 new Invoice(5, GOOD_CUSTOMERS.get(2), 500, LocalDate.of(2022,  1,  4), LocalDate.of(2022, 1, 8)),
		 new Invoice(6, GOOD_CUSTOMERS.get(2), 600, LocalDate.of(2021, 12,  4))).toList();
	
	public static final Map<Integer,Invoice> GOOD_INVOICES_MAP =
			GOOD_INVOICES.stream().collect(Collectors.toMap
					(Invoice::getNumber, Function.identity()));

	public static final List<Invoice> BAD_INVOICES = IntStream.of(0, 1, 5)
			.mapToObj(GOOD_INVOICES::get).toList();
	
	////////////////////////////////////////////////////////////////////////
	// These values are primarily used in integration tests, 
	// but can be used with the unit-test data set, too:
	
	public static final String NEW_CUSTOMER_FIRST_NAME = "Merle";
	public static final String NEW_CUSTOMER_LAST_NAME = "Haggard";
	public static final Terms NEW_CUSTOMER_TERMS = Terms.CASH;
	public static final String NEW_CUSTOMER_NAME = 
			NEW_CUSTOMER_FIRST_NAME + " " + NEW_CUSTOMER_LAST_NAME;
	
	public static final int NEW_INVOICE_NUMBER = 125;
	public static final String NEW_INVOICE_CUSTOMER_FIRST_NAME = "John";
	public static final String NEW_INVOICE_CUSTOMER_LAST_NAME = "Hiatt";
	public static final Terms NEW_INVOICE_CUSTOMER_TERMS = Terms.CREDIT_90;
	public static final double NEW_INVOICE_AMOUNT = 999.0;
	public static final String NEW_INVOICE_CUSTOMER_NAME = 
		NEW_INVOICE_CUSTOMER_FIRST_NAME + " " + 
			NEW_INVOICE_CUSTOMER_LAST_NAME;
	
	public static final int PAID_INVOICE_NUMBER = 107;
	public static final String PAID_INVOICE_CUSTOMER_FIRST_NAME = "Glen";
	public static final String PAID_INVOICE_CUSTOMER_LAST_NAME = "Campbell";
	public static final Terms PAID_INVOICE_CUSTOMER_TERMS = Terms.CREDIT_60;
	public static final double PAID_INVOICE_AMOUNT = 800.0;
	public static final LocalDate PAID_INVOICE_DATE = LocalDate.of(2021, 9, 15);
	public static final String PAID_INVOICE_CUSTOMER_NAME = 
			PAID_INVOICE_CUSTOMER_FIRST_NAME + " " + 
					PAID_INVOICE_CUSTOMER_LAST_NAME;
	
	public static final LocalDateTime UPDATES_START = 
			LocalDateTime.of(2022, 1, 1, 8, 0, 0);
	public static LocalDateTime updatesEnd;
	
	public static Stream<Update> createMockHistory() {
		LocalDateTime clock = UPDATES_START; 
		
		Update update1 =  new Update(new Customer(NEW_CUSTOMER_FIRST_NAME, 
				NEW_CUSTOMER_LAST_NAME, NEW_CUSTOMER_TERMS));
		clock = clock.plus(1, ChronoUnit.SECONDS);
		update1.setWhen(clock);
		
		Update update2 = new Update(new Invoice(NEW_INVOICE_NUMBER, 
			new Customer(NEW_INVOICE_CUSTOMER_FIRST_NAME, 
					NEW_INVOICE_CUSTOMER_LAST_NAME, 
					NEW_INVOICE_CUSTOMER_TERMS), 
			NEW_INVOICE_AMOUNT, LocalDate.now()));
		clock = clock.plus(1, ChronoUnit.SECONDS);
		update2.setWhen(clock);
		
		Update update3 = new Update(new Invoice(PAID_INVOICE_NUMBER, 
			new Customer(PAID_INVOICE_CUSTOMER_FIRST_NAME, 
					PAID_INVOICE_CUSTOMER_LAST_NAME, 
					PAID_INVOICE_CUSTOMER_TERMS), 
			PAID_INVOICE_AMOUNT, PAID_INVOICE_DATE, LocalDate.now()));
		clock = clock.plus(1, ChronoUnit.SECONDS);
		update3.setWhen(clock);
	
		updatesEnd = clock.plus(1, ChronoUnit.SECONDS);
		
		return Stream.of(update1, update2, update3);
		
	}

	/**
	 * Helper to produce a list of matchers that assert each candidate object
	 * has the same properties as the corresponding list element.
	 */
	public static <T> List<Matcher<? super T>> samePropertiesAsList(List<T> source) {
		Stream<Matcher<? super T>> matchers = 
				source.stream().map(Matchers::samePropertyValuesAs);
		return matchers.toList();
	}

	/**
	 * Custom matcher that facilitates comparing two lists when we expect
	 * all properties of the compared elements to match.
	 */
	public static <T> Matcher<Iterable<? extends T>> sameAsList(List<T> source) {
		return contains(samePropertiesAsList(source));
	}
	
	/**
	 * Loads the contents of files at the two given paths,
	 * and asserts that they are the same, first subjecting the content of
	 * each file to the given canonicalization function.  
	 */
	public static void assertCorrectOutput(Path actualPath, 
			Path expectedPath, Function<String,String> canonicalizer) {
		try ( 
			Stream<String> actualLines = Files.lines(actualPath);
			Stream<String> expectedLines = Files.lines(expectedPath);
		) {
			String actual = actualLines.collect(Collectors.joining("\n"));
			String expected = expectedLines.collect(Collectors.joining("\n"));
			assertThat(canonicalizer.apply(actual), 
					equalTo(canonicalizer.apply(expected)));
		} catch (IOException ex) {
			fail("Couldn't open actual and expected file content.", ex);
		}
	}
	
	/**
	 * Loads the tonents of files at the two given paths,
	 * and asserts that they are exactly the same.  
	 */
	public static void assertCorrectOutput(Path actualPath, Path expectedPath) {
		assertCorrectOutput(actualPath, expectedPath, Function.identity());
	}

	/**
	 * Custom matcher that assures a stream of invoices has the expected
	 * invoice numbers, in order.
	 */
	public static Matcher<Stream<? extends Invoice>> hasNumbers(Integer... numbers) {
		return hasKeys(Invoice::getNumber, numbers);
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
	public static void assertCorrectInvoicesByCustomer(String expectedFolder, String filename, String customerName) {
		try (
			Stream<String> actualLines = 
					Files.lines(Paths.get(OUTPUT_FOLDER, filename)); 
			Stream<String> expectedLines = Files.lines(Paths.get
					(expectedFolder, Reporter.FILENAME_INVOICES_BY_CUSTOMER));
		) {
			String actualGroup = getCustomerGroup(actualLines, customerName);
			String expectedGroup = getCustomerGroup(expectedLines, customerName);
			assertThat(actualGroup.length(), greaterThan(0));
			assertThat("Couldn't find matching customer group", 
					actualGroup, equalTo(expectedGroup));
					
		} catch (IOException ex) {
			fail("Couldn't open actual and expected file content.", ex);
		}
	}

	/**
	 * Spring configuration for integration tests.
	 */
    @ComponentScan
    @EnableAutoConfiguration
    @EnableMongoRepositories
    @EnableScheduling
    @PropertySource({"classpath:test.properties","classpath:migration.properties"})
	public static class IntegrationConfig {
	}
    
    /**
     * Creates a mock Billing object with the unit-test data set.
     */
	public static Billing createMockBilling() {
		Map<Customer,List<Invoice>> invoicesByCustomer = new HashMap<>();
		invoicesByCustomer.put(GOOD_CUSTOMERS.get(0), GOOD_INVOICES.subList(0, 1));
		invoicesByCustomer.put(GOOD_CUSTOMERS.get(1), GOOD_INVOICES.subList(1, 4));
		invoicesByCustomer.put(GOOD_CUSTOMERS.get(2), GOOD_INVOICES.subList(4, 6));
				
		Stream<Invoice> overdueInvoices = 
				IntStream.of(3, 5, 0).mapToObj(GOOD_INVOICES::get);
		
		List<CustomerAndVolume> customersAndVolume = new ArrayList<>();
		
		CustomerAndVolume cv1 = mock(CustomerAndVolume.class);
		when(cv1.getCustomer()).thenReturn(GOOD_CUSTOMERS.get(2));
		when(cv1.getVolume()).thenReturn(1100.0);
		customersAndVolume.add(cv1);
		
		CustomerAndVolume cv2 = mock(CustomerAndVolume.class);
		when(cv2.getCustomer()).thenReturn(GOOD_CUSTOMERS.get(1));
		when(cv2.getVolume()).thenReturn(900.0);
		customersAndVolume.add(cv2);
		
		CustomerAndVolume cv3 = mock(CustomerAndVolume.class);
		when(cv3.getCustomer()).thenReturn(GOOD_CUSTOMERS.get(0));
		when(cv3.getVolume()).thenReturn(100.0);
		customersAndVolume.add(cv3);
		
		Billing mockBilling = mock(Billing.class);
		when(mockBilling.getCustomers()).thenReturn(GOOD_CUSTOMERS_MAP);
		when(mockBilling.getInvoices()).thenReturn(GOOD_INVOICES_MAP);
		when(mockBilling.getInvoicesOrderedByNumber())
				.thenReturn(GOOD_INVOICES.stream());
		when(mockBilling.getInvoicesGroupedByCustomer())
				.thenReturn(invoicesByCustomer);
		when(mockBilling.getOverdueInvoices(AS_OF_DATE))
				.thenReturn(overdueInvoices);
		when(mockBilling.getCustomersAndVolumeStream())
				.thenReturn(customersAndVolume.stream());
		
		return mockBilling;
	}

	/**
	 * Matcher that checks if the customer matches our new-customer constants.
	 */
	public static Matcher<Object> isNewCustomer() {
		return allOf(hasProperty("name", equalTo(NEW_CUSTOMER_NAME)),
				hasProperty("terms", equalTo(NEW_CUSTOMER_TERMS)));
	}
	
	/**
	 * Matcher that checks if the invoice matches our new-invoice constants.
	 */
	public static Matcher<Object> isNewInvoice() {
		return allOf(hasProperty("number", equalTo(NEW_INVOICE_NUMBER)),
				hasProperty("customer", allOf
					(hasProperty("name", equalTo(NEW_INVOICE_CUSTOMER_NAME)),
						hasProperty("terms", equalTo(NEW_INVOICE_CUSTOMER_TERMS)))),
				hasProperty("amount", closeTo(NEW_INVOICE_AMOUNT, 0.0001)),
				hasProperty("issueDate", equalTo(LocalDate.now())),
				hasProperty("paidDate", hasProperty("present", equalTo(false))));
	}
	
	/**
	 * Matcher that checks if the invoice matches our paid-invoice constants.
	 */
	public static Matcher<Object> isPaidInvoice() {
		return allOf(hasProperty("number", equalTo(PAID_INVOICE_NUMBER)),
				hasProperty("customer", allOf
					(hasProperty("name", equalTo(PAID_INVOICE_CUSTOMER_NAME)),
						hasProperty("terms", equalTo(PAID_INVOICE_CUSTOMER_TERMS)))),
				hasProperty("amount", closeTo(PAID_INVOICE_AMOUNT, 0.0001)),
				hasProperty("issueDate", equalTo(PAID_INVOICE_DATE)),
				hasProperty("paidDate", hasProperty("present", equalTo(true))));
	}
	
	/**
	 * Helper that provides a matcher for the timestamp on an update.
	 */
	public static Matcher<Update> isOnTime(LocalDateTime before, LocalDateTime after) {
		return hasProperty("when", both(greaterThanOrEqualTo(before))
				.and(lessThanOrEqualTo(after)));
	}
	
	/**
	 * Helper to check the history after a sequence of create-customer, 
	 * create-invoice, and pay-invoice operations.
	 */
	public static void assertCorrectHistory(Stream<Update> historyStream,
			boolean partial, LocalDateTime before, LocalDateTime after) {
		
		List<Update> history = historyStream.toList();
		assertThat(history, hasSize(partial ? 2 : 3));
		
		int index = 0;
		if (!partial) {
			Update update1 = history.get(index++);
			assertThat(update1, isOnTime(before, after));
			assertThat(update1.getInvoice(), nullValue());
			assertThat(update1.getCustomer(), isNewCustomer());
		}
		
		Update update2 = history.get(index++);
		assertThat(update2, isOnTime(before, after));
		assertThat(update2.getCustomer(), nullValue());
		Update.UpdatedInvoice update2Invoice = update2.getInvoice();
		assertThat(update2Invoice, isNewInvoice());
		
		Update update3 = history.get(index++);
		assertThat(update3, isOnTime(before, after));
		assertThat(update3.getCustomer(), nullValue());
		Update.UpdatedInvoice update3Invoice = update3.getInvoice();
		assertThat(update3Invoice, isPaidInvoice());
	}
}
