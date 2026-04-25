-- WC 2026 Group Stage Matches (72 matches, match numbers 1-72)
-- Schedule is approximate based on expected calendar (June 11 - July 3, 2026).
-- Admin can update dates, times and venues via the admin panel.
-- prediction_deadline = match_datetime - INTERVAL '30 minutes'
-- All times in UTC. USA Eastern = UTC-4 (summer). 19:00 UTC = 15:00 ET, 22:00 UTC = 18:00 ET.

-- Helper: get stage id for group stage
DO $$
DECLARE
    v_stage_id BIGINT;
    -- team ids
    v_USA BIGINT; v_JAM BIGINT; v_PAN BIGINT; v_HON BIGINT;
    v_MEX BIGINT; v_ECU BIGINT; v_VEN BIGINT; v_NZL BIGINT;
    v_CAN BIGINT; v_MAR BIGINT; v_BEL BIGINT; v_CRO BIGINT;
    v_BRA BIGINT; v_COL BIGINT; v_CRC BIGINT; v_PAR BIGINT;
    v_ARG BIGINT; v_URU BIGINT; v_PER BIGINT; v_NGA BIGINT;
    v_ESP BIGINT; v_POR BIGINT; v_SEN BIGINT; v_CMR BIGINT;
    v_FRA BIGINT; v_GER BIGINT; v_TUN BIGINT; v_GHA BIGINT;
    v_ENG BIGINT; v_NED BIGINT; v_IRN BIGINT; v_KSA BIGINT;
    v_JPN BIGINT; v_KOR BIGINT; v_AUS BIGINT; v_KAZ BIGINT;
    v_ITA BIGINT; v_SUI BIGINT; v_TUR BIGINT; v_ALB BIGINT;
    v_DEN BIGINT; v_SRB BIGINT; v_SVK BIGINT; v_COD BIGINT;
    v_ROU BIGINT; v_AUT BIGINT; v_EGY BIGINT; v_BOL BIGINT;
