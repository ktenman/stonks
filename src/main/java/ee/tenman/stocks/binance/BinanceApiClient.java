package ee.tenman.stocks.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import ee.tenman.stocks.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
class BinanceApiClient {
	
	@Value("binance_api_key.txt")
	ClassPathResource binanceApiKey;
	@Value("binance_secret_key.txt")
	ClassPathResource binanceSecretKey;
	
	@Bean
	public SpotClientImpl spotClient() {
		String key = FileUtils.getSecret(binanceApiKey);
		String secret = FileUtils.getSecret(binanceSecretKey);
		
		return new SpotClientImpl(key, secret);
	}
}
