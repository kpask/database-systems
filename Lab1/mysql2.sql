-- Visu Leidyklu skaicius, knygu turinciu autoriu skaicius ir knygu pavadinimu skaicius
SELECT COUNT(DISTINCT(k.isbn)) AS Knygu_skaicius, 
COUNT(DISTINCT(k.leidykla)) AS Leidyklu_skaicius,
COUNT(DISTINCT(a.isbn)) AS Turincios_autoriu_knygos, 
COUNT(DISTINCT k.pavadinimas) AS Knygu_pavadinimu_skaicius
FROM stud.knyga k LEFT JOIN stud.autorius a
ON a.isbn = k.isbn;
--
