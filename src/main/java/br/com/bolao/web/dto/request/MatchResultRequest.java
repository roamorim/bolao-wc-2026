package br.com.bolao.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchResultRequest(
    @NotNull @Min(0) Integer homeScore,
    @NotNull @Min(0) Integer awayScore
) {}
