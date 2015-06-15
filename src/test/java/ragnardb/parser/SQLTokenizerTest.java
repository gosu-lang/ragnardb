package ragnardb.parser;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class SQLTokenizerTest
{
  @Test
  public void basicTokenizingTest() {
    File inFile = new File("src/test/resources/Foo/Users.ddl");
    System.out.println(inFile.getAbsolutePath());
    Assert.assertTrue(inFile.exists());



  }


}
