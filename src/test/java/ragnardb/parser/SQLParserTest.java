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

    s = new StringReader("CREATE TEMP contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch(SQLParseError e) {
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
    } catch(SQLParseError e) {
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
    } catch(SQLParseError e) {
      assertEquals("[1, 27] - ERROR: Expecting 'not' but found 'exists'.", e.getMessage());
    }

    s = new StringReader("CreAte tABLE if NoT databae.contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch(SQLParseError e) {
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
    } catch(SQLParseError e) {
      assertEquals("[1, 37] - ERROR: The statement has not terminated but the grammar has been exhausted.", e.getMessage());
    }
  }
}
