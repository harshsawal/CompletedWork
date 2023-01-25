package com.amica.billing.report;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.Billing;
import com.amica.billing.Customer;
import com.amica.billing.Invoice;

public class Reporter {

	private Billing billing;
	private LocalDate asOf;
	private String REPORTS_FOLDER;// = "C:\\Users\\A036779\\JavaTraining\\Billing1\\reports";
	
	private static final String FILENAME_INVOICE_BY_NUMBER = "_invoices_by_number.txt";
	private static final String FILENAME_INVOICE_BY_CUSTOMER = "_invoices_by_customer.txt";
	private static final String FILENAME_INVOICE_BY_ISSUEDATE = "_invoices_by_issuedate.txt";
	private static final String FILENAME_OVERDUE_INVOICES= "_overdue_invoices.txt";
	private static final String FILENAME_CUSTOMER_AND_VOLUME = "_customer_and_volume.txt";
	//private static final String FILENAME_INVOICE_BY_NUMBER = "";
	//private static final String FILENAME_INVOICE_BY_NUMBER = "";

	public Reporter(Billing billing, String REPORTS_FOLDER, LocalDate asOf) {
		this.billing = billing;
		this.asOf = asOf;
		this.REPORTS_FOLDER = REPORTS_FOLDER;
		// billing.addInvoiceListener(onInvoiceChanged());
	}

//	public void printToFile(String path, String header, Stream<Invoice> data) {
//
//		try (PrintWriter output = new PrintWriter(new FileWriter(path))) {
//			output.println(header);
//			output.println("==================================================================");
//			output.println();
//			output.println(" ###  Customer                  Issued      Amount      Paid      ");
//			output.println("----  ------------------------  ----------  ----------  ----------");
//			data.toList().forEach(s -> output.println(String.format("%4s  %-24s  %10s  %10s %10s", s.getNumber(),
//					s.getCustomer().getName(), s.getInvoiceDate(), s.getAmount(), s.getPaidDate().orElse(null))));
//			output.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	public void printToFile(String path, String header, Stream<Invoice> data) {

