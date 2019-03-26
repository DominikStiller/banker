SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE Transfers;
TRUNCATE Accounts;
TRUNCATE Customers;
TRUNCATE ExchangeRates;
TRUNCATE Currencies;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO currencies (code, name)
VALUES ('CAD', 'Canadian Dollar'),
       ('USD', 'US Dollar'),
       ('EUR', 'Euro');

-- from_rate * from_amount = to_amount
INSERT INTO exchangerates(from_currency, to_currency, rate)
VALUES ('CAD', 'CAD', 1.0),
       ('USD', 'USD', 1.0),
       ('EUR', 'EUR', 1.0),
       ('CAD', 'USD', 0.75126),
       ('CAD', 'EUR', 0.66356),
       ('USD', 'CAD', 1.33114),
       ('USD', 'EUR', 0.88323),
       ('EUR', 'CAD', 1.50715),
       ('EUR', 'USD', 1.13220);

INSERT INTO customers (name, address, email, phone)
VALUES ('Roxane Weavers', '78976 Almo Drive, North Bay Ontario P1A T7N', 'rweavers4@cargocollective.com', '126-293-9927'),
       ('Theresina Whitehouse', '7 Butterfield Hill, Edson Alberta T7E A0A', 'twhitehouse3@abc.net.au', '663-893-1617'),
       ('Nicol Powdrell', '669 Buell Plaza, Pemberton British Columbia E1W G8A', 'npowdrell7@canalblog.com', '622-687-1447'),
       ('Worth Rotherham', '763 Sherman Trail, Sylvan Lake Alberta T4S T4T', 'wrotherham1t@economist.com', '480-982-2219'),
       ('Wye Najara', '073 Hermina Lane, Olds Alberta T4H P7L', 'wnajara25@ning.com', '179-338-0077');

INSERT INTO accounts (customer_id, account_no, type, currency, initial_balance)
VALUES (1, 1, 'checking', 'CAD', 523.21),
       (1, 2, 'savings', 'USD', 2934.12),
       (2, 1, 'checking', 'EUR', 10.32),
       (3, 1, 'savings', 'CAD', 14235.00),
       (4, 1, 'savings', 'CAD', 18900.32),
       (4, 2, 'savings', 'EUR', 25002.88),
       (5, 1, 'checking', 'USD', 239.12),
       (5, 2, 'checking', 'EUR', 521.01);

INSERT INTO transfers (sender_id, sender_account, receiver_id, receiver_account, amount, currency, execution_date, reference)
VALUES (1, 1, 3, 1, 200, 'CAD', '2019-03-18 19:30:12', ''),
       (2, 1, 4, 1, 2.57, 'EUR', '2019-04-05 02:52:18', 'Movie Theatre Tickets'),
       (5, 2, 5, 1, 300.97, 'EUR', '2019-04-01 09:38:31', ''),
       (4, 1, 1, 2, 1500.00, 'EUR', '2019-03-30 12:23:52', 'Birthday Gift'),
       (4, 1, 5, 1, 1281.34, 'CAD', '2019-04-01 00:00:00', 'Rent');
