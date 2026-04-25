-- CHAR(n) em PostgreSQL vira bpchar, incompatível com o mapeamento JPA (varchar).
ALTER TABLE teams ALTER COLUMN code      TYPE VARCHAR(3);
ALTER TABLE teams ALTER COLUMN group_name TYPE VARCHAR(1);
ALTER TABLE group_classification_predictions ALTER COLUMN group_name TYPE VARCHAR(1);
