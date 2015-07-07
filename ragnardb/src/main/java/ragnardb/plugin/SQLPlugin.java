package ragnardb.plugin;

import gw.config.CommonServices;
import gw.fs.FileFactory;
import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.reflect.*;
import gw.lang.reflect.module.IModule;
import gw.util.GosuStringUtil;
import gw.util.Pair;
import gw.util.concurrent.LockingLazyVar;
import ragnardb.parser.ast.CreateTable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SQLPlugin extends TypeLoaderBase {

  private static final String DDL_EXTENSION = ".ddl";
  private static final String SQL_EXTENSION = ".sql";

  private final LockingLazyVar<Map<String, IFile>> _ddlSourcesByPackage = new LockingLazyVar<Map<String, IFile>>() {
    @Override
    protected Map<String, IFile> init() {
      Map<String, IFile> result = new HashMap<>();
      for(Pair<String, IFile> p : findAllFilesByExtension(DDL_EXTENSION)) {
        IFile file = p.getSecond();
        String fileName = p.getFirst();
        String fqn = fileName.substring(0, fileName.length() - DDL_EXTENSION.length()).replace('/', '.');
        result.put(fqn, file);
      }
      return result;
    }
  };

  private final LockingLazyVar<Map<IFile, String>> _fileToDdlTypeName = new LockingLazyVar<Map<IFile, String>>() {
    @Override
    protected Map<IFile, String> init() {
      Map<IFile, String> result = new HashMap<>();
      for(Pair<String, IFile> p : findAllFilesByExtension(DDL_EXTENSION)) {
        IFile file = p.getSecond();
        String fileName = p.getFirst();
        String fqn = fileName.substring(0, fileName.length() - DDL_EXTENSION.length()).replace('/', '.');
        result.put(file, fqn);
      }
      return result;
    }
  };

  private Map<String, ISQLDdlType> _fqnToDdlType = new HashMap<>();
  private Set<String> _namespaces;

  public SQLPlugin(IModule module) {
    super(module);
    appendSqlSources(); // Todo in maven
    init();
  }

  private void appendSqlSources() {
    IDirectory moduleRoot = FileFactory.instance().getIDirectory(new File(""));
    IDirectory sourceRootDir = moduleRoot.dir("src/main/java");
    IDirectory testRootDir = moduleRoot.dir("src/test/java");

    List<IDirectory> sourcePaths = new ArrayList<>(_module.getSourcePath());
    sourcePaths.add(sourceRootDir);
    sourcePaths.add(testRootDir);

    _module.setSourcePath(sourcePaths);
  }

  @Override
  public IType getType(final String fullyQualifiedName) {
    if(_ddlSourcesByPackage.get().keySet().contains(fullyQualifiedName)) { //hence, a ddltype
      return ((SqlDdlType) getOrCreateDdlType(fullyQualifiedName)).getTypeRef();
    }

    String[] packagesAndType = fullyQualifiedName.split("\\.");
    String[] packages = Arrays.copyOfRange(packagesAndType, 0, packagesAndType.length - 1);
    String typeName = packagesAndType[packagesAndType.length - 1];
    String packageName = String.join(".", packages);

    IFile iFile = _ddlSourcesByPackage.get().get(packageName);
    if(iFile != null) {
      ISQLDdlType ddlType = getOrCreateDdlType(packageName);
      List<String> typeNames = ddlType.getTables().stream().map(CreateTable::getTypeName).collect(Collectors.toList());
      if(typeNames.contains(typeName) && isValidTypeName(fullyQualifiedName)) {
        return ddlType.getInnerClass(typeName);
      }
    }
    return null;
  }

  private ISQLDdlType getOrCreateDdlType(String fullyQualifiedName) {
    ISQLDdlType ddlType = _fqnToDdlType.get(fullyQualifiedName);
    if(ddlType != null) {
      return ddlType;
    }
    IFile sqlFile = _ddlSourcesByPackage.get().get(fullyQualifiedName);
    ddlType = new SqlDdlType(sqlFile, this);
    _fqnToDdlType.put(fullyQualifiedName, ddlType);
    return ddlType;
  }

  @Override
  public Set<String> getAllNamespaces() {
    if( _namespaces == null ) {
      try {
        _namespaces = TypeSystem.getNamespacesFromTypeNames( getAllTypeNames(), new HashSet<String>() );
      }
      catch( NullPointerException e ) {
        //!! hack to get past dependency issue with tests
        return Collections.emptySet();
      }
    }
    return _namespaces;
  }

  @Override
  public void refreshedNamespace( String namespace, IDirectory dir, RefreshKind kind ) {
    if( _namespaces != null ) {
      if( kind == RefreshKind.CREATION )  {
        _namespaces.add( namespace );
      }
      else if( kind == RefreshKind.DELETION ) {
        _namespaces.remove( namespace );
      }
    }
  }

  @Override
  public boolean hasNamespace( String namespace ) {
    return getAllNamespaces().contains( namespace );
  }

  @Override
  public List<String> getHandledPrefixes() {
    return Collections.emptyList();
  }

  @Override
  public boolean handlesNonPrefixLoads() {
    return true;
  }

  @Override
  public Set<String> computeTypeNames() {
    Set<String> result = new HashSet<>();
    for(String pkg : _ddlSourcesByPackage.get().keySet()) {
      for(CreateTable table : getOrCreateDdlType(pkg).getTables()) {
        result.add(pkg + '.' + table.getTypeName());
      }
    }
    return result;
  }

  private static boolean isValidTypeName(String typeName) {
    String[] nameParts = GosuStringUtil.tokenize(typeName, '.');
    if(nameParts == null || nameParts.length == 0) {
      return false;
    }
//    for (String namePart : nameParts) {
//      if (!PropertyNode.isGosuIdentifier(namePart)) {
//        return false;
//      }
//    }
    return true;
  }

  public List<Pair<String, IFile>> findAllFilesByExtension(String extension) {
    List<Pair<String, IFile>> results = new ArrayList<>();

    for (IDirectory sourceEntry : _module.getSourcePath()) {
      if (sourceEntry.exists()) {
        String prefix = sourceEntry.getName().equals(IModule.CONFIG_RESOURCE_PREFIX) ? IModule.CONFIG_RESOURCE_PREFIX : "";
        addAllLocalResourceFilesByExtensionInternal(prefix, sourceEntry, extension, results);
      }
    }
    return results;
  }

  private void addAllLocalResourceFilesByExtensionInternal(String relativePath, IDirectory dir, String extension, List<Pair<String, IFile>> results) {
    List<IDirectory> excludedPath = Arrays.asList(_module.getFileRepository().getExcludedPath());
    if ( excludedPath.contains( dir )) {
      return;
    }
    if(!CommonServices.getPlatformHelper().isPathIgnored(relativePath)) {
      for(IFile file : dir.listFiles()) {
        if(file.getName().endsWith(extension)) {
          String path = appendResourceNameToPath(relativePath, file.getName());
          results.add(new Pair<>(path, file));
        }
      }
      for(IDirectory subdir : dir.listDirs()) {
        String path = appendResourceNameToPath(relativePath, subdir.getName());
        addAllLocalResourceFilesByExtensionInternal(path, subdir, extension, results);
      }
    }
  }

  private static String appendResourceNameToPath(String relativePath, String resourceName) {
    String path;
    if(relativePath.length() > 0) {
      path = relativePath + '/' + resourceName;
    } else {
      path = resourceName;
    }
    return path;
  }

  @Override
  public String[] getTypesForFile(IFile file) {
    String ddlTypeName = _fileToDdlTypeName.get().get(file);
    if( ddlTypeName != null ) {
      return new String[] {ddlTypeName};
    }
    return NO_TYPES;
  }

  @Override
  public boolean handlesFile(IFile file) {
    return DDL_EXTENSION.substring( 1 ).equals(file.getExtension());
  }

  @Override
  protected void refreshedImpl() {
    clear();
  }

  @Override
  public RefreshKind refreshedFile(IFile file, String[] types, RefreshKind kind) {
    clear();
    return kind;
  }
  @Override
  protected void refreshedTypesImpl(RefreshRequest request) {
    clear();
  }

  private void clear() {
    _fileToDdlTypeName.clear();
    _ddlSourcesByPackage.clear();
    _fqnToDdlType.clear();
    //_namespaces.clear();
  }
}
