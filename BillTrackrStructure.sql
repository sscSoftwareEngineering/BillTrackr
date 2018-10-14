CREATE DATABASE IF NOT EXISTS BillTrackr;

USE BillTrackr;

CREATE TABLE IF NOT EXISTS Companies (
	CompanyID int NOT NULL AUTO_INCREMENT,
	CompName varchar (25) NULL,
	CompStreetAddress varchar (50) NULL,
	CompCity varchar (30) NULL,
	CompState varchar (2) NULL,
	CompZipCode varchar (10) NULL,
	CompPhoneNumber varchar (15) NULL,
    PRIMARY KEY (CompanyID)
);

CREATE TABLE IF NOT EXISTS Bills (
	BillID int NOT NULL AUTO_INCREMENT,
	CompanyID int NOT NULL,
	BillDueDate date NOT NULL,
	BillDueAmount decimal(10,2) NOT NULL,
	BillPaidDate date NULL,
	BillPaidAmount decimal(10,2) NULL,
  PRIMARY KEY (BillID),
  FOREIGN KEY (CompanyID) 
		REFERENCES Companies(CompanyID)
);


