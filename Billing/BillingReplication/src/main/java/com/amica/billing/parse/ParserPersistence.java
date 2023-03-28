package com.amica.billing.parse;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.ParserFactory;
import com.amica.billing.Update;
import com.amica.billing.db.CachingPersistence;

import lombok.Setter;
import lombok.extern.java.Log;

/**
 * Persistence service based in the file system.
 * Configurable filenames are found and loaded, and an appropriate
 * {@link Parser} is derived from the {@link ParserFactory}.
 * Acts as a write-through cache, but saves the whole collection
 * each time any element is updated.
 *
 * We opt out of persisting updates, essentially treating file-based persistence
 * as a legacy from this point forward.
 * 
 * @author Will Provost
 */
@Component
@Log
public class ParserPersistence extends CachingPersistence {

	@Value("${ParserPersistence.customersFile:data/customers.csv}")
	@Setter
	private String customersFile;
	
	@Value("${ParserPersistence.invoicesFile:data/invoices.csv}")
	@Setter
	private String invoicesFile;

	private Parser parser;
	
	/**
	 * We trigger immediately loading when configured as a Spring bean,
	 * finding an appropriate parser and calling the superclass method.
	 */
	@Override
	@PostConstruct
	public void load() {
		parser = ParserFactory.createParser(customersFile);
		super.load();
	}

	/**
	 * Load customers from the configured file, and let the superclass
	 * compile it into a map.
	 */
	protected Stream<Customer> readCustomers() {
		try {
			return parser.parseCustomers(Files.lines(Paths.get(customersFile)));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't load customers.", ex);
		}
		return Stream.empty();
	}

	@Override
	public Stream<Update> getHistory() {
		throw new UnsupportedOperationException("We don't support history.");
	}

	/**
	 * Load customers from the configured file, and let the superclass
	 * compile it into a map.
	 */
	protected Stream<Invoice> readInvoices() {
		try {
			return parser.parseInvoices
					(Files.lines(Paths.get(invoicesFile)), getCustomers());
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't load invoices.", ex);
		}
		return Stream.empty();
	}

	/**
	 * Re-write the whole file when any element changes.
	 */
	protected void writeCustomer(Customer customer) {
		try ( PrintWriter out = new PrintWriter(new FileWriter(customersFile)); ) {
			parser.produceCustomers(getCustomers().values().stream())
					.forEach(out::println);
		} catch (Exception ex) {
			log.log(Level.WARNING, ex, 
					() -> "Couldn't open " + customersFile + " in write mode.");
		}
		
	}

	/**
	 * Re-write the whole file when any element changes.
	 */
	protected void writeInvoice(Invoice invoice) {
		try ( PrintWriter out = new PrintWriter(new FileWriter(invoicesFile)); ) {
			parser.produceInvoices(getInvoices().values().stream())
					.forEach(out::println);
		} catch (Exception ex) {
			log.log(Level.WARNING, ex, 
					() -> "Couldn't open " + invoicesFile + " in write mode.");
		}
		
	}
	
	protected Stream<Update> readHistory() {
		return Stream.empty();
	}

	protected void writeUpdate(Update update) {
	}
}
