package ragnardb.plugin;

import gw.config.CommonServices;
import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.RefreshKind;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.GosuStringUtil;
import gw.util.Pair;
import gw.util.concurrent.LockingLazyVar;

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
        Set<String> typeNames = ddlFile.getTypeNames();

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

  public SQLPlugin(IModule module) {
    super(module);
    initSources(module);
    init();
  }

  /**
   * Populates _sqlSources from the provided IModule
   * @param module Find *.ddl files in this module
   */
  private void initSources(IModule module) {
    //leverage Streams API to basically cast Pair<String, IFile> to Pair<String, ISQLSource> in place
    _sqlSources = findAllFilesByExtension(FILE_EXTENSION)
        .stream()
        .map(pair -> new Pair<String, ISQLSource>(pair.getFirst(), new SQLSource(pair.getSecond().getPath())))
        .collect(Collectors.toList());
  }

  @Override
  public IType getType(final String fullyQualifiedName) {
    String[] packagesAndType = fullyQualifiedName.split("\\.");
    String[] packages = Arrays.copyOfRange(packagesAndType, 0, packagesAndType.length - 1);
    String typeName = packagesAndType[packagesAndType.length - 1];
    String packageName = String.join(".", packages);

    if(_sqlSourcesByPackage.get().keySet().contains(packageName)) {
      ISQLSource ddlFile = new SQLSource(_sqlSourcesByPackage.get().get(packageName).getPath());
      if(ddlFile.getTypeNames().contains(typeName)) {
        return TypeSystem.getOrCreateTypeReference(new SQLType(this, fullyQualifiedName));
      }
    }
    return null;
  }

  @Override
  public Set<? extends CharSequence> getAllNamespaces() {
    return Collections.emptySet();
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
  public void refreshedNamespace(String s, IDirectory iDirectory, RefreshKind refreshKind) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean hasNamespace(String s) {
    return false;
  }

  @Override
  public Set<String> computeTypeNames() {
    Set<String> result = new HashSet<>();
    for(String pkg : _sqlSourcesByPackage.get().keySet()) {
      for(String typeName : _sqlSourcesByPackage.get().get(pkg).getTypeNames()) {
        result.add(pkg + '.' + typeName);
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

}
