package com.amica.billing.parse;

import java.io.FileWriter;
import java.io.IOException;
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
import com.amica.billing.db.CachingPersistence;

import lombok.Setter;
import lombok.extern.java.Log;


@Component
@Log
public class ParserPersistence extends CachingPersistence{
	
	@Value("${arserPersistence.customersFile}")
	@Setter
	private static String customersFile;
	
	@Value("${arserPersistence.invoicesFile}")
	@Setter
	private static String invoicesFile;
	
	private static Parser parser;
	
	@Override
	@PostConstruct
	public void load() {
		parser = ParserFactory.createParser(customersFile);
		super.load();
	}

	@Override
	protected Stream<Customer> readCustomers() {
		// TODO Auto-generated method stub
		
		try {
			return parser.parseCustomers(Files.lines(Paths.get(customersFile)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, "Unable to load customers.", e);
			e.printStackTrace();
		}
		return Stream.empty();
	}

	@Override
	protected Stream<Invoice> readInvoices() {
		// TODO Auto-generated method stub
		
		try {
			return parser.parseInvoices(Files.lines(Paths.get(invoicesFile)), getCustomers());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, "Unable to load invoices.", e);
			e.printStackTrace();
		}
		return Stream.empty();
	}

	@Override
	protected void writeCustomer(Customer customer) {
		// TODO Auto-generated method stub
		try (PrintWriter out = new PrintWriter(new FileWriter(customersFile));) {
			parser.produceCustomers(getCustomers().values().stream()).forEach(out::println);
		} catch (IOException e) {
			log.log(Level.WARNING, e, () -> "Unable to open and write to " + customersFile);
		}
		
	}

	@Override
	protected void writeInvoice(Invoice invoice) {
		// TODO Auto-generated method stub
		try (PrintWriter out = new PrintWriter(new FileWriter(invoicesFile));) {
			parser.produceInvoices(getInvoices().values().stream()).forEach(out::println);
		} catch (IOException e) {
			log.log(Level.WARNING, e, () -> "Unable to open and write to " + invoicesFile);
		}
		
	}

}
