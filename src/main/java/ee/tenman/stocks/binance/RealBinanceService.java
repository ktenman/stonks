package ee.tenman.stocks.binance;

import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.impl.SpotClientImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
@Slf4j
@Profile("!test")
public class RealBinanceService implements BinanceService {
	@Resource
	private SpotClientImpl spotClient;
	
	@Retryable(backoff = @Backoff(delay = 1000))
	public SortedMap<LocalDate, BigDecimal> getMonthlyPrices(final String symbol) {
		final TreeMap<LocalDate, BigDecimal> monthlyPrices = new TreeMap<>();
		try {
			final LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
			parameters.put("symbol", symbol);
			parameters.put("interval", "1M");
			final String result = this.spotClient.createMarket().klines(parameters);
			final JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				final JSONArray candlestick = jsonArray.getJSONArray(i);
				final long timestamp = candlestick.getLong(0);
				final BigDecimal closePrice = new BigDecimal(candlestick.getString(4));
				final LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
				monthlyPrices.put(date, closePrice);
			}
		} catch (final BinanceConnectorException e) {
			log.error("Error getting monthly prices for symbol: {}", symbol, e);
		}
		return monthlyPrices;
	}
}
