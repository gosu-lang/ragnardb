package ragnardb.plugin;

import gw.lang.parser.IHasInnerClass;
import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;

import java.io.IOException;

public interface ISqlDdlType extends IType, IHasInnerClass, IFileBasedType {
  String getSqlSource() throws IOException;
}