		try (PrintWriter output = new PrintWriter(new FileWriter(path))) {
			output.println(header);
			output.println("=".repeat(66));
			output.println();
			output.format("%4s  %-24s  %10s  %10s %10s"," ###", "Customer","Issued","Amount","Paid");
			output.println("-".repeat(4)+"  "+"-".repeat(24)+"  "+"-".repeat(10)+"  "+"-".repeat(10)+"  "+"-".repeat(10));
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
	
	public static String formatInvoice(Invoice invoice) {
		return String.format("%4s  %-24s  %10s  %10s %10s", 
				invoice.getNumber(),
				invoice.getCustomer().getName(),
				invoice.getInvoiceDate(), 
				invoice.getAmount(), 
				invoice.getPaidDate().orElse(null));
	}

	public void reportInvoicesOrderedByNumber() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + FILENAME_INVOICE_BY_NUMBER;
		String header = "All invoices, ordered by invoice number : ";
		//printToFile(filePath, header, billing.getInvoicesOrderedByNumber());
		
		try (PrintWriter output = new PrintWriter(new FileWriter(filePath))) {
			Stream<Invoice> data = billing.getInvoicesOrderedByNumber();
			output.println(header);
			output.println("=".repeat(66));
			output.println();
			output.format("%4s  %-24s  %10s  %10s %10s%n"," ###", "Customer","Issued","Amount","Paid");
			output.println("-".repeat(4)+"  "+"-".repeat(24)+"  "+"-".repeat(10)+"  "+"-".repeat(10)+"  "+"-".repeat(10));
			//output.println(data.map(Reporter::formatInvoice));
			data.toList().forEach(s -> output.println(String.format("%s", formatInvoice(s))));
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void reportInvoicesByIssueDate() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + FILENAME_INVOICE_BY_ISSUEDATE;
		String header = "Invoices ordered by IssueDate : ";
		//printToFile(filePath, header, billing.getInvoicesOrderedByIssueDate());
		
		try (PrintWriter output = new PrintWriter(new FileWriter(filePath))) {
			Stream<Invoice> data = billing.getInvoicesOrderedByIssueDate();
			output.println(header);
			output.println("=".repeat(66));
			output.println();
			output.format("%4s  %-24s  %10s  %10s %10s%n"," ###", "Customer","Issued","Amount","Paid");
			output.println("-".repeat(4)+"  "+"-".repeat(24)+"  "+"-".repeat(10)+"  "+"-".repeat(10)+"  "+"-".repeat(10));
			//output.println(data.map(Reporter::formatInvoice));
			data.toList().forEach(s -> output.println(String.format("%s", formatInvoice(s))));
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void reportInvoicesGroupedByCustomer() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + FILENAME_INVOICE_BY_CUSTOMER;
		String header = "All invoices, grouped by customer : ";
		//printToFile(filePath, header, billing.getInvoicesGroupedByCustomer());
		
		try (PrintWriter output = new PrintWriter(new FileWriter(filePath))) {
			Map<Customer, List<Invoice>> data = billing.getInvoicesGroupedByCustomer();
			output.println(header);
			output.println("=".repeat(66));
			output.println();
			output.format("       %-24s  %10s  %10s %10s%n", "Customer","Issued","Amount","Paid");
			output.println("-".repeat(4)+"  "+"-".repeat(24)+"  "+"-".repeat(10)+"  "+"-".repeat(10)+"  "+"-".repeat(10));
			
			for(Customer customer : data.keySet()) {
				output.println();
				output.println(customer.getName());
				//output.println(data.get(customer).stream().map(Reporter::formatInvoice));
				data.get(customer).forEach(s -> output.println(String.format("%s", formatInvoice(s))));//.stream().map(Reporter::formatInvoice));
			}
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void reportOverdueInvoices() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + FILENAME_OVERDUE_INVOICES;
		String header = "All overdue invoices : ";
		//printToFile(filePath, header, billing.getInvoicesGroupedByCustomer());
		
		try (PrintWriter output = new PrintWriter(new FileWriter(filePath))) {
			//Stream<Invoice> data = billing.getOverdueInvoices();
			Stream<Invoice> data = billing.getOverdueInvoices(asOf);
			output.println(header);
			output.println("=".repeat(72));
			output.println();
			output.format("       %-24s  %10s  %10s %10s %10s%n", "Customer","Issued","Amount","Paid","Due");
			output.println("-".repeat(4)+"  "+"-".repeat(24)+"  "+"-".repeat(10)+"  "+"-".repeat(10)+"  "+"-".repeat(10));
			
			data.toList().forEach(s -> output.println(String.format("%s  %10s", formatInvoice(s), s.getDueDate())));
			
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void reportCustomersAndVolume() {
		String filePath = REPORTS_FOLDER + "/" + getSource() + FILENAME_CUSTOMER_AND_VOLUME;
		String header = "All overdue invoices : ";
		//printToFile(filePath, header, billing.getInvoicesGroupedByCustomer());
		
//		try (PrintWriter output = new PrintWriter(new FileWriter(filePath))) {
//			Stream<Invoice> data = billing.getOverdueInvoices();
//			output.println(header);
//			output.println("=".repeat(72));
//			output.println();
//			output.format("       %-24s  %10s  %10s %10s %10s%n", "Customer","Issued","Amount","Paid","Due");
//			output.println("-".repeat(4)+"  "+"-".repeat(24)+"  "+"-".repeat(10)+"  "+"-".repeat(10)+"  "+"-".repeat(10));
//			
//			data.toList().forEach(s -> output.println(String.format("%s  %10s", formatInvoice(s), s.getDueDate())));
//			
//			output.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}

	public void onInvoiceChanged(Invoice invoice) {
		reportInvoicesOrderedByNumber();
		reportInvoicesGroupedByCustomer();
		reportOverdueInvoices();
		reportCustomersAndVolume();
	}
}
