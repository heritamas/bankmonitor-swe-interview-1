CREATE TABLE IF NOT EXISTS transaction (
  id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  data VARCHAR(1000) NOT NULL,
  created_at TIMESTAMP NOT NULL default NOW()
);

CREATE TABLE IF NOT EXISTS transactiondata (
  id INT NOT NULL PRIMARY KEY REFERENCES transaction(id) ON DELETE CASCADE,
  amount INT,
  reference VARCHAR(100),
  sender VARCHAR(100),
  recipient VARCHAR(100),
  reason VARCHAR(100)
);