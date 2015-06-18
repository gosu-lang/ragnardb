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

    s = new StringReader("CREATE HELLO contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch(SQLParseError e) {
      assertEquals("[1, 8] - ERROR: Expecting 'table' but found 'hello'.", e.getMessage());
    }
  }

  private void parseWithNoErrors(SQLParser parser) {
    try {
      parser.parse();
    } catch(SQLParseError e) {
      e.printStackTrace();
      fail();
    }
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
    } catch(SQLParseError e) {
      assertEquals("[1, 8] - ERROR: Expecting 'table' but found 'tempo'.", e.getMessage());
    }
  }

  @Test
  public void createIfNotExists() {
    StringReader s = new StringReader("CREATE TABLE contacts IF NOT EXISTS database.contacts");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch(SQLParseError e) {
      assertEquals("[1, 23] - ERROR: The statement has not terminated but the grammar has been exhausted.", e.getMessage());
    }
  }
}
