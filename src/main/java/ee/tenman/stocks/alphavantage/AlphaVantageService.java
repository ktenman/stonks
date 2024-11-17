package ee.tenman.stocks.alphavantage;

import com.google.gson.Gson;
import ee.tenman.stocks.configuration.RedisConfiguration;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class AlphaVantageService {
	
	Gson gson = new Gson();
	
	@Resource
	private AlphaVantageClient client;
	
	@Retryable(backoff = @Backoff(delay = 1000))
	@Cacheable(value = RedisConfiguration.ONE_DAY_CACHE, key = "#symbol")
	public AlphaVantageResponse getMonthlyTimeSeries(final String symbol) {
		return this.getTicker(symbol)
				.map(ticker -> {
					final AlphaVantageResponse timeSeriesMonthly = this.client.getMonthlyTimeSeries("TIME_SERIES_MONTHLY", ticker);
					log.info("Retrieved monthly ticker data: {}", this.gson.toJson(timeSeriesMonthly));
					return timeSeriesMonthly;
				})
				.orElseThrow(() -> new RuntimeException("Error while fetching data from Alpha Vantage"));
	}
	
	@Retryable(backoff = @Backoff(delay = 1000))
	public Optional<String> getTicker(final String search) {
		final SearchResponse symbolSearch = this.client.getSearch("SYMBOL_SEARCH", search, String.valueOf(1));
		return Optional.ofNullable(symbolSearch)
				.map(SearchResponse::getBestMatches)
				.stream()
				.flatMap(Collection::stream)
				.findFirst()
				.map(SearchResponse.SearchData::getSymbol);
	}
	
}
