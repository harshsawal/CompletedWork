package com.amica.billing.db;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;

import lombok.Getter;

@Getter
public abstract class CachingPersistence implements Persistence {

	protected Map<String, Customer> customers;
	protected Map<Integer, Invoice> invoices;
	
	protected abstract Stream<Customer> readCustomers();
	protected abstract Stream<Invoice> readInvoices();
	protected abstract void writeCustomer(Customer customer);
	protected abstract void writeInvoice(Invoice invoice);
	
	public void load() {
		try(Stream<Customer> customerData = readCustomers();){
			customers = customerData.collect(Collectors.toMap(Customer::getName, s->s));
		}
		try(Stream<Invoice> invoiceData = readInvoices();){
			invoices = invoiceData.collect(Collectors.toMap(Invoice::getNumber, s->s));
		}
	}
	
	public void saveCustomer(Customer customer) {
		customers.put(customer.getName(), customer);
		writeCustomer(customer);
	}
	
	public void saveInvoice(Invoice invoice) {
		invoices.put(invoice.getNumber(), invoice);
		writeInvoice(invoice);
	}

}
