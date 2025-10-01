-- Sarasas leidyklu, kuriose konkretus autorius, nurodytas vardu ir pavarde, 
-- isleido bent viena knyga
SELECT DISTINCT k.leidykla
FROM stud.knyga k, stud.autorius a
WHERE (a.vardas = 'Jonas' and a.pavarde = 'Petraitis') 
AND a.isbn = k.isbn;
