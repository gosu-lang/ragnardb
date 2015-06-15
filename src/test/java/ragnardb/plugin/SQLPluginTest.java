package ragnardb.plugin;

import gw.fs.FileFactory;
import gw.fs.IFile;
import gw.lang.Gosu;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeSystem;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.*;

public class SQLPluginTest {

  @Before
  public void beforeMethod() {
    Gosu.init();
  }

  @Test
  public void getTypeExplicitly() {
    FileFactory ff = FileFactory.instance();
    IFile srcFile = ff.getIFile(new File("src/test/resources/Foo/Users.ddl"));
    assertTrue(srcFile.exists());
    ITypeLoader sqlPlugin = new SQLPlugin(TypeSystem.getGlobalModule());
    //Set<String> allTypes = (Set<String>) sqlPlugin.getAllTypeNames();
    IType result = sqlPlugin.getType("Users");
    assertNotNull(result);
    assertEquals("Foo.Users", result.getName());


//    ISQLSource src = new SQLSource(srcFile);
//    Set<IType> types = src.getTypes();
//    assertEquals(1, types.size());

//    fail("not implemented yet");
  }

  @Test
  public void getTypes() {


    fail("not implemented yet");
  }
}
