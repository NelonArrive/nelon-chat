package com.nelonchat.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(req ->
				req.requestMatchers(
						"/v3/api-docs",
						"/v3/api-docs/**",
						"/swagger-resources",
						"/swagger-resources/**",
						"/configuration/vi",
						"/configuration/security",
						"/swagger-vi/**",
						"/webjars/**",
						"/swagger-vi.html",
						"/swagger-ui/**",
						"/ws/**")
					.permitAll()
					.anyRequest()
					.authenticated()
			)
			.oauth2ResourceServer(auth ->
				auth.jwt(token ->
					token.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));
		
		return http.build();
	}
}
