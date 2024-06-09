package ee.tenman.stocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StocksApplication {
	
	public static void main(final String[] args) {
		SpringApplication.run(StocksApplication.class, args);
	}
	
}
