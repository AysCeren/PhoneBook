package com.project.contactsdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") //uses StubCityLookup instead of the external city service
class ContactsdemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
