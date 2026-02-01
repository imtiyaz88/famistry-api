package com.famistry.famistry_personnel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.famistry.famistry_personnel.config.TestKafkaConfig;

@SpringBootTest
@Import(TestKafkaConfig.class)
@ActiveProfiles("test")
class FamistryPersonnelApplicationTests {

	@Test
	void contextLoads() {
	}

}
