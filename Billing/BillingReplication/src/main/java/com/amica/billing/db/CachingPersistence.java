package com.amica.billing.db;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Update;

import lombok.Getter;


/**
 * Base implementation that sets up a write-through cache using two maps.
 * Derived classes must implement the "read" and "write" methods
 * and this class will assure caching and updates on all write operations.
 * 
 * @author Will Provost
 */
@Getter
public abstract class CachingPersistence implements Persistence {

	private Map<String,Customer> customers;
	private Map<Integer,Invoice> invoices;
	protected List<Update> history;

	protected abstract Stream<Customer> readCustomers();
	protected abstract Stream<Invoice> readInvoices();
	protected abstract Stream<Update> readHistory();
	protected abstract void writeCustomer(Customer customer);
	protected abstract void writeInvoice(Invoice invoice);
	protected abstract void writeUpdate(Update update);
	
	/**
	 * Returns a stream derived from our history list.
	 */
	public Stream<Update> getHistory() {
		return history.stream();
	}
	
	/**
	 * Compiles maps of loaded data based on streams returned by
	 * helper methods.
	 */
	public void load() {
		try ( Stream<Customer> customerStream = readCustomers(); ) {
			customers = customerStream.collect(Collectors.toMap
					(Customer::getName, Function.identity()));
		}
		try ( Stream<Invoice> invoiceStream = readInvoices(); ) {
			invoices = invoiceStream.collect(Collectors.toMap
					(Invoice::getNumber, Function.identity()));
		}
		try ( Stream<Update> updateStream = readHistory(); ) {
			history = updateStream.collect(Collectors.toList());
		}
	}
	
	/**
	 * Updates the cache and calls the helper method.
	 */
	public void saveCustomer(Customer customer) {
		customers.put(customer.getName(), customer);
		writeCustomer(customer);
		
		Update update = new Update(customer);
		history.add(update);
		writeUpdate(update);
	}
	
	/**
	 * Updates the cache and calls the helper method.
	 */
	public void saveInvoice(Invoice invoice) {
		invoices.put(invoice.getNumber(), invoice);
		writeInvoice(invoice);
		
		Update update = new Update(invoice);
		history.add(update);
		writeUpdate(update);
	}
}
