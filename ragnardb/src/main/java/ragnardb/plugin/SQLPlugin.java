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

  private static final String FILE_EXTENSION = ".ddl";

  private List<Pair<String, ISQLSource>> _sqlSources;

  private final LockingLazyVar<Map<String, ISQLSource>> _sqlSourcesByPackage = new LockingLazyVar<Map<String, ISQLSource>>() {
    @Override
    protected Map<String, ISQLSource> init() {
      Map<String, ISQLSource> result = new HashMap<>(_sqlSources.size());

      for(Pair<String, ISQLSource> ddlFilePair : _sqlSources) {
        String fileName = ddlFilePair.getFirst();
        String packageName = fileName.substring(0, fileName.length() - FILE_EXTENSION.length()).replace('/', '.');
        ISQLSource ddlFile = ddlFilePair.getSecond();
        List<String> typeNames = ddlFile.getTables().stream().map(CreateTable::getTypeName).collect(Collectors.toList());

        boolean allTypeNamesAreValid = true;
        for(String typeName : typeNames) {
          String fullyQualifiedName = packageName + '.' + typeName;
          if(!isValidTypeName(fullyQualifiedName)) {
            allTypeNamesAreValid = false;
          }
          if(allTypeNamesAreValid) {
            result.put(packageName, ddlFile);
          }
        }
      }
      return result;
    }
  };
  private Map<IFile, String> _fileToDdlTypeNames;
  private Set<String> _namespaces;

  public SQLPlugin(IModule module) {
    super(module);
    appendSqlSources();
    initSources();
    init();
  }

  private void appendSqlSources() {
    IDirectory moduleRoot = FileFactory.instance().getIDirectory(new File(""));
    IDirectory sourceRootDir = moduleRoot.dir("src/main/java");
    IDirectory testRootDir = moduleRoot.dir("src/test/java");

    List<IDirectory> sourcePaths = new ArrayList<>(_module.getSourcePath());
    sourcePaths.add(sourceRootDir);
    sourcePaths.add(testRootDir);

    _module.setSourcePath( sourcePaths );
  }

  /**
   * Populates _sqlSources from the provided IModule
   */
  private void initSources() {
    _fileToDdlTypeNames = new HashMap<>();
    //leverage Streams API to basically cast Pair<String, IFile> to Pair<String, ISQLSource> in place
    _sqlSources = findAllFilesByExtension(FILE_EXTENSION)
        .stream()
        .map(pair -> new Pair<String, ISQLSource>(pair.getFirst(), new SQLSource(pair.getSecond())))
        .collect(Collectors.toList());
    _sqlSourcesByPackage.clear();
  }

  @Override
  public IType getType(final String fullyQualifiedName) {
    if(_sqlSourcesByPackage.get().keySet().contains(fullyQualifiedName)) { //hence, a ddltype
      ISQLSource sqlSource = _sqlSourcesByPackage.get().get(fullyQualifiedName);
      ITypeRef ddlType = TypeSystem.getOrCreateTypeReference(new SqlDdlType(this, sqlSource));
      _fileToDdlTypeNames.put(sqlSource.getFile(), ddlType.getName());
      return ddlType;
    }

    String[] packagesAndType = fullyQualifiedName.split("\\.");
    String[] packages = Arrays.copyOfRange(packagesAndType, 0, packagesAndType.length - 1);
    String typeName = packagesAndType[packagesAndType.length - 1];
    String packageName = String.join(".", packages);

    if(_sqlSourcesByPackage.get().keySet().contains(packageName)) {
      ISqlDdlType sourceFile = (ISqlDdlType) TypeSystem.getByFullName(packageName);
      return sourceFile.getInnerClass(typeName);
    }
    return null;
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
    for(String pkg : _sqlSourcesByPackage.get().keySet()) {
      for(CreateTable table : _sqlSourcesByPackage.get().get(pkg).getTables()) {
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
    String ddlTypeName = _fileToDdlTypeNames.get(file);
    if( ddlTypeName != null ) {
      return new String[] {ddlTypeName};
    }
    return NO_TYPES;
  }

  @Override
  public boolean handlesFile(IFile file) {
    return FILE_EXTENSION.substring( 1 ).equals(file.getExtension());
  }

  @Override
  protected void refreshedImpl() {
    initSources();
  }

  @Override
  public RefreshKind refreshedFile(IFile file, String[] types, RefreshKind kind) {
    initSources();
    return kind;
  }

  @Override
  protected void refreshedTypesImpl(RefreshRequest request) {
    initSources();
  }
}
