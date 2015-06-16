package ragnardb.plugin;

import gw.lang.Gosu;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeSystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SQLPluginTest {

  @Before
  public void beforeMethod() {
    Gosu.init();
  }

  @Test
  public void getTypeExplicitly() {
    ITypeLoader sqlPlugin = new SQLPlugin(TypeSystem.getGlobalModule());
    TypeSystem.pushTypeLoader(TypeSystem.getGlobalModule(), sqlPlugin);
    IType result = sqlPlugin.getType("ragnardb.foo.Users.Contacts");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users.Contacts", result.getName());
  }

  @Test
  public void getNonExistantType() {
    ITypeLoader sqlPlugin = new SQLPlugin(TypeSystem.getGlobalModule());
    TypeSystem.pushTypeLoader(TypeSystem.getGlobalModule(), sqlPlugin);
    IType result = sqlPlugin.getType("ragnardb.foo.Unknown.DoesNotExist");
    assertNull(result);
  }

  @Test
  public void getTypes() {


    fail("not implemented yet");
  }
}
