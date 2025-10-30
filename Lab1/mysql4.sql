-- Leidykla (leidyklos), kurios knygos yra laabiausiai neskaitomos, t.y leidykla
-- Kurioje isleistu knygu nepaimtu egzemplioriu yra daugiausiai.
-- Greta pateikti ir nepaimtu tos leidyklos egzemplioriu skaiciu
WITH visos AS(
  SELECT
  COUNT(DISTINCT k.isbn) as knygu_skaicius,
  COUNT(DISTINCT e.nr) as visu_egzemplioriu_skaicius,
  COUNT(s.egzempliorius) as paimti_egzemplioriai,
  k.leidykla as leidykla
  FROM stud.knyga k
  JOIN stud.egzempliorius e
  ON e.isbn = k.isbn
  LEFT JOIN stud.skaitymas s
  ON s.egzempliorius = e.nr
  WHERE s.grazinta IS NULL
  GROUP BY leidykla
),
labiausiai_skaitoma AS (
  SELECT MAX(visu_egzemplioriu_skaicius - paimti_egzemplioriai) AS skaicius
  FROM visos
)
SELECT
  v.visu_egzemplioriu_skaicius - v.paimti_egzemplioriai AS nepaimtu_skaicius,
  v.leidykla,
  l.skaicius AS labiausiai
FROM visos v, labiausiai_skaitoma l
WHERE v.visu_egzemplioriu_skaicius - v.paimti_egzemplioriai < l.skaicius
ORDER BY nepaimtu_skaicius ASC;

