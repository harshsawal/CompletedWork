package com.amica.billing.ws;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amica.escm.mvc.exception.exceptions.BaseConflictException;
import com.amica.escm.mvc.exception.exceptions.BaseNotFoundException;

import lombok.AllArgsConstructor;

/**
 * Custom exception types for our request handlers.
 *
 * @author Will Provost
 */
@RestController
@RequestMapping("invoices")
@AllArgsConstructor
public class Exceptions {

	public static class CustomerNotFoundException extends BaseNotFoundException {
		public CustomerNotFoundException(String name) {
			super(String.format("No customer with name: %s", name));
		}

		public CustomerNotFoundException(String name, Throwable cause) {
			super(String.format("No customer with name: %s", name), cause);
		}
	}

	public static class InvoiceNotFoundException extends BaseNotFoundException {
		public InvoiceNotFoundException(int number) {
			super(String.format("No invoice with number: %d", number));
		}
		public InvoiceNotFoundException(int number, Throwable cause) {
			super(String.format("No invoice with number: %d", number), cause);
		}
	}

	public static class CustomerNameConflictException extends BaseConflictException {
		public CustomerNameConflictException(String name) {
			super(String.format
				("There is already a customer with name: %s", name));
		}

	public CustomerNameConflictException(String name, Throwable cause) {
			super(String.format
				("There is already a customer with name: %s", name), cause);
		}
	}

	public static class InvoiceAlreadyPaidException extends BaseConflictException {
		public InvoiceAlreadyPaidException(int number) {
			super(String.format
					("Invoice number %d is already paid.", number));
		}

		public InvoiceAlreadyPaidException(int number, Throwable cause) {
			super(String.format
					("Invoice number %d is already paid.", number), cause);
		}
	}
}
