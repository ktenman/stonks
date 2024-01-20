package ee.tenman.stocks.xirr;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TransactionCalculator {
	private final BigDecimal baseMonthlyInvestment;
	private final List<Transaction> transactions = new ArrayList<>();
	private BigDecimal totalBoughtStocksCount = BigDecimal.ZERO;
	private LocalDate lastMonthDate = null;
	
	public TransactionCalculator(BigDecimal baseMonthlyInvestment) {
		this.baseMonthlyInvestment = baseMonthlyInvestment;
	}
	
	public void processDate(LocalDate date, BigDecimal price, LocalDate lastDataDate) {
		if (lastMonthDate == null || Period.between(lastMonthDate, date).getMonths() >= 1) {
			addStockPurchaseTransaction(date, price);
		}
		if (date.isEqual(lastDataDate)) {
			addFinalSellingTransaction(date, price);
		}
	}
	
	private void addStockPurchaseTransaction(LocalDate date, BigDecimal price) {
		lastMonthDate = date;
		BigDecimal stocksCount = baseMonthlyInvestment.divide(price, RoundingMode.DOWN);
		totalBoughtStocksCount = totalBoughtStocksCount.add(stocksCount);
		transactions.add(new Transaction(baseMonthlyInvestment.negate().doubleValue(), date));
	}
	
	private void addFinalSellingTransaction(LocalDate date, BigDecimal price) {
		BigDecimal amount = price.multiply(totalBoughtStocksCount);
		transactions.add(new Transaction(amount.doubleValue(), date));
	}
}
