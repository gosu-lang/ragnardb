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
      return new SQLTypeInfo( (ISqlTableType)getTheRef() );
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

    for(ColumnDefinition def : _table.getColumnDefinitions()){
     defs.add(def);
    }

    return defs;


  }

  @Override
  public IFile[] getSourceFiles() {
    return _enclosingType.getSourceFiles();
  }
}
