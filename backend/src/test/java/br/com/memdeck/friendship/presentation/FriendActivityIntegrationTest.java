package br.com.memdeck.friendship.presentation;

import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FriendActivityIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	private UUID idOf(String email) {
		return jdbc.queryForObject("select id from users where email = ?", UUID.class, email);
	}

	@Test
	void showsWhatAFriendReviewedWithoutTellingWhichDeck() throws Exception {
		String myEmail = uniqueEmail();
		String friendEmail = uniqueEmail();
		Session me = registerAndLogin(mockMvc, myEmail);
		registerAndLogin(mockMvc, friendEmail);
		UUID friendId = idOf(friendEmail);
		jdbc.update("insert into friendships (id, requester_id, addressee_id, status, created_at, responded_at) "
				+ "values (?, ?, ?, 'ACCEPTED', now(), now())", UUID.randomUUID(), idOf(myEmail), friendId);
		UUID deckId = UUID.randomUUID();
		jdbc.update("insert into decks (id, name, visibility, owner_id, created_at) "
				+ "values (?, 'Secret deck', 'PRIVATE', ?, now())", deckId, friendId);
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		for (int i = 0; i < 2; i++) {
			jdbc.update("insert into review_logs (id, user_id, deck_id, flashcard_id, reviewed_on) values (?, ?, ?, ?, ?)",
					UUID.randomUUID(), friendId, deckId, UUID.randomUUID(), Date.valueOf(today));
		}

		mockMvc.perform(get("/api/users/me/friends/activity").cookie(me.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].userId").value(friendId.toString()))
				.andExpect(jsonPath("$[0].day").value(today.toString()))
				.andExpect(jsonPath("$[0].reviewedFlashcards").value(2))
				.andExpect(jsonPath("$[0].deckId").doesNotExist())
				.andExpect(jsonPath("$[0].email").doesNotExist());
	}

	@Test
	void isEmptyWithoutFriendsAndRequiresALogin() throws Exception {
		Session me = registerAndLogin(mockMvc, uniqueEmail());

		mockMvc.perform(get("/api/users/me/friends/activity").cookie(me.session()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/api/users/me/friends/activity")).andExpect(status().isUnauthorized());
	}
}
