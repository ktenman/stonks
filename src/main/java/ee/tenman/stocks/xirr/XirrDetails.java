package ee.tenman.stocks.xirr;

import java.time.LocalDate;
import java.util.Collection;

class XirrDetails {
    LocalDate start;
    LocalDate end;
    double minAmount = Double.POSITIVE_INFINITY;
    double maxAmount = Double.NEGATIVE_INFINITY;
    double total;
    double deposits;
    
    public XirrDetails(final Collection<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("Transactions collection cannot be null or empty.");
        }
        transactions.forEach(this::processTransaction);
        this.validateDateRange();
        this.validateTransactionAmounts();
    }
    
    private void processTransaction(final Transaction transaction) {
        this.updateDateRange(transaction);
        this.updateAmounts(transaction);
        this.total += transaction.amount();
        if (transaction.amount() < 0) {
            this.deposits -= transaction.amount();
        }
    }
    
    private void updateDateRange(final Transaction transaction) {
        if (this.start == null || this.start.isAfter(transaction.when())) {
            this.start = transaction.when();
        }
        if (this.end == null || this.end.isBefore(transaction.when())) {
            this.end = transaction.when();
        }
    }
    
    private void updateAmounts(final Transaction transaction) {
        this.minAmount = Math.min(this.minAmount, transaction.amount());
        this.maxAmount = Math.max(this.maxAmount, transaction.amount());
    }
    
    private void validateDateRange() {
        if (this.start == null || this.end == null || this.start.equals(this.end)) {
            throw new IllegalArgumentException("Invalid date range for transactions.");
        }
    }
    
    private void validateTransactionAmounts() {
        if (this.minAmount >= 0 || this.maxAmount <= 0) {
            throw new IllegalArgumentException("Need both positive and negative transactions.");
        }
    }
}
