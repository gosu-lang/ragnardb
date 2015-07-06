package ragnardb.plugin;

import gw.lang.Gosu;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class SQLPluginTest {

  @BeforeClass
  public static void beforeClass() {
    Gosu.init();
    ITypeLoader sqlPlugin = new SQLPlugin(TypeSystem.getCurrentModule()); // global vs. current?
    TypeSystem.pushTypeLoader(TypeSystem.getGlobalModule(), sqlPlugin);
  }

  @Test
  public void getDdlType() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.Users");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users", result.getName());
  }

  @Test
  public void getTypeExplicitly() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users.Contact", result.getName());
  }

  @Test
  public void getNonExistantType() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.Unknown.DoesNotExist");
    assertNull(result);
  }

  @Test
  public void oneSourceWithMultipleTypes() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.Vehicles.Car");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Vehicles.Car", result.getName());

    result = TypeSystem.getByFullNameIfValid("ragnardb.foo.Vehicles.Motorcycle");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Vehicles.Motorcycle", result.getName());
  }

  @Test
  public void getColumnDefs() {
    ISqlTableType result = (ISqlTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
    assertNotNull(result);

    List<ColumnDefinition> colDefs = result.getColumnDefinitions();
    assertNotNull(colDefs);

    Set<String> expectedColumnNames = Stream.of("UserId", "LastName", "FirstName", "Age").collect(Collectors.toSet());
    Set<String> actualColumnNames = colDefs.stream().map(ColumnDefinition::getColumnName).collect(Collectors.toSet());

    assertEquals(expectedColumnNames, actualColumnNames);

    //TODO check columndef positions in the source files
  }

  @Test
  public void getTypeInfo() {
    ISqlTableType result = (ISqlTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users.Contact", result.getName());
    assertEquals("ragnardb.foo.Users", result.getNamespace());
    assertEquals("Contact", result.getRelativeName());

    SQLTypeInfo ti = (SQLTypeInfo) result.getTypeInfo();
    assertEquals("Contact", ti.getName());

    //make a set of expected Name/IJavaType pairs
    Set<String> expectedPropertyNames = Stream.of("UserId", "LastName", "FirstName", "Age").collect(Collectors.toSet());
    Map<String, IJavaType> expectedPropertyNameAndType = new HashMap<>(expectedPropertyNames.size());

    expectedPropertyNameAndType.put("UserId", JavaTypes.pINT());
    expectedPropertyNameAndType.put("LastName", JavaTypes.STRING());
    expectedPropertyNameAndType.put("FirstName", JavaTypes.STRING());
    expectedPropertyNameAndType.put("Age", JavaTypes.pINT());

    //number of properties is what we expect
    assertEquals(expectedPropertyNameAndType.size(), ti.getProperties().size());

    //each property name has a match in the map, and the type is identical
    for(IPropertyInfo actualProp : ti.getProperties()) {
      IJavaType expectedType = expectedPropertyNameAndType.get(actualProp.getName());
      assertNotNull("expectedType was null, meaning the actualProp's name was not found in the map", expectedType);
      assertSame(expectedType, actualProp.getFeatureType());
    }
  }

  @Test
  public void getMethodInfo() {
    ISqlTableType result = (ISqlTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users.Contact", result.getName());
    assertEquals("ragnardb.foo.Users", result.getNamespace());
    assertEquals("Contact", result.getRelativeName());

    SQLTypeInfo ti = (SQLTypeInfo) result.getTypeInfo();
    assertEquals("Contact", ti.getName());

    IMethodInfo findByAge = ti.getMethod("findByAge", JavaTypes.pINT());
    assertNotNull(findByAge);
    assertEquals("ragnardb.foo.Users.Contact", findByAge.getReturnType().getName()); //returns single Contact

    IMethodInfo findAllByAge = ti.getMethod("findAllByAge", JavaTypes.pINT());
    assertNotNull(findAllByAge);
    assertEquals("java.lang.Iterable<ragnardb.foo.Users.Contact>", findAllByAge.getReturnType().getName()); //returns Iterable of Contacts //TODO KB

    IMethodInfo findByAgeWithWrongSignature = ti.getMethod("findByAge", JavaTypes.STRING());
    assertNull(findByAgeWithWrongSignature);

    IMethodInfo unknownMethodWithNoArgs = ti.getMethod("findSomethingWhichDoesNotExist");
    assertNull(unknownMethodWithNoArgs);
  }

  @Test
  public void getRawSQL() {
    ISqlDdlType result = (ISqlDdlType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users", result.getName());

    String expectedSource = "CREATE TABLE CONTACTS (\n" +
        "    UserId int,\n" +
        "    FirstName nchar(50),\n" +
        "    LastName nchar(50),\n" +
        "    Age int\n" +
        "    -- TODO add Gender\n" +
        ");";
    String actualSource = null;

    try {
      actualSource = result.getSqlSource();
      System.out.println(actualSource.toString());
    } catch(IOException e) {
      e.printStackTrace();
      fail();
    }

    assertEquals(expectedSource, actualSource);

    SQLTypeInfo ti = (SQLTypeInfo) result.getTypeInfo();
    assertEquals("Users", ti.getName());

    IPropertyInfo readOnlySqlSourceProperty = ti.getProperty("SqlSource");
    assertNotNull(readOnlySqlSourceProperty);
    assertTrue(readOnlySqlSourceProperty.isReadable());
    assertFalse(readOnlySqlSourceProperty.isWritable());
    assertEquals(expectedSource, readOnlySqlSourceProperty.getAccessor().getValue(null));
  }

}
