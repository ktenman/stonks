package ee.tenman.stocks.xirr;

import ee.tenman.stocks.alphavantage.AlphaVantageResponse;
import ee.tenman.stocks.alphavantage.AlphaVantageService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;

@Service
@Slf4j
public class XirrService {
    
    private static final BigDecimal BASE_ORIGINAL_BIG_DECIMAL_STOCK = new BigDecimal("1000.00");
    @Resource
    private AlphaVantageService alphaVantageService;
    
    @SneakyThrows
    public double calculateStockXirr(String ticker) {
        TreeMap<LocalDate, BigDecimal> historicalData = fetchHistoricalData(ticker);
        List<Transaction> transactions = calculateTransactions(historicalData);
        Xirr xirr = new Xirr(transactions);
	    double xirrValue = xirr.xirr();
	    log.info("{} : {}%", ticker, xirrValue * 100);
	    return xirrValue + 1;
    }
	
	private TreeMap<LocalDate, BigDecimal> fetchHistoricalData(String ticker) {
		TreeMap<LocalDate, BigDecimal> historicalData = new TreeMap<>();
        try {
            AlphaVantageResponse response = alphaVantageService.getMonthlyTimeSeries(ticker);
	        response.getMonthlyTimeSeries().forEach((key, value) -> {
		        LocalDate date = LocalDate.parse(key, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		        BigDecimal price = value.getClose();
		        historicalData.put(date, price);
	        });
        } catch (Exception e) {
            log.error("Error fetching historical data for ticker: {}, error: ", ticker, e);
        }
		return historicalData;
    }
	
	private List<Transaction> calculateTransactions(TreeMap<LocalDate, BigDecimal> historicalData) {
		TransactionCalculator calculator = new TransactionCalculator(BASE_ORIGINAL_BIG_DECIMAL_STOCK);
		LocalDate lastDataDate = historicalData.lastKey();
		historicalData.forEach((date, price) -> calculator.processDate(date, price, lastDataDate));
		return calculator.getTransactions();
    }
    
}
