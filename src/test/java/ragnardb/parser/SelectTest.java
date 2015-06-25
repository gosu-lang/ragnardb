package ragnardb.parser;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by pjennings on 6/25/2015.
 */
public class SelectTest {
  @Test
  public void basicSelect() {
    parseString("SELECT * FROM contacts");
    parseString("SELECT name FROM contacts");
    parseString("SELECT * from bugs WHERE age > 5");
    parseString("SELECT * from bugs WHERE age > 5 AND age < 10");
    parseString("SELECT ALL * FROM employees WHERE 5 = 5 UNION ALL SELECT * FROM CONTACTS ");
    parseString("SELECT * FROM contacts ORDER BY age ASC NULLS FIRST LIMIT 55 OFFSET 3 SAMPLE_SIZE 20 FOR UPDATE");


  }
  private void parseString(String str){
    StringReader s = new StringReader(str);
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  private void expectError(String str, String err){
    StringReader s = new StringReader(str);
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    try {
      parser.parse();
      fail();
    } catch (SQLParseError e) {
      assertEquals(err, e.getMessage());
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
}
