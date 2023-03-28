package com.amica.billing;


import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amica.billing.TestUtility.IntegrationConfig;
import com.amica.billing.db.Migration;

/**
 * Integration test for the {@link Reporter} that uses MongoDB.
 * We configure a {@link Migration} 
 * utility, and reset the database before each test.
 * 
 * @author Will Provost
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=IntegrationConfig.class)
public class ReporterMongoIntegrationTest extends ReporterIntegrationTestBase {
	
	@Autowired
	private Migration migration;
	
	/**
	 * Let the base class set up the reporter, and make sure that we
	 * reset the database before the next test.
	 */
	@Override
	@BeforeEach
	public void setUp() throws IOException {
		super.setUp();
		migration.migrate();
	}
}
