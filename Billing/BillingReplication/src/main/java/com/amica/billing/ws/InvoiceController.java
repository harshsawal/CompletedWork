package com.amica.billing.ws;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.amica.billing.Billing;
import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.ws.Exceptions.CustomerNotFoundException;
import com.amica.billing.ws.Exceptions.InvoiceAlreadyPaidException;
import com.amica.billing.ws.Exceptions.InvoiceNotFoundException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * REST controller for invoice queries and updates.
 *
 * @author Will Provost
 */
@RestController
@RequestMapping("invoices")
@AllArgsConstructor
public class InvoiceController {

	private Billing billing;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NewInvoice {
		private String customerName;
    
	    @DecimalMin(value="0.01", inclusive=true)
		private double amount;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class InvoiceWithoutCustomer {

		private int number;
		private double amount;
		private LocalDate issueDate;
		private LocalDate paideDate;
		
		public InvoiceWithoutCustomer(Invoice source) {
			this(source.getNumber(), source.getAmount(), 
					source.getIssueDate(),source.getPaidDate().orElse(null));
		}
		
		public static List<InvoiceWithoutCustomer> forInvoices
				(List<Invoice> source) {
			return source.stream().map(InvoiceWithoutCustomer::new).toList();
		}
	}

	@GetMapping("{number}")
	public Invoice getInvoice(@PathVariable("number") int number) {
		Invoice invoice = billing.getInvoices().get(number);
		if (invoice != null) {
			return invoice;
		}

		throw new InvoiceNotFoundException(number);
	}

	
	@GetMapping(params="customerName")
	public List<InvoiceWithoutCustomer> getInvoicesForCustomer
			(@RequestParam("customerName") String customerName) {
		Customer customer = billing.getCustomers().get(customerName);
		if (customer != null) {
			return InvoiceWithoutCustomer.forInvoices
					(billing.getInvoicesGroupedByCustomer().get(customer));
		} else {
			throw new CustomerNotFoundException(customerName);
		}
	}

	@GetMapping(params="byCustomer")
	public Map<String,List<InvoiceWithoutCustomer>> getInvoicesByCustomer() {
		return billing.getInvoicesGroupedByCustomer().entrySet().stream()
			.collect(Collectors.toMap(entry -> entry.getKey().getName(),
				entry -> InvoiceWithoutCustomer.forInvoices(entry.getValue()),
				(a,b) -> a, TreeMap::new));
	}

	@GetMapping(params="overdueAsOf")
	public Stream<Invoice> getOverdueInvoices(@RequestParam("overdueAsOf")
			 @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate asOf) {
		return billing.getOverdueInvoices(asOf);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Invoice createInvoice(@RequestBody @Valid NewInvoice newInvoice) {
		try {
			return billing.createInvoice
				(newInvoice.getCustomerName(), newInvoice.getAmount());
		} catch (IllegalArgumentException ex) {
			throw new CustomerNotFoundException(newInvoice.getCustomerName());
		}
	}

	@PatchMapping(value="{number}", params="pay")
	public Invoice payInvoice(@PathVariable("number") int number) {
		try {
			billing.payInvoice(number);
			return getInvoice(number);
		} catch (IllegalArgumentException ex) {
			throw new InvoiceNotFoundException(number, ex);
		} catch (IllegalStateException ex) {
			throw new InvoiceAlreadyPaidException(number, ex);
		}
	}
}
