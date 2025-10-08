-- Kiekvienai datai, kada buvo paimta bent viena knyga
-- Visu paimtu skirtingu knygu ir visu ju egzemplioriu skaiciai
SELECT s.paimta,
COUNT(DISTINCT e.isbn) AS skirtingu_paimtu_skaicius,
COUNT(DISTINCT s.egzempliorius) as paimtu_skaicius
FROM stud.skaitymas s
JOIN stud.egzempliorius e ON s.egzempliorius = e.nr
GROUP BY s.paimta
ORDER BY s.paimta DESC;
