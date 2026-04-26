-- WC 2026 Group Stage Matches — official FIFA calendar
-- All times stored in UTC. Source: FIFA (times originally in BRT = UTC-3)
-- prediction_deadline = match_datetime - INTERVAL '30 minutes'

DO $$
DECLARE
    v_stage_id BIGINT;
    v_MEX BIGINT; v_RSA BIGINT; v_KOR BIGINT; v_CZE BIGINT;
    v_CAN BIGINT; v_BIH BIGINT; v_SUI BIGINT; v_QAT BIGINT;
    v_BRA BIGINT; v_MAR BIGINT; v_SCO BIGINT; v_HAI BIGINT;
    v_USA BIGINT; v_PAR BIGINT; v_AUS BIGINT; v_TUR BIGINT;
    v_GER BIGINT; v_CUW BIGINT; v_CIV BIGINT; v_ECU BIGINT;
    v_NED BIGINT; v_JPN BIGINT; v_TUN BIGINT; v_SWE BIGINT;
    v_BEL BIGINT; v_EGY BIGINT; v_IRN BIGINT; v_NZL BIGINT;
    v_ESP BIGINT; v_CPV BIGINT; v_KSA BIGINT; v_URU BIGINT;
    v_FRA BIGINT; v_SEN BIGINT; v_NOR BIGINT; v_IRQ BIGINT;
    v_ARG BIGINT; v_ALG BIGINT; v_AUT BIGINT; v_JOR BIGINT;
    v_POR BIGINT; v_COD BIGINT; v_COL BIGINT; v_UZB BIGINT;
    v_ENG BIGINT; v_CRO BIGINT; v_GHA BIGINT; v_PAN BIGINT;
