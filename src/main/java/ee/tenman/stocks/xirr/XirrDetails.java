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
    
    public XirrDetails(Collection<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("Transactions collection cannot be null or empty.");
        }
        transactions.forEach(this::processTransaction);
        validateDateRange();
        validateTransactionAmounts();
    }
    
    private void processTransaction(final Transaction transaction) {
        updateDateRange(transaction);
        updateAmounts(transaction);
        total += transaction.amount();
        if (transaction.amount() < 0) {
            deposits -= transaction.amount();
        }
    }
    
    private void updateDateRange(Transaction transaction) {
        if (start == null || start.isAfter(transaction.when())) {
            start = transaction.when();
        }
        if (end == null || end.isBefore(transaction.when())) {
            end = transaction.when();
        }
    }
    
    private void updateAmounts(Transaction transaction) {
        minAmount = Math.min(minAmount, transaction.amount());
        maxAmount = Math.max(maxAmount, transaction.amount());
    }
    
    private void validateDateRange() {
        if (start == null || end == null || start.equals(end)) {
            throw new IllegalArgumentException("Invalid date range for transactions.");
        }
    }
    
    private void validateTransactionAmounts() {
        if (minAmount >= 0 || maxAmount <= 0) {
            throw new IllegalArgumentException("Need both positive and negative transactions.");
        }
    }
}
