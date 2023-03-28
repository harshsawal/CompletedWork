package com.amica.billing.db;

import java.util.Map;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Update;

/**
 * Strategy for loading and saving data.
 * 
 * @author Will Provost
 */
public interface Persistence {
	
	/**
	 * Returns a map of all customers, keyed by name.
	 */
	public Map<String,Customer> getCustomers();
	
	/**
	 * returns a map of all invoices, keyed by number.
	 */
	public Map<Integer,Invoice> getInvoices();
	
	/**
	 * Returns a stream of update records, in chronological order.
	 */
	public Stream<Update> getHistory();
	
	/**
	 * Updates a customer with the same name, or inserts a new customer.
	 */
	public void saveCustomer(Customer customer);
	
	/**
	 * Updates an invoice with the same number, or inserts a new invoice.
	 */
	public void saveInvoice(Invoice invoice);
}
