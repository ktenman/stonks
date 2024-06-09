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
	
	public Xirr(final Collection<Transaction> transactions) {
		if (transactions == null || transactions.size() < 2) {
			throw new IllegalArgumentException("Must have at least two transactions");
		}
		this.details = new XirrDetails(transactions);
		this.investments = transactions.stream()
				.map(t -> new Investment(t, this.details.end))
				.toList();
	}
	
	public double xirr() {
		if (this.details.maxAmount == 0) {
			return -1;
		}
		this.guess = (this.guess != null) ? this.guess : (this.details.total / this.details.deposits) / (DAYS.between(this.details.start, this.details.end) / DAYS_IN_YEAR);
		final UnivariateDifferentiableFunction xirrFunction = this.createXirrFunction();
		return this.solver.solve(1000, xirrFunction, this.guess, -1.0, 1.0);
	}
	
	private UnivariateDifferentiableFunction createXirrFunction() {
		return new UnivariateDifferentiableFunction() {
			@Override
			public double value(final double rate) {
				return Xirr.this.investments.stream().mapToDouble(investment -> investment.presentValue(rate)).sum();
			}
			
			@Override
			public DerivativeStructure value(final DerivativeStructure t) {
				final double rate = t.getValue();
				return new DerivativeStructure(t.getFreeParameters(), t.getOrder(), this.value(rate),
						Xirr.this.investments.stream().mapToDouble(inv -> inv.derivative(rate)).sum());
			}
		};
	}
	
	private static class Investment {
		final double amount;
		final double years;
		
		Investment(final Transaction transaction, final LocalDate endDate) {
			this.amount = transaction.amount();
			this.years = DAYS.between(transaction.when(), endDate) / DAYS_IN_YEAR;
		}
		
		double presentValue(final double rate) {
			return this.years == 0 ? this.amount : this.amount * Math.pow(1 + rate, this.years);
		}
		
		double derivative(final double rate) {
			return this.years == 0 ? 0 : this.amount * this.years * Math.pow(1 + rate, this.years - 1);
		}
	}
}
