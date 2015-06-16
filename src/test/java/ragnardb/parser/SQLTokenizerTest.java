package ragnardb.parser;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class SQLTokenizerTest {
  @Test
  public void basicTokenizingTest() {
    File inFile = new File("src/test/resources/Foo/Users.ddl");
    System.out.println(inFile.getAbsolutePath());
    Assert.assertTrue(inFile.exists());
  }

  @Test
  public void identifierTest() {
    StringReader s = new StringReader("hello HELLo SELECT sElEcT");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    for (int i = 0; i < 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.IDENT, tok.getType());
      assertEquals("hello", tok.getText());
    }
    for (int i = 0; i < 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.SELECT, tok.getType());
    }
  }
  @Test
   public void numVarTest() {
    StringReader s = new StringReader("he1lo HE1Lo SELECT sElEcT");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    for(int i = 0; i < 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.IDENT, tok.getType());
      assertEquals("he1lo", tok.getText());
    }
    for(int i = 0; i < 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.SELECT, tok.getType());
    }
  }

  @Test
  public void newLineTest() {
    StringReader s = new StringReader("hello\nHELLo");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    for(int i = 1; i <= 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.IDENT, tok.getType());
      assertEquals("hello", tok.getText());
      assertEquals(i, tok.getLine());
      assertEquals(i, tok.getCol());
    }

    s = new StringReader("hello\r\nHELLo");
    tokenizer = new SQLTokenizer(s);
    for(int i = 1; i <= 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.IDENT, tok.getType());
      assertEquals("hello", tok.getText());
      assertEquals(i, tok.getLine());
      assertEquals(i, tok.getCol());
    }
  }

  @Test
  public void numberTest() {
    StringReader s = new StringReader("100");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    Token tok = tokenizer.get();

    assertEquals(TokenType.LONG, tok.getType());
    assertEquals(100, tok.getLongNumber());

    s = new StringReader("100.023");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(100.023, tok.getDoubleNumber(), 0.01);
  }
}