BEGIN
    SELECT id INTO v_stage_id FROM tournament_stages WHERE code = 'GROUP';

    SELECT id INTO v_USA FROM teams WHERE code = 'USA';
    SELECT id INTO v_JAM FROM teams WHERE code = 'JAM';
    SELECT id INTO v_PAN FROM teams WHERE code = 'PAN';
    SELECT id INTO v_HON FROM teams WHERE code = 'HON';
    SELECT id INTO v_MEX FROM teams WHERE code = 'MEX';
    SELECT id INTO v_ECU FROM teams WHERE code = 'ECU';
    SELECT id INTO v_VEN FROM teams WHERE code = 'VEN';
    SELECT id INTO v_NZL FROM teams WHERE code = 'NZL';
    SELECT id INTO v_CAN FROM teams WHERE code = 'CAN';
    SELECT id INTO v_MAR FROM teams WHERE code = 'MAR';
    SELECT id INTO v_BEL FROM teams WHERE code = 'BEL';
    SELECT id INTO v_CRO FROM teams WHERE code = 'CRO';
    SELECT id INTO v_BRA FROM teams WHERE code = 'BRA';
    SELECT id INTO v_COL FROM teams WHERE code = 'COL';
    SELECT id INTO v_CRC FROM teams WHERE code = 'CRC';
    SELECT id INTO v_PAR FROM teams WHERE code = 'PAR';
    SELECT id INTO v_ARG FROM teams WHERE code = 'ARG';
    SELECT id INTO v_URU FROM teams WHERE code = 'URU';
    SELECT id INTO v_PER FROM teams WHERE code = 'PER';
    SELECT id INTO v_NGA FROM teams WHERE code = 'NGA';
    SELECT id INTO v_ESP FROM teams WHERE code = 'ESP';
    SELECT id INTO v_POR FROM teams WHERE code = 'POR';
    SELECT id INTO v_SEN FROM teams WHERE code = 'SEN';
    SELECT id INTO v_CMR FROM teams WHERE code = 'CMR';
    SELECT id INTO v_FRA FROM teams WHERE code = 'FRA';
    SELECT id INTO v_GER FROM teams WHERE code = 'GER';
    SELECT id INTO v_TUN FROM teams WHERE code = 'TUN';
    SELECT id INTO v_GHA FROM teams WHERE code = 'GHA';
    SELECT id INTO v_ENG FROM teams WHERE code = 'ENG';
    SELECT id INTO v_NED FROM teams WHERE code = 'NED';
    SELECT id INTO v_IRN FROM teams WHERE code = 'IRN';
    SELECT id INTO v_KSA FROM teams WHERE code = 'KSA';
    SELECT id INTO v_JPN FROM teams WHERE code = 'JPN';
    SELECT id INTO v_KOR FROM teams WHERE code = 'KOR';
    SELECT id INTO v_AUS FROM teams WHERE code = 'AUS';
    SELECT id INTO v_KAZ FROM teams WHERE code = 'KAZ';
    SELECT id INTO v_ITA FROM teams WHERE code = 'ITA';
    SELECT id INTO v_SUI FROM teams WHERE code = 'SUI';
    SELECT id INTO v_TUR FROM teams WHERE code = 'TUR';
    SELECT id INTO v_ALB FROM teams WHERE code = 'ALB';
    SELECT id INTO v_DEN FROM teams WHERE code = 'DEN';
    SELECT id INTO v_SRB FROM teams WHERE code = 'SRB';
    SELECT id INTO v_SVK FROM teams WHERE code = 'SVK';
    SELECT id INTO v_COD FROM teams WHERE code = 'COD';
    SELECT id INTO v_ROU FROM teams WHERE code = 'ROU';
    SELECT id INTO v_AUT FROM teams WHERE code = 'AUT';
    SELECT id INTO v_EGY FROM teams WHERE code = 'EGY';
    SELECT id INTO v_BOL FROM teams WHERE code = 'BOL';

    -- ============================================================
    -- GROUP A: USA, JAM, PAN, HON  (matches 1-6)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id,  1, v_USA, v_JAM, '2026-06-11 23:00:00+00', '2026-06-11 22:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id,  2, v_PAN, v_HON, '2026-06-12 02:00:00+00', '2026-06-12 01:30:00+00', 'SoFi Stadium, Los Angeles'),
    (v_stage_id,  3, v_USA, v_PAN, '2026-06-16 22:00:00+00', '2026-06-16 21:30:00+00', 'AT&T Stadium, Dallas'),
    (v_stage_id,  4, v_JAM, v_HON, '2026-06-16 02:00:00+00', '2026-06-16 01:30:00+00', 'Levi''s Stadium, Santa Clara'),
    (v_stage_id,  5, v_USA, v_HON, '2026-06-24 23:00:00+00', '2026-06-24 22:30:00+00', 'Arrowhead Stadium, Kansas City'),
    (v_stage_id,  6, v_JAM, v_PAN, '2026-06-24 23:00:00+00', '2026-06-24 22:30:00+00', 'Allegiant Stadium, Las Vegas');

    -- ============================================================
    -- GROUP B: MEX, ECU, VEN, NZL  (matches 7-12)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id,  7, v_MEX, v_ECU,  '2026-06-12 22:00:00+00', '2026-06-12 21:30:00+00', 'Estadio Azteca, Cidade do México'),
    (v_stage_id,  8, v_VEN, v_NZL,  '2026-06-13 02:00:00+00', '2026-06-13 01:30:00+00', 'Estadio Akron, Guadalajara'),
    (v_stage_id,  9, v_MEX, v_VEN,  '2026-06-17 22:00:00+00', '2026-06-17 21:30:00+00', 'Estadio BBVA, Monterrey'),
    (v_stage_id, 10, v_ECU, v_NZL,  '2026-06-17 02:00:00+00', '2026-06-17 01:30:00+00', 'Estadio Azteca, Cidade do México'),
    (v_stage_id, 11, v_MEX, v_NZL,  '2026-06-24 02:00:00+00', '2026-06-24 01:30:00+00', 'Estadio BBVA, Monterrey'),
    (v_stage_id, 12, v_ECU, v_VEN,  '2026-06-24 02:00:00+00', '2026-06-24 01:30:00+00', 'Estadio Akron, Guadalajara');

    -- ============================================================
    -- GROUP C: CAN, MAR, BEL, CRO  (matches 13-18)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 13, v_CAN, v_MAR,  '2026-06-12 19:00:00+00', '2026-06-12 18:30:00+00', 'BC Place, Vancouver'),
    (v_stage_id, 14, v_BEL, v_CRO,  '2026-06-12 23:00:00+00', '2026-06-12 22:30:00+00', 'BMO Field, Toronto'),
    (v_stage_id, 15, v_CAN, v_BEL,  '2026-06-16 19:00:00+00', '2026-06-16 18:30:00+00', 'BC Place, Vancouver'),
    (v_stage_id, 16, v_MAR, v_CRO,  '2026-06-16 23:00:00+00', '2026-06-16 22:30:00+00', 'BMO Field, Toronto'),
    (v_stage_id, 17, v_CAN, v_CRO,  '2026-06-25 19:00:00+00', '2026-06-25 18:30:00+00', 'BMO Field, Toronto'),
    (v_stage_id, 18, v_MAR, v_BEL,  '2026-06-25 19:00:00+00', '2026-06-25 18:30:00+00', 'BC Place, Vancouver');

    -- ============================================================
    -- GROUP D: BRA, COL, CRC, PAR  (matches 19-24)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 19, v_BRA, v_CRC,  '2026-06-13 19:00:00+00', '2026-06-13 18:30:00+00', 'Lumen Field, Seattle'),
    (v_stage_id, 20, v_COL, v_PAR,  '2026-06-13 23:00:00+00', '2026-06-13 22:30:00+00', 'Lincoln Financial Field, Philadelphia'),
    (v_stage_id, 21, v_BRA, v_PAR,  '2026-06-18 22:00:00+00', '2026-06-18 21:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 22, v_COL, v_CRC,  '2026-06-18 02:00:00+00', '2026-06-18 01:30:00+00', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 23, v_BRA, v_COL,  '2026-06-25 23:00:00+00', '2026-06-25 22:30:00+00', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 24, v_CRC, v_PAR,  '2026-06-25 23:00:00+00', '2026-06-25 22:30:00+00', 'Lumen Field, Seattle');

    -- ============================================================
    -- GROUP E: ARG, URU, PER, NGA  (matches 25-30)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 25, v_ARG, v_PER,  '2026-06-14 02:00:00+00', '2026-06-14 01:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 26, v_URU, v_NGA,  '2026-06-14 22:00:00+00', '2026-06-14 21:30:00+00', 'AT&T Stadium, Dallas'),
    (v_stage_id, 27, v_ARG, v_NGA,  '2026-06-19 02:00:00+00', '2026-06-19 01:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 28, v_URU, v_PER,  '2026-06-19 22:00:00+00', '2026-06-19 21:30:00+00', 'Empower Field, Denver'),
    (v_stage_id, 29, v_ARG, v_URU,  '2026-06-26 23:00:00+00', '2026-06-26 22:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 30, v_PER, v_NGA,  '2026-06-26 23:00:00+00', '2026-06-26 22:30:00+00', 'AT&T Stadium, Dallas');

    -- ============================================================
    -- GROUP F: ESP, POR, SEN, CMR  (matches 31-36)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 31, v_ESP, v_SEN,  '2026-06-14 19:00:00+00', '2026-06-14 18:30:00+00', 'Lincoln Financial Field, Philadelphia'),
    (v_stage_id, 32, v_POR, v_CMR,  '2026-06-14 23:00:00+00', '2026-06-14 22:30:00+00', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 33, v_ESP, v_CMR,  '2026-06-19 19:00:00+00', '2026-06-19 18:30:00+00', 'Levi''s Stadium, Santa Clara'),
    (v_stage_id, 34, v_POR, v_SEN,  '2026-06-19 23:00:00+00', '2026-06-19 22:30:00+00', 'Allegiant Stadium, Las Vegas'),
    (v_stage_id, 35, v_ESP, v_POR,  '2026-06-26 02:00:00+00', '2026-06-26 01:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 36, v_SEN, v_CMR,  '2026-06-26 02:00:00+00', '2026-06-26 01:30:00+00', 'Lincoln Financial Field, Philadelphia');

    -- ============================================================
    -- GROUP G: FRA, GER, TUN, GHA  (matches 37-42)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 37, v_FRA, v_GHA,  '2026-06-15 19:00:00+00', '2026-06-15 18:30:00+00', 'Empower Field, Denver'),
    (v_stage_id, 38, v_GER, v_TUN,  '2026-06-15 23:00:00+00', '2026-06-15 22:30:00+00', 'Arrowhead Stadium, Kansas City'),
    (v_stage_id, 39, v_FRA, v_TUN,  '2026-06-20 22:00:00+00', '2026-06-20 21:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 40, v_GER, v_GHA,  '2026-06-20 02:00:00+00', '2026-06-20 01:30:00+00', 'AT&T Stadium, Dallas'),
    (v_stage_id, 41, v_FRA, v_GER,  '2026-06-27 02:00:00+00', '2026-06-27 01:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 42, v_GHA, v_TUN,  '2026-06-27 02:00:00+00', '2026-06-27 01:30:00+00', 'Allegiant Stadium, Las Vegas');

    -- ============================================================
    -- GROUP H: ENG, NED, IRN, KSA  (matches 43-48)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 43, v_ENG, v_IRN,  '2026-06-15 02:00:00+00', '2026-06-15 01:30:00+00', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 44, v_NED, v_KSA,  '2026-06-15 22:00:00+00', '2026-06-15 21:30:00+00', 'Lumen Field, Seattle'),
    (v_stage_id, 45, v_ENG, v_KSA,  '2026-06-21 02:00:00+00', '2026-06-21 01:30:00+00', 'Lincoln Financial Field, Philadelphia'),
    (v_stage_id, 46, v_NED, v_IRN,  '2026-06-21 19:00:00+00', '2026-06-21 18:30:00+00', 'Levi''s Stadium, Santa Clara'),
    (v_stage_id, 47, v_ENG, v_NED,  '2026-06-27 23:00:00+00', '2026-06-27 22:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 48, v_IRN, v_KSA,  '2026-06-27 23:00:00+00', '2026-06-27 22:30:00+00', 'AT&T Stadium, Dallas');

    -- ============================================================
    -- GROUP I: JPN, KOR, AUS, KAZ  (matches 49-54)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 49, v_JPN, v_KAZ,  '2026-06-16 02:00:00+00', '2026-06-16 01:30:00+00', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 50, v_KOR, v_AUS,  '2026-06-16 22:00:00+00', '2026-06-16 21:30:00+00', 'Lumen Field, Seattle'),
    (v_stage_id, 51, v_JPN, v_AUS,  '2026-06-21 23:00:00+00', '2026-06-21 22:30:00+00', 'Empower Field, Denver'),
    (v_stage_id, 52, v_KOR, v_KAZ,  '2026-06-21 02:00:00+00', '2026-06-21 01:30:00+00', 'Arrowhead Stadium, Kansas City'),
    (v_stage_id, 53, v_JPN, v_KOR,  '2026-06-28 19:00:00+00', '2026-06-28 18:30:00+00', 'SoFi Stadium, Los Angeles'),
    (v_stage_id, 54, v_AUS, v_KAZ,  '2026-06-28 19:00:00+00', '2026-06-28 18:30:00+00', 'Lumen Field, Seattle');

    -- ============================================================
    -- GROUP J: ITA, SUI, TUR, ALB  (matches 55-60)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 55, v_ITA, v_ALB,  '2026-06-17 19:00:00+00', '2026-06-17 18:30:00+00', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 56, v_SUI, v_TUR,  '2026-06-17 23:00:00+00', '2026-06-17 22:30:00+00', 'Lincoln Financial Field, Philadelphia'),
    (v_stage_id, 57, v_ITA, v_TUR,  '2026-06-22 22:00:00+00', '2026-06-22 21:30:00+00', 'MetLife Stadium, East Rutherford'),
    (v_stage_id, 58, v_SUI, v_ALB,  '2026-06-22 02:00:00+00', '2026-06-22 01:30:00+00', 'Arrowhead Stadium, Kansas City'),
    (v_stage_id, 59, v_ITA, v_SUI,  '2026-06-28 23:00:00+00', '2026-06-28 22:30:00+00', 'AT&T Stadium, Dallas'),
    (v_stage_id, 60, v_ALB, v_TUR,  '2026-06-28 23:00:00+00', '2026-06-28 22:30:00+00', 'Empower Field, Denver');

    -- ============================================================
    -- GROUP K: DEN, SRB, SVK, COD  (matches 61-66)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 61, v_DEN, v_SVK,  '2026-06-18 19:00:00+00', '2026-06-18 18:30:00+00', 'BC Place, Vancouver'),
    (v_stage_id, 62, v_SRB, v_COD,  '2026-06-18 23:00:00+00', '2026-06-18 22:30:00+00', 'BMO Field, Toronto'),
    (v_stage_id, 63, v_DEN, v_COD,  '2026-06-22 19:00:00+00', '2026-06-22 18:30:00+00', 'BC Place, Vancouver'),
    (v_stage_id, 64, v_SRB, v_SVK,  '2026-06-22 23:00:00+00', '2026-06-22 22:30:00+00', 'BMO Field, Toronto'),
    (v_stage_id, 65, v_DEN, v_SRB,  '2026-06-29 19:00:00+00', '2026-06-29 18:30:00+00', 'BMO Field, Toronto'),
    (v_stage_id, 66, v_SVK, v_COD,  '2026-06-29 19:00:00+00', '2026-06-29 18:30:00+00', 'BC Place, Vancouver');

    -- ============================================================
    -- GROUP L: ROU, AUT, EGY, BOL  (matches 67-72)
    -- ============================================================
    INSERT INTO matches (stage_id, match_number, home_team_id, away_team_id, match_datetime, prediction_deadline, venue) VALUES
    (v_stage_id, 67, v_ROU, v_EGY,  '2026-06-18 02:00:00+00', '2026-06-18 01:30:00+00', 'Allegiant Stadium, Las Vegas'),
    (v_stage_id, 68, v_AUT, v_BOL,  '2026-06-18 22:00:00+00', '2026-06-18 21:30:00+00', 'Levi''s Stadium, Santa Clara'),
    (v_stage_id, 69, v_ROU, v_BOL,  '2026-06-23 02:00:00+00', '2026-06-23 01:30:00+00', 'Allegiant Stadium, Las Vegas'),
    (v_stage_id, 70, v_AUT, v_EGY,  '2026-06-23 22:00:00+00', '2026-06-23 21:30:00+00', 'Mercedes-Benz Stadium, Atlanta'),
    (v_stage_id, 71, v_ROU, v_AUT,  '2026-06-29 23:00:00+00', '2026-06-29 22:30:00+00', 'Allegiant Stadium, Las Vegas'),
    (v_stage_id, 72, v_EGY, v_BOL,  '2026-06-29 23:00:00+00', '2026-06-29 22:30:00+00', 'Levi''s Stadium, Santa Clara');

END $$;
