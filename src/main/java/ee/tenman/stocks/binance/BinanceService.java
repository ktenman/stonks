package ee.tenman.stocks.binance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;

public interface BinanceService {
	
	SortedMap<LocalDate, BigDecimal> getMonthlyPrices(final String symbol);
	
	Map<String, String> getSymbols();
}
