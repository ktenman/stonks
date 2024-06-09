package ee.tenman.stocks.binance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
@Slf4j
@Profile("test")
public class MockBinanceService implements BinanceService {
	@Override
	public SortedMap<LocalDate, BigDecimal> getMonthlyPrices(String symbol) {
		return new TreeMap<>() {
		};
	}
	
	@Override
	public Map<String, String> getSymbols() {
		return Map.of();
	}
}
