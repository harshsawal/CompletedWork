package com.amica.billing;

import lombok.Data;

@Data
public class Customer {
	
	private String firstName;
	private String lastName;
	private Terms terms;
	
	public Customer() {
		this("", "", Terms.CASH);
	}
	
	public Customer(String firstName, String lastName, Terms terms) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.terms = terms;
	}
	
	public String getName() {
		return String.format("%s %s", firstName, lastName);
	}
	
	public String getCustomerInfo() {
		return String.format("%s %s %s", firstName, lastName, terms.toString());
	}
}
