package ragnardb.plugin;

import gw.lang.Gosu;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import jdk.nashorn.internal.ir.annotations.Ignore;
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
  public void getSQLType() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.MyQuery");
    assertNotNull(result);
    assertEquals("ragnardb.foo.MyQuery", result.getName());
  }

  @Test
  public void getTablesFromSelect() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.MyQuery.Contact");
    assertNull(result);
  }

  @Test
  public void getTypeExplicitly() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users.Contact", result.getName());
  }

  @Test
  public void getNamesFromSQL() {
    ISQLQueryType result = (ISQLQueryType) TypeSystem.getByFullNameIfValid("ragnardb.foo.MyQuery");
    assertNotNull(result);
    assertEquals("ragnardb.foo.MyQuery", result.getName());
    assertEquals("ragnardb.foo", result.getNamespace());
    assertEquals("MyQuery", result.getRelativeName());
  }

  @Test
  public void getNonExistantType() {
    IType result = TypeSystem.getByFullNameIfValid("ragnardb.foo.Unknown.DoesNotExist");
    assertNull(result);
  }

  @Test
  @org.junit.Ignore
  public void testSQLExecute() {
    ISQLQueryType result = (ISQLQueryType) TypeSystem.getByFullNameIfValid("ragnardb.foo.MyQuery");
    assertNotNull(result);
    SQLBaseTypeInfo ti = (SQLBaseTypeInfo) result.getTypeInfo();
    IMethodInfo execute = ti.getMethod("execute", JavaTypes.STRING());
    assertNotNull(execute);
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
    ISQLTableType result = (ISQLTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
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
    ISQLTableType result = (ISQLTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users.Contact", result.getName());
    assertEquals("ragnardb.foo.Users", result.getNamespace());
    assertEquals("Contact", result.getRelativeName());

    SQLBaseTypeInfo ti = (SQLBaseTypeInfo) result.getTypeInfo();
    assertEquals("Contact", ti.getName());

    //make a set of expected Name/IJavaType pairs
    Set<String> expectedPropertyNames = Stream.of("UserId", "LastName", "FirstName", "Age").collect(Collectors.toSet());
    Map<String, IJavaType> expectedPropertyNameAndType = new HashMap<>(expectedPropertyNames.size());

    expectedPropertyNameAndType.put("UserId", JavaTypes.pINT());
    expectedPropertyNameAndType.put("LastName", JavaTypes.STRING());
    expectedPropertyNameAndType.put("FirstName", JavaTypes.STRING());
    expectedPropertyNameAndType.put("Age", JavaTypes.pINT());

    //number of properties is what we expect
    assertEquals(expectedPropertyNameAndType.size(), ti.getProperties().size()); //TODO domain logic breaks this test

    //each property name has a match in the map, and the type is identical
    for(IPropertyInfo actualProp : ti.getProperties()) {
      IJavaType expectedType = expectedPropertyNameAndType.get(actualProp.getName());
      assertNotNull("expectedType was null, meaning the actualProp's name was not found in the map", expectedType);
      assertSame(expectedType, actualProp.getFeatureType());
    }
  }

  @Test
  public void getMethodInfo() {
    ISQLTableType result = (ISQLTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users.Contact");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users.Contact", result.getName());
    assertEquals("ragnardb.foo.Users", result.getNamespace());
    assertEquals("Contact", result.getRelativeName());

    SQLBaseTypeInfo ti = (SQLBaseTypeInfo) result.getTypeInfo();
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
    ISQLDdlType result = (ISQLDdlType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Users");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Users", result.getName());
    String nl = System.getProperty("line.separator");

    String expectedSource = "CREATE TABLE CONTACTS (" + nl +
        "    UserId int," + nl +
        "    FirstName nchar(50)," + nl +
        "    LastName nchar(50)," + nl +
        "    Age int" + nl +
        "    -- TODO add Gender" + nl +
        ");";
    String actualSource = null;

    try {
      actualSource = result.getSqlSource();
      System.out.println(actualSource);
    } catch(IOException e) {
      e.printStackTrace();
      fail();
    }

    assertEquals(expectedSource, actualSource);

    SQLBaseTypeInfo ti = (SQLBaseTypeInfo) result.getTypeInfo();
    assertEquals("Users", ti.getName());

    IPropertyInfo readOnlySqlSourceProperty = ti.getProperty("SqlSource");
    assertNotNull(readOnlySqlSourceProperty);
    assertTrue(readOnlySqlSourceProperty.isReadable());
    assertFalse(readOnlySqlSourceProperty.isWritable());
    assertEquals(expectedSource, readOnlySqlSourceProperty.getAccessor().getValue(null));
  }

  @Test
  public void getInjectedMethod() {
    ISQLTableType result = (ISQLTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Bars.Baz");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Bars.Baz", result.getName());
    assertEquals("ragnardb.foo.Bars", result.getNamespace());
    assertEquals("Baz", result.getRelativeName());

    SQLTableTypeInfo ti = (SQLTableTypeInfo) result.getTypeInfo();
    assertEquals("Baz", ti.getName());

    IMethodInfo domainLogicMethod = ti.getMethod("sayHi", JavaTypes.STRING());
    assertNotNull(domainLogicMethod);
    assertEquals("void", domainLogicMethod.getReturnType().getName());
  }

  @Test
  public void getInjectedProperty() {
    ISQLTableType result = (ISQLTableType) TypeSystem.getByFullNameIfValid("ragnardb.foo.Bars.Baz");
    assertNotNull(result);
    assertEquals("ragnardb.foo.Bars.Baz", result.getName());
    assertEquals("ragnardb.foo.Bars", result.getNamespace());
    assertEquals("Baz", result.getRelativeName());

    SQLTableTypeInfo ti = (SQLTableTypeInfo) result.getTypeInfo();
    assertEquals("Baz", ti.getName());

    IPropertyInfo domainLogicProperty = ti.getProperty("MeaningOfLife");
    assertNotNull(domainLogicProperty);
    assertTrue(domainLogicProperty.isReadable());
    assertFalse(domainLogicProperty.isWritable());
    assertEquals(JavaTypes.pINT(), domainLogicProperty.getFeatureType());
    //assertEquals(42, domainLogicProperty.getAccessor().getValue(result)); //TODO failing with seemingly legitimate error:
    /**
     * java.lang.IllegalArgumentException: object is not an instance of declaring class
     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     at gw.internal.gosu.parser.GosuPropertyInfo$GosuPropertyAccessor.getValue(GosuPropertyInfo.java:290)
     at ragnardb.plugin.SQLPluginTest.getInjectedProperty(SQLPluginTest.java:201)
     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)
     at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)
     at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)
     at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)
     at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)
     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)
     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)
     at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)
     at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)
     at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)
     at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)
     at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)
     at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:28)
     at org.junit.runners.ParentRunner.run(ParentRunner.java:236)
     at org.junit.runners.Suite.runChild(Suite.java:128)
     at org.junit.runners.Suite.runChild(Suite.java:24)
     at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)
     at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)
     at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)
     at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)
     at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)
     at org.junit.runners.ParentRunner.run(ParentRunner.java:236)
     at org.junit.runner.JUnitCore.run(JUnitCore.java:157)
     at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:78)
     at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:212)
     at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:68)
     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     at com.intellij.rt.execution.application.AppMain.main(AppMain.java:140)
     */
  }

}
