package com.amica.billing.parse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;

import lombok.extern.java.Log;

/**
 * A parser that can read a specific flat file format.
 * 
 * @author Will Provost
 */
 @Log
public class FlatParser implements Parser {

	public static final int CUSTOMER_FIRST_NAME_OFFSET = 0;
	public static final int CUSTOMER_FIRST_NAME_LENGTH = 12;
	public static final int CUSTOMER_LAST_NAME_OFFSET = 
			CUSTOMER_FIRST_NAME_OFFSET + CUSTOMER_FIRST_NAME_LENGTH;
	public static final int CUSTOMER_LAST_NAME_LENGTH = 12;
	public static final int CUSTOMER_TERMS_OFFSET = 
			CUSTOMER_LAST_NAME_OFFSET + CUSTOMER_LAST_NAME_LENGTH;
	public static final int CUSTOMER_TERMS_LENGTH = 10;
	public static final int CUSTOMER_LENGTH = 
			CUSTOMER_TERMS_OFFSET + CUSTOMER_TERMS_LENGTH;

	public static final int INVOICE_NUMBER_OFFSET = 0;
	public static final int INVOICE_NUMBER_LENGTH = 4;
	public static final int INVOICE_FIRST_NAME_OFFSET = 
			INVOICE_NUMBER_OFFSET + INVOICE_NUMBER_LENGTH;
	public static final int INVOICE_FIRST_NAME_LENGTH = 12;
	public static final int INVOICE_LAST_NAME_OFFSET = 
			INVOICE_FIRST_NAME_OFFSET + INVOICE_FIRST_NAME_LENGTH;
	public static final int INVOICE_LAST_NAME_LENGTH = 12;
	public static final int INVOICE_AMOUNT_OFFSET = 
			INVOICE_LAST_NAME_OFFSET + INVOICE_LAST_NAME_LENGTH;
	public static final int INVOICE_AMOUNT_LENGTH = 8;
	public static final int INVOICE_DATE_OFFSET = 
			INVOICE_AMOUNT_OFFSET + INVOICE_AMOUNT_LENGTH;
	public static final int INVOICE_DATE_LENGTH = 6;
	public static final int INVOICE_PAID_DATE_OFFSET = 
			INVOICE_DATE_OFFSET + INVOICE_DATE_LENGTH;
	public static final int INVOICE_PAID_DATE_LENGTH = 6;
	public static final int INVOICE_LENGTH = 
			INVOICE_PAID_DATE_OFFSET + INVOICE_PAID_DATE_LENGTH;

	/**
	 * Helper that can parse one line of text in order to
	 * produce a {@link Customer} object.
	 */
	private Customer parseCustomer(String line) {
		if (line.length() >= CUSTOMER_LENGTH) {
			try {
				String firstName = line.substring(CUSTOMER_FIRST_NAME_OFFSET, 
						CUSTOMER_LAST_NAME_OFFSET).trim();
				String lastName = line.substring(CUSTOMER_LAST_NAME_OFFSET, 
						CUSTOMER_TERMS_OFFSET).trim();
				String termsString = line.substring
						(CUSTOMER_TERMS_OFFSET, CUSTOMER_LENGTH).trim();
				
				//TODO convert the terms string to an enum
				Terms term = Terms.valueOf(termsString);
				//TODO create a customer object and return it
				Customer customer = new Customer(firstName, lastName, term);
				return customer;
			} catch (Exception ex) {
				log.warning(() -> 
						"Couldn't parse terms value, skipping customer: " + line);
			}
		} else {
			log.warning(() -> "Incorrect length, skipping customer: " + line);
		}
		
		return null;
	}

