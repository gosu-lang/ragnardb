package ragnardb.plugin;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LockingLazyVar;
import ragnardb.parser.ast.CreateTable;
import ragnardb.parser.ast.DDL;

import java.util.ArrayList;
import java.util.List;

public class SqlTableType extends TypeBase implements ISqlTableType {

  private final String _name;
  private final ISqlDdlType _enclosingType;
  private final CreateTable _table;
  private LockingLazyVar<ITypeInfo> _typeInfo = new LockingLazyVar<ITypeInfo>()
  {
    @Override
    protected ITypeInfo init()
    {
      return new SQLTypeInfo( SqlTableType.this );
    }
  };

  public SqlTableType(ISqlDdlType parent, CreateTable table, String name){
    _name = name;
    _table = table;
    _enclosingType = parent;
  }

  @Override
  public CreateTable getTable() {
    return _table;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getRelativeName() {
    return GosuClassUtil.getShortClassName(getName());
  }

  @Override
  public String getNamespace() {
    return GosuClassUtil.getPackage(getName());
  }

  @Override
  public ITypeLoader getTypeLoader() {
    return _enclosingType.getTypeLoader();
  }

  @Override
  public IType getSupertype() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ISqlDdlType getEnclosingType() {
    return _enclosingType;
  }

  @Override
  public IType[] getInterfaces() {
    return new IType[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _typeInfo.get();
  }

  public List<ColumnDefinition> getColumnDefinitions() {

    List<ColumnDefinition> defs = new ArrayList<>();

    DDL ddlFile = getEnclosingType().getSqlSource();
    for (CreateTable table: ddlFile.getList()){
      for(ColumnDefinition def : table.getColumnDefinitions()){
       defs.add(def);
      }
    }

    return defs;

    /*
    //TODO mock implementation; remove these once we hook up with the parser
    ColumnDefinition userId = new ColumnDefinition("UserId", Types.INTEGER);
    ColumnDefinition lastName = new ColumnDefinition("LastName", Types.NVARCHAR);
    ColumnDefinition firstName = new ColumnDefinition("FirstName", Types.NVARCHAR);
    ColumnDefinition age = new ColumnDefinition("Age", Types.INTEGER);
    final List<ColumnDefinition> contactCols = Arrays.asList(userId, lastName, firstName, age);

    switch(getName()) {
      case "ragnardb.foo.Users.Contacts": return contactCols;
      case "": return Collections.emptyList();
      default: throw new Error("Unknown sqlType: " + getName());
    }
    */
  }

  @Override
  public IFile[] getSourceFiles() {
    return _enclosingType.getSourceFiles();
  }
}
