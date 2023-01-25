package com.amica.billing;

import java.time.LocalDate;
import java.util.Optional;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of="number")
public class Invoice {

	private int number;
	private double amount;
	private LocalDate invoiceDate;
	private Optional<LocalDate> paidDate;
	private Customer customer;

	
	public Invoice(int number, Customer customer, double amount, LocalDate invoiceDate) {
		this(number, amount, invoiceDate, null, customer);
	}

	public Invoice(int number, double amount, LocalDate invoiceDate, Optional<LocalDate> paidDate, Customer customer) {
		this.number = number;
		this.amount = amount;
		this.invoiceDate = invoiceDate;
		this.paidDate = paidDate;
		this.customer = customer;
	}

	public boolean isOverDue(LocalDate asOf) {
		return paidDate.orElse(asOf).isAfter(getDueDate());
	}

	public LocalDate getDueDate() {
		int allowedDays = customer.getTerms().getDays();
		return invoiceDate.plusDays(allowedDays);
	}

	@Override
	public String toString() {
		return String.format("Invoice %s", number);
	}

}
