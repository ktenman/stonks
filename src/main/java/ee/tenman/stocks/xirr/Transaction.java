package ee.tenman.stocks.xirr;

import java.time.LocalDate;

public record Transaction(double amount, LocalDate when) {
}
