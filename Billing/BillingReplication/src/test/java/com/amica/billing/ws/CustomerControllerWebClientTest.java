package com.amica.billing.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException.*;

import com.amica.billing.Customer;
import com.amica.billing.Terms;

import lombok.Data;

/**
 * Functional test for the Billing web service, focused on the 
 * /customers root URL. This test is not repeatable, because
 * unlike the integration tests it can't reset the database
 * underneath the running service (which caches data in memory).
 *  
 * @author Will Provost
 */
public class CustomerControllerWebClientTest extends WebClientTestBase {

	@Data
	public static class CustomerAndVolume {
		private Customer customer;
		private double volume;
	}
	
	protected String getResourceName() {
		return "customers";
	}

	@Test
	public void testGetCustomer() {
		Customer customer = getOne("/Roy Clark", Customer.class);
		assertThat(customer, samePropertyValuesAs
				(new Customer("Roy", "Clark", Terms.CREDIT_60)));
	}
	
	@Test
	public void testGetCustomerNonExistent() {
		assertThrows(NotFound.class, 
			() -> getOne("Omar Khayyam", Customer.class));
	}
	
	@Test
	public void testCreateCustomer() {
		final String FIRST = "William";
		final String LAST = "Fitzwilliam";
		final Terms TERMS = Terms.CREDIT_60;
		
		put(new Customer(FIRST, LAST, TERMS), Customer.class);
		Customer customer = getOne("/" + FIRST + " " + LAST, Customer.class);
		assertThat(customer, samePropertyValuesAs
				(new Customer(FIRST, LAST, TERMS)));
	}
	
	@Test
	public void testCreateCustomerWithDuplicateName() {
		assertThrows(Conflict.class,
			() -> put(new Customer("Roy", "Clark", Terms.CASH), Customer.class));
	}
	
	@Test
	public void testGetCustomersByVolume() {
		List<CustomerAndVolume> customers = 
				getMany("?byVolume", CustomerAndVolume.class);
		assertThat(customers.get(0).getCustomer().getName(), equalTo("Jerry Reed"));
	}
}
