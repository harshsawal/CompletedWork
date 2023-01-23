package com.amica.billing.report;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.amica.billing.Billing;
import com.amica.billing.Customer;
import com.amica.billing.Invoice;

public class Reporter {

	private Billing billing;
	private LocalDate asOf;
	private String REPORTS_FOLDER;// = "C:\\Users\\A036779\\JavaTraining\\Billing1\\reports";

	public Reporter(Billing billing, String REPORTS_FOLDER, LocalDate asOf) {
		this.billing = billing;
		this.asOf = asOf;
		this.REPORTS_FOLDER = REPORTS_FOLDER;
		// billing.addInvoiceListener(onInvoiceChanged());
	}

	public void printToFile(String path, String header, Stream<Invoice> data) {

		try (PrintWriter output = new PrintWriter(new FileWriter(path))) {
			output.println(header);
			output.println("==================================================================");
			output.println();
			output.println(" ###  Customer                  Issued      Amount      Paid      ");
			output.println("----  ------------------------  ----------  ----------  ----------");
			data.toList().forEach(s -> output.println(String.format("%4s  %-24s  %10s  %10s %10s", s.getNumber(),
					s.getCustomer().getName(), s.getInvoiceDate(), s.getAmount(), s.getPaidDate().orElse(null))));
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void printToFile(String path, String header, Map<Customer, List<Invoice>> map) {

		try (PrintWriter output = new PrintWriter(new FileWriter(path))) {
			output.println(header);
			output.println("==================================================================");
			output.println();
			output.println(" ###  Customer                  Issued      Amount      Paid      ");
			output.println("----  ------------------------  ----------  ----------  ----------");
			data.toList().forEach(s -> output.println(String.format("%4s  %-24s  %10s  %10s %10s", s.getNumber(),
					s.getCustomer().getName(), s.getInvoiceDate(), s.getAmount(), s.getPaidDate().orElse(null))));
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getSource() {
		String source = "";
		if (REPORTS_FOLDER.contains("/")) {
			source = REPORTS_FOLDER.substring(REPORTS_FOLDER.lastIndexOf("/"));
		}
		return source;
	}

	public void reportInvoicesOrderedByNumber() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + "_invoices_by_number.txt";
		String header = "All invoices, ordered by invoice number : ";
		printToFile(filePath, header, billing.getInvoicesOrderedByNumber());

	}

	public void reportInvoicesByIssueDate() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + "_invoices_by_issuedate.txt";
		String header = "Invoices ordered by Number : ";
		printToFile(filePath, header, billing.getInvoicesOrderedByIssueDate());
	}

	public void reportInvoicesGroupedByCustomer() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + "_invoices_by_customer.txt";
		String header = "All invoices, ordered by invoice number : ";
		printToFile(filePath, header, billing.getInvoicesGroupedByCustomer());
	}

	public void reportOverdueInvoices() {

	}

	public void reportCustomersAndVolume() {

	}

	public void onInvoiceChanged(Invoice invoice) {
		reportInvoicesOrderedByNumber();
	}
}
