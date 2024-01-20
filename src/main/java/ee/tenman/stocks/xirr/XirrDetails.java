package ee.tenman.stocks.xirr;

import java.time.LocalDate;
import java.util.stream.Collector;

class XirrDetails {
    LocalDate start;
    LocalDate end;
    double minAmount = Double.POSITIVE_INFINITY;
    double maxAmount = Double.NEGATIVE_INFINITY;
    double total;
    double deposits;
    
    public static Collector<Transaction, XirrDetails, XirrDetails> collector() {
        return Collector.of(XirrDetails::new, XirrDetails::accumulate, XirrDetails::combine);
    }
    
    public void accumulate(final Transaction tx) {
        updateStartEnd(tx);
        updateAmounts(tx);
        total += tx.amount();
        if (tx.amount() < 0) {
            deposits -= tx.amount();
        }
    }
    
    private void updateStartEnd(Transaction tx) {
        start = (start == null || start.isAfter(tx.when())) ? tx.when() : start;
        end = (end == null || end.isBefore(tx.when())) ? tx.when() : end;
    }
    
    private void updateAmounts(Transaction tx) {
        minAmount = Math.min(minAmount, tx.amount());
        maxAmount = Math.max(maxAmount, tx.amount());
    }
    
    public XirrDetails combine(final XirrDetails other) {
        start = start.isBefore(other.start) ? start : other.start;
        end = end.isAfter(other.end) ? end : other.end;
        minAmount = Math.min(minAmount, other.minAmount);
        maxAmount = Math.max(maxAmount, other.maxAmount);
        total += other.total;
        deposits += other.deposits;
        return this;
    }
    
    public void validate() {
        if (start == null || end == null) {
            throw new IllegalArgumentException("No transactions to analyze");
        }
        if (start.equals(end)) {
            throw new IllegalArgumentException("Transactions must not all be on the same day.");
        }
        if (minAmount >= 0 || maxAmount <= 0) {
            throw new IllegalArgumentException("There must be both positive and negative transactions.");
        }
    }
}
