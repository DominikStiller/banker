-- Get all customers
SELECT id, name, address, email, phone, COUNT(account_no)
FROM customers
LEFT JOIN accounts ON customers.id = accounts.customer_id
GROUP BY id, name, address, email, phone;


-- -----------------------------------------------------------------------------------


-- Get customer
SELECT id, name, address, email, phone, COUNT(account_no)
FROM customers
LEFT JOIN accounts ON customers.id = accounts.customer_id
WHERE id = ?
GROUP BY id, name, address, email, phone;


-- -----------------------------------------------------------------------------------

-- Get account balance
-- balance = initial_balance + incoming - outgoing
-- All transfers are converted to the account currency with the
--    most recent rate at the time of the transfer
-- Test result for account 4-1:
-- 18900.32 CAD + 2.57 EUR (3.87 CAD) - 1500.00 EUR (2260.73 CAD) - 1281.34 CAD = 15362.12 CAD
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


-- Get transfers
SELECT transfers.id,
       sender_id, sender_account, s.name as sender_name,
       receiver_id, receiver_account, r.name as receiver_name,
       amount, currency, execution_date, reference
FROM transfers
JOIN customers s ON transfers.sender_id = s.id
JOIN customers r ON transfers.receiver_id = r.id
WHERE (sender_id = ? AND sender_account = ?)
OR (receiver_id = ? AND receiver_account = ?)
ORDER BY execution_date ASC;


-- -----------------------------------------------------------------------------------


-- Convert currency
SELECT ? * rate
FROM exchangerates
WHERE from_currency = ?
AND to_currency = ?;
