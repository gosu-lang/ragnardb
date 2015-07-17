

CREATE TABLE IF NOT EXISTS STATES (
    id int,
    name varchar(255)
);


CREATE TABLE IF NOT EXISTS CONTACTS (
    id bigint auto_increment,
    user_id  int,
    company_id int,
    first_name nchar(50),
    last_name nchar(50),
    age int,
    state_id int,
    FOREIGN KEY (state_id) REFERENCES STATES (id)
);


CREATE TABLE IF NOT EXISTS COMPANY (
    id bigint auto_increment,
    name nchar(50)
);