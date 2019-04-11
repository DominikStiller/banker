-- -----------------------------------------------------------------------------------
-- All queries which are used in the Java application
-- -----------------------------------------------------------------------------------


-- Get a specific customer and number of associated accounts
SELECT id, name, address, email, phone, COUNT(account_no)
FROM customers
LEFT JOIN accounts ON customers.id = accounts.customer_id
WHERE id = ?
GROUP BY id, name, address, email, phone;


-- -----------------------------------------------------------------------------------


-- Get all customers and numbers of associated accounts
SELECT id, name, address, email, phone, COUNT(account_no)
FROM customers
LEFT JOIN accounts ON customers.id = accounts.customer_id
GROUP BY id, name, address, email, phone;


-- -----------------------------------------------------------------------------------


-- Create a new customer
INSERT INTO customers (name, address, email, phone)
VALUES (?, ?, ?, ?);


-- -----------------------------------------------------------------------------------


-- Get a specific account
SELECT *
FROM accounts
WHERE customer_id = ?
AND account_no = ?;


-- -----------------------------------------------------------------------------------


-- Get all accounts for a specific customer
SELECT * FROM accounts
WHERE customer_id = ?;


-- -----------------------------------------------------------------------------------


-- Get account balance
-- balance = initial_balance + incoming - outgoing
-- All transfers are converted to the account's currency
--
-- Test result with initial data:
--    - Account 4-1:
--      18900.32 CAD + 2.57 EUR (3.87 CAD) + 7000 EUR (10550.05 CAD)
--      - 1500.00 EUR (2260.73 CAD) - 1281.34 CAD = 25912.17 CAD
SELECT initial_balance
   -- Incoming
+ (SELECT IFNULL(SUM(x.amount * x.rate), 0)
   FROM (SELECT t.amount, (SELECT rate
                           FROM exchangerates
                           WHERE from_currency = t.currency
                           AND to_currency = (SELECT currency
                                                FROM accounts
                                                WHERE customer_id = ?
                                                AND account_no = ?)
                           ) as rate
         FROM transfers t
         WHERE receiver_id = ?
         AND receiver_account = ?) as x)
   -- Outgoing
- (SELECT IFNULL(SUM(x.amount * x.rate), 0)
   FROM (SELECT t.amount, (SELECT rate
                           FROM exchangerates
                           WHERE from_currency = t.currency
                           AND to_currency = (SELECT currency
                                                FROM accounts
                                                WHERE customer_id = ?
                                                AND account_no = ?)
                           ) as rate
         FROM transfers t
         WHERE sender_id = ?
         AND sender_account = ?) as x) as balance, currency
FROM accounts
WHERE customer_id = ?
AND account_no = ?;


-- -----------------------------------------------------------------------------------


-- Create a new account
INSERT INTO accounts (customer_id, account_no, currency, initial_balance)
VALUES (?, ?, ?, ?);


-- -----------------------------------------------------------------------------------


-- Get incoming and outgoing transfers for a specific account
SELECT transfers.id,
       sender_id, sender_account, s.name as sender_name,
       receiver_id, receiver_account, r.name as receiver_name,
       amount, currency, execution_date, reference
FROM transfers
JOIN customers s ON transfers.sender_id = s.id
JOIN customers r ON transfers.receiver_id = r.id
-- Outgoing
WHERE (sender_id = ? AND sender_account = ?)
-- Incoming
OR (receiver_id = ? AND receiver_account = ?)
ORDER BY execution_date ASC;


-- -----------------------------------------------------------------------------------


-- Make wire transfer
INSERT INTO transfers(sender_id, sender_account, receiver_id, receiver_account,
                  amount, currency, execution_date, reference)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);


-- -----------------------------------------------------------------------------------


-- Get all currencies
SELECT code, name FROM currencies;


-- -----------------------------------------------------------------------------------


-- Convert amount of money between currencies
SELECT ? * rate
FROM exchangerates
WHERE from_currency = ?
AND to_currency = ?;
