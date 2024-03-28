package practice.virtualcurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import practice.virtualcurrency.test.TestDataInit;
import practice.virtualcurrency.test.repository.TestRepository;
import practice.virtualcurrency.test.service.TestService;

@SpringBootApplication
public class VirtualcurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualcurrencyApplication.class, args);
	}

	@Bean
	@Profile("Test")
	public TestDataInit testDataInit(TestService testService) {
		return new TestDataInit(testService);
	}

}
