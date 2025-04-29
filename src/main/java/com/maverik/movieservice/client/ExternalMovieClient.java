
package com.maverik.movieservice.client;

import com.maverik.movieservice.config.MovieServiceConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ExternalMovieClient {

    private final WebClient webClient;

    public ExternalMovieClient(MovieServiceConfig config) {
        this.webClient = WebClient.builder()
            .baseUrl(config.getBaseUrl())
            .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/119.0.0.0 Safari/537.36")
            .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/plain, */*")
            .defaultHeader(HttpHeaders.REFERER, "https://gateway.maverik.com/")
            .build();
    }

    @CircuitBreaker(name = "movieSearchCircuitBreaker", fallbackMethod = "fallbackSearchMovie")
    public Mono<String> searchMovieByTitle(String title) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/movie/title/{title}")
                .queryParam("source", "web")
                .build(title))
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                log.error("Error fetching movie search for title {}: Status {}", title, clientResponse.statusCode());
                return clientResponse.createException();
            })
            .bodyToMono(String.class)
            .retry(3);
    }

    @CircuitBreaker(name = "movieDetailsCircuitBreaker", fallbackMethod = "fallbackGetDetails")
    public Mono<String> getMovieDetailsByImdbId(String imdbId) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/movie/{imdbId}")
                .queryParam("source", "web")
                .build(imdbId))
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                log.error("Error fetching movie details for IMDb ID {}: Status {}", imdbId, clientResponse.statusCode());
                return clientResponse.createException();
            })
            .bodyToMono(String.class)
            .retry(3);
    }

    private Mono<String> fallbackSearchMovie(String title, Throwable t) {
        log.error("CircuitBreaker Fallback: searchMovieByTitle {} - error: {}", title, t.toString());
        return Mono.empty();
    }

    private Mono<String> fallbackGetDetails(String imdbId, Throwable t) {
        log.error("CircuitBreaker Fallback: getMovieDetailsByImdbId {} - error: {}", imdbId, t.toString());
        return Mono.empty();
    }

}