	/**
	 * Helper that can parse one line of text in order to
	 * produce an {@link Invoice} object.
	 */
	private Invoice parseInvoice(String line, Map<String, Customer> customers) {

		if (line.length() >= INVOICE_PAID_DATE_OFFSET) {
			try {
				int number = Integer.parseInt(line.substring
					(INVOICE_NUMBER_OFFSET,
						INVOICE_FIRST_NAME_OFFSET).trim());
				
				String firstName = line.substring
					(INVOICE_FIRST_NAME_OFFSET,
						INVOICE_LAST_NAME_OFFSET).trim();
				String lastName = line.substring
					(INVOICE_LAST_NAME_OFFSET, 
						INVOICE_AMOUNT_OFFSET).trim();
				
				double amount = Double.parseDouble(line.substring
					(INVOICE_AMOUNT_OFFSET, INVOICE_DATE_OFFSET).trim());
		
				DateTimeFormatter parser =
						DateTimeFormatter.ofPattern("MMddyy");
				LocalDate theDate = LocalDate.parse(line.substring
					(INVOICE_DATE_OFFSET, INVOICE_PAID_DATE_OFFSET), parser);
				String paidString = line.substring(
						INVOICE_PAID_DATE_OFFSET, INVOICE_LENGTH).trim();
				Optional<LocalDate> paidDate = paidString.length() == 
					INVOICE_PAID_DATE_LENGTH
						? Optional.of(LocalDate.parse(paidString, parser))
						: Optional.empty();
		
				//TODO find the corresponding customer in the map
				Customer customer = customers.get(firstName+" "+lastName);
				
				//TODO create an invoice and return it
				Invoice invoice = new Invoice(number, amount, theDate, paidDate, customer);
				return invoice;
				
			} catch (Exception ex) {
				log.warning(() -> 
						"Couldn't parse values, skipping invoice: " + line);
			}
		} else {
			log.warning(() -> "Incorrect length, skipping invoice: " + line);
		}
		
		return null;
	}

	/**
	 * Helper to write a flat representation of one customer.
	 */
	public String formatCustomer(Customer customer) {
		final String formatString = String.format("%%-%ds%%-%ds%%-%ds",
				CUSTOMER_FIRST_NAME_LENGTH, CUSTOMER_LAST_NAME_LENGTH,
				CUSTOMER_TERMS_LENGTH);
		//TODO provide the values to be formatted
		return String.format(formatString, "NYI", "NYI", "NYI");
	}
	
	/**
	 * Helper to write a flat representation of one invoice.
	 */
	public String formatInvoice(Invoice invoice) {
		final String formatString = String.format
			("%%%dd%%-%ds%%-%ds%%%d.2f%%%ds%%%ds", 
				INVOICE_NUMBER_LENGTH, INVOICE_FIRST_NAME_LENGTH,
				INVOICE_LAST_NAME_LENGTH, INVOICE_AMOUNT_LENGTH,
				INVOICE_DATE_LENGTH, INVOICE_PAID_DATE_LENGTH);
		DateTimeFormatter formatter = 
				DateTimeFormatter.ofPattern("MMddyy");

		//TODO provide the values to be formatted
		return String.format(formatString, 
				0, "NYI", "NYI", 0.0, "NYI", "NYI");
	}

	@Override
	public Stream<Customer> parseCustomers(Stream<String> customerLines) {
		// TODO Auto-generated method stub
		
		Stream<Customer> customers = customerLines.map(s -> s.split("\n"))
				.map(s -> String.join(",", s))
				.map(s -> this.parseCustomer(s));
		
		//customers.forEach(System.out::println);
		System.out.println();
		
		return customers;
	}

	@Override
	public Stream<Invoice> parseInvoices(Stream<String> invoiceLines, Map<String, Customer> customers) {
		// TODO Auto-generated method stub
		
		Stream<Invoice> invoices = invoiceLines.map(s -> s.split("\n"))
				.map(s -> String.join(",", s))
				.map(s -> this.parseInvoice(s, customers));
		
		//invoices.forEach(System.out::println);
		System.out.println();
		
		return invoices;
	}

	@Override
	public Stream<String> produceCustomers(Stream<Customer> customers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<String> produceInvoices(Stream<Invoice> invoices) {
		// TODO Auto-generated method stub
		return null;
	}
}
