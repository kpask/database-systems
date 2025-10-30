-- Virtualiosios lenteles, kurioms turi teise rasyti uzklausas konkretus naudotojas
SELECT table_schema, table_name
FROM information_schema.table_privileges
WHERE grantee = 'kapa1135'
OR grantee = 'PUBLIC'
AND privilege_type = 'SELECT'
ORDER BY table_name;
