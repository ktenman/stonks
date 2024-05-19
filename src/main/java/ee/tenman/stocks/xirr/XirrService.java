package ee.tenman.stocks.xirr;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

@Service
@Slf4j
public class XirrService {
	
	private static final BigDecimal BASE_ORIGINAL_BIG_DECIMAL_STOCK = new BigDecimal("3000.00");
	
	@Resource
	PriceService priceService;
	
	@PostConstruct
	public void init() {
		log.info("XIRR service initialized");
        this.calculateStockXirr("QDVE.DEX");
        this.calculateStockXirr("IITU");
        this.calculateStockXirr("AMZN");
        this.calculateStockXirr("BTCUSDT");
        this.calculateStockXirr("ETHUSDT");
	}
	
	public double calculateStockXirr(final String ticker) {
		try {
			final List<Transaction> transactions = this.processHistoricalData(ticker);
			final double xirrValue = new Xirr(transactions).xirr();
			log.info("{} : {}%", ticker, xirrValue * 100);
			return xirrValue + 1;
		} catch (final Exception e) {
			log.error("Error in calculating XIRR for ticker: {}, error: ", ticker, e);
			return Double.NaN;
		}
	}
	
	private List<Transaction> processHistoricalData(final String ticker) {
		final SortedMap<LocalDate, BigDecimal> historicalData = this.priceService.getHistoricalData(ticker);
		final TransactionCalculator calculator = new TransactionCalculator(BASE_ORIGINAL_BIG_DECIMAL_STOCK);
		final LocalDate lastDataDate = historicalData.lastKey();
		historicalData.forEach((date, price) -> calculator.processDate(date, price, lastDataDate));
		return calculator.getTransactions();
	}
}
