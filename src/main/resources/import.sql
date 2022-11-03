-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-1');
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-2');
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-3');

insert into Customer (id, name, phoneNumber, email) values(nextval('customerId_seq'), 'customer', '01234567890', 'customer@email.com');

insert into Flight (id, number, departure, destination) values(nextval('flightId_seq'), 'A1234', 'ABC', 'XYZ');
