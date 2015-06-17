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
    for(int i = 0; i < 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.IDENT, tok.getType());
      assertEquals("hello", tok.getText());
    }
    for(int i = 0; i < 2; i++) {
      Token tok = tokenizer.get();
      assertEquals(TokenType.SELECT, tok.getType());
    }
  }

  @Test
  public void specialCharTest() {
    StringReader s = new StringReader("+ - hello .");
    SQLTokenizer tokenizer = new SQLTokenizer(s);

    Token tok = tokenizer.get();
    assertEquals(TokenType.PLUS, tok.getType());

    tok = tokenizer.get();
    assertEquals(TokenType.MINUS, tok.getType());

    tok = tokenizer.get();
    assertEquals(TokenType.IDENT, tok.getType());

    tok = tokenizer.get();
    assertEquals(TokenType.DOT, tok.getType());
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

    s = new StringReader("00100");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.LONG, tok.getType());
    assertEquals(100, tok.getLongNumber());

    s = new StringReader("100.023");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(100.023, tok.getDoubleNumber(), 0.01);

    s = new StringReader(".2");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(.2, tok.getDoubleNumber(), 0.01);

    s = new StringReader("3.");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(3, tok.getDoubleNumber(), 0.01);

    s = new StringReader("1e2");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(100.0, tok.getDoubleNumber(), 0.01);

    s = new StringReader("1.02e1");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(10.2, tok.getDoubleNumber(), 0.01);

    s = new StringReader("109.05e-2");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(1.0905, tok.getDoubleNumber(), 0.01);

    /*Exceptional Number handling*/
    s = new StringReader("109.57.234.41.");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(109.57, tok.getDoubleNumber(), 0.01);

    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(.234, tok.getDoubleNumber(), 0.01);

    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(.41, tok.getDoubleNumber(), 0.01);

    tok = tokenizer.get();

    assertEquals(TokenType.DOT, tok.getType());

    s = new StringReader("1.3E1n");
    tokenizer = new SQLTokenizer(s);
    tok = tokenizer.get();

    assertEquals(TokenType.DOUBLE, tok.getType());
    assertEquals(13., tok.getDoubleNumber(), 0.01);

    tok = tokenizer.get();

    assertEquals(TokenType.IDENT, tok.getType());
    assertEquals("n", tok.getText());

  }

  @Test
  public void commentTest() {
    StringReader s = new StringReader("/*Pure Comment*/");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    Token tok = tokenizer.get();

    assertEquals(TokenType.EOF, tok.getType());

    s = new StringReader("HELLO/*COMMENT*/HELLO");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.IDENT, tok.getType());
    assertEquals("hello", tok.getText());

    tok = tokenizer.get();
    assertEquals(TokenType.IDENT, tok.getType());
    assertEquals("hello", tok.getText());

    s = new StringReader("1/2");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.LONG, tok.getType());
    assertEquals(1, tok.getLongNumber());

    tok = tokenizer.get();
    assertEquals(TokenType.SLASH, tok.getType());

    tok = tokenizer.get();
    assertEquals(TokenType.LONG, tok.getType());
    assertEquals(2, tok.getLongNumber());

    s = new StringReader("/**/");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.EOF, tok.getType());

    s = new StringReader("/***/");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.EOF, tok.getType());

    s = new StringReader("/* Incomplete Comment");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.EOF, tok.getType());

    s = new StringReader("/*Comment*withstar*/ +");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.PLUS, tok.getType());

    s = new StringReader("/***/ +");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.PLUS, tok.getType());


    s = new StringReader("--All of this is ommited + - \n ;");
    tokenizer = new SQLTokenizer(s);

    tok = tokenizer.get();
    assertEquals(TokenType.SEMI, tok.getType());
  }

  @Test
  public void stringLiteralTest() {
    StringReader s = new StringReader("\"dklfa dlfAl192@#   \"");
    SQLTokenizer tokenizer = new SQLTokenizer(s);
    Token tok = tokenizer.get();

    assertEquals(TokenType.IDENT, tok.getType());
    assertEquals("dklfa dlfAl192@#   ", tok.getText());
  }
}
