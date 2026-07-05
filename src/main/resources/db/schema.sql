CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS dealer_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  business_name VARCHAR(160) NOT NULL,
  approved_at TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT fk_dealer_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vehicles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dealer_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  make VARCHAR(80) NOT NULL,
  model VARCHAR(80) NOT NULL,
  year INT NOT NULL,
  mileage INT NOT NULL DEFAULT 0,
  fuel_type VARCHAR(30) NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  listing_type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_vehicles_dealer FOREIGN KEY (dealer_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_vehicles_dealer (dealer_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  vehicle_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  rental_start DATE NULL,
  rental_end DATE NULL,
  total_price DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_orders_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  INDEX idx_orders_user (user_id),
  INDEX idx_orders_vehicle (vehicle_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vehicle_features (
  vehicle_id BIGINT PRIMARY KEY,
  color VARCHAR(40),
  engine_capacity VARCHAR(30),
  transmission VARCHAR(30),
  horsepower VARCHAR(30),
  seating_capacity INT,
  safety_rating VARCHAR(20),
  has_gps BOOLEAN NOT NULL DEFAULT FALSE,
  has_bluetooth BOOLEAN NOT NULL DEFAULT FALSE,
  has_sunroof BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_vehicle_features_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ratings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  dealer_id BIGINT NOT NULL,
  stars INT NOT NULL,
  comment VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ratings_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_ratings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ratings_dealer FOREIGN KEY (dealer_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_ratings_stars CHECK (stars BETWEEN 1 AND 5),
  INDEX idx_ratings_dealer (dealer_id)
) ENGINE=InnoDB;
