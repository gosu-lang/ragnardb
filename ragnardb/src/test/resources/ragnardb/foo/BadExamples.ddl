-- sample DDL file for domain logic extension testing (negative pattern)
-- this DDL is OK, and will have a properly named extension class at ragnardb.foo.BadExampleExtensions.InvalidExt.gs
-- but InvalidExt.gs will not extend SQLRecord; therefore the type system will ignore the extension methods

CREATE TABLE INVALIDS (
    InvalidId int
);