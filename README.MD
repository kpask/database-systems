# Laboratory Work for Database Systems (DBS)

Laboratory work for the 3rd semester Database Management Systems (DBMS) module at Vilnius University. 

## Laboratory Work 1 (PostgreSQL Queries)

1. Visos pavardės, kuriuos turi knygų autoriai.  
   → All surnames of book authors.

2. Sąrašas leidyklų, kuriose konkretus autorius, nurodytas vardu ir pavarde, išleido bent vieną knygą.  
   → A list of publishers where a specific author, identified by first name and surname, has published at least one book.

3. Kiekvienai datai, kada buvo paimta bent viena knyga, visų paimtų skirtingų knygų ir visų jų egzempliorių skaičiai.  
   → For each date when at least one book was borrowed, the number of distinct borrowed books and the total number of their copies.

4. Leidykla (leidyklos), kurios knygos yra labiausiai neskaitomos, t.y. leidykla, kurioje išleistų knygų nepaimtų egzempliorių yra daugiausia. Greta pateikti ir nepaimtų tos leidyklos egzempliorių skaičių.  
   → The publisher(s) whose books are the least read, i.e., the publisher with the largest number of unborrowed copies. Also show the number of unborrowed copies for that publisher.

5. Virtualiosios lentelės, kurioms turi teisę rašyti užklausas konkretus naudotojas.  
   → Virtual tables (views) on which a specific user has permission to execute queries.

![alt text](https://klevas.mif.vu.lt/~baronas/dbvs/biblio/DB-Schema.jpg)


# Laboratory work 2 (Pharmacy Database Management System)

## Project Overview

This database system is designed to manage a **pharmacy's operations**, including client orders, medicine inventory, and supplier relationships. The system tracks customer orders, maintains medicine stock levels, and manages relationships with multiple suppliers who provide medicines at different prices.

## Domain Description

The **Pharmacy Management System** handles the following business processes:

- **Client Management**: Store customer information including names and addresses
- **Order Processing**: Track customer orders with dates and total prices
- **Medicine Inventory**: Maintain stock levels and retail prices for medicines
- **Supplier Network**: Manage multiple suppliers and their contact information
- **Supply Chain**: Track which suppliers provide which medicines and at what prices

## Database Entities

### 1. **Client**
Represents customers who purchase medicines from the pharmacy.
- **Attributes**: 
  - `client_id` (Primary Key)
  - `first_name`, `last_name`
  - Composite address: `street`, `city`, `postal_code`, `country`

### 2. **Order**
Represents a customer's order.
- **Attributes**:
  - `order_id` (Primary Key)
  - `client_id` (Foreign Key → Client)
  - `order_date`
  - `total_price` (derived attribute)

### 3. **OrderItem**
Represents individual items within an order (many medicines per order).
- **Attributes**:
  - `orderitem_id` (Primary Key)
  - `order_id` (Foreign Key → Order)
  - `quantity`
  - `unit_price` (at time of order)

### 4. **Medicine**
Represents medicines available in the pharmacy.
- **Attributes**:
  - `medicine_id` (Primary Key)
  - `name`
  - `stock` (quantity available)
  - `unit_price` (retail selling price to customers)

### 5. **Supplier**
Represents companies that supply medicines to the pharmacy.
- **Attributes**:
  - `supplier_id` (Primary Key)
  - `name`
  - Composite address: `street`, `city`, `postal_code`, `country`

### 6. **Supplier_Medicine** (Weak Entity / Junction Table)
Represents the many-to-many relationship between suppliers and medicines.
- **Attributes**:
  - `supplier_id` (PK, FK → Supplier)
  - `medicine_id` (PK, FK → Medicine)
  - `supply_price` (wholesale price from supplier)

## Entity-Relationship Model

### Key Relationships:

1. **Client → Order** (1:N)
   - One client can make many orders
   - Each order belongs to one client

2. **Order → OrderItem** (1:N)
   - One order contains many items
   - Each item belongs to one order

3. **Medicine → OrderItem** (1:N)
   - One medicine can appear in many order items
   - Each order item references one medicine

4. **Supplier ↔ Medicine** (N:M via Supplier_Medicine)
   - Many suppliers can provide many medicines
   - Many medicines can be supplied by many suppliers
   - The relationship includes `supply_price` attribute