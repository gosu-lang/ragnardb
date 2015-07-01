package ragnardb.runtime

uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.IType
uses gw.lang.reflect.features.IPropertyReference
uses org.junit.Assert
uses org.junit.Before
uses org.junit.BeforeClass
uses org.junit.Test
uses ragnardb.RagnarDB

uses java.lang.Integer

class QueryBootstrapTest {

  @BeforeClass
  static function beforeClass(){
    RagnarDB.setDBUrl( "jdbc:h2:mem:runtimebootstraptest;DB_CLOSE_DELAY=-1" );
    RagnarDB.execStatement( "CREATE TABLE CONTACTS (\n" +
        "    id bigint auto_increment,\n" +
        "    user_id int,\n" +
        "    first_name nchar(50),\n" +
        "    last_name nchar(50),\n" +
        "    age int\n" +
        ");" )
  }

  @Before
  function clearContacts(){
    RagnarDB.execStatement( "DELETE FROM CONTACTS" );
  }

  @Test
  function basicWhereWorks(){

    new Contact(){
      :FirstName = "Carson",
      :LastName = "Gross",
      :Age = 39
    }.create()


    var carson = Contact.where( Contact#FirstName.isEqualTo( "Carson" ) ).first()

    Assert.assertEquals( "Carson", carson.FirstName )
    Assert.assertEquals( "Gross", carson.LastName )
    Assert.assertEquals( 39, carson.Age )

    Assert.assertNull( Contact.where( Contact#FirstName.isEqualTo( "Scott" ) ).first() )
  }


  static class Contact extends SQLRecord {
    construct(){
      super("CONTACTS", "id");
    }

    // simulate generated methods
    property get FirstName() : String {
      return getRawValue( "first_name" ) as String;
    }
    property set FirstName(s : String) {
      setRawValue( "first_name", s );
    }
    property get LastName() : String {
      return getRawValue( "last_name" ) as String;
    }
    property set LastName(s : String) {
      setRawValue( "last_name", s );
    }
    property get UserId() : Integer{
      return getRawValue( "user_id" ) as Integer;
    }
    property set UserId(s : Integer) {
      setRawValue( "user_id", s );
    }
    property get Age() : Integer{
      return getRawValue( "age" ) as Integer;
    }
    property set Age(s : Integer) {
      setRawValue( "age", s );
    }
    property get Id() : Integer{
      return getRawValue( "user_id" ) as Integer;
    }

    static function where( c: SQLConstraint) : SQLQuery<Contact> {
      return new SQLQuery<Contact>(new ContactMetadata(), Contact).where(c)
    }
  }

  static class ContactMetadata implements ITypeToSQLMetadata {
    var propertyMap = {
        "FirstName" -> "first_name",
        "LastName" -> "last_name",
        "UserId" -> "user_id",
        "Age" -> "age",
        "Id" -> "id"
    }

    override function getTableForType( type: IType ): String{
      return "contacts"
    }

    override function getColumnForProperty( pi: IPropertyInfo ) : String {
      return propertyMap[pi.Name]
    }
  }

}