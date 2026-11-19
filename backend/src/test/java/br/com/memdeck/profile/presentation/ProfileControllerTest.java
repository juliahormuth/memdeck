package br.com.memdeck.profile.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.profile.application.GetProfileUseCase;
import br.com.memdeck.profile.application.ProfileView;
import br.com.memdeck.profile.application.UpdateProfileCommand;
import br.com.memdeck.profile.application.UpdateProfileUseCase;
import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SecurityConfig;
import br.com.memdeck.user.domain.Avatar;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.User;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final Authentication USER = UsernamePasswordAuthenticationToken.authenticated(
			new AuthenticatedUser(USER_ID, "julia@example.com"), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	GetProfileUseCase getProfile;

	@MockitoBean
	UpdateProfileUseCase updateProfile;

	private ProfileView view(User user) {
		return new ProfileView(user, 12, 4, 2);
	}

	@Test
	void showsMyProfileWithTheActivityAndFriends() throws Exception {
		User julia = User.register("Julia", "julia@example.com", "hash");
		when(getProfile.execute(USER_ID)).thenReturn(view(julia));

		mockMvc.perform(get("/api/users/me").with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Julia"))
				.andExpect(jsonPath("$.email").value("julia@example.com"))
				.andExpect(jsonPath("$.avatar").value("BOOK"))
				.andExpect(jsonPath("$.reviewedDays").value(12))
				.andExpect(jsonPath("$.streakDays").value(4))
				.andExpect(jsonPath("$.friends").value(2))
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void updatesTheNameAndTheAvatarOfTheLoggedUser() throws Exception {
		User changed = User.register("Julia H", "julia@example.com", "hash").withProfile("Julia H", Avatar.STAR);
		when(getProfile.execute(USER_ID)).thenReturn(view(changed));

		mockMvc.perform(put("/api/users/me").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Julia H\",\"avatar\":\"STAR\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Julia H"))
				.andExpect(jsonPath("$.avatar").value("STAR"));

		ArgumentCaptor<UpdateProfileCommand> command = ArgumentCaptor.forClass(UpdateProfileCommand.class);
		verify(updateProfile).execute(command.capture());
		assertThat(command.getValue().userId()).isEqualTo(USER_ID);
		assertThat(command.getValue().avatar()).isEqualTo(Avatar.STAR);
	}

	@Test
	void rejectsABlankNameATooLongNameAndAnUnknownOrMissingAvatar() throws Exception {
		for (String body : List.of("{\"name\":\" \",\"avatar\":\"STAR\"}",
				"{\"name\":\"%s\",\"avatar\":\"STAR\"}".formatted("a".repeat(101)), "{\"name\":\"Julia\"}",
				"{\"name\":\"Julia\",\"avatar\":\"ROCKET\"}")) {
			mockMvc.perform(put("/api/users/me").with(authentication(USER)).with(csrf())
					.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
		}

		verifyNoInteractions(updateProfile);
	}

	@Test
	void translatesADomainValidationErrorTo400() throws Exception {
		when(updateProfile.execute(any())).thenThrow(new InvalidUserException("Name must not be blank"));

		mockMvc.perform(put("/api/users/me").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Julia\",\"avatar\":\"STAR\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Name must not be blank"));
	}

	@Test
	void requiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
		mockMvc.perform(put("/api/users/me").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Julia\",\"avatar\":\"STAR\"}")).andExpect(status().isUnauthorized());

		verifyNoInteractions(getProfile, updateProfile);
	}
}
