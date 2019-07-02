package sample;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Rob Winch
 */
public class SimpleTests {
	MockWebServer server = new MockWebServer();

	@BeforeClass
	public static void setupBlockHound() {
		BlockHound.install();
	}

	@Before
	public void setupServer() {
		String accessTokenResponse = "{\n" +
				"	\"access_token\": \"refreshed-access-token\",\n" +
				"   \"token_type\": \"bearer\",\n" +
				"   \"expires_in\": \"3600\"\n" +
				"}\n";
		String clientResponse = "{\n" +
				"	\"attribute1\": \"value1\",\n" +
				"	\"attribute2\": \"value2\"\n" +
				"}\n";

		this.server.enqueue(jsonResponse(accessTokenResponse));
		this.server.enqueue(jsonResponse(clientResponse));
	}

	@Test
	public void go() {
		// Uncomment below to fix
//		Map.class.getPackage().getName();
		WebClient client = WebClient.builder()
				.build();

		client.get()
			.uri(this.server.url("/").uri())
			.exchange()
			.flatMap(r -> {
				ParameterizedTypeReference<Map<String, Object>> type =
						new ParameterizedTypeReference<Map<String, Object>>() {};
				BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate =
						BodyExtractors.toMono(type);
				return r.body(delegate);
			})
			.block();
	}

	private static MockResponse jsonResponse(String json) {
		return new MockResponse()
				.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.setBody(json);
	}
}
