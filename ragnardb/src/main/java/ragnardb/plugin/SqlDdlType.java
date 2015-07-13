package ragnardb.plugin;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.util.concurrent.LockingLazyVar;
import ragnardb.parser.ast.CreateTable;
import ragnardb.parser.ast.DDL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlDdlType extends SQLTypeBase implements ISQLDdlType {

  LockingLazyVar<List<ISQLTableType>> _innerClasses = new LockingLazyVar<List<ISQLTableType>>() {
    @Override
    protected List<ISQLTableType> init() {
      List<ISQLTableType> innerClasses = new ArrayList<>();
      for(CreateTable table : getTables()) {
        String fqn = getName() + '.' + table.getTypeName();
        innerClasses.add((ISQLTableType) TypeSystem.getOrCreateTypeReference(new SQLTableType((ISQLDdlType) getTypeRef(), table, fqn)));
      }
      return innerClasses;
    }
  };

  public SqlDdlType(IFile file, SQLPlugin plugin) {
    super(file, plugin);
  }

  @Override
  protected SQLBaseTypeInfo initTypeInfo() {
    return new SQLDdlTypeInfo((ISQLDdlType) getTypeRef());
  }

  @Override
  public IType getInnerClass(CharSequence strTypeName) {
    for(ISQLTableType type : _innerClasses.get()) {
      if(type.getRelativeName().equals(strTypeName)) {
        return type;
      }
    }
    return null;
  }

  public List<? extends ISQLTableType> getTableTypes() {
    return _innerClasses.get();
  }

  @Override
  public List<? extends IType> getInnerClasses() {
    return _innerClasses.get();
  }

  @Override
  public List<? extends IType> getLoadedInnerClasses() {
    return getInnerClasses();
  }

  @Override
  public List<CreateTable> getTables() {
    return ((DDL) getParseTree()).getList().stream().collect(Collectors.toList());
  }
}
