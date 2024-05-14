package ee.tenman.stocks.alphavantage;

import com.google.gson.Gson;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class AlphaVantageService {
    
    
    Gson gson = new Gson();
    
    @Resource
    private AlphaVantageClient client;
    
    @Retryable(backoff = @Backoff(delay = 1000))
    public AlphaVantageResponse getMonthlyTimeSeries(String symbol) {
        return getTicker(symbol)
                .map(ticker -> {
                    AlphaVantageResponse timeSeriesMonthly = client.getMonthlyTimeSeries("TIME_SERIES_MONTHLY", ticker);
                    log.info("Retrieved monthly ticker data: {}", gson.toJson(timeSeriesMonthly));
                    return timeSeriesMonthly;
                })
                .orElseThrow(() -> new RuntimeException("Error while fetching data from Alpha Vantage"));
    }
    
    @Retryable(backoff = @Backoff(delay = 1000))
    public Optional<String> getTicker(String search) {
        SearchResponse symbolSearch = client.getSearch("SYMBOL_SEARCH", search, String.valueOf(1));
        return Optional.ofNullable(symbolSearch)
                .map(SearchResponse::getBestMatches)
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .map(SearchResponse.SearchData::getSymbol);
    }
    
}
