package com.amica.billing.ws;

import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.amica.billing.Billing;
import com.amica.billing.Billing.CustomerAndVolume;
import com.amica.billing.Customer;
import com.amica.billing.ws.Exceptions.CustomerNameConflictException;
import com.amica.billing.ws.Exceptions.CustomerNotFoundException;

import lombok.AllArgsConstructor;

/**
 * REST controller for customer queries and updates.
 *
 * @author Will Provost
 */
@RestController
@RequestMapping("customers")
@AllArgsConstructor
public class CustomerController {

	private Billing billing;

	@GetMapping("{name}")
	public Customer getCustomer(@PathVariable("name") String name) {
		Customer customer = billing.getCustomers().get(name);
		if (customer != null) {
			return customer;
		}

		throw new CustomerNotFoundException(name);
	}

	@GetMapping(params="byVolume")
	public Stream<CustomerAndVolume> getCustomersAndVolume() {
		return billing.getCustomersAndVolumeStream();
	}

	@PutMapping("{name}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void createCustomer(@RequestBody @Valid Customer customer) {
		try {
			
			billing.createCustomer(customer.getFirstName(), 
					customer.getLastName(), customer.getTerms());
		} catch (IllegalArgumentException ex) {
			throw new CustomerNameConflictException(customer.getName(), ex);
		}
	}
}
