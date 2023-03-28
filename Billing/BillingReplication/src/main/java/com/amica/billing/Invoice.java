package com.amica.billing;

import java.time.LocalDate;
import java.util.Optional;

import javax.validation.constraints.DecimalMin;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Simple JavaBean representing an invoice.
 *
 * @author Will Provost
 */
@Data
@EqualsAndHashCode(of="number")
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

	@Id
	private int number;

	@DBRef
    private Customer customer;
    
    @DecimalMin(value="0.01", inclusive=true)
    private double amount;
    
    private LocalDate issueDate;
    private LocalDate paidDate;
    
    public Invoice(int number, Customer customer, double amount, 
    		LocalDate issueDate) {
    	this(number, customer, amount, issueDate, Optional.empty());
    }
    
    public Invoice(int number, Customer customer, double amount, 
    		LocalDate issueDate, Optional<LocalDate> paidDate) {
    	this(number, customer, amount, issueDate, paidDate.orElse(null));
    }
    
    public Optional<LocalDate> getPaidDate() {
    	return Optional.ofNullable(paidDate);
    }
    
    public void setPaidDate(Optional<LocalDate> paidDate) {
    	this.paidDate = paidDate.orElse(null);
    }
    
    @JsonIgnore
    public LocalDate getDueDate() {
    	int daysAllowed = customer.getTerms().getDays();
    	return issueDate.plusDays(daysAllowed);
    }
    
    public boolean isOverdue(LocalDate asOf) {
		LocalDate endDate = getPaidDate().orElse(asOf);
		return endDate.isAfter(getDueDate());		
    }

    @Override
    public String toString() {
    	return "Invoice " + number;
    }
}
