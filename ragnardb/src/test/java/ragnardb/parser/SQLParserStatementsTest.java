package ragnardb.parser;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.fail;

/**
 * Created by klu on 8/6/2015.
 */
public class SQLParserStatementsTest {

  private void parseWithNoErrors(SQLParser parser) {
    try {
      parser.parse();
    } catch (SQLParseError e) {
      e.printStackTrace();
      fail();
    }
  }

  private void parseWithNoErrorsComputer(SQLParser parser, String statement) {
    try {
      parser.parse();
    } catch (SQLParseError e) {
      System.out.print("Failed on:" + statement + "\n");
      e.printStackTrace();
      fail();
    } catch (Exception e){
      System.out.print("EXCEPTIONAL FAILURE! Failed on: " + statement + "\n");
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void basicInsertTest(){
    StringReader s = new StringReader("INSERT INTO Contacts VALUES (1, 'Troll', 'Lord');");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("INSERT INTO Contacts(name) VALUES ('Yolo'), ('Swag'), ('Blazeit');");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("INSERT INTO Contacts SET name = 'ThisName';");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  @Test
  public void basicUpdateTest(){
    StringReader s = new StringReader("UPDATE Contacts AS temptable SET id = 1, name = 'Kai' WHERE age > 9000");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("UPDATE Contacts(ID, name) = (SELECT ID, name FROM linkedin WHERE workingtime = 'full time' AND experience > 5) LIMIT 20");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }


}