BEGIN
    SELECT id INTO v_stage_id FROM tournament_stages WHERE name = 'Fase de Grupos';

    SELECT id INTO v_MEX FROM teams WHERE code = 'MEX';
    SELECT id INTO v_RSA FROM teams WHERE code = 'RSA';
    SELECT id INTO v_KOR FROM teams WHERE code = 'KOR';
    SELECT id INTO v_CZE FROM teams WHERE code = 'CZE';
    SELECT id INTO v_CAN FROM teams WHERE code = 'CAN';
    SELECT id INTO v_BIH FROM teams WHERE code = 'BIH';
    SELECT id INTO v_SUI FROM teams WHERE code = 'SUI';
    SELECT id INTO v_QAT FROM teams WHERE code = 'QAT';
    SELECT id INTO v_BRA FROM teams WHERE code = 'BRA';
    SELECT id INTO v_MAR FROM teams WHERE code = 'MAR';
    SELECT id INTO v_SCO FROM teams WHERE code = 'SCO';
    SELECT id INTO v_HAI FROM teams WHERE code = 'HAI';
    SELECT id INTO v_USA FROM teams WHERE code = 'USA';
    SELECT id INTO v_PAR FROM teams WHERE code = 'PAR';
    SELECT id INTO v_AUS FROM teams WHERE code = 'AUS';
    SELECT id INTO v_TUR FROM teams WHERE code = 'TUR';
    SELECT id INTO v_GER FROM teams WHERE code = 'GER';
    SELECT id INTO v_CUW FROM teams WHERE code = 'CUW';
    SELECT id INTO v_CIV FROM teams WHERE code = 'CIV';
    SELECT id INTO v_ECU FROM teams WHERE code = 'ECU';
    SELECT id INTO v_NED FROM teams WHERE code = 'NED';
    SELECT id INTO v_JPN FROM teams WHERE code = 'JPN';
    SELECT id INTO v_TUN FROM teams WHERE code = 'TUN';
    SELECT id INTO v_SWE FROM teams WHERE code = 'SWE';
    SELECT id INTO v_BEL FROM teams WHERE code = 'BEL';
    SELECT id INTO v_EGY FROM teams WHERE code = 'EGY';
    SELECT id INTO v_IRN FROM teams WHERE code = 'IRN';
    SELECT id INTO v_NZL FROM teams WHERE code = 'NZL';
    SELECT id INTO v_ESP FROM teams WHERE code = 'ESP';
    SELECT id INTO v_CPV FROM teams WHERE code = 'CPV';
    SELECT id INTO v_KSA FROM teams WHERE code = 'KSA';
    SELECT id INTO v_URU FROM teams WHERE code = 'URU';
    SELECT id INTO v_FRA FROM teams WHERE code = 'FRA';
    SELECT id INTO v_SEN FROM teams WHERE code = 'SEN';
    SELECT id INTO v_NOR FROM teams WHERE code = 'NOR';
    SELECT id INTO v_IRQ FROM teams WHERE code = 'IRQ';
    SELECT id INTO v_ARG FROM teams WHERE code = 'ARG';
    SELECT id INTO v_ALG FROM teams WHERE code = 'ALG';
    SELECT id INTO v_AUT FROM teams WHERE code = 'AUT';
    SELECT id INTO v_JOR FROM teams WHERE code = 'JOR';
    SELECT id INTO v_POR FROM teams WHERE code = 'POR';
    SELECT id INTO v_COD FROM teams WHERE code = 'COD';
    SELECT id INTO v_COL FROM teams WHERE code = 'COL';
    SELECT id INTO v_UZB FROM teams WHERE code = 'UZB';
    SELECT id INTO v_ENG FROM teams WHERE code = 'ENG';
    SELECT id INTO v_CRO FROM teams WHERE code = 'CRO';
    SELECT id INTO v_GHA FROM teams WHERE code = 'GHA';
    SELECT id INTO v_PAN FROM teams WHERE code = 'PAN';

    -- Rodada 1
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id,  1, v_MEX, v_RSA, '2026-06-11T19:00:00Z', '2026-06-11T18:30:00Z', 'Estádio Azteca, Cidade do México'),
    (v_stage_id,  2, v_KOR, v_CZE, '2026-06-12T02:00:00Z', '2026-06-12T01:30:00Z', 'Estádio Akron, Guadalajara'),
    (v_stage_id,  3, v_CAN, v_BIH, '2026-06-12T19:00:00Z', '2026-06-12T18:30:00Z', 'BMO Field, Toronto'),
    (v_stage_id,  4, v_USA, v_PAR, '2026-06-13T01:00:00Z', '2026-06-13T00:30:00Z', 'SoFi Stadium, Los Angeles'),
    (v_stage_id,  5, v_SUI, v_QAT, '2026-06-13T19:00:00Z', '2026-06-13T18:30:00Z', 'Levi''s Stadium, San Francisco'),
    (v_stage_id,  6, v_BRA, v_MAR, '2026-06-13T22:00:00Z', '2026-06-13T21:30:00Z', 'MetLife Stadium, Nova York/NJ'),
    (v_stage_id,  7, v_SCO, v_HAI, '2026-06-14T01:00:00Z', '2026-06-14T00:30:00Z', 'Gillette Stadium, Boston'),
    (v_stage_id,  8, v_AUS, v_TUR, '2026-06-14T04:00:00Z', '2026-06-14T03:30:00Z', 'BC Place, Vancouver'),
    (v_stage_id,  9, v_GER, v_CUW, '2026-06-14T17:00:00Z', '2026-06-14T16:30:00Z', 'NRG Stadium, Houston'),
    (v_stage_id, 10, v_NED, v_JPN, '2026-06-14T20:00:00Z', '2026-06-14T19:30:00Z', 'AT&T Stadium, Dallas'),
    (v_stage_id, 11, v_CIV, v_ECU, '2026-06-14T23:00:00Z', '2026-06-14T22:30:00Z', 'Lincoln Financial Field, Filadélfia'),
    (v_stage_id, 12, v_TUN, v_SWE, '2026-06-15T02:00:00Z', '2026-06-15T01:30:00Z', 'BBVA Stadium, Monterrey'),
    (v_stage_id, 13, v_ESP, v_CPV, '2026-06-15T16:00:00Z', '2026-06-15T15:30:00Z', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 14, v_BEL, v_EGY, '2026-06-15T19:00:00Z', '2026-06-15T18:30:00Z', 'Lumen Field, Seattle'),
    (v_stage_id, 15, v_KSA, v_URU, '2026-06-15T22:00:00Z', '2026-06-15T21:30:00Z', 'Hard Rock Stadium, Miami'),
    (v_stage_id, 16, v_IRN, v_NZL, '2026-06-16T01:00:00Z', '2026-06-16T00:30:00Z', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 17, v_FRA, v_SEN, '2026-06-16T19:00:00Z', '2026-06-16T18:30:00Z', 'MetLife Stadium, Nova York/NJ'),
    (v_stage_id, 18, v_NOR, v_IRQ, '2026-06-16T22:00:00Z', '2026-06-16T21:30:00Z', 'Gillette Stadium, Boston'),
    (v_stage_id, 19, v_ARG, v_ALG, '2026-06-17T01:00:00Z', '2026-06-17T00:30:00Z', 'Arrowhead Stadium, Kansas City'),
    (v_stage_id, 20, v_AUT, v_JOR, '2026-06-17T04:00:00Z', '2026-06-17T03:30:00Z', 'Levi''s Stadium, San Francisco');

    -- Rodada 1 (cont.)
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 21, v_POR, v_COD, '2026-06-17T17:00:00Z', '2026-06-17T16:30:00Z', 'NRG Stadium, Houston'),
    (v_stage_id, 22, v_COL, v_UZB, '2026-06-17T17:00:00Z', '2026-06-17T16:30:00Z', 'Estádio Azteca, Cidade do México'),
    (v_stage_id, 23, v_ENG, v_CRO, '2026-06-17T20:00:00Z', '2026-06-17T19:30:00Z', 'AT&T Stadium, Dallas'),
    (v_stage_id, 24, v_GHA, v_PAN, '2026-06-17T23:00:00Z', '2026-06-17T22:30:00Z', 'BMO Field, Toronto');

    -- Rodada 2
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 25, v_RSA, v_CZE, '2026-06-18T16:00:00Z', '2026-06-18T15:30:00Z', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 26, v_SUI, v_BIH, '2026-06-18T19:00:00Z', '2026-06-18T18:30:00Z', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 27, v_CAN, v_QAT, '2026-06-18T22:00:00Z', '2026-06-18T21:30:00Z', 'BC Place, Vancouver'),
    (v_stage_id, 28, v_MEX, v_KOR, '2026-06-19T01:00:00Z', '2026-06-19T00:30:00Z', 'Estádio Akron, Guadalajara'),
    (v_stage_id, 29, v_USA, v_AUS, '2026-06-19T19:00:00Z', '2026-06-19T18:30:00Z', 'Lumen Field, Seattle'),
    (v_stage_id, 30, v_SCO, v_MAR, '2026-06-19T22:00:00Z', '2026-06-19T21:30:00Z', 'Gillette Stadium, Boston'),
    (v_stage_id, 31, v_BRA, v_HAI, '2026-06-19T22:30:00Z', '2026-06-19T22:00:00Z', 'Lincoln Financial Field, Filadélfia'),
    (v_stage_id, 32, v_PAR, v_TUR, '2026-06-20T04:00:00Z', '2026-06-20T03:30:00Z', 'Levi''s Stadium, San Francisco'),
    (v_stage_id, 33, v_NED, v_SWE, '2026-06-20T17:00:00Z', '2026-06-20T16:30:00Z', 'NRG Stadium, Houston'),
    (v_stage_id, 34, v_GER, v_CIV, '2026-06-20T20:00:00Z', '2026-06-20T19:30:00Z', 'BMO Field, Toronto'),
    (v_stage_id, 35, v_ECU, v_CUW, '2026-06-21T00:00:00Z', '2026-06-20T23:30:00Z', 'Arrowhead Stadium, Kansas City'),
    (v_stage_id, 36, v_JPN, v_TUN, '2026-06-21T04:00:00Z', '2026-06-21T03:30:00Z', 'BBVA Stadium, Monterrey'),
    (v_stage_id, 37, v_ESP, v_KSA, '2026-06-21T16:00:00Z', '2026-06-21T15:30:00Z', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 38, v_BEL, v_IRN, '2026-06-21T19:00:00Z', '2026-06-21T18:30:00Z', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 39, v_URU, v_CPV, '2026-06-21T22:00:00Z', '2026-06-21T21:30:00Z', 'Hard Rock Stadium, Miami'),
    (v_stage_id, 40, v_EGY, v_NZL, '2026-06-22T01:00:00Z', '2026-06-22T00:30:00Z', 'BC Place, Vancouver'),
    (v_stage_id, 41, v_ARG, v_AUT, '2026-06-22T17:00:00Z', '2026-06-22T16:30:00Z', 'AT&T Stadium, Dallas'),
    (v_stage_id, 42, v_FRA, v_IRQ, '2026-06-22T21:00:00Z', '2026-06-22T20:30:00Z', 'Lincoln Financial Field, Filadélfia'),
    (v_stage_id, 43, v_SEN, v_NOR, '2026-06-23T00:00:00Z', '2026-06-22T23:30:00Z', 'MetLife Stadium, Nova York/NJ'),
    (v_stage_id, 44, v_ALG, v_JOR, '2026-06-23T04:00:00Z', '2026-06-23T03:30:00Z', 'Levi''s Stadium, San Francisco');

    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 45, v_POR, v_UZB, '2026-06-23T17:00:00Z', '2026-06-23T16:30:00Z', 'NRG Stadium, Houston'),
    (v_stage_id, 46, v_ENG, v_GHA, '2026-06-23T20:00:00Z', '2026-06-23T19:30:00Z', 'Gillette Stadium, Boston'),
    (v_stage_id, 47, v_PAN, v_CRO, '2026-06-23T23:00:00Z', '2026-06-23T22:30:00Z', 'BMO Field, Toronto'),
    (v_stage_id, 48, v_COL, v_COD, '2026-06-24T02:00:00Z', '2026-06-24T01:30:00Z', 'Estádio Akron, Guadalajara');

    -- Rodada 3 (jogos simultâneos por grupo)
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 49, v_CAN, v_SUI, '2026-06-24T19:00:00Z', '2026-06-24T18:30:00Z', 'BC Place, Vancouver'),
    (v_stage_id, 50, v_QAT, v_BIH, '2026-06-24T19:00:00Z', '2026-06-24T18:30:00Z', 'Lumen Field, Seattle'),
    (v_stage_id, 51, v_BRA, v_SCO, '2026-06-24T22:00:00Z', '2026-06-24T21:30:00Z', 'Hard Rock Stadium, Miami'),
    (v_stage_id, 52, v_MAR, v_HAI, '2026-06-24T22:00:00Z', '2026-06-24T21:30:00Z', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 53, v_MEX, v_CZE, '2026-06-25T01:00:00Z', '2026-06-25T00:30:00Z', 'Estádio Azteca, Cidade do México'),
    (v_stage_id, 54, v_KOR, v_RSA, '2026-06-25T01:00:00Z', '2026-06-25T00:30:00Z', 'BBVA Stadium, Monterrey'),
    (v_stage_id, 55, v_GER, v_ECU, '2026-06-25T20:00:00Z', '2026-06-25T19:30:00Z', 'MetLife Stadium, Nova York/NJ'),
    (v_stage_id, 56, v_CIV, v_CUW, '2026-06-25T20:00:00Z', '2026-06-25T19:30:00Z', 'Lincoln Financial Field, Filadélfia'),
    (v_stage_id, 57, v_NED, v_TUN, '2026-06-25T23:00:00Z', '2026-06-25T22:30:00Z', 'Arrowhead Stadium, Kansas City'),
    (v_stage_id, 58, v_JPN, v_SWE, '2026-06-25T23:00:00Z', '2026-06-25T22:30:00Z', 'AT&T Stadium, Dallas'),
    (v_stage_id, 59, v_USA, v_TUR, '2026-06-26T02:00:00Z', '2026-06-26T01:30:00Z', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 60, v_AUS, v_PAR, '2026-06-26T02:00:00Z', '2026-06-26T01:30:00Z', 'Levi''s Stadium, San Francisco'),
    (v_stage_id, 61, v_FRA, v_NOR, '2026-06-26T19:00:00Z', '2026-06-26T18:30:00Z', 'Gillette Stadium, Boston'),
    (v_stage_id, 62, v_SEN, v_IRQ, '2026-06-26T19:00:00Z', '2026-06-26T18:30:00Z', 'BMO Field, Toronto'),
    (v_stage_id, 63, v_ESP, v_URU, '2026-06-27T00:00:00Z', '2026-06-26T23:30:00Z', 'Estádio Akron, Guadalajara'),
    (v_stage_id, 64, v_CPV, v_KSA, '2026-06-27T00:00:00Z', '2026-06-26T23:30:00Z', 'NRG Stadium, Houston'),
    (v_stage_id, 65, v_BEL, v_NZL, '2026-06-27T03:00:00Z', '2026-06-27T02:30:00Z', 'BC Place, Vancouver'),
    (v_stage_id, 66, v_IRN, v_EGY, '2026-06-27T03:00:00Z', '2026-06-27T02:30:00Z', 'Lumen Field, Seattle'),
    (v_stage_id, 67, v_ENG, v_PAN, '2026-06-27T21:00:00Z', '2026-06-27T20:30:00Z', 'MetLife Stadium, Nova York/NJ'),
    (v_stage_id, 68, v_CRO, v_GHA, '2026-06-27T21:00:00Z', '2026-06-27T20:30:00Z', 'Lincoln Financial Field, Filadélfia'),
    (v_stage_id, 69, v_POR, v_COL, '2026-06-27T23:30:00Z', '2026-06-27T23:00:00Z', 'Hard Rock Stadium, Miami'),
    (v_stage_id, 70, v_UZB, v_COD, '2026-06-27T23:30:00Z', '2026-06-27T23:00:00Z', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 71, v_ARG, v_JOR, '2026-06-28T02:00:00Z', '2026-06-28T01:30:00Z', 'AT&T Stadium, Dallas'),
    (v_stage_id, 72, v_AUT, v_ALG, '2026-06-28T02:00:00Z', '2026-06-28T01:30:00Z', 'Arrowhead Stadium, Kansas City');

END $$;
