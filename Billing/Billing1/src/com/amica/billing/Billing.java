package com.amica.billing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private List<Invoice> invoiceList = new ArrayList();
	private Map<String, Customer> customerMap;
	private int runningInvoiceNumber = 900;

	private List<Consumer<Invoice>> invoiceListeners = new ArrayList();
	private List<Consumer<Customer>> customerListeners = new ArrayList<>();

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

		try (Stream<String> customerData = Files.lines(Paths.get(this.customersFilename));) {
			Stream<Customer> customers = this.parser.parseCustomers(customerData);
			customerMap = customers.collect(Collectors.toMap(value -> value.getName(), Function.identity()));
			//customerMap = Collections.unmodifiableMap(customerMap);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void openInvoiceFile() {
		try (Stream<String> invoiceData = Files.lines(Paths.get(this.invoicesFileName));) {
			Stream<Invoice> invoices = this.parser.parseInvoices(invoiceData, this.customerMap);
			//invoiceList = invoices.toList(); //Creates an immutable list
			invoiceList = invoices.collect(Collectors.toList());
			//invoiceList = Collections.unmodifiableList(invoiceList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createCustomer(String firstName, String lastName, Terms term) {
		Customer customer = new Customer(firstName, lastName, term);
		if(!customerMap.containsKey(customer.getName())) {
			customerMap.put(customer.getName(), customer);
			saveCustomer();
			for (Consumer<Customer> listener : customerListeners) {
				listener.accept(customer);
			}
		}
		else {
			throw new IllegalArgumentException("Customer already exists! "+customer.getName());
		}
	}
	
	public void createInvoice(String customerName, double amount) {
		System.out.println(customerMap);
		if(customerMap.containsKey(customerName)) {
			Invoice invoice = new Invoice(runningInvoiceNumber++, customerMap.get(customerName), amount, LocalDate.now());
			invoiceList.add(invoice);
			saveInvoice();
			for (Consumer<Invoice> listener : invoiceListeners) {
				listener.accept(invoice);
			}
		}
		else {
			throw new IllegalArgumentException("This customer does not exists! "+customerName);
		}
	}

	public void saveCustomer() {
		// write it to new file
		try (PrintWriter out = new PrintWriter(new FileWriter(Paths.get(customersFilename).toFile()));) {
			parser.produceCustomers(customerMap.values().stream()).forEach(out::println);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void saveInvoice() {
		// write it to new file
		try (PrintWriter out = new PrintWriter(new FileWriter(Paths.get(invoicesFileName).toFile()));) {
			parser.produceInvoices(invoiceList.stream()).forEach(out::println);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void payInvoice(int number) {
		// search invoice by number
		Invoice invoice = getInvoices().stream().filter(s -> s.getNumber() == number).findAny().orElse(null);

		if (invoice != null) {
			invoice.setPaidDate(Optional.of(LocalDate.now()));
			invoiceListeners.forEach(s -> s.accept(invoice));
		} else {
			log.log(Level.WARNING, String.format("No invoice with invoice # %s was found! Try again!", number));
		}

	}

	public Map<String, Customer> getCustomers() {
		return Collections.unmodifiableMap(customerMap);
	}

	public List<Invoice> getInvoices() {
		return Collections.unmodifiableList(invoiceList);
	}

	public Stream<Invoice> getInvoicesOrderedByNumber() {

		return getInvoices().stream()
				.sorted(Comparator.comparing(Invoice::getNumber));
	}

	public Stream<Invoice> getInvoicesOrderedByIssueDate() {
		return getInvoices().stream()
				.sorted(Comparator.comparing(Invoice::getInvoiceDate));
	}


	public Map<Customer, List<Invoice>> getInvoicesGroupedByCustomer() {
		return getInvoices().stream()
				.collect(Collectors.groupingBy(s -> s.getCustomer(), Collectors.toList()));
	}

	public static boolean dateDiff(Invoice invoice) {

		int calculatedDays = invoice.getPaidDate().orElseGet(() -> LocalDate.now()).compareTo(invoice.getInvoiceDate());
		int expectedDays = invoice.getCustomer().getTerms().getDays();
		return calculatedDays > expectedDays;
	}

	public Stream<Invoice> getOverdueInvoices() {

		// 2 scenarios
		// paid but later than credited days
		// not paid but over credited days
		return getInvoices().stream().filter(Billing::dateDiff);

	}
	
	public Stream<Invoice> getOverdueInvoices(LocalDate asOf) {

		// 2 scenarios
		// paid but later than credited days
		// not paid but over credited days
		return getInvoices().stream()
				.filter(s -> s.isOverDue(asOf))
				.sorted(Comparator.comparing(Invoice::getInvoiceDate));

	}
	
	public Stream<Invoice> getInvoicesForCustomer(Customer customer){
		return invoiceList.stream()
				.filter(s -> s.getCustomer().equals(customer))
				.sorted(Comparator.comparing(Invoice::getNumber));
	}

	public double getVolumeForCustomer(Customer customer) {
		return getInvoicesForCustomer(customer)
				.mapToDouble(s->s.getAmount())
				.sum();
	}
	
	public Map<Customer, Double> getCustomersAndVolume() {
		return customerMap.values().stream().collect(Collectors.toMap(Function.identity(), this::getVolumeForCustomer));
	}

	public void addInvoiceListener(Consumer<Invoice> item) {
		invoiceListeners.add(item);
	}

	public void removeInvoiceListener(Consumer<Invoice> item) {
		invoiceListeners.remove(item);
	}

	public void addCustomerListener(Consumer<Customer> item) {
		customerListeners.add(item);
	}

	public void removeCustomerListener(Consumer<Customer> item) {
		customerListeners.remove(item);
	}

}
