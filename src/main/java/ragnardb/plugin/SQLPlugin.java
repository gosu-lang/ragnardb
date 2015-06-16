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

    //private Set<IResource> _sources; //TODO populate set of all DDL files on disk? Use IFile?
    //private Map<ISQLSource, Set<String>> _sqlTypeNames = new HashMap<>();
    //private Map<String, Set<ISQLSource>> _sqlSourcesByPackage = new HashMap<>(); //package corresponds to fqn
    Map<String, IFile> _sqlSourcesByPackage;

    public SQLPlugin(IModule module) {
      super(module);
      //_sources = new HashSet<>();
      List<Pair<String, IFile>> ddlFiles = module.getFileRepository().findAllFilesByExtension(FILE_EXTENSION);
      final int initialCapacity = ddlFiles.size();
      Map<String, IFile> result = new HashMap<>(initialCapacity);

      for(Pair<String, IFile> pair : ddlFiles) {
        String fileName = pair.getFirst();
        String packageName = fileName.substring(0, fileName.length() - FILE_EXTENSION.length()).replace('/', '.');
        Set<String> tableNames = new SQLSource(pair.getSecond()).getTypeNames();
        for(String tableName : tableNames) {
          String fullyQualifiedName = packageName + '.' + tableName;
          if (isValidTypeName(fullyQualifiedName)) {
            result.put(fullyQualifiedName, pair.getSecond());
          }
        }
      }

      _sqlSourcesByPackage = result; //TODO replace with lockinglazyvar impl


      //TODO now cache everything

    }

    @Override
    public IType getType(String fullyQualifiedName) {

      if(_sqlSourcesByPackage.keySet().contains(fullyQualifiedName)) {
        return TypeSystem.getOrCreateTypeReference(new SQLType(this, fullyQualifiedName));
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
    if (nameParts == null || nameParts.isEmpty()) {
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
