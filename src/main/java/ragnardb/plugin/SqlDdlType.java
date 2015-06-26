package ragnardb.plugin;

import gw.fs.IFile;
import gw.lang.reflect.*;
import gw.util.concurrent.LockingLazyVar;
import ragnardb.parser.ast.CreateTable;
import ragnardb.parser.ast.DDL;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kmoore on 6/25/15.
 */
public class SqlDdlType extends TypeBase implements ISqlDdlType {

    private final ISQLSource _sqlSource;
    private final SQLPlugin _plugin;

    LockingLazyVar<List<ISqlTableType>> _innerClasses = new LockingLazyVar<List<ISqlTableType>>() {
        @Override
        protected List<ISqlTableType> init() {
            List<ISqlTableType> innerClasses = new ArrayList<>();
            for( CreateTable table : _sqlSource.getTables() ) {
                String fqn = getName() + '.' + table.getName();
                innerClasses.add((ISqlTableType) TypeSystem.getOrCreateTypeReference(new SqlTableType(getTypeRef(), table, fqn)));
            }
            return innerClasses;
        }
    };
    private ITypeRef _typeRef;

    public SqlDdlType( SQLPlugin plugin, ISQLSource sqlSource ) {
        _plugin = plugin;
        _sqlSource = sqlSource;
    }

    @Override
    public IType getInnerClass(CharSequence strTypeName) {
        for( ISqlTableType type: _innerClasses.get() ) {
            if( type.getRelativeName().equals( strTypeName ) ) {
                return type;
            }
        }
        return null;
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
    public String getName() {
        return _sqlSource.getTypeName(getTypeLoader().getModule());
    }

    @Override
    public String getRelativeName() {
        return getName().substring(getName().lastIndexOf('.') + 1);
    }

    @Override
    public String getNamespace() {
        return getName().substring( 0, getName().lastIndexOf( '.' ) );
    }

    @Override
    public SQLPlugin getTypeLoader() {
        return _plugin;
    }

    @Override
    public IType getSupertype() {
        return null;
    }

    @Override
    public IType[] getInterfaces() {
        return new IType[0];
    }

    private ISqlDdlType getTypeRef() {
        return (ISqlDdlType) (_typeRef == null ? _typeRef = TypeSystem.getOrCreateTypeReference( this ) : _typeRef);
    }
    @Override
    public ITypeInfo getTypeInfo() {
        return new BaseTypeInfo( getTypeRef() );
    }

    @Override
    public DDL getSqlSource() {
        return _sqlSource.getParseTree();
    }

    @Override
    public IFile[] getSourceFiles() {
        return new IFile[] {_sqlSource.getFile()};
    }
}
