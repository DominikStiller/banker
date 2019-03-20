DROP TABLE IF EXISTS Transfers;
DROP TABLE IF EXISTS Accounts;
DROP TABLE IF EXISTS Customers;
DROP TABLE IF EXISTS ExchangeRates;
DROP TABLE IF EXISTS Currencies;


CREATE TABLE Currencies
(
	code char(3),
	name varchar(20),
	PRIMARY KEY (code)
);

CREATE TABLE ExchangeRates
(
  from_currency char(3),
  to_currency char(3),
  date DATETIME,
  rate decimal(8, 5),
  PRIMARY KEY (from_currency, to_currency, date),
  FOREIGN KEY (from_currency)
		REFERENCES Currencies(code),
	FOREIGN KEY (to_currency)
		REFERENCES Currencies(code)
);

CREATE TABLE Customers
(
	id int AUTO_INCREMENT,
	name varchar(50) NOT NULL,
	address varchar(200),
	email varchar(50),
	phone varchar(20),
	PRIMARY KEY (id)
);

CREATE TABLE Accounts
(
	customer_id int,
	account_no int,
	type ENUM('checking', 'savings') NOT NULL,
	currency char(3) NOT NULL,
	initial_balance decimal(10, 2) NOT NULL,
	PRIMARY KEY (customer_id, account_no),
	FOREIGN KEY (customer_id)
	   REFERENCES Customers(id)
		ON DELETE CASCADE,
	FOREIGN KEY (currency)
		REFERENCES Currencies(code)
);

CREATE TABLE Transfers
(
   id int AUTO_INCREMENT,
   sender_id int NOT NULL,
   sender_account int NOT NULL,
   receiver_id int NOT NULL,
   receiver_account int NOT NULL,
   amount decimal(10, 2) NOT NULL,
   currency char(3) NOT NULL,
   date DATETIME NOT NULL,
   reference varchar(100) DEFAULT '',
   PRIMARY KEY (id),
	FOREIGN KEY (sender_id)
		REFERENCES Customers(id),
	FOREIGN KEY (sender_id, sender_account)
	   REFERENCES Accounts(customer_id, account_no),
	FOREIGN KEY (receiver_id)
		REFERENCES Customers(id),
	FOREIGN KEY (receiver_id, receiver_account)
	   REFERENCES Accounts(customer_id, account_no),
	FOREIGN KEY (currency)
		REFERENCES Currencies(code)
);
