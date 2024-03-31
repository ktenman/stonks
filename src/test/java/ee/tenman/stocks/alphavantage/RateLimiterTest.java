package ee.tenman.stocks.alphavantage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterTest {
	
	@Mock
	private Clock clock;
	
	private RateLimiter rateLimiter;
	
	@BeforeEach
	void setUp() {
		rateLimiter = new RateLimiter(3, clock);
	}
	
	@Test
	void testRateLimiterAllowsRequestsWithinLimit() {
		AtomicInteger counter = new AtomicInteger();
		Runnable task = counter::incrementAndGet;
		when(clock.millis()).thenReturn(0L, 1000L, 2000L);
		
		rateLimiter.check(task);
		rateLimiter.check(task);
		rateLimiter.check(task);
		
		assertThat(counter.get()).isEqualTo(3);
	}
	
	@Test
	void testRateLimiterBlocksRequestsExceedingLimit() {
		AtomicInteger counter = new AtomicInteger();
		Runnable task = counter::incrementAndGet;
		when(clock.millis()).thenReturn(0L, 1000L, 2000L, 3000L);
		
		rateLimiter.check(task);
		rateLimiter.check(task);
		rateLimiter.check(task);
		rateLimiter.check(task);
		
		assertThat(counter.get()).isEqualTo(3);
	}
	
	@Test
	void testRateLimiterResetsAfterTimePeriod() {
		AtomicInteger counter = new AtomicInteger();
		Runnable task = counter::incrementAndGet;
		when(clock.millis()).thenReturn(0L, 1000L, 2000L, 60001L, 61000L, 62000L);
		
		rateLimiter.check(task);
		rateLimiter.check(task);
		rateLimiter.check(task);
		rateLimiter.check(task);
		rateLimiter.check(task);
		rateLimiter.check(task);
		
		assertThat(counter.get()).isEqualTo(6);
	}
}
