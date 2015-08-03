package ragnardb.parser.ast;

import java.util.ArrayList;

/**
 * Created by klu on 8/3/2015.
 */
public class EmptyType extends SQL{
  /*
  This entire class is empty: in the event that the parser eats the entire input (possible in certain cases),
  we return a totally empty type. We resolve this problem in the plugin by returning nothing.
   */

  public EmptyType(){}

}
