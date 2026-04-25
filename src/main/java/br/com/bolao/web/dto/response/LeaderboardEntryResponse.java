package br.com.bolao.web.dto.response;

public record LeaderboardEntryResponse(
    Long userId,
    String displayName,
    int totalPoints,
    int matchPoints,
    int groupPoints,
    int semifinalistsPoints,
    int topScorerPoints
) {}
