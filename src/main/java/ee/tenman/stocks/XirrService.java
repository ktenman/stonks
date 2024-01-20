package ee.tenman.stocks;

import ee.tenman.stocks.alphavantage.AlphaVantageResponse;
import ee.tenman.stocks.alphavantage.AlphaVantageResponse.AlphaVantageDayData;
import ee.tenman.stocks.alphavantage.AlphaVantageService;
import ee.tenman.stocks.xirr.Transaction;
import ee.tenman.stocks.xirr.Xirr;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.math.BigDecimal.ZERO;

@Service
@Slf4j
public class XirrService {
    
    private static final BigDecimal BASE_ORIGINAL_BIG_DECIMAL_STOCK = new BigDecimal("1000.00");
    @Resource
    AlphaVantageService alphaVantageService;
    
    @SneakyThrows
    public double calculateStockXirr(String ticker) {
        TreeMap<LocalDate, BigDecimal> historicalData = fetchHistoricalData(ticker);
        List<Transaction> transactions = calculateTransactions(historicalData);
        Xirr xirr = new Xirr(transactions);
        log.info("{} : {}%", ticker, xirr.xirr() * 100);
        return xirr.xirr() + 1;
    }

    private TreeMap<LocalDate, BigDecimal> fetchHistoricalData(String ticker) {
        try {
            AlphaVantageResponse response = alphaVantageService.getMonthlyTimeSeries(ticker);
            TreeMap<LocalDate, BigDecimal> historicalData = new TreeMap<>();

            for (Map.Entry<String, AlphaVantageDayData> entry : response.getMonthlyTimeSeries().entrySet()) {
                String date = entry.getKey();
                BigDecimal price = entry.getValue().getClose();
                LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                historicalData.put(localDate, price);
            }

            return historicalData;
        } catch (Exception e) {
            log.error("Error fetching historical data for ticker: {}, error: ", ticker, e);
        }
        return new TreeMap<>();
    }


    private List<Transaction> calculateTransactions(TreeMap<LocalDate, BigDecimal> historicalData) {
        int totalBoughtStocksCount = 0;
        int countOfDifferences = 0;
        BigDecimal sumOfPercentageDifferences = ZERO;
        BigDecimal totalInvestment = ZERO;
        BigDecimal highestPrice = ZERO;
        BigDecimal biggestPercentageDrop = ZERO;
        BigDecimal monthlyInvestment = BASE_ORIGINAL_BIG_DECIMAL_STOCK;
        BigDecimal previousPrice = null;
        LocalDate biggestDropStartDate = null;
        LocalDate biggestDropEndDate = null;
        LocalDate currentDropStartDate = null;
        LocalDate lastYearDate = historicalData.firstKey();
        LocalDate lastMonthDate = historicalData.firstKey();
        
        List<Transaction> transactions = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : historicalData.entrySet()) {
            LocalDate date = entry.getKey();
            BigDecimal price = entry.getValue();
            if (Period.between(lastYearDate, date).getYears() >= 1) {
                monthlyInvestment = monthlyInvestment.multiply(BigDecimal.valueOf(1.0));
                lastYearDate = date;
            }
            if (Period.between(lastMonthDate, date).getMonths() >= 1) {
                totalInvestment = totalInvestment.add(monthlyInvestment);
                lastMonthDate = date;
            }
            if (previousPrice != null) {
                BigDecimal difference = price.subtract(previousPrice).abs();
                BigDecimal percentageDifference = difference.divide(previousPrice, 10, RoundingMode.HALF_UP);
                sumOfPercentageDifferences = sumOfPercentageDifferences.add(percentageDifference);
                countOfDifferences++;
                if (countOfDifferences == 12) {
                    BigDecimal averagePercentageFluctuation = sumOfPercentageDifferences.divide(BigDecimal.valueOf(countOfDifferences), 10, RoundingMode.HALF_UP);
                    BigDecimal averagePercentageFluctuationInPercent = averagePercentageFluctuation.multiply(BigDecimal.valueOf(100));
                    log.info("Average percentage fluctuation over " + 12 + " months: " + averagePercentageFluctuationInPercent + "%");
                    // Reset for the next period
                    sumOfPercentageDifferences = ZERO;
                    countOfDifferences = 0;
                }
            }
            previousPrice = price;
            int stocksCount = monthlyInvestment.divide(price, RoundingMode.DOWN).intValue();
            totalBoughtStocksCount += stocksCount;
            BigDecimal spent = BigDecimal.valueOf(stocksCount).multiply(price);
            transactions.add(new Transaction(spent.doubleValue(), date));
            if (price.compareTo(highestPrice) > 0) {
                highestPrice = price;
                currentDropStartDate = date;
            } else {
                BigDecimal percentageDrop = highestPrice.subtract(price)
                        .divide(highestPrice, 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                if (percentageDrop.compareTo(biggestPercentageDrop) > 0) {
                    biggestPercentageDrop = percentageDrop;
                    biggestDropStartDate = currentDropStartDate;
                    biggestDropEndDate = date;
                }
            }
            if (Period.between(lastMonthDate, date).getMonths() >= 1) {
                BigDecimal portfolioValue = BigDecimal.valueOf(totalBoughtStocksCount).multiply(price);
                log.info("Total portfolio value at end of " + date + ": " + portfolioValue);
            }
        }
        BigDecimal lastPrice = historicalData.lastEntry().getValue();
        BigDecimal totalValue = BigDecimal.valueOf(totalBoughtStocksCount).multiply(lastPrice);
        transactions.add(new Transaction(totalValue.negate().doubleValue(), LocalDate.now()));
        BigDecimal profit = totalValue.subtract(totalInvestment);
        BigDecimal growthPercentage = profit.divide(totalInvestment, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        log.info("Total profit: " + profit);
        log.info("Growth in portfolio: " + growthPercentage + "%");
        if (biggestDropStartDate != null && biggestDropEndDate != null) {
            log.info("Biggest percentage drop occurred from " + biggestDropStartDate + " to " + biggestDropEndDate);
        } else {
            log.info("No drop found in the historical data.");
        }
        log.info("Total investment at end of : " + totalInvestment);
        return transactions;
    }
    
}
