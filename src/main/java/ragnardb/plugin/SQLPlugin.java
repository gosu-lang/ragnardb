package ragnardb.plugin;

import gw.fs.IDirectory;
import gw.internal.gosu.util.StringUtil;
import gw.lang.reflect.IType;
import gw.lang.reflect.RefreshKind;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;
import gw.util.concurrent.LockingLazyVar;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
  }

  /**
   * Populates _sqlSources from the provided IModule
   * @param module Find *.ddl files in this module
   */
  private void initSources(IModule module) {
    //leverage Streams API to basically cast Pair<String, IFile> to Pair<String, ISQLSource> in place
    _sqlSources = module.getFileRepository().findAllFilesByExtension(FILE_EXTENSION)
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
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public List<String> getHandledPrefixes() {
    return Collections.emptyList();
  }

  @Override
  public boolean handlesNonPrefixLoads() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void refreshedNamespace(String s, IDirectory iDirectory, RefreshKind refreshKind) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean hasNamespace(String s) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Set<String> computeTypeNames() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  private static boolean isValidTypeName(String typeName) {
    List<String> nameParts = StringUtil.tokenizeToList(typeName, '.');
    if(nameParts == null || nameParts.isEmpty()) {
      return false;
    }
//    for (String namePart : nameParts) {
//      if (!PropertyNode.isGosuIdentifier(namePart)) {
//        return false;
//      }
//    }
    return true;
  }

}
