package com.amica.billing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.parse.Parser;
import com.amica.billing.parse.ParserFactory;

public class Billing {

	private String customersFilename;
	private String invoicesFileName;
	private Parser parser;
	private List<Invoice> invoiceList;
	private Map<String, Customer> customerList;

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

	public Map<String, Customer> getCustomers() {
		return customerList;
	}
	
	public List<Invoice> getInvoices(){
		return invoiceList;
	}
	
	public Stream<Invoice> getInvoicesOrderedByNumber(){
		
		return invoiceList.stream()
				.sorted(Comparator.comparing(Invoice::getNumber));
	}
	
	public Stream<Invoice> getInvoicesOrderedByIssueDate(){
		return invoiceList.stream()
				.sorted(Comparator.comparing(Invoice::getInvoiceDate));
	}
	
//	public Stream<Invoice> getInvoicesGroupedByCustomer() {
//		
//	}
//	public Stream<Invoice> getOverdueInvoices(){
//		
//	}
//	
//	public Stream<Customer> getCustomersAndVolume(){
//		
//	}

}
