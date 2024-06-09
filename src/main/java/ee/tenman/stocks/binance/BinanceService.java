package ee.tenman.stocks.binance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.SortedMap;

public interface BinanceService {
	
	SortedMap<LocalDate, BigDecimal> getMonthlyPrices(final String symbol);
}
