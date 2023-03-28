package com.amica.billing.ws;

import static com.amica.billing.Reporter.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Setter;
import lombok.SneakyThrows;

/**
 * REST controller for reports.
 *
 * @author Will Provost
 */
@RestController
@RequestMapping("reports")
public class ReportController {

	@Value("${Reporter.outputFolder}")
	@Setter
	private String reportsFolder;

	@SneakyThrows
	private String readReport(String filename) {
		try ( Stream<String> lines = Files.lines(Paths.get(String.format
				("%s/%s", reportsFolder, filename))); ) {
			return lines.collect(Collectors.joining("\n"));
		}
	}
	
	@GetMapping("invoices")
	public String getInvoicesByNumber() {
		return readReport(FILENAME_INVOICES_BY_NUMBER);
	}
	
	@GetMapping("overdue_invoices")
	public String getOverdueInvoices() {
		return readReport(FILENAME_OVERDUE_INVOICES);
	}
	
	@GetMapping("invoices_by_customer")
	public String getInvoicesByCustomer() {
		return readReport(FILENAME_INVOICES_BY_CUSTOMER);
	}
	
	@GetMapping("customers_and_volume")
	public String getCustomersAndVolume() {
		return readReport(FILENAME_CUSTOMERS_AND_VOLUME);
	}
}
