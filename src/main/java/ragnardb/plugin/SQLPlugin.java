package ragnardb.plugin;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.internal.gosu.util.StringUtil;
import gw.lang.reflect.IType;
import gw.lang.reflect.RefreshKind;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;

import java.util.*;

public class SQLPlugin extends TypeLoaderBase {

  private static final String FILE_EXTENSION = ".ddl";

  Map<String, ISQLSource> _sqlSourcesByPackage;

  public SQLPlugin(IModule module) {
    super(module);
    List<Pair<String, IFile>> ddlFiles = module.getFileRepository().findAllFilesByExtension(FILE_EXTENSION);
    final int initialCapacity = ddlFiles.size();
    Map<String, ISQLSource> result = new HashMap<>(initialCapacity);

    for(Pair<String, IFile> pair : ddlFiles) {
      String fileName = pair.getFirst();
      String packageName = fileName.substring(0, fileName.length() - FILE_EXTENSION.length()).replace('/', '.');
      ISQLSource sqlSource = new SQLSource(pair.getSecond().getPath());
      Set<String> typeNames = sqlSource.getTypeNames();

      boolean allTypeNamesAreValid = true;
      for(String typeName : typeNames) {
        String fullyQualifiedName = packageName + '.' + typeName;
        if(!isValidTypeName(fullyQualifiedName)) {
          allTypeNamesAreValid = false;
        }
        if(allTypeNamesAreValid) {
          result.put(packageName, sqlSource);
        }
      }
    }

    _sqlSourcesByPackage = result; //TODO replace with lockinglazyvar impl

  }

  @Override
  public IType getType(final String fullyQualifiedName) {
    String[] packagesAndType = fullyQualifiedName.split("\\.");
    String[] packages = Arrays.copyOfRange(packagesAndType, 0, packagesAndType.length - 1);
    String typeName = packagesAndType[packagesAndType.length - 1];
    String packageName = String.join(".", packages);

    if(_sqlSourcesByPackage.keySet().contains(packageName)) {
      ISQLSource ddlFile = new SQLSource(_sqlSourcesByPackage.get(packageName).getPath());
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
