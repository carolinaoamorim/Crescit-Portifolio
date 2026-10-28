package com.acessorinvestimentos.portfolio.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void shouldConvertAuth0PermissionsToAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("auth0|user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("permissions", List.of("read:portfolio", "write:portfolio"))
                .build();

        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("auth0|user");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("read:portfolio", "write:portfolio");
    }

    @Test
    void shouldReturnNoAuthoritiesWhenPermissionsClaimIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("auth0|user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        JwtAuthenticationToken authentication = (JwtAuthenticationToken)
                securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).isEmpty();
    }

    @Test
    void shouldConfigureJwtDecoderAndFrontendCors() {
        assertThat(securityConfig.jwtDecoder("https://tenant.auth0.com/", "portfolio-api")).isNotNull();
        assertThat(securityConfig.jwtDecoder("https://tenant.auth0.com", "portfolio-api")).isNotNull();

        CorsConfigurationSource source = securityConfig.corsConfigurationSource("https://crescit-frontend.vercel.app");
        HttpServletRequest request = new MockHttpServletRequest("GET", "/portfolios/auth0|user");
        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("https://crescit-frontend.vercel.app");
        assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).contains("Authorization", "Content-Type");
    }
}
