package ee.tenman.stocks.xirr;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TransactionCalculator {
	private final BigDecimal baseMonthlyInvestment;
	private final List<Transaction> transactions = new ArrayList<>();
	private BigDecimal totalBoughtStocksCount = BigDecimal.ZERO;
	private LocalDate lastMonthDate = null;
	
	public TransactionCalculator(final BigDecimal baseMonthlyInvestment) {
		this.baseMonthlyInvestment = baseMonthlyInvestment;
	}
	
	public void processDate(final LocalDate date, final BigDecimal price, final LocalDate lastDataDate) {
		if (this.shouldAddStockPurchase(date)) {
            this.addStockPurchaseTransaction(date, price);
		}
		if (date.isEqual(lastDataDate)) {
            this.addFinalSellingTransaction(date, price);
		}
	}
	
	private boolean shouldAddStockPurchase(final LocalDate date) {
		return this.lastMonthDate == null || ChronoUnit.MONTHS.between(this.lastMonthDate.withDayOfMonth(1), date.withDayOfMonth(1)) >= 1;
	}
	
	private void addStockPurchaseTransaction(final LocalDate date, final BigDecimal price) {
        this.lastMonthDate = date;
		final BigDecimal stocksCount = this.baseMonthlyInvestment.divide(price, RoundingMode.DOWN);
        this.totalBoughtStocksCount = this.totalBoughtStocksCount.add(stocksCount);
        this.transactions.add(new Transaction(this.baseMonthlyInvestment.negate().doubleValue(), date));
	}
	
	private void addFinalSellingTransaction(final LocalDate date, final BigDecimal price) {
		final BigDecimal amount = price.multiply(this.totalBoughtStocksCount);
        this.transactions.add(new Transaction(amount.doubleValue(), date));
	}
}
