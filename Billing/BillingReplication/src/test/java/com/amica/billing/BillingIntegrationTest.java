package com.amica.billing;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amica.billing.TestUtility.IntegrationConfig;
import com.amica.billing.db.CustomerRepository;
import com.amica.billing.db.HistoryRepository;
import com.amica.billing.db.InvoiceRepository;
import com.amica.billing.db.Migration;
import com.amica.billing.db.mongo.MongoPersistence;

/**
 * Integration test for the {@link Billing} class.
 * This test focuses on the "country singers" data. We make a copy of the
 * data files at the start of each test case, create the Billing object
 * to load them, and check its getters and query methods.
 * A few more test cases drive updates to the object, and assure that
 * they are reflected in updates to the staged data files.
 * 
 * @author Will Provost
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=IntegrationConfig.class)
public class BillingIntegrationTest {

	public static final String SOURCE_FOLDER = "data";

	@Autowired
	protected Billing billing;
	
	@Autowired
	private MongoPersistence persistence;
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired 
	private InvoiceRepository invoiceRepo;
	
	@Autowired
	private HistoryRepository historyRepo;
		
    @Autowired
    private Migration migration;
    
    @BeforeEach
    public void setUp() {
    	migration.migrate();
    	persistence.load();
    }
    
	@Test
	public void testGetInvoicesOrderedByNumber() {
		assertThat(billing.getInvoicesOrderedByNumber(),
				hasNumbers(101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 121, 122, 123, 124));
	}
	
	@Test
	public void testGetInvoicesOrderedByDate() {
		assertThat(billing.getInvoicesOrderedByDate(),
				hasNumbers(101, 102, 103, 104, 110, 105, 106, 107, 109, 111, 112, 113, 108, 114, 115, 116, 117, 118, 119, 121, 123, 122, 124));
	}
	
	@Test
	public void testGetInvoicesGroupedByCustomer() {
		Map<Customer,List<Invoice>> map = billing.getInvoicesGroupedByCustomer();
		Customer jerryReed = billing.getCustomers().get("Jerry Reed");
		assertThat(map.get(jerryReed).stream(), hasNumbers(109, 122));
	}

	@Test
	public void testGetOverdueInvoices() {
		assertThat(billing.getOverdueInvoices(LocalDate.of(2022, 1, 8)),
				hasNumbers(102, 105, 106, 107, 113, 116, 118, 122, 124));
	}
	
	@Test
	public void testGetCustomersAndVolume() {
		List<Billing.CustomerAndVolume> list = 
				billing.getCustomersAndVolumeStream().toList();
		
		assertThat(list.get(0).getCustomer(), hasProperty("name", equalTo("Jerry Reed")));
		assertThat(list.get(0).getVolume(), closeTo(2640.0, .0001));
		
		Billing.CustomerAndVolume last = list.get(list.size() - 1);
		assertThat(last.getCustomer(), hasProperty("name", equalTo("Janis Joplin")));
		assertThat(last.getVolume(), closeTo(510.0, .0001));
	}
	
	/**
	 * After adding a customer, assure that there is one new line in the
	 * customers data file.
	 */
	@Test
	public void testCreateCustomer() {
		billing.createCustomer(NEW_CUSTOMER_FIRST_NAME, 
				NEW_CUSTOMER_LAST_NAME, NEW_CUSTOMER_TERMS);
		assertThat(customerRepo.findById(NEW_CUSTOMER_NAME).get(), 
				isNewCustomer());
	}
	
	/**
	 * After adding an invoice, assure that there is one new line in the
	 * invoices data file.
	 */
	@Test
	public void testCreateInvoice() {
		Invoice created = billing.createInvoice
				(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT);
		
		assertThat(invoiceRepo.findById(created.getNumber()).get(), 
				isNewInvoice());
	}

	/**
	 * After paying an invoice, assure that the line for that invoice in the
	 * data file now bears the correct paid date.
	 */
	@Test
	public void testPayInvoice() {
		billing.payInvoice(PAID_INVOICE_NUMBER);
		
		assertThat(invoiceRepo.findById(PAID_INVOICE_NUMBER).get(), 
				isPaidInvoice());
	}
	
	/**
	 * Make a few changes, and check that the history reports them all,
	 * as reported by the Billing component and as found in the database.
	 */
	@Test
	public void testGetHistory() {

		// We expand the range by a millisecond on either end, to account for
		// truncation to millisecond precision by the database's type system.
		LocalDateTime before = LocalDateTime.now().minus(1, ChronoUnit.MILLIS);
		billing.createCustomer(NEW_CUSTOMER_FIRST_NAME, 
				NEW_CUSTOMER_LAST_NAME, NEW_CUSTOMER_TERMS);
		billing.createInvoice(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT);
		billing.payInvoice(PAID_INVOICE_NUMBER);
		LocalDateTime after = LocalDateTime.now().plus(1, ChronoUnit.MILLIS);
		
		assertCorrectHistory(billing.getHistory(), false, before, after);
		assertCorrectHistory(historyRepo.streamAllBy(), false, before, after);
	}
}
