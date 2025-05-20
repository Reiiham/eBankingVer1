-- current acc
INSERT INTO bankaccount (
    id, balance, createat, status, client_id, type, overdraft
) VALUES (
             'ACC123456', 10000.0, CURRENT_TIMESTAMP, 'ACTIVATED', 2, 'CA', 2000.0
         );


--CHANGE CLIENT ID TO A CLIENT ID THAT EXISTS IN YOUR DATABASE
--saving acc
INSERT INTO bankaccount (
    id, balance, createat, status, client_id, type, interestrate
) VALUES (
             'ACC987654', 15000.0, CURRENT_TIMESTAMP, 'ACTIVATED', 2, 'SA', 0.03
         );
--Créditer un compte
INSERT INTO accountoperation (
    amount, operationdate, bankaccount_id, description, type
) VALUES (
             1000.0, CURRENT_TIMESTAMP, 'ACC123456', 'Initial deposit', 'CREDIT'
         );
--Debit exemple
INSERT INTO accountoperation (
    amount, operationdate, bankaccount_id, description, type
) VALUES (
             300.0, CURRENT_TIMESTAMP, 'ACC123456', 'ATM withdrawal', 'DEBIT'
         );
