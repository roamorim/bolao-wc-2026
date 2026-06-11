package br.com.bolao.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class FootballDataClient {

    private static final Logger log = LoggerFactory.getLogger(FootballDataClient.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String competition;

    public FootballDataClient(
            RestTemplate restTemplate,
            @Value("${football-data.api.base-url}") String baseUrl,
            @Value("${football-data.api.key}") String apiKey,
            @Value("${football-data.competition}") String competition) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.competition = competition;
    }

    public List<FdMatch> fetchMatchesByDate(LocalDate date) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("football-data API key not configured, skipping auto-fetch");
            return Collections.emptyList();
        }

        String url = "%s/competitions/%s/matches?dateFrom=%s&dateTo=%s".formatted(
                baseUrl, competition, DATE_FMT.format(date), DATE_FMT.format(date));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);

        try {
            ResponseEntity<FdMatchesResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), FdMatchesResponse.class);
            if (response.getBody() == null || response.getBody().matches() == null) {
                return Collections.emptyList();
            }
            return response.getBody().matches();
        } catch (RestClientException e) {
            log.warn("Failed to fetch matches from football-data.org for {}: {}", date, e.getMessage());
            return Collections.emptyList();
        }
    }

    public record FdMatchesResponse(List<FdMatch> matches) {}

    public record FdMatch(String status, FdTeam homeTeam, FdTeam awayTeam, FdScore score) {
        public boolean isFinished() {
            return "FINISHED".equals(status);
        }
    }

    public record FdTeam(String tla) {}

    public record FdScore(FdFullTime fullTime) {}

    public record FdFullTime(Integer home, Integer away) {}
}
