package ee.tenman.stocks.xirr;

import ee.tenman.stocks.alphavantage.AlphaVantageResponse;
import ee.tenman.stocks.alphavantage.AlphaVantageService;
import ee.tenman.stocks.binance.BinanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class PriceService {
	
	@Resource
	private AlphaVantageService alphaVantageService;
	
	@Resource
	private BinanceService binanceService;
	
	public SortedMap<LocalDate, BigDecimal> getHistoricalData(final String ticker) {
		try {
			return this.binanceService.getMonthlyPrices(ticker);
		} catch (final Exception e) {
			final TreeMap<LocalDate, BigDecimal> historicalData = new TreeMap<>();
			final AlphaVantageResponse response = this.alphaVantageService.getMonthlyTimeSeries(ticker);
			response.getMonthlyTimeSeries().forEach((key, value) -> {
				final LocalDate date = LocalDate.parse(key, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				final BigDecimal price = value.getClose();
				historicalData.put(date, price);
			});
			return historicalData;
		}
	}
}
