package com.amica.billing.db.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.amica.billing.db.CustomerRepository;
import com.amica.billing.db.InvoiceRepository;
import com.amica.billing.db.Migration;

/**
 * Utility application that replicates all data from the prepared
 * CSV-format files to a MongoDB database.
 * 
 * @author Will Provost
 */
@ComponentScan(basePackageClasses={
		com.amica.billing.parse.ParserPersistence.class, 
		CustomerRepository.class
	})
@EnableAutoConfiguration
@EnableMongoRepositories(basePackageClasses=CustomerRepository.class)
@PropertySource(value={"classpath:DB.properties","classpath:migration.properties"})
public class MigrateCSVToMongo {
	
	/**
	 * Run the Spring application, get the {@link Migration} bean,
	 * call its {@link Migration#migrate migrate method},
	 * and then use the repository beans to report the counts of
	 * customers and invoices in the replicated database.
	 */
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(MigrateCSVToMongo.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		
		try ( ConfigurableApplicationContext context = application.run(); ) {
			context.getBean(Migration.class).migrate();
			
			CustomerRepository customers = context.getBean(CustomerRepository.class);
			InvoiceRepository invoices = context.getBean(InvoiceRepository.class);
			System.out.format("Migrated %d customers.%n", customers.count());
			System.out.format("Migrated %d invoices.%n", invoices.count());
		}
	}
}
