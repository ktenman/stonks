package ee.tenman.stocks.binance;

import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.impl.SpotClientImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class BinanceService {
	
	@Resource
	private SpotClientImpl spotClient;
	
	@Retryable(backoff = @Backoff(delay = 1000))
	public TreeMap<LocalDate, BigDecimal> getMonthlyPrices(String symbol) {
		TreeMap<LocalDate, BigDecimal> monthlyPrices = new TreeMap<>();
		try {
			LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
			parameters.put("symbol", symbol);
			parameters.put("interval", "1M");
			String result = spotClient.createMarket().klines(parameters);
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray candlestick = jsonArray.getJSONArray(i);
				long timestamp = candlestick.getLong(0);
				BigDecimal closePrice = new BigDecimal(candlestick.getString(4));
				LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
				monthlyPrices.put(date, closePrice);
			}
		} catch (BinanceConnectorException e) {
			log.error("Error getting monthly prices for symbol: {}", symbol, e);
		}
		return monthlyPrices;
	}
	
	@Retryable(backoff = @Backoff(delay = 1000))
	public Map<String, String> getSymbols() {
		Map<String, String> symbols = new HashMap<>();
		try {
			String result = spotClient.createMarket().exchangeInfo(new LinkedHashMap<>());
			JSONArray jsonArray = new JSONArray(result);
			jsonArray.toList().forEach(obj -> {
				JSONArray symbol = (JSONArray) obj;
				String symbolName = symbol.getString(0);
				String baseAsset = symbol.getString(1);
				symbols.put(symbolName, baseAsset);
			});
		} catch (BinanceConnectorException e) {
			log.error("Error getting symbols", e);
		}
		return symbols;
	}
}
