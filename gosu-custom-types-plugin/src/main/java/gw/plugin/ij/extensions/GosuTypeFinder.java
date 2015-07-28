package gw.plugin.ij.extensions;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import gw.lang.reflect.INamespaceType;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.TypeName;
import gw.lang.reflect.java.IJavaType;
import gw.plugin.ij.lang.psi.impl.CustomPsiClassCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class GosuTypeFinder extends PsiElementFinder
{
  private static GosuTypeFinder INSTANCE = null;

  public GosuTypeFinder()
  {
    INSTANCE = this;
  }

  @Nullable
  @Override
  public PsiClass findClass( @NotNull String s, @NotNull GlobalSearchScope globalSearchScope )
  {
    TypeSystem.pushGlobalModule();
    try
    {
      IType type = TypeSystem.getByFullNameIfValid( s );
      if( acceptType( type ) )
      {
        return CustomPsiClassCache.instance().getPsiClass( type );
      }
      return null;
    }
    finally
    {
      TypeSystem.popGlobalModule();
    }
  }

  @NotNull
  @Override
  public PsiClass[] getClasses( @NotNull PsiPackage psiPackage, @NotNull GlobalSearchScope scope )
  {
    TypeSystem.pushGlobalModule();
    try
    {
      INamespaceType namespace = TypeSystem.getNamespace( psiPackage.getQualifiedName() );
      if( namespace != null )
      {
        PsiManager manager = PsiManagerImpl.getInstance( (Project)namespace.getModule().getExecutionEnvironment().getProject().getNativeProject() );
        Set<PsiClass> types = new HashSet<PsiClass>();
        Set<TypeName> children = namespace.getChildren( null );
        for( TypeName tn : children )
        {
          IType type = TypeSystem.getByFullNameIfValidNoJava( tn.name );
          if( acceptType( type ) )
          {
            PsiClass psiClass = CustomPsiClassCache.instance().getPsiClass( type );
            if( psiClass != null )
            {
              types.add( psiClass );
            }
          }
        }
        return types.toArray( new PsiClass[types.size()] );
      }
      return new PsiClass[0];
    }
    finally
    {
      TypeSystem.popGlobalModule();
    }
  }

  @NotNull
  @Override
  public PsiClass[] getClasses( @Nullable String className, @NotNull PsiPackage psiPackage, @NotNull GlobalSearchScope scope )
  {
    return super.getClasses( className, psiPackage, scope );
  }

  @NotNull
  @Override
  public PsiPackage[] getSubPackages( @NotNull PsiPackage psiPackage, @NotNull GlobalSearchScope scope )
  {
    TypeSystem.pushGlobalModule();
    try
    {
      String parentPackage = psiPackage.getQualifiedName();
      INamespaceType namespace = TypeSystem.getNamespace( parentPackage );
      if( namespace != null )
      {
        PsiManager manager = PsiManagerImpl.getInstance( (Project)namespace.getModule().getExecutionEnvironment().getProject().getNativeProject() );
        List<PsiPackage> children = new ArrayList<>();
        for( TypeName child: namespace.getChildren( null ) )
        {
          if( child.kind == TypeName.Kind.NAMESPACE )
          {
            children.add( new NonDirectoryPackage( manager, parentPackage + '.' + child.name ) );
          }
        }
        return children.toArray( new PsiPackage[children.size()] );
      }
    }
    finally
    {
      TypeSystem.popGlobalModule();
    }
    return super.getSubPackages( psiPackage, scope );
  }

  @Override
  public boolean processPackageDirectories( @NotNull PsiPackage psiPackage, @NotNull GlobalSearchScope scope, @NotNull Processor<PsiDirectory> consumer )
  {
    return super.processPackageDirectories( psiPackage, scope, consumer );
  }

  @Nullable
  @Override
  public PsiPackage findPackage( @NotNull String qualifiedName )
  {
//    PsiPackage pkg = JavaPsiFacadeUtil.findPackage( (Project)TypeSystem.getGlobalModule().getExecutionEnvironment().getProject().getNativeProject(), qualifiedName );
//    if( pkg != null )
//    {
//      return null;
//    }

    TypeSystem.pushGlobalModule();
    try
    {
      INamespaceType namespace = TypeSystem.getNamespace( qualifiedName );
      if( namespace != null )
      {
        // If the namespace comes from a non-default typeloader, we assume it is a "virtual" namespace
        // and that it does not reflect a directory tree like a normal java package, otherwise it would
        // be resolved by the DefaultTypeloader

        PsiManager manager = PsiManagerImpl.getInstance( (Project)namespace.getModule().getExecutionEnvironment().getProject().getNativeProject() );
        return new NonDirectoryPackage( manager, namespace.getName() );
      }
    }
    finally
    {
      TypeSystem.popGlobalModule();
    }

    return null;
  }


  @NotNull
  @Override
  public PsiClass[] findClasses( @NotNull String s, @NotNull GlobalSearchScope globalSearchScope )
  {
    PsiClass gsType = findClass( s, globalSearchScope );
    if( gsType != null ) {
      return new PsiClass[] {gsType};
    }
    return new PsiClass[0];
  }

  private boolean acceptType( IType type )
  {
    //## todo: accept only types with extensions specified in the Manifest
    return type != null && !(type instanceof IJavaType); // && !(type instanceof IGosuClass);
  }

  public static GosuTypeFinder instance()
  {
    return INSTANCE;
  }
}
