package ee.tenman.stocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class XirrServiceTest {
	
	@Resource
	XirrService xirrService;
	
	@Test
	void calculateStockXirr() {
		stubFor(WireMock.get(urlPathEqualTo("/query"))
				.withQueryParam("function", WireMock.equalTo("SYMBOL_SEARCH"))
				.withQueryParam("keywords", WireMock.equalTo("QDVE"))
				.withQueryParam("page", WireMock.equalTo("1"))
				.willReturn(WireMock.aResponse()
						.withHeader("Content-Type", "application/json")
						.withBodyFile("search.json")));
		
		stubFor(WireMock.get(urlPathEqualTo("/query"))
				.withQueryParam("function", WireMock.equalTo("TIME_SERIES_MONTHLY"))
				.withQueryParam("symbol", WireMock.equalTo("QDVE.FRK"))
				.willReturn(WireMock.aResponse()
						.withHeader("Content-Type", "application/json")
						.withBodyFile("QDVE.json")));
		
		double annualReturn = xirrService.calculateStockXirr("QDVE");
		
		assertThat(annualReturn).isEqualTo(1.2396980376670366);
	}
}
