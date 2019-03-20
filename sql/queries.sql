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
                           AND date <= t.date
                           ORDER BY date DESC
                           LIMIT 1) as rate
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
                           AND date <= t.date
                           ORDER BY date DESC
                           LIMIT 1) as rate
         FROM transfers t
         WHERE sender_id = ?
         AND sender_account = ?) as x) as balance, currency
FROM accounts
WHERE customer_id = ?
AND account_no = ?;


-- -----------------------------------------------------------------------------------


-- Get transfers
SELECT *
FROM transfers
WHERE (sender_id = ? AND sender_account = ?)
OR (receiver_id = ? AND receiver_account = ?)
ORDER BY date ASC;

