DROP DATABASE IF EXISTS tastypizza;
CREATE DATABASE tastypizza;
USE tastypizza;


CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50)  NOT NULL,
    last_name VARCHAR(50)  NOT NULL,
    phone VARCHAR(20),
    role ENUM('CUSTOMER', 'EMPLOYEE') NOT NULL,
    date_of_birth DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);

CREATE TABLE employee_details (
    user_id BIGINT PRIMARY KEY,
    salary DECIMAL(10, 2) NOT NULL CHECK (salary >= 0),
    hire_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
);


CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(8, 2) NOT NULL CHECK (price >= 0),
    base_grammage DECIMAL(8, 2),
    category_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_products_category_id ON products(category_id);


CREATE TABLE ingredients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    unit VARCHAR(20) NOT NULL,
    stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    minimum_stock DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (minimum_stock >= 0)
);


CREATE TABLE product_ingredients (
    product_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    standard_quantity DECIMAL(10, 2) NOT NULL CHECK (standard_quantity > 0),
    PRIMARY KEY (product_id, ingredient_id),
    FOREIGN KEY (product_id)    REFERENCES products(id)    ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
);

CREATE INDEX idx_product_ingredients_ingredient_id ON product_ingredients(ingredient_id);


CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    processed_by BIGINT,
    status ENUM('PENDING', 'PROCESSING', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    estimated_delivery INT,
    delivery_fee DECIMAL(6, 2) NOT NULL DEFAULT 0,
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (address_id) REFERENCES addresses(id),
    FOREIGN KEY (processed_by) REFERENCES users(id)
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_processed_by ON orders(processed_by);
CREATE INDEX idx_orders_created_at ON orders(created_at);

CREATE TABLE product_sizes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    size_label VARCHAR(50) NOT NULL,
    price DECIMAL(8, 2) NOT NULL CHECK (price >= 0),
    grammage DECIMAL(8, 2),
    UNIQUE KEY uq_product_size (product_id, size_label),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_sizes_product_id ON product_sizes(product_id);


CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_size_id BIGINT,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price  DECIMAL(8, 2)  NOT NULL,
    subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    special_instructions VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (product_size_id) REFERENCES product_sizes(id)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

CREATE TABLE shifts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CHECK (end_time > start_time),
    FOREIGN KEY (employee_id) REFERENCES employee_details(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_shifts_employee_id ON shifts(employee_id);
CREATE INDEX idx_shifts_shift_date ON shifts(shift_date);


CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    method ENUM('CASH', 'CARD') NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    transaction_ref VARCHAR(100),
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);


CREATE TABLE order_item_toppings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_item_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    action ENUM('ADD', 'REMOVE') NOT NULL,
    quantity DECIMAL(8, 3),
    UNIQUE KEY uq_topping (order_item_id, ingredient_id, action),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
);

CREATE INDEX idx_order_item_toppings_order_item_id ON order_item_toppings(order_item_id);


CREATE TABLE order_status_history (
    id         BIGINT      PRIMARY KEY AUTO_INCREMENT,
    order_id   BIGINT      NOT NULL,
    old_status ENUM('PENDING', 'PROCESSING', 'DELIVERED', 'CANCELLED'),
    new_status ENUM('PENDING', 'PROCESSING', 'DELIVERED', 'CANCELLED') NOT NULL,
    changed_by BIGINT,
    note       VARCHAR(255),
    changed_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);


DELIMITER //
CREATE TRIGGER orders_status_change
    AFTER UPDATE ON orders
    FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO order_status_history (order_id, old_status, new_status, changed_at)
        VALUES (NEW.id, OLD.status, NEW.status, NOW());
    END IF;
END //
DELIMITER ;


CREATE TABLE operating_hours (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    day_of_week TINYINT  NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (close_time > open_time),
    UNIQUE KEY uq_day (day_of_week)
);


