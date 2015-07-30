package ragnardb.parser;

import org.junit.Test;
import ragnardb.parser.ast.CreateTable;
import ragnardb.parser.ast.DDL;
import ragnardb.plugin.ColumnDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SQLParserTest {

  @Test
  public void basicCreateTable() {
    StringReader s = new StringReader("CREATE TABLE contacts( name varchar(255))");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMP TABLE IF NOT EXISTS somedatabase.contacts( name varchar(255)) WITHOUT ROWID");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);


    s = new StringReader("CREATE TABLE contacts( id int PRIMARY KEY , name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  @Test
  public void erroneusCreateTable() {
    StringReader s = new StringReader("CREATE TABLE contacts");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithErrors(parser, Collections.singletonList("[1, 21] - ERROR: Expected to find '(' to start the column definition list"));

    s = new StringReader("CREATE HELLO contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithErrors(parser, Arrays.asList("[1, 8] - ERROR: Expecting 'table' but found 'hello'.", "[1, 14] - ERROR: Expected to find '(' to start the column definition list"));

    s = new StringReader("CREATE TABLE contacts 1 2 3 CREATE TABLE contacts( name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    DDL ddl = (DDL) parser.parse();
    assertEquals(Collections.singletonList("[1, 23] - ERROR: Expected to find '(' to start the column definition list"), parser.getErrors());
    CreateTable table = ddl.getList().get(0);
    assertEquals("contacts", table.getTableName());
    ColumnDefinition columnDefinition = table.getColumnDefinitions().get(0);
    assertEquals("name", columnDefinition.getColumnName());
    assertEquals(255, columnDefinition.getStartInt());

    s = new StringReader("HELLO CREATE TABLE contacts( name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    ddl = (DDL) parser.parse();
    assertEquals(Collections.singletonList("[1, 1] - ERROR: Expecting a SQL statement (ex CREATE.., ALTER.., DROP..., ...)"), parser.getErrors());
    table = ddl.getList().get(0);
    assertEquals("contacts", table.getTableName());
    columnDefinition = table.getColumnDefinitions().get(0);
    assertEquals("name", columnDefinition.getColumnName());
    assertEquals(255, columnDefinition.getStartInt());
  }

  private void parseWithNoErrors(SQLParser parser) {
    parser.parse();
    assertEquals(0, parser.getErrors().size());
  }

  private void parseWithErrors(SQLParser parser, List<String> errList) {
    parser.parse();
    assertEquals(errList, parser.getErrors());
  }

  private void parseWithNoErrorsComputer(SQLParser parser, String statement) {
    try {
      parser.parse();
    } catch (SQLParseError e) {
      System.out.print("Failed on:" + statement + "\n");
      e.printStackTrace();
      fail();
    } catch (Exception e) {
      System.out.print("Failed on: " + statement + "\n");
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

    s = new StringReader("CREATE TABLE contacts( id int NOT NULL PRIMARY KEY  , name varchar(255), num decimal(50,60))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  @Test
  public void createTemporary() {
    StringReader s = new StringReader("CREATE TEMP TABLE contacts(name varchar(255))");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMPORARY TABLE contacts(name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMPO TABLE contacts(name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    DDL ddl = (DDL) parser.parse();
    assertEquals(Arrays.asList("[1, 8] - ERROR: Expecting 'table' but found 'tempo'.", "[1, 14] - ERROR: Expected to find '(' to start the column definition list"), parser.getErrors());
    CreateTable table = ddl.getList().get(0);
    assertEquals("tempo", table.getTableName());
    ColumnDefinition columnDefinition = table.getColumnDefinitions().get(0);
    assertEquals("name", columnDefinition.getColumnName());
    assertEquals(255, columnDefinition.getStartInt());

    s = new StringReader("CREATE TEMP contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithErrors(parser, Arrays.asList("[1, 13] - ERROR: Expecting 'table' but found 'contacts'.", "[1, 20] - ERROR: Expected to find '(' to start the column definition list"));
  }

  @Test
  public void createIfNotExists() {
    StringReader s = new StringReader("CREATE TABLE contacts IF NOT EXISTS database.contacts(name varchar(255))");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithErrors(parser, Collections.singletonList("[1, 23] - ERROR: Expected to find '(' to start the column definition list"));

    s = new StringReader("CREATE TABLE IF NOT EXISTS database.contacts(name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TEMPORARY TABLE IF EXISTS database.contacts(name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithErrors(parser, Collections.singletonList("[1, 27] - ERROR: Expecting 'not' but found 'exists'."));

    s = new StringReader("CreAte tABLE if NoT database.contacts(name varchar(255))");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithErrors(parser, Collections.singletonList("[1, 21] - ERROR: Expecting 'exists' but found 'database'."));
  }

  @Test
  public void createWithoutRowID() {
    StringReader s = new StringReader("CREATE TEMP TABLE database.contacts (name varchar(255)) WITHOUT ROWID");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE IF NOT EXISTS contacts (name varchar(255)) WO ROWID");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithErrors(parser, Arrays.asList("[1, 57] - ERROR: Expecting 'SEMI' or 'EOF but found identifier", "[1, 64] - ERROR: Expecting 'create' but found 'End of file'."));
  }

  @Test
  public void complexColumns() {
    StringReader s = new StringReader("CREATE TABLE contacts(ID int DEFAULT 555555  IDENTITY (5,6) PRIMARY KEY)");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE contacts(ID int DEFAULT 5 NOT NULL AUTO_INCREMENT (5) PRIMARY KEY)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  @Test
  public void testForeignKeys() {
    StringReader s = new StringReader("CREATE TABLE contacts(id int , parent int, FOREIGN KEY (parent) REFERENCES parents (id) )  ");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("CREATE TABLE providences(id int, countryName int, countryId varchar(255), " +
            "FOREIGN KEY (countryName , countryId) REFERENCES countries (name, id ) ON DELETE CASCADE ON UPDATE SET NULL)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);


    s = new StringReader("CREATE TABLE tasks (id int, FOREIGN KEY ( id ) REFERENCES )");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

  }

  @Test
  public void testBatches() {
    StringReader s = new StringReader("CREATE TABLE contacts(id int PRIMARY KEY) ; CREATE TABLE buyers(name varchar(255))");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }
  /*@Test
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

  }

*/

  @Test
  public void computerGenerated() {

    String currentLine;
    String currentInput = "";
    try{
      InputStreamReader f = new InputStreamReader(getClass().getResourceAsStream("/createtablestatements.txt"));
      BufferedReader br = new BufferedReader(f);
      while((currentLine = br.readLine()) != null) {
        currentInput += currentLine;
      }
      String[] inputs = currentInput.split(";");
      for(String input: inputs){
        SQLParser p = new SQLParser(new SQLTokenizer(new StringReader(input)));
        parseWithNoErrorsComputer(p, input);
      }
    } catch (IOException e) {
      System.out.println("Cannot read file.");
      e.printStackTrace();
      fail();
    }
  }




}
