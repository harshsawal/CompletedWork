package com.amica.billing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.parse.Parser;
import com.amica.billing.parse.ParserFactory;

import lombok.extern.java.Log;

@Log
public class Billing {

	private String customersFilename;
	private String invoicesFileName;
	private Parser parser;
	private List<Invoice> invoiceList;
	private Map<String, Customer> customerList;
	
	private List<Consumer<Invoice>> invoiceListeners = new ArrayList();

	public Billing(String customersFilename, String invoicesFilename) {
		// TODO Auto-generated constructor stub
		this.customersFilename = customersFilename;
		this.invoicesFileName = invoicesFilename;
		this.parser = ParserFactory.createParser(customersFilename);
		openCustomerFile();
		openInvoiceFile();
	}

	// create a method to open file and return items as a stream
	public void openCustomerFile() {
		
		try {
			//Stream<Object> result = 
			Stream<String> customerData = Files.lines(Paths.get(this.customersFilename));
			Stream<Customer> customers = this.parser.parseCustomers(customerData);
			customerList = customers.collect(Collectors.toMap(value -> value.getName(), Function.identity()));
			customerList = Collections.unmodifiableMap(customerList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void openInvoiceFile() {
		try {
			Stream<String> invoiceData = Files.lines(Paths.get(this.invoicesFileName));
			Stream<Invoice> invoices = this.parser.parseInvoices(invoiceData, this.customerList);
			invoiceList = invoices.toList();
			invoiceList = Collections.unmodifiableList(invoiceList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createCustomer(String firstName, String lastName, Terms term) {
		new Customer(firstName, lastName, term);
	}
	
	public void saveCustomer(Stream<Customer> customers) {
		//write it to new file
		parser.produceCustomers(customers);
	}
	
	public void createInvoice(String name, int number) {
		Customer customer = getCustomers().get(name);
		if(customer != null) {
			new Invoice(name, number, customer);
		}
		
	}
	
	public void payInvoice(int number) {
		//search invoice by number
		Invoice invoice = getInvoices().stream()
				.filter(s -> s.getNumber() == number)
				.findAny()
				.orElse(null);
		
		if(invoice != null) {
			invoice.setPaidDate(Optional.of(LocalDate.now()));
			invoiceListeners.forEach(s -> s.accept(invoice));
		} else {
			log.log(Level.WARNING, String.format("No invoice with invoice # %s was found! Try again!",number));
		}
		
	}

	public Map<String, Customer> getCustomers() {
		return customerList;
	}
	
	public List<Invoice> getInvoices(){
		return invoiceList;
	}
	
	public Stream<Invoice> getInvoicesOrderedByNumber(){
		
		return getInvoices().stream()
				.sorted(Comparator.comparing(Invoice::getNumber));
	}
	
	public Stream<Invoice> getInvoicesOrderedByIssueDate(){
		return getInvoices().stream()
				.sorted(Comparator.comparing(Invoice::getInvoiceDate));
	}
	
//	public Stream<Invoice> getInvoicesGroupedByCustomer() {
//		return getInvoices().stream()
//				.collect(Collectors.groupingBy(Customer::getName));
//	}
	
	public Map<Customer, List<Invoice>> getInvoicesGroupedByCustomer() {
		return getInvoices().stream()
				.collect(Collectors.groupingBy(s -> s.getCustomer()));
	}
	
	public static boolean dateDiff(Invoice invoice) {
		
		int calculatedDays = invoice.getPaidDate().orElseGet(() -> LocalDate.now()).compareTo(invoice.getInvoiceDate());
		int expectedDays = invoice.getCustomer().getTerms().getDays();
		return  calculatedDays > expectedDays;
	}
	
	public Stream<Invoice> getOverdueInvoices(){
		
		//2 scenarios
		//paid but later than credited days
		//not paid but over credited days
		return getInvoices().stream()
				.filter(Billing::dateDiff);
		
	}
	
	public Stream<Customer> getCustomersAndVolume(){
		return null;
		
	}
	
	public void addInvoiceListener(Consumer<Invoice> item) {
		invoiceListeners.add(item);
	}
	
	public void removeInvoiceListener(Consumer<Invoice> item) {
		invoiceListeners.remove(item);
	}

}
