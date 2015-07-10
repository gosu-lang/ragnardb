package ragnardb.plugin;

import java.util.List;

/**
 * Created by klu on 7/9/2015.
 */
public interface ISQLQueryResultType extends ISQLTypeBase{
  List<ColumnDefinition> getColumnDefinitions();

}
