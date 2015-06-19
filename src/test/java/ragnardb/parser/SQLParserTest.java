package ragnardb.parser;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SQLParserTest {

  @Test
  public void basicCreateTable() {
    StringReader s = new StringReader("CREATE TABLE contacts");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts( name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMP TABLE IF NOT EXISTS somedatabase.contacts( name varchar(255)) WITHOUT ROWID");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);


    s = new StringReader("CREATE TABLE contacts( id int PRIMARY KEY , name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE HELLO contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 8] - ERROR: Expecting 'table' but found 'hello'.", e.getMessage());
    }
  }

  private void parseWithNoErrors(SQLParser parser) {
    try {
      parser.parse();
    } catch (SQLParseError e) {
      e.printStackTrace();
      fail();
    }
  }


  @Test
  public void testConstraints() {
    StringReader s = new StringReader("CREATE TABLE contacts( id int PRIMARY KEY , name varchar(255))");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts( id int NOT NULL PRIMARY KEY  , name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  @Test
  public void createTemporary() {
    StringReader s = new StringReader("CREATE TEMP TABLE contacts");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMPORARY TABLE contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMPO TABLE contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 8] - ERROR: Expecting 'table' but found 'tempo'.", e.getMessage());
    }

    s = new StringReader("CREATE TEMP contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 13] - ERROR: Expecting 'table' but found 'contacts'.", e.getMessage());
    }
  }

  @Test
  public void createIfNotExists() {
    StringReader s = new StringReader("CREATE TABLE contacts IF NOT EXISTS databae.contacts");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 23] - ERROR: The statement has not terminated but the grammar has been exhausted.", e.getMessage());
    }

    s = new StringReader("CREATE TABLE IF NOT EXISTS databae.contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMPORARY TABLE IF EXISTS databae.contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 27] - ERROR: Expecting 'not' but found 'exists'.", e.getMessage());
    }

    s = new StringReader("CreAte tABLE if NoT databae.contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 21] - ERROR: Expecting 'exists' but found 'databae'.", e.getMessage());
    }
  }

  @Test
  public void createWIITHOUTROWID() {
    StringReader s = new StringReader("CREATE TEMP TABLE databae.contacts WITHOUT ROWID");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE IF NOT EXISTS contacts WO ROWID");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 37] - ERROR: The statement has not terminated but the grammar has been exhausted.", e.getMessage());
    }
  }
  @Test
  public void complexColumns() {
    StringReader s = new StringReader("CREATE TABLE contacts(ID int DEFAULT 5 NOT NULL IDENTITY (5,6) PRIMARY KEY)");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int DEFAULT 5 NOT NULL AUTO_INCREMENT (5) PRIMARY KEY)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }
  @Test
  public void tableConstraint() {
    StringReader s = new StringReader("CREATE TABLE contacts(ID int, CONSTRAINT test PRIMARY KEY (col) ON CONFLICT ABORT)");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int, PRIMARY KEY (col) ON CONFLICT REPLACE)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int, CONSTRAINT test UNIQUE (col) ON CONFLICT FAIL)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int, CONSTRAINT test CHECK (3 * 3))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int, PRIMARY KEY (col, col COLLATE cname DESC, col COLLATE cname ASC, col COLLATE cname) ON CONFLICT IGNORE)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int, FOREIGN KEY (col) REFERENCES ftable)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int, PRIMARY (col) ON CONFLICT ABORT)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 39] - ERROR: Expecting 'key' but found '('.", e.getMessage());
    }

    s = new StringReader("CREATE TABLE contacts(ID int, CONSTRAINT PRIMARY KEY (col) ON CONFLICT ABORT)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 42] - ERROR: Expecting 'identifier' but found 'primary'.", e.getMessage());
    }

    s = new StringReader("CREATE TABLE contacts(ID int, CONSTRAINT name (col) ON CONFLICT)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 47] - ERROR: Expecting 'CONSTRAINT', 'PRIMARY', 'UNIQUE', 'CHECK' or 'FOREIGN' but found " +
        "'LPAREN'.", e.getMessage());
    }

    s = new StringReader("CREATE TABLE contacts(ID int, PRIMARY KEY (col) ON CONFLICT)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals("[1, 60] - ERROR: Expecting conflict action but found 'null'.", e.getMessage());
    }
  }

  /*
  @Test
  public void foreignKeyClauseTest() {
    StringReader s = new StringReader("CREATE TABLE contacts(name varchar(255) CONSTRAINT cname REFERENCES " +
      "foreigntable (columnname, columnname) ON DELETE CASCADE ON UPDATE SET NULL MATCH anothername DEFERRABLE" +
      " INITIALLY IMMEDIATE, ID int REFERENCES nexttable MATCH name ON UPDATE NO ACTION)");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(name varchar(255) CONSTRAINT cname REFERENCES " +
      "foreigntable (columnname, columnname) ON DELETE SET DEFAULT ON UPDATE RESTRICT MATCH anothername DEFERRABLE" +
      " INITIALLY IMMEDIATE, ID int REFERENCES nexttable MATCH name ON UPDATE NO ACTION ON UPDATE RESTRICT ON UPDATE " +
      "NO ACTION)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(name varchar(255) CONSTRAINT cname REFERENCES " +
      "foreigntable (columnname, columnname) ON DELETE SET DEFAULT ON UPDATE RESTRICT MATCH anothername DEFERRABLE" +
      " INITIALLY IMMEDIATE, ID int REFERENCES nexttable MATCH name ON UPDATE NO ACTION ON UPDATE RESTRICT ON UPDATE " +
      "NO ACTION NOT DEFERRABLE)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(name varchar(255) CONSTRAINT cname REFERENCES " +
      "foreigntable (columnname, columnname) ON DELETE SET DEFAULT ON UPDATE RESTRICT MATCH anothername DEFERRABLE" +
      " INITIALLY DEFERRED, ID int REFERENCES nexttable)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    //TODO: add some more tests for the foreign key clause
  }

 */


}
