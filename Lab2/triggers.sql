DROP FUNCTION IF EXISTS update_order_total_price() CASCADE;
DROP FUNCTION IF EXISTS set_orderitem_unit_price() CASCADE;

-- Function that recalculates and updates the order's total price
CREATE OR REPLACE FUNCTION update_order_total_price()
RETURNS TRIGGER AS $$
DECLARE
    oid BIGINT;
BEGIN
    oid := COALESCE(NEW.order_id, OLD.order_id);

    -- Recalculate the total price
    UPDATE "order"
    SET total_price = (
        SELECT COALESCE(SUM(quantity * unit_price), 0)
        FROM orderitem
        WHERE order_id = oid
    )
    WHERE order_id = oid;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Function that automatically assigns the unit_price in orderitem
CREATE OR REPLACE FUNCTION set_orderitem_unit_price()
RETURNS TRIGGER AS $$
BEGIN
    -- Read the current selling price from the medicine table
    SELECT unit_price
    INTO NEW.unit_price
    FROM medicine
    WHERE medicine_id = NEW.medicine_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger that updates total order price after insert, update, or delete
CREATE TRIGGER trg_update_order_total
AFTER INSERT OR UPDATE OR DELETE ON orderitem
FOR EACH ROW
EXECUTE FUNCTION update_order_total_price();

-- Trigger that sets unit_price before inserting or updating an order item
CREATE TRIGGER trg_set_orderitem_unit_price
BEFORE INSERT OR UPDATE ON orderitem
FOR EACH ROW
EXECUTE FUNCTION set_orderitem_unit_price();
