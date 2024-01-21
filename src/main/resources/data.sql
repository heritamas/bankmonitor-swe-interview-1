DELETE FROM transaction CASCADE;
INSERT INTO transaction (id, data, created_at) VALUES
  (default, '{ "amount": 100, "reference": "BM_2023_101" }', NOW()),
  (default, '{ "amount": 3333, "reference": "", "sender": "Bankmonitor" }', NOW()),
  (default, '{ "amount": -100, "reference": "BM_2023_101_BACK", "reason": "duplicate" }', NOW()),
  (default, '{ "amount": 12345, "reference": "BM_2023_105" }', NOW()),
  (default, '{ "amount": 54321, "sender": "Bankmonitor", "recipient": "John Doe" }', NOW());

--DELETE FROM transactiondata;
