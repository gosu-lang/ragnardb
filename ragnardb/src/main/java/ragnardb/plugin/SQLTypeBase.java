package ragnardb.plugin;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.ResourcePath;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeRef;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.module.IModule;
import gw.util.StreamUtil;
import gw.util.concurrent.LockingLazyVar;
import ragnardb.parser.SQLParser;
import ragnardb.parser.SQLTokenizer;
import ragnardb.parser.ast.SQL;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class SQLTypeBase extends TypeBase implements ISQLTypeBase {

  private SQL _parseTree;
  private IFile _file;
  private final SQLPlugin _plugin;
  private ITypeRef _typeRef;
  private LockingLazyVar<SQLBaseTypeInfo> _typeInfo;

  public SQLTypeBase(IFile file, SQLPlugin plugin) {
    _file = file;
    _plugin = plugin;
    _typeInfo = new LockingLazyVar<SQLBaseTypeInfo>() {
      @Override
      protected SQLBaseTypeInfo init() {
        return  initTypeInfo();
      }
    };
    setParseTree();
  }

  protected abstract SQLBaseTypeInfo initTypeInfo();

  public SQL getParseTree(){
    return _parseTree;
  }

  @Override
  public IFile getFile() {
    return _file;
  }

  private void setParseTree(){
    SQLParser p = null;
    try {
      p = new SQLParser(new SQLTokenizer(getReader()));
    } catch (Exception e) {
      throw new RuntimeException( e );
    }
    _parseTree = p.parse();
  }

  @Override
  public String getTypeName( IModule module ) {
    ResourcePath path = getFile().getPath();
    for( IDirectory dir: module.getSourcePath() ) {
      if( dir.getPath().isDescendant( path ) ) {
        String rawName = dir.getPath().relativePath(path);
        rawName = rawName.substring(0, rawName.lastIndexOf('.'));
        rawName = rawName.replace(File.separator, ".");
        return rawName;
      }
    }
    throw new IllegalStateException( "Expected to have name" );
  }

  @Override
  public Reader getReader() throws IOException {
    return new InputStreamReader( getFile().openInputStream() );
  }

  @Override
  public String getName() {
    return getTypeName(getTypeLoader().getModule());
  }

  @Override
  public String getRelativeName() {
    return getName().substring(getName().lastIndexOf('.') + 1);
  }

  @Override
  public String getNamespace() {
    return getName().substring(0, getName().lastIndexOf('.'));
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

  public ITypeRef getTypeRef() {
    if(_typeRef == null) {
      _typeRef = getTypeLoader().getModule().getModuleTypeLoader().getTypeRefFactory().create(this);
    }
    return _typeRef;
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _typeInfo.get();
  }

  @Override
  public IFile[] getSourceFiles() {
    return new IFile[]{ getFile()};
  }

  @Override
  public String getSqlSource() throws IOException {
    Reader reader = StreamUtil.getInputStreamReader(getSourceFiles()[0].openInputStream());
    return StreamUtil.getContent(reader);
  }

}
