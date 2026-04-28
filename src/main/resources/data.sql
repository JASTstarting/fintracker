MERGE INTO categories c
USING (
    SELECT 1 AS id, 'Зарплата'    AS name, 'INCOME'  AS type, 'Y' AS is_active FROM DUAL UNION ALL
    SELECT 2, 'Подарок', 'INCOME',  'Y' FROM DUAL UNION ALL
    SELECT 3, 'Продукты', 'EXPENSE', 'Y' FROM DUAL UNION ALL
    SELECT 4, 'Транспорт', 'EXPENSE','Y' FROM DUAL UNION ALL
    SELECT 5, 'Развлечения','EXPENSE','Y' FROM DUAL UNION ALL
    SELECT 6, 'Здоровье', 'EXPENSE', 'Y' FROM DUAL UNION ALL
    SELECT 7, 'Другое',   'EXPENSE', 'Y' FROM DUAL UNION ALL
    SELECT 8, 'Другое',   'INCOME',  'Y' FROM DUAL
) src
ON (c.id = src.id)
WHEN NOT MATCHED THEN
  INSERT (id, name, type, is_active)
  VALUES (src.id, src.name, src.type, src.is_active);

COMMIT;
