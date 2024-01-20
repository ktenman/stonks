package ee.tenman.stocks.xirr;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class Xirr {
    private static final double DAYS_IN_YEAR = 365.25;
    private final List<Investment> investments;
    private final XirrDetails details;
    private final NewtonRaphsonSolver solver = new NewtonRaphsonSolver();
    private Double guess;
    
    public Xirr(Collection<Transaction> transactions) {
        if (transactions == null || transactions.size() < 2) {
            throw new IllegalArgumentException("Must have at least two transactions");
        }
        this.details = new XirrDetails(transactions);
        this.investments = transactions.stream()
                .map(t -> new Investment(t, details.end))
                .toList();
    }
    
    public double xirr() {
        if (details.maxAmount == 0) {
            return -1;
        }
        guess = (guess != null) ? guess : (details.total / details.deposits) / (DAYS.between(details.start, details.end) / DAYS_IN_YEAR);
        UnivariateDifferentiableFunction xirrFunction = createXirrFunction();
        return solver.solve(1000, xirrFunction, guess, -1.0, 1.0);
    }
    
    private UnivariateDifferentiableFunction createXirrFunction() {
        return new UnivariateDifferentiableFunction() {
            @Override
            public double value(double rate) {
                return investments.stream().mapToDouble(investment -> investment.presentValue(rate)).sum();
            }
            
            @Override
            public DerivativeStructure value(DerivativeStructure t) {
                double rate = t.getValue();
                return new DerivativeStructure(t.getFreeParameters(), t.getOrder(), value(rate),
                        investments.stream().mapToDouble(inv -> inv.derivative(rate)).sum());
            }
        };
    }
    
    private static class Investment {
        final double amount;
        final double years;
        
        Investment(Transaction transaction, LocalDate endDate) {
            this.amount = transaction.amount();
            this.years = DAYS.between(transaction.when(), endDate) / DAYS_IN_YEAR;
        }
        
        double presentValue(double rate) {
            return years == 0 ? amount : amount * Math.pow(1 + rate, years);
        }
        
        double derivative(double rate) {
            return years == 0 ? 0 : amount * years * Math.pow(1 + rate, years - 1);
        }
    }
}
