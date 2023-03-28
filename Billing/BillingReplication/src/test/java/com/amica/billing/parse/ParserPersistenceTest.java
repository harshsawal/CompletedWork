package com.amica.billing.parse;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;

/**
 * Unit test for the {@link ParserPersistence} class.
 * This absorbs a lot of logic from the prior version of the 
 * {@link BillingTest}, now that the persistence service is responsible
 * for finding and parsing the files.
 * 
 * @author Will Provost
 */
public class ParserPersistenceTest {

	public static final String SOURCE_FOLDER = "src/test/resources/data";

	private ParserPersistence persistence;
	
	@BeforeEach
	public void setUp() throws IOException {
		Files.createDirectories(Paths.get(TEMP_FOLDER));
		Files.createDirectories(Paths.get(OUTPUT_FOLDER));
		Files.copy(Paths.get(SOURCE_FOLDER, CUSTOMERS_FILENAME), 
				Paths.get(TEMP_FOLDER, CUSTOMERS_FILENAME),
				StandardCopyOption.REPLACE_EXISTING);
		Files.copy(Paths.get(SOURCE_FOLDER, INVOICES_FILENAME), 
				Paths.get(TEMP_FOLDER, INVOICES_FILENAME),
				StandardCopyOption.REPLACE_EXISTING);
		
		persistence = new ParserPersistence();
		persistence.setCustomersFile(TEMP_FOLDER + "/" + CUSTOMERS_FILENAME);
		persistence.setInvoicesFile(TEMP_FOLDER + "/" + INVOICES_FILENAME);
		persistence.load();
	}
	
	@Test
	public void testGetCustomers() {
		Map<String,Customer> customerMap = persistence.getCustomers(); 
		assertThat(customerMap.keySet(), 
				hasSize(GOOD_CUSTOMERS_MAP.keySet().size()));
	}
	
	@Test
	public void testGetInvoices() {
		Map<Integer,Invoice> invoices =persistence.getInvoices();
		assertThat(invoices.keySet(), hasSize(GOOD_INVOICES.size()));
		for (int number : invoices.keySet()) {
			assertThat(invoices.get(number), 
					samePropertyValuesAs(GOOD_INVOICES_MAP.get(number)));
		}
	}
	
	@Test
	public void testSaveCustomer() throws IOException {
		persistence.saveCustomer(new Customer("Customer", "Four", Terms.CASH));
		try ( Stream<String> lines = 
				Files.lines(Paths.get(TEMP_FOLDER, CUSTOMERS_FILENAME)); ) {
			assertThat(lines.anyMatch(s -> s.equals("Customer,Four,CASH")),
					equalTo(true));
		}
	}
	
	public void testSaveInvoice() throws IOException {
		persistence.saveInvoice(new Invoice
				(7, GOOD_CUSTOMERS.get(0), 999, LocalDate.now()));
		try ( Stream<String> lines = 
				Files.lines(Paths.get(TEMP_FOLDER, INVOICES_FILENAME)); ) {
			assertThat(lines.anyMatch(s -> s.startsWith("7,Customer,One,999.00,")),
					equalTo(true));
		}
	}
}
