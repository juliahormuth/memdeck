package br.com.memdeck.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.user.application.RegisterUserUseCase;
import br.com.memdeck.user.domain.EmailAlreadyRegisteredException;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.User;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	RegisterUserUseCase registerUser;

	private static final String VALID_BODY = """
			{"name":"Julia","email":"julia@example.com","password":"correct-horse"}
			""";

	@Test
	void registersUserAndNeverReturnsPasswordData() throws Exception {
		when(registerUser.execute(any())).thenReturn(User.register("Julia", "julia@example.com", "secret-hash"));

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value("Julia"))
				.andExpect(jsonPath("$.email").value("julia@example.com"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void rejectsInvalidEmailBeforeCallingTheUseCase() throws Exception {
		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("""
				{"name":"Julia","email":"nope","password":"correct-horse"}
				""")).andExpect(status().isBadRequest());

		verifyNoInteractions(registerUser);
	}

	@Test
	void rejectsShortPasswordBeforeCallingTheUseCase() throws Exception {
		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("""
				{"name":"Julia","email":"julia@example.com","password":"short"}
				""")).andExpect(status().isBadRequest());

		verifyNoInteractions(registerUser);
	}

	@Test
	void translatesDomainValidationErrorTo400() throws Exception {
		when(registerUser.execute(any())).thenThrow(new InvalidUserException("Email is not valid"));

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Email is not valid"));
	}

	@Test
	void translatesDuplicateEmailTo409() throws Exception {
		when(registerUser.execute(any())).thenThrow(new EmailAlreadyRegisteredException());

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isConflict());
	}
}
