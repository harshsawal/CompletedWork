package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amica.billing.Billing;
import com.amica.billing.TestUtility.IntegrationConfig;
import com.amica.billing.db.HistoryRepository;
import com.amica.billing.db.Migration;
import com.amica.billing.db.mongo.MongoPersistence;

import lombok.SneakyThrows;

/**
 * Integration test for the {@link HistoryController}.
 * We use the test database, and reset the data before each test.
 *
 * @author Will Provost
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=IntegrationConfig.class)
public class HistoryControllerIntegrationTest {

	@Autowired
	private HistoryController controller;

	@Autowired
	private Billing billing;
	
	@Autowired
	private Migration migration;
	
	@Autowired
	private MongoPersistence persistence;
	
	@Autowired
	private HistoryRepository historyRepo;

	@Value("${MongoPersistence.archiveFolder:archives}")
	private String archiveFolder;
	
    private LocalDateTime before;
    private LocalDateTime during;
    private LocalDateTime after;
    
	@BeforeEach
	@SneakyThrows
    public void setUp() {
		migration.migrate();
		persistence.load();
    	
	    	File root = new File(archiveFolder);
	    	root.mkdir();
    		for (File archiveFile : root.listFiles()) {
    			archiveFile.delete();
	    	}
    	
		before = LocalDateTime.now().minus(1, ChronoUnit.MILLIS);
		billing.createCustomer(NEW_CUSTOMER_FIRST_NAME, 
				NEW_CUSTOMER_LAST_NAME, NEW_CUSTOMER_TERMS);
		
		Thread.sleep(10);
		during = LocalDateTime.now();
		Thread.sleep(10);
		
		billing.createInvoice(NEW_INVOICE_CUSTOMER_NAME, NEW_INVOICE_AMOUNT);
		billing.payInvoice(PAID_INVOICE_NUMBER);
		after = LocalDateTime.now().plus(1, ChronoUnit.MILLIS);
    }
    
    @Test
	public void testGetHistory() {
		assertCorrectHistory(controller.getHistory(before), false, before, after);
		assertCorrectHistory(historyRepo.streamAllBy(), false, before, after);
	}
    
    @Test
	public void testGetHistoryPartial() {
		assertCorrectHistory(controller.getHistory(during), true, before, after);
	}

    @Test
	public void testGetHistoryEmpty() {
		assertThat(controller.getHistory(after).count(), equalTo(0L));
	}
    
	@Test
	@SneakyThrows
	public void testArchiving() {
		Thread.sleep(MongoPersistence.ARCHIVE_INTERVAL_MSEC + 1000);
		assertThat(new File(archiveFolder).list().length, not(equalTo(0)));
	}
}
