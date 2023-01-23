package com.amica.billing;

import java.time.LocalDate;
import java.util.Optional;

import lombok.Data;

@Data
public class Invoice {

	private int number;
	private double amount;
	private LocalDate invoiceDate;
	private Optional<LocalDate> paidDate;
	private Customer customer;

	public Invoice() {
		this(0, 0.0, LocalDate.MIN, null, null);
	}
	
	public Invoice(String name, int number, Customer customer) {
		this(number, 0.0, LocalDate.now(), null, customer);
	}

	public Invoice(int number, double amount, LocalDate invoiceDate, Optional<LocalDate> paidDate, Customer customer) {
		this.number = number;
		this.amount = amount;
		this.invoiceDate = invoiceDate;
		this.paidDate = paidDate;
		this.customer = customer;
	}
	

}
