package ee.tenman.stocks;

import ee.tenman.stocks.xirr.XirrService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class XirrServiceTest {
	
	@Resource
	XirrService xirrService;
	
	@Test
	void calculateStockXirr() {
		stubFor(get(urlPathEqualTo("/query"))
				.withQueryParam("function", equalTo("SYMBOL_SEARCH"))
				.withQueryParam("keywords", equalTo("QDVE"))
				.withQueryParam("page", equalTo("1"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("search.json")));
		
		stubFor(get(urlPathEqualTo("/query"))
				.withQueryParam("function", equalTo("TIME_SERIES_MONTHLY"))
				.withQueryParam("symbol", equalTo("QDVE.FRK"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("QDVE.json")));
		
		final double annualReturn = this.xirrService.calculateStockXirr("QDVE");
		
		assertThat(annualReturn).isEqualTo(1.2398377773004172);
	}
}
