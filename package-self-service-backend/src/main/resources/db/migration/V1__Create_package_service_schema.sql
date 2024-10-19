-- Create the employees table
CREATE TABLE employees
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    country     VARCHAR(100) NOT NULL
);

-- Create the packages table
CREATE TABLE packages
(
    id              UUID PRIMARY KEY,
    package_name    VARCHAR(255) NOT NULL,
    weight_in_grams INT          NOT NULL,
    sender_id       UUID         NOT NULL,
    receiver_id     UUID         NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES employees (id),
    FOREIGN KEY (receiver_id) REFERENCES employees (id)
);

-- Create the package_details table linked to the packages
CREATE TABLE package_details
(
    id                   UUID PRIMARY KEY,
    package_id           UUID NOT NULL,                    -- Reference to the package
    status               VARCHAR(50) DEFAULT 'REGISTERED', -- Package status: REGISTERED, SENT, DELIVERED
    date_of_registration TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    date_of_receipt      TIMESTAMP,
    FOREIGN KEY (package_id) REFERENCES packages (id)
);

-- Insert sample data for employees with split addresses and UUIDs
INSERT INTO employees (id, name, street, city, state, postal_code, country)
VALUES ('c1a5a8e8-1b0e-4c6c-8342-576b9a9e6b5f', 'Alice Johnson', '123 Oak St', 'Springfield', 'IL', '62704', 'USA');

INSERT INTO employees (id, name, street, city, state, postal_code, country)
VALUES ('d2b7f9e9-2c1d-4e7a-8b3f-7d0e6c2a8c7f', 'Bob Smith', '456 Pine St', 'Metropolis', 'NY', '10001', 'USA');

INSERT INTO employees (id, name, street, city, state, postal_code, country)
VALUES ('e3c9a0ea-3e2e-4f8b-9d3f-8f1e7d3b9c8f', 'Charlie Lee', '789 Maple St', 'Gotham', 'NJ', '07030', 'USA');
