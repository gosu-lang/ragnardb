package ragnardb.plugin;

import gw.lang.parser.IHasInnerClass;
import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;
import ragnardb.parser.ast.DDL;

/**
 * Created by kmoore on 6/25/15.
 */
public interface ISqlDdlType extends IType, IHasInnerClass, IFileBasedType {
    DDL getSqlSource();
}
