package ee.tenman.stocks.alphavantage;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final int maxRequests;
    private final int timePeriodInMillis;
    private long startTime;
    private int requestCount;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Clock clock;
    
    private static final int MAX_REQUESTS_PER_MINUTE = 29;
    
    public RateLimiter() {
        this(MAX_REQUESTS_PER_MINUTE, Clock.systemUTC());
    }
    
    public RateLimiter(int maxRequestsPerMinute, Clock clock) {
        this.maxRequests = maxRequestsPerMinute;
        this.timePeriodInMillis = 60 * 1000;
        this.clock = clock;
        this.startTime = clock.millis();
        this.requestCount = 0;
    }
    
    public synchronized void check(Runnable task) {
        long currentTime = clock.millis();
        if (currentTime > startTime + timePeriodInMillis) {
            startTime = currentTime;
            requestCount = 0;
        }
        if (requestCount < maxRequests) {
            requestCount++;
            task.run();
        } else {
            scheduler.schedule(task, 1, TimeUnit.SECONDS);
        }
    }
}
