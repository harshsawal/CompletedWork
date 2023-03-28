package com.amica.billing;

import javax.validation.constraints.Pattern;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Simple JavaBean representing a customer.
 * For a smoother experience with MongoDB, and JSON serialization for REST,
 * we re-work the private state model to hold a single 'name' field that is
 * the concatenation of the first and last names. We keep the constructor
 * and getter methods so that existing code won't break, but Mongo and Jackson
 * will both use the 'name' field.
 *
 * @author Will Provost
 */
@Getter
@EqualsAndHashCode(of={"name"})
@NoArgsConstructor
public class Customer {
    
    	@Id
    	@Pattern(regexp="[A-Za-z]+ [A-Za-z]+")
    	private String name;
    	
    private Terms terms;
    
    public Customer(String firstName, String lastName, Terms terms) {
    	this.name = firstName + " " + lastName;
    	this.terms = terms;
    }
    
    @JsonIgnore
    public String getFirstName() {
    	return name != null ? name.split(" ")[0] : null;
    }
    
    @JsonIgnore
    public String getLastName() {
    	return name != null ? name.split(" ")[1] : null;
    }
    
    @Override
    public String toString() {
    	return "Customer: " + getName();
    }
}
