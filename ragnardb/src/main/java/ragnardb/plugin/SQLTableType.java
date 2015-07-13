package ragnardb.plugin;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.Modifier;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.TypeSystem;
import gw.util.GosuClassUtil;
import gw.util.GosuExceptionUtil;
import gw.util.concurrent.LockingLazyVar;
import ragnardb.RagnarDB;
import ragnardb.parser.ast.CreateTable;
import ragnardb.runtime.SQLRecord;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SQLTableType extends TypeBase implements ISQLTableType {

  private final String _name;
  private final ISQLDdlType _enclosingType;
  private final CreateTable _table;
  private LockingLazyVar<ITypeInfo> _typeInfo = new LockingLazyVar<ITypeInfo>()
  {
    @Override
    protected ITypeInfo init()
    {
      return new SQLTableTypeInfo( (ISQLTableType)getTheRef() );
    }
  };

  public SQLTableType(ISQLDdlType parent, CreateTable table, String name){
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
    return TypeSystem.get( SQLRecord.class );
  }

  @Override
  public ISQLDdlType getEnclosingType() {
    return _enclosingType;
  }

  @Override
  public int getModifiers()
  {
    return super.getModifiers() | Modifier.STATIC;
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
    return getTable().getColumnDefinitions();
  }

  @Override
  public void deleteAll( boolean confirm )
  {
    if( confirm )
    {
      try
      {
        RagnarDB.execStatement( "DELETE FROM " + _table.getTableName() );
      }
      catch( SQLException e )
      {
        throw GosuExceptionUtil.forceThrow( e );
      }
    }
  }

  @Override
  public IFile[] getSourceFiles() {
    return _enclosingType.getSourceFiles();
  }
}
