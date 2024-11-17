package ee.tenman.stocks.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@Profile("!test")
class BinanceApiClient {
	
	@Bean
	public SpotClientImpl spotClient() {
		return new SpotClientImpl();
	}
}
