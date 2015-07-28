package gw.plugin.ij.extensions;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiPackageImpl;

/**
 */
public class NonDirectoryPackage extends PsiPackageImpl
{
  private final String _name;

  public NonDirectoryPackage( PsiManager manager, String qualifiedName )
  {
    super( manager, qualifiedName );
    _name = qualifiedName;
  }

  @Override
  public PsiElement copy()
  {
    return new NonDirectoryPackage( getManager(), _name );
  }

  @Override
  public boolean isValid()
  {
    return true;
  }
}