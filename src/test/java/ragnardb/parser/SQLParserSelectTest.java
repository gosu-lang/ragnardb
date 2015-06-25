package ragnardb.parser;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.fail;

/**
 * Created by klu on 6/24/2015.
 */
public class SQLParserSelectTest {

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
  public void basicSelect(){
    StringReader s = new StringReader("SELECT * FROM contacts");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("SELECT (contact_name, ID) FROM contacts");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

  }

  @Test
  public void selectWithExpressions(){
    StringReader s = new StringReader("SELECT * FROM contacts WHERE ID = 3");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("SELECT (contact_name, ID, address) FROM contacts WHERE ID = 3 AND City LIKE \"M%\"");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("SELECT DISTINCT city FROM contacts WHERE ordermo = Jan GROUP BY contactName, address");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("SELECT ALL cheese FROM deliciousfoods WHERE region=1 HAVING age<2");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("SELECT ALL awesomeballers FROM ballers WHERE awesomeness>=(SELECT sicknesslevel FROM " +
      "sickdudes ORDER BY sicknesslevel DESC LIMIT 1)");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  @Test
  public void variableNames(){
    StringReader s = new StringReader("SELECT * FROM contacts WHERE firstName = @name:java.lang.String OR " +
      "firstName = @name");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    SQLParser parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);

    s = new StringReader("SELECT (contactName, address, schedule) FROM personsOfInterest WHERE firstName = " +
      "@name:java.lang.String OR age = @bestAge:java.lang.Integer");
    tokenizer = new SQLTokenizer(s);
    parser = new SQLParser(tokenizer);
    parseWithNoErrors(parser);
  }

  @Test
  public void computerGenerated(){
    String currentLine;
    String currentInput = "";
    try{
      FileReader f = new FileReader("doc/selectstatements.txt");
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
