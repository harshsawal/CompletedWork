package com.amica.billing.db.mongo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Update;
import com.amica.billing.db.CachingPersistence;
import com.amica.billing.db.CustomerRepository;
import com.amica.billing.db.HistoryRepository;
import com.amica.billing.db.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/**
 * Persistence implementation using MongoDB.
 * We trigger an initial load when configured as a Spring bean,
 * and support re-loading programmatically after that.
 * 
 * @author Will Provost
 */
@Component
@Primary
@Log
public class MongoPersistence extends CachingPersistence {

	public static final long ARCHIVE_INTERVAL_MSEC = 30000;
	
	private CustomerRepository customers;
	private InvoiceRepository invoices;
	private HistoryRepository historyRepo;
	private ObjectMapper mapper;

	public MongoPersistence(CustomerRepository customers, InvoiceRepository invoices,
			HistoryRepository historyRepo, ObjectMapper mapper) {
		this.customers = customers;
		this.invoices = invoices;
		this.historyRepo = historyRepo;
		this.mapper = mapper;
	}
	
	@Value("${MongoPersistence.archiveFolder:archives}")
	private String archiveFolder;
	
	private Optional<LocalDateTime> spooledUpTo = Optional.empty(); 

	@Override
	@PostConstruct
	public void load() {
		super.load();
	}
	
	protected Stream<Customer> readCustomers() {
		return customers.streamAllBy();
	}
	
	protected Stream<Invoice> readInvoices() {
		return invoices.streamAllBy();
	}
	
	protected Stream<Update> readHistory() {
		return historyRepo.streamAllBy();
	}

	protected void writeCustomer(Customer customer) {
		customers.save(customer);
	}
	
	protected void writeInvoice(Invoice invoice) {
		invoices.save(invoice);
	}
	
	protected void writeUpdate(Update update) {
		historyRepo.save(update);
	}

	@Scheduled(fixedRate=ARCHIVE_INTERVAL_MSEC)
	@SneakyThrows
	public void spoolUpdates() {
		List<Update> segment = history.stream()
				.filter(u -> spooledUpTo.map(u.getWhen()::isAfter).orElse(true))
				.toList();
		if (!segment.isEmpty()) {
			spooledUpTo = segment.stream()
					.map(Update::getWhen).max(LocalDateTime::compareTo);
			String archive = archiveFolder + "/History_" + spooledUpTo.toString()
			.replace("T", "_").replace(":", "_") + ".json";
			try ( PrintWriter out = new PrintWriter(new FileWriter(archive)); ) {
				for (Update update : history) { 
					String timestamp = update.getWhen().toString().substring(0,23);
					String updated = update.getCustomer() != null
							? mapper.writeValueAsString(update.getCustomer())
							: mapper.writeValueAsString(update.getInvoice());
					out.format("%s %s%n", timestamp, updated);
				}
			}
			
			log.info(() -> "Archived updates up to " + spooledUpTo);
		}
	}
}
