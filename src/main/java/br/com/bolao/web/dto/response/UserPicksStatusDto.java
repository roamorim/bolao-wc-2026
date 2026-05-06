package br.com.bolao.web.dto.response;

public record UserPicksStatusDto(
    long matchPicksCount,
    long totalGroupMatches,
    long groupClassPicksCount,
    boolean hasSemifinalists,
    boolean hasTopScorer
) {}
