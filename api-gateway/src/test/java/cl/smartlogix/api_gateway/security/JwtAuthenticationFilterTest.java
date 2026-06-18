package cl.smartlogix.api_gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private GatewayFilterChain chain;
    private SecretKey secretKey;

    private static final String SECRET_STRING = "smartlogix-super-secret-key-2026-must-be-long-enough";

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "secretKeyString", SECRET_STRING);
        jwtAuthenticationFilter.init();

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        secretKey = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void getOrder_ReturnsMinusOne() {
        assertThat(jwtAuthenticationFilter.getOrder()).isEqualTo(-1);
    }

    @Test
    void filter_WithOptionsMethod_PassesThrough() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/pedidos").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_WithExcludedPath_PassesThrough() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/envios/pedido/123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_MissingAuthorizationHeader_ReturnsUnauthorized() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/pedidos").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_InvalidHeaderFormat_ReturnsUnauthorized() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/pedidos")
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_NullHeaderValue_ReturnsUnauthorized() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/pedidos")
                .header("Authorization", (String) null)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_ValidToken_PassesThrough() {
        // Arrange
        String token = Jwts.builder()
                .setSubject("user123")
                .signWith(secretKey)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/pedidos")
                .header("Authorization", "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_InvalidSignatureToken_ReturnsUnauthorized() {
        // Arrange
        SecretKey invalidKey = Keys.hmacShaKeyFor("different-super-secret-key-that-is-also-long-enough".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject("user123")
                .signWith(invalidKey)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/pedidos")
                .header("Authorization", "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_MalformedToken_ReturnsUnauthorized() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/pedidos")
                .header("Authorization", "Bearer malformed_token_value")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }
}
