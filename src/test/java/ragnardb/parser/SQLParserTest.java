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

}
