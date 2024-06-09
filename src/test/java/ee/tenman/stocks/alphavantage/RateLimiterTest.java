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
		this.rateLimiter = new RateLimiter(3, this.clock);
	}
	
	@Test
	void testRateLimiterAllowsRequestsWithinLimit() {
		final var counter = new AtomicInteger();
		final Runnable task = counter::incrementAndGet;
		when(this.clock.millis()).thenReturn(0L, 1000L, 2000L);
		
		for (int i = 0; i < 3; i++) {
			this.rateLimiter.check(task);
		}
		
		assertThat(counter.get()).isEqualTo(3);
	}
	
	@Test
	void testRateLimiterBlocksRequestsExceedingLimit() {
		final AtomicInteger counter = new AtomicInteger();
		final Runnable task = counter::incrementAndGet;
		when(this.clock.millis()).thenReturn(0L, 1000L, 2000L, 3000L);
		
		for (int i = 0; i < 4; i++) {
			this.rateLimiter.check(task);
		}
		
		assertThat(counter.get()).isEqualTo(3);
	}
	
	@Test
	void testRateLimiterResetsAfterTimePeriod() {
		final AtomicInteger counter = new AtomicInteger();
		final Runnable task = counter::incrementAndGet;
		when(this.clock.millis()).thenReturn(0L, 1000L, 2000L, 60001L, 61000L, 62000L);
		
		for (int i = 0; i < 6; i++) {
			this.rateLimiter.check(task);
		}
		
		assertThat(counter.get()).isEqualTo(6);
	}
}
