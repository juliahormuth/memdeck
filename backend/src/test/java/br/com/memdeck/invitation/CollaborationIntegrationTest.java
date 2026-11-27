package br.com.memdeck.invitation;

import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CollaborationIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	private String createPrivateDeck(Session owner) throws Exception {
		String location = mockMvc.perform(withCsrf(post("/api/decks").cookie(owner.session())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Team deck\",\"visibility\":\"PRIVATE\"}"),
				owner.csrf())).andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1);
	}

	private ResultActions invite(Session owner, String deckId, String email, String role) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/invitations").cookie(owner.session())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"role\":\"%s\"}"
						.formatted(email.contains("@") ? br.com.memdeck.AuthTestSupport.usernameOf(email) : email, role)), owner.csrf()));
	}

	private String firstInvitationId(Session guest) throws Exception {
		String json = mockMvc.perform(get("/api/invitations").cookie(guest.session())).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$[0].id");
	}

	private ResultActions answer(Session guest, String invitationId, String action) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/invitations/" + invitationId + "/" + action).cookie(guest.session()),
				guest.csrf()));
	}

	private ResultActions addFlashcard(Session session, String deckId) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/flashcards").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON).content("{\"front\":\"Q\",\"back\":\"A\"}"), session.csrf()));
	}

	private String join(Session owner, String deckId, String role) throws Exception {
		String email = uniqueEmail();
		Session guest = registerAndLogin(mockMvc, email);
		invite(owner, deckId, email, role).andExpect(status().isAccepted());
		answer(guest, firstInvitationId(guest), "accept").andExpect(status().isOk());
		return email;
	}

	private int count(String table, String deckId) {
		return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE deck_id = ?::uuid", Integer.class, deckId);
	}

	@Test
	void anInvitedEditorAcceptsAndThenWorksOnAPrivateDeck() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String guestEmail = uniqueEmail();
		Session guest = registerAndLogin(mockMvc, guestEmail);
		String deckId = createPrivateDeck(owner);

		mockMvc.perform(get("/api/decks/" + deckId).cookie(guest.session())).andExpect(status().isNotFound());
		invite(owner, deckId, guestEmail.toUpperCase(), "EDITOR").andExpect(status().isAccepted()).andExpect(content().string(""));

		mockMvc.perform(get("/api/decks/" + deckId + "/invitations").cookie(owner.session()))
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].role").value("EDITOR"));
		mockMvc.perform(get("/api/invitations").cookie(guest.session()))
				.andExpect(jsonPath("$[0].deckId").value(deckId))
				.andExpect(jsonPath("$[0].deckName").value("Team deck"))
				.andExpect(jsonPath("$[0].role").value("EDITOR"));

		answer(guest, firstInvitationId(guest), "accept").andExpect(status().isOk())
				.andExpect(jsonPath("$.deckId").value(deckId)).andExpect(jsonPath("$.role").value("EDITOR"));

		mockMvc.perform(get("/api/invitations").cookie(guest.session())).andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/api/decks/" + deckId + "/invitations").cookie(owner.session()))
				.andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/api/decks").cookie(guest.session()))
				.andExpect(jsonPath("$[0].id").value(deckId)).andExpect(jsonPath("$[0].role").value("EDITOR"))
				.andExpect(jsonPath("$[0].ownedByMe").value(false));
		mockMvc.perform(get("/api/decks/" + deckId).cookie(guest.session())).andExpect(status().isOk());
		addFlashcard(guest, deckId).andExpect(status().isCreated());
		mockMvc.perform(get("/api/decks/" + deckId + "/flashcards").cookie(owner.session()))
				.andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(withCsrf(put("/api/decks/" + deckId).cookie(guest.session())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\",\"visibility\":\"PUBLIC\"}"),
				guest.csrf())).andExpect(status().isForbidden());
		invite(guest, deckId, uniqueEmail(), "READER").andExpect(status().isForbidden());
		mockMvc.perform(get("/api/decks/" + deckId + "/members").cookie(guest.session())).andExpect(status().isForbidden());
		mockMvc.perform(get("/api/decks/" + deckId + "/members").cookie(owner.session()))
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].role").value("EDITOR"));
	}

	@Test
	void aReaderStudiesAPrivateDeckButCannotChangeItsFlashcards() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createPrivateDeck(owner);
		addFlashcard(owner, deckId).andExpect(status().isCreated());
		String email = uniqueEmail();
		Session reader = registerAndLogin(mockMvc, email);
		invite(owner, deckId, email, "READER").andExpect(status().isAccepted());
		answer(reader, firstInvitationId(reader), "accept").andExpect(status().isOk());

		mockMvc.perform(get("/api/decks/" + deckId + "/study").cookie(reader.session()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
		addFlashcard(reader, deckId).andExpect(status().isForbidden());
	}

	@Test
	void decliningKeepsTheGuestOutAndTheInvitationCannotBeAnsweredTwice() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String email = uniqueEmail();
		Session guest = registerAndLogin(mockMvc, email);
		String deckId = createPrivateDeck(owner);
		invite(owner, deckId, email, "EDITOR").andExpect(status().isAccepted());
		String invitationId = firstInvitationId(guest);

		answer(guest, invitationId, "decline").andExpect(status().isNoContent());

		answer(guest, invitationId, "accept").andExpect(status().isConflict());
		mockMvc.perform(get("/api/decks/" + deckId).cookie(guest.session())).andExpect(status().isNotFound());
		invite(owner, deckId, email, "EDITOR").andExpect(status().isAccepted());
		mockMvc.perform(get("/api/invitations").cookie(guest.session())).andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void anUnknownEmailGetsTheSameAnswerAsAKnownOneAndCreatesNothing() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createPrivateDeck(owner);

		invite(owner, deckId, "nobody-" + UUID.randomUUID() + "@example.com", "READER")
				.andExpect(status().isAccepted()).andExpect(content().string(""));

		assertThat(count("deck_invitations", deckId)).isZero();
	}

	@Test
	void rejectsAMalformedEmailTheOwnerEmailAndTheOwnerRole() throws Exception {
		String ownerEmail = uniqueEmail();
		Session owner = registerAndLogin(mockMvc, ownerEmail);
		String deckId = createPrivateDeck(owner);

		invite(owner, deckId, "not a username", "READER").andExpect(status().isBadRequest());
		invite(owner, deckId, ownerEmail, "READER").andExpect(status().isBadRequest());
		invite(owner, deckId, uniqueEmail(), "OWNER").andExpect(status().isBadRequest());
	}

	@Test
	void theOwnerCancelsAnInvitationAndTheGuestCanNoLongerAnswerIt() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String email = uniqueEmail();
		Session guest = registerAndLogin(mockMvc, email);
		String deckId = createPrivateDeck(owner);
		invite(owner, deckId, email, "READER").andExpect(status().isAccepted());
		String invitationId = firstInvitationId(guest);

		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/invitations/" + invitationId)
				.cookie(owner.session()), owner.csrf())).andExpect(status().isNoContent());

		answer(guest, invitationId, "accept").andExpect(status().isNotFound());
		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/invitations/" + invitationId)
				.cookie(owner.session()), owner.csrf())).andExpect(status().isNotFound());
	}

	@Test
	void nobodyElseCanSeeOrAnswerSomeoneElsesInvitation() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String email = uniqueEmail();
		Session guest = registerAndLogin(mockMvc, email);
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createPrivateDeck(owner);
		invite(owner, deckId, email, "READER").andExpect(status().isAccepted());
		String invitationId = firstInvitationId(guest);

		mockMvc.perform(get("/api/invitations").cookie(stranger.session())).andExpect(jsonPath("$.length()").value(0));
		answer(stranger, invitationId, "accept").andExpect(status().isNotFound());
		answer(owner, invitationId, "decline").andExpect(status().isNotFound());
		mockMvc.perform(get("/api/decks/" + deckId + "/invitations").cookie(stranger.session()))
				.andExpect(status().isNotFound());
		invite(stranger, deckId, uniqueEmail(), "READER").andExpect(status().isNotFound());
	}

	@Test
	void theOwnerRemovesAMemberWhoLosesAccessToThePrivateDeck() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String email = uniqueEmail();
		Session guest = registerAndLogin(mockMvc, email);
		String deckId = createPrivateDeck(owner);
		invite(owner, deckId, email, "EDITOR").andExpect(status().isAccepted());
		answer(guest, firstInvitationId(guest), "accept").andExpect(status().isOk());
		String memberId = JsonPath.read(mockMvc.perform(get("/api/decks/" + deckId + "/members")
				.cookie(owner.session())).andReturn().getResponse().getContentAsString(), "$[0].userId");

		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/members/" + memberId).cookie(owner.session()),
				owner.csrf())).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/decks/" + deckId).cookie(guest.session())).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/decks").cookie(guest.session())).andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/members/" + memberId).cookie(owner.session()),
				owner.csrf())).andExpect(status().isNotFound());
	}

	@Test
	void aMemberCannotRemoveOthersButCanLeaveAndTheOwnerCannotLeave() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createPrivateDeck(owner);
		String firstEmail = join(owner, deckId, "READER");
		String secondEmail = uniqueEmail();
		Session second = registerAndLogin(mockMvc, secondEmail);
		invite(owner, deckId, secondEmail, "EDITOR").andExpect(status().isAccepted());
		answer(second, firstInvitationId(second), "accept").andExpect(status().isOk());
		String firstUserId = JsonPath.read(mockMvc.perform(get("/api/decks/" + deckId + "/members")
				.cookie(owner.session())).andReturn().getResponse().getContentAsString(), "$[0].userId");

		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/members/" + firstUserId).cookie(second.session()),
				second.csrf())).andExpect(status().isForbidden());
		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/members/me").cookie(second.session()),
				second.csrf())).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/decks/" + deckId).cookie(second.session())).andExpect(status().isNotFound());
		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/members/me").cookie(owner.session()),
				owner.csrf())).andExpect(status().isConflict());
		assertThat(count("deck_members", deckId)).isEqualTo(1);
		assertThat(firstEmail).isNotBlank();
	}

	@Test
	void aPublicReaderBecomesAnEditorByAcceptingAnInvitation() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String email = uniqueEmail();
		Session reader = registerAndLogin(mockMvc, email);
		String location = mockMvc.perform(withCsrf(post("/api/decks").cookie(owner.session())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Open\",\"visibility\":\"PUBLIC\"}"),
				owner.csrf())).andReturn().getResponse().getHeader("Location");
		String deckId = location.substring(location.lastIndexOf('/') + 1);
		mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/members").cookie(reader.session()), reader.csrf()))
				.andExpect(status().isCreated());

		invite(owner, deckId, email, "READER").andExpect(status().isAccepted());
		mockMvc.perform(get("/api/invitations").cookie(reader.session())).andExpect(jsonPath("$.length()").value(0));
		invite(owner, deckId, email, "EDITOR").andExpect(status().isAccepted());
		answer(reader, firstInvitationId(reader), "accept").andExpect(jsonPath("$.role").value("EDITOR"));

		assertThat(count("deck_members", deckId)).isEqualTo(1);
		addFlashcard(reader, deckId).andExpect(status().isCreated());
	}

	@Test
	void deletingTheDeckRemovesItsMembersAndInvitations() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createPrivateDeck(owner);
		join(owner, deckId, "READER");
		invite(owner, deckId, joinedLater(), "READER");

		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId).cookie(owner.session()), owner.csrf()))
				.andExpect(status().isNoContent());

		assertThat(count("deck_members", deckId)).isZero();
		assertThat(count("deck_invitations", deckId)).isZero();
	}

	private String joinedLater() throws Exception {
		String email = uniqueEmail();
		registerAndLogin(mockMvc, email);
		return email;
	}

	@Test
	void everyRouteRequiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/invitations")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID() + "/members")).andExpect(status().isUnauthorized());
	}
}
