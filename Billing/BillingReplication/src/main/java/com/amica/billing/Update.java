package com.amica.billing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A serializable record or "memento" of a change to the database.
 * We embed customer and invoice records, and the invoice embeds a customer.
 * An instance will have either a non-null customer or invoice, but not both.
 * 
 * @author Will Provost
 */
@Data
@NoArgsConstructor
public class Update {
	
	/**
	 * Memento for {@link Customer}s, with a copying constructor. 
	 */
	@Data
	@NoArgsConstructor
	public static class UpdatedCustomer {
		private String name;
		private Terms terms;
		
		public UpdatedCustomer(Customer customer) {
			this.name = customer.getName();
			this.terms = customer.getTerms();
		}
	}
	
	/**
	 * Memento for {@link Invoice}s, with a copying constructor. 
	 */
	@Data
	@NoArgsConstructor
	public static class UpdatedInvoice {
		private int number;
		private UpdatedCustomer customer;
		private double amount;
		private LocalDate issueDate;
		private LocalDate paidDate;
		
		public UpdatedInvoice(Invoice invoice) {
			this.number = invoice.getNumber();
			this.customer = new UpdatedCustomer(invoice.getCustomer());
			this.amount = invoice.getAmount();
			this.issueDate = invoice.getIssueDate();
			this.paidDate = invoice.getPaidDate().orElse(null);
		}
		
	    public Optional<LocalDate> getPaidDate() {
	    	return Optional.ofNullable(paidDate);
	    }
	    
	    public void setPaidDate(Optional<LocalDate> paidDate) {
	    	this.paidDate = paidDate.orElse(null);
	    }
	    
	}
	
	private String _id;
	private LocalDateTime when;
	private UpdatedCustomer customer;
	private UpdatedInvoice invoice;
	
	/**
	 * Records the state of the given object, and sets a timestamp.
	 */
	public Update(Customer customer) {
		this.when = LocalDateTime.now();
		this.customer = new UpdatedCustomer(customer);
	}
	
	/**
	 * Records the state of the given object, and sets a timestamp.
	 */
	public Update(Invoice invoice) {
		this.when = LocalDateTime.now();
		this.invoice = new UpdatedInvoice(invoice);
	}
}
