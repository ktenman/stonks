package ee.tenman.stocks.binance;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

@SpringBootTest
class BinanceServiceTest {
	
	@Resource
	BinanceService binanceService;
	
	@Test
	@Disabled
	void getMonthlyPrices() {
		TreeMap<LocalDate, BigDecimal> btc = binanceService.getMonthlyPrices("BTCUSDT");
		TreeMap<LocalDate, BigDecimal> btc2 = binanceService.getMonthlyPrices("ETHUSDT");
		System.out.println();
	}
	
}
