-- Kiekvienai datai, kada buvo paimta bent viena knyga
-- Visu paimtu skirtingu knygu ir visu ju egzemplioriu skaiciai, paemimu skaicius
-- datos kai paemimu skaicius lygus skaitytoju skaiciui
SELECT s.paimta,
COUNT(DISTINCT e.isbn) AS skirtingu_paimtu_skaicius,
COUNT(DISTINCT s.egzempliorius) as paimtu_egzemplioriu_skaicius,
COUNT(*) as paemimu_skaicius,
COUNT(DISTINCT s.skaitytojas) as skaitytoju_skaicius
FROM stud.skaitymas s
JOIN stud.egzempliorius e ON s.egzempliorius = e.nr
GROUP BY s.paimta
HAVING COUNT(*) = COUNT(DISTINCT s.skaitytojas)
ORDER BY s.paimta DESC;

