package ee.tenman.stocks.xirr;

import ee.tenman.stocks.alphavantage.AlphaVantageResponse;
import ee.tenman.stocks.alphavantage.AlphaVantageService;
import ee.tenman.stocks.binance.BinanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

@Service
public class PriceService {
	
	@Resource
	private AlphaVantageService alphaVantageService;
	
	@Resource
	private BinanceService binanceService;
	
	public TreeMap<LocalDate, BigDecimal> getHistoricalData(String ticker) {
		try {
			return binanceService.getMonthlyPrices(ticker);
		} catch (Exception e) {
			TreeMap<LocalDate, BigDecimal> historicalData = new TreeMap<>();
			AlphaVantageResponse response = alphaVantageService.getMonthlyTimeSeries(ticker);
			response.getMonthlyTimeSeries().forEach((key, value) -> {
				LocalDate date = LocalDate.parse(key, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				BigDecimal price = value.getClose();
				historicalData.put(date, price);
			});
			return historicalData;
		}
	}
}
