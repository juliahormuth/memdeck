package br.com.memdeck.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class RealServerSecurityTest {

	@LocalServerPort
	int port;

	@Autowired
	JdbcTemplate jdbc;

	private final HttpClient client = HttpClient.newHttpClient();

	private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
		return client.send(request.build(), BodyHandlers.ofString());
	}

	private HttpRequest.Builder to(String path) {
		return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
	}

	@Test
	void aPostWithoutCsrfTokenIsForbiddenNotUnauthorized() throws Exception {
		HttpResponse<String> response = send(to("/api/users")
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(
						"{\"name\":\"A\",\"email\":\"a@example.com\",\"password\":\"correct-horse-battery\"}")));

		assertThat(response.statusCode()).isEqualTo(403);
	}

	@Test
	void anonymousRequestsAreRejectedWithoutCreatingSessions() throws Exception {
		int before = jdbc.queryForObject("select count(*) from spring_session", Integer.class);

		HttpResponse<String> protectedRoute = send(to("/api/decks/00000000-0000-0000-0000-000000000000").GET());
		HttpResponse<String> me = send(to("/api/auth/me").GET());
		HttpResponse<String> unknownRoute = send(to("/api/does-not-exist").GET());

		assertThat(protectedRoute.statusCode()).isEqualTo(401);
		assertThat(me.statusCode()).isEqualTo(401);
		assertThat(unknownRoute.statusCode()).isEqualTo(401);
		for (HttpResponse<String> response : new HttpResponse[] { protectedRoute, me, unknownRoute }) {
			assertThat(response.headers().allValues("Set-Cookie")).noneMatch(cookie -> cookie.startsWith("SESSION="));
		}
		assertThat(jdbc.queryForObject("select count(*) from spring_session", Integer.class)).isEqualTo(before);
	}
}
