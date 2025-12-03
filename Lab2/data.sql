-- ********************************************
-- I. CLIENTS
-- ********************************************
INSERT INTO client (first_name, last_name, country, city, street, postal_code)
VALUES 
('Jonas', 'Jonaitis', 'Lithuania', 'Kaunas', 'Laisvės al. 10', 'LT-44249'),          -- ID 1
('Eglė', 'Petrauskaitė', 'Lithuania', 'Vilnius', 'Didžioji g. 5', 'LT-01128'),       -- ID 2
('Tomas', 'Vaitkus', 'Latvia', 'Riga', 'Brīvības iela 15', 'LV-1050'),               -- ID 3
('Mark', 'Johnson', 'USA', 'New York', '5th Avenue 101', '10001'),                   -- ID 4
('Anna', 'Kowalska', 'Poland', 'Warsaw', 'Marszałkowska 50', '00-001');              -- ID 5


-- ********************************************
-- II. SUPPLIERS
-- ********************************************
INSERT INTO supplier (name, country, city, street, postal_code)
VALUES 
('MedTiekimas UAB', 'Lithuania', 'Vilnius', 'Gedimino pr. 5', 'LT-01103'),           -- ID 1
('PharmaBaltic', 'Latvia', 'Riga', 'Vienības gatve 20', 'LV-1004'),                  -- ID 2
('EuroPharm Ltd.', 'Poland', 'Warsaw', 'Nowy Świat 22', '00-496'),                   -- ID 3
('GlobalMed Supply', 'USA', 'Chicago', 'Lake Shore Dr. 77', '60601');                -- ID 4


-- ********************************************
-- III. MEDICINE
-- ********************************************
INSERT INTO medicine (name, unit_price, stock) VALUES 
('Paracetamol 500mg', 2.50, 150),            -- ID 1
('Ibuprofen 400mg', 4.00, 75),               -- ID 2
('Vitamin C 1000mg', 8.99, 200),             -- ID 3
('Aspirin 300mg', 3.20, 90),                 -- ID 4
('Amoxicillin 500mg', 12.50, 40),            -- ID 5
('Omeprazole 20mg', 6.90, 120),              -- ID 6
('Cetirizine 10mg', 5.30, 80);               -- ID 7


-- ********************************************
-- IV. SUPPLIER–MEDICINE PRICES
-- ********************************************

-- Paracetamol
INSERT INTO suppliermedicine (supplier_id, medicine_id, supply_price) VALUES
(1, 1, 1.80),
(2, 1, 1.95),
(3, 1, 1.70);

-- Ibuprofen
INSERT INTO suppliermedicine (supplier_id, medicine_id, supply_price) VALUES
(1, 2, 3.10),
(4, 2, 3.50);

-- Vitamin C
INSERT INTO suppliermedicine (supplier_id, medicine_id, supply_price) VALUES
(2, 3, 7.20),
(3, 3, 7.10);

-- Aspirin
INSERT INTO suppliermedicine (supplier_id, medicine_id, supply_price) VALUES
(1, 4, 2.40),
(3, 4, 2.55);

-- Amoxicillin
INSERT INTO suppliermedicine (supplier_id, medicine_id, supply_price) VALUES
(4, 5, 9.80);

-- Omeprazole
INSERT INTO suppliermedicine (supplier_id, medicine_id, supply_price) VALUES
(3, 6, 5.00);

-- Cetirizine
INSERT INTO suppliermedicine (supplier_id, medicine_id, supply_price) VALUES
(2, 7, 3.90),
(4, 7, 4.10);


-- ********************************************
-- V. ORDERS
-- ********************************************

-- Order 1: Client 1 (Jonas Jonaitis)
INSERT INTO "order" (client_id, order_date) VALUES (1, '2025-11-20'); -- order_id 1

-- Order 2: Client 2 (Eglė Petrauskaitė)
INSERT INTO "order" (client_id, order_date) VALUES (2, '2025-12-01'); -- order_id 2

-- Order 3: Client 4 (Mark Johnson)
INSERT INTO "order" (client_id, order_date) VALUES (4, '2025-12-05'); -- order_id 3

-- Order 4: Client 5 (Anna Kowalska)
INSERT INTO "order" (client_id, order_date) VALUES (5, '2025-12-07'); -- order_id 4


-- ********************************************
-- VI. ORDER ITEMS
-- ********************************************

-- Order 1 Items --------------------------------------
INSERT INTO orderitem (order_id, medicine_id, quantity) VALUES
(1, 1, 5),   -- Paracetamol: 5 pcs (12.50 EUR)
(1, 3, 1);   -- Vitamin C: 1 pc (8.99 EUR)


-- Order 2 Items --------------------------------------
INSERT INTO orderitem (order_id, medicine_id, quantity) VALUES
(2, 2, 10);  -- Ibuprofen: 10 pcs → 40.00 EUR
INSERT INTO orderitem (order_id, medicine_id, quantity) VALUES
(2, 7, 2);   -- Cetirizine: 2 pcs (10.60 EUR)


-- Order 3 Items --------------------------------------
INSERT INTO orderitem (order_id, medicine_id, quantity) VALUES
(3, 5, 3),  -- Amoxicillin: 3 pcs (37.50 EUR)
(3, 6, 1);   -- Omeprazole: 1 pc (6.90 EUR)
INSERT INTO orderitem (order_id, medicine_id, quantity) VALUES
(3, 1, 2);   -- Paracetamol: 2 pcs (5.00 EUR)


-- Order 4 Items --------------------------------------
INSERT INTO orderitem (order_id, medicine_id, quantity) VALUES
(4, 4, 6),   -- Aspirin: 6 pcs (19.20 EUR)
(4, 3, 2);   -- Vitamin C: 2 pcs (17.98 EUR)
INSERT INTO orderitem (order_id, medicine_id, quantity) VALUES
(4, 2, 1);   -- Ibuprofen: 1 pc (4.00 EUR)
