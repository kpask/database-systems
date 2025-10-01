-- Visos pavardes, kurios turi knygu autoriai
-- + Pirma pavardes raide ir Vardas + Pavarde stulpeliai
SELECT vardas, pavarde, SUBSTRING(pavarde, 1, 1) AS a, CONCAT(vardas, ' ', pavarde)
FROM stud.autorius
ORDER BY a ASC;
