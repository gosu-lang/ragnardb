package ragnardb.plugin;

import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;
import ragnardb.parser.ast.CreateTable;
import ragnardb.runtime.IHasListenableProperties;

import java.util.List;

public interface ISQLTableType extends IType, IFileBasedType, IHasListenableProperties {

  CreateTable getTable();

  List<ColumnDefinition> getColumnDefinitions();

  void deleteAll( boolean confirm );
}
