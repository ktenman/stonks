package ee.tenman.stocks.alphavantage;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Random;

@FeignClient(name = AlphaVantageClient.CLIENT_NAME,
		url = "${alphavantage.url}",
		configuration = AlphaVantageClient.Configuration.class)
public interface AlphaVantageClient {
	
	String CLIENT_NAME = "alphaVantageClient";
	
	@GetMapping("/query?function={function}&symbol={symbol}")
	AlphaVantageResponse getMonthlyTimeSeries(@PathVariable String function, @PathVariable String symbol);
	
	@GetMapping("/query?function={function}&keywords={search}&page={page}")
	SearchResponse getSearch(
			@PathVariable String function,
			@PathVariable String search,
			@PathVariable String page
	);
	
	@GetMapping("/query?function={function}&symbol={symbol}")
	String getString(@PathVariable String function, @PathVariable String symbol);
	
	class Configuration {
		private static final Random RANDOM = new Random();
		
		private final RateLimiter rateLimiter = new RateLimiter();
		
		@Bean
		public RequestInterceptor requestInterceptor() {
			List<String> keys = List.of(
					"LP89F1C09XFYGW1U",
					"MNIF0F2HMV93J8TX",
					"DOSJJM3U7HKZ741I",
					"WIODF0D96B7EKTK2",
					"3919KXD313S1RX7J",
					"0MOK4N559XJ9EIDR"
			);
			
			String randomKey = keys.get(RANDOM.nextInt(0, keys.size()));
			return template -> rateLimiter.check(() -> template.query("apikey", randomKey));
		}
	}
}
