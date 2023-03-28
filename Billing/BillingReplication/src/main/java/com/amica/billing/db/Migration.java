package com.amica.billing.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amica.billing.db.mongo.MongoPersistence;
import com.amica.billing.parse.ParserPersistence;

import lombok.Setter;

/**
 * Component that can replicate data from a {@link Persistence data source}
 * to a {@link Persistence target}. The source and target are autowired,
 * but can also be set programmatically.
 * 
 * @author Will Provost
 */
 @Component
public class Migration {

	@Autowired
	@Setter
	private ParserPersistence source;
	
	@Autowired
	@Setter
	private MongoPersistence target;
	
	@Autowired
	private CustomerRepository customers;
	
	@Autowired
	private InvoiceRepository invoices;

	@Autowired
	private HistoryRepository historyRepo;
	
	/**
	 * Clear out all data from the target.
	 * Reload the source and the target, for a clean slate.
	 * Save every object found in the source data set to the target. 
	 */
	public void migrate() {
		invoices.deleteAll();
		customers.deleteAll();
		
		source.load();
		target.load();
		
		source.getCustomers().values().forEach(target::saveCustomer);
		source.getInvoices().values().forEach(target::saveInvoice);
		
		historyRepo.deleteAll();
		target.load();
	}
}
