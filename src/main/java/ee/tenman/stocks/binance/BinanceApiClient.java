package ee.tenman.stocks.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import ee.tenman.stocks.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.util.Optional;

@Configuration
@Slf4j
@Profile("!test")
class BinanceApiClient {
	
	@Value("binance_api_key.txt")
	ClassPathResource binanceApiKey;
	@Value("binance_secret_key.txt")
	ClassPathResource binanceSecretKey;
	
	@Bean
	public SpotClientImpl spotClient() {
		final Optional<String> key = FileUtils.getSecret(this.binanceApiKey);
		final Optional<String> secret = FileUtils.getSecret(this.binanceSecretKey);
		if (key.isEmpty() || secret.isEmpty()) {
			return new SpotClientImpl();
		}
		return new SpotClientImpl(key.get(), secret.get());
	}
}
