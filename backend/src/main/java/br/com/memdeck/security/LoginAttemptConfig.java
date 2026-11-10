package br.com.memdeck.security;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import br.com.memdeck.user.application.LoginAttemptPolicy;

@Configuration
@EnableScheduling
class LoginAttemptConfig {

	@Bean
	LoginAttemptPolicy loginAttemptPolicy(
			@Value("${memdeck.security.login-max-failures:5}") int maxFailures,
			@Value("${memdeck.security.login-window:15m}") Duration window) {
		return new LoginAttemptPolicy(maxFailures, window);
	}

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
