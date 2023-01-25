package com.amica.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
	
	private String firstName;
	private String lastName;
	private Terms terms;
	
	
//	public Customer(String firstName, String lastName, Terms terms) {
//		this.firstName = firstName;
//		this.lastName = lastName;
//		this.terms = terms;
//	}
	
	public String getName() {
		return String.format("%s %s", firstName, lastName);
	}
	
	public String getCustomerInfo() {
		return String.format("%s %s %s", firstName, lastName, terms.toString());
	}
	
	@Override
    public String toString() {
    	return String.format("%s", getName());
    }
}
