package com.amica.billing.ws;

import static com.amica.billing.TestUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.billing.Billing;
import com.amica.billing.Customer;
import com.amica.billing.Terms;
import com.amica.billing.ws.Exceptions.CustomerNameConflictException;
import com.amica.billing.ws.Exceptions.CustomerNotFoundException;

/**
 * Unit test for the {@link CustomerController}.
 *
 * @author Will Provost
 */
public class CustomerControllerTest {

	private Billing mockBilling;
	private CustomerController controller;

	@BeforeEach
	public void setUp() {
		mockBilling = createMockBilling();
		controller = new CustomerController(mockBilling);
	}

	@Test
	public void testGetCustomer() {
		final String customerName = GOOD_CUSTOMERS.get(0).getName();
		assertThat(controller.getCustomer(customerName),
				samePropertyValuesAs(GOOD_CUSTOMERS.get(0)));
	}

	@Test
	public void testGetCustomerNonExistent() {
		assertThrows(CustomerNotFoundException.class,
				() -> controller.getCustomer("Omar Khayyam"));
	}

	@Test
	public void testCreateCustomer() {
		final String firstName = "William";
		final String lastName = "Fitzwilliam";
		final Terms terms = Terms.CASH;
		
		controller.createCustomer(new Customer(firstName, lastName, terms));
		verify(mockBilling).createCustomer(firstName, lastName, terms);
	}

	@Test
	public void testCreateCustomerWithDuplicateName() {
		doThrow(new IllegalArgumentException())
				.when(mockBilling).createCustomer(any(), any(), any());
		assertThrows(CustomerNameConflictException.class, 
				() -> controller.createCustomer(new Customer("A", "B", Terms.CASH)));
	}

	@Test
	public void testGetCustomersByVolume() {
		assertThat(controller.getCustomersAndVolume().findFirst().get()
				.getCustomer().getName(), equalTo("Customer Three"));
	}
}
