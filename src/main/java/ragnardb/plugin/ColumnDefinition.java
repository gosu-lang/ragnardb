package ragnardb.plugin;

public class ColumnDefinition
{
  private String _columnName;
  private int _sqlType; // see java.sql.Types

  public String getColumnName()
  {
    return _columnName;
  }

  public void setColumnName( String columnName )
  {
    _columnName = columnName;
  }

  public int getSQLType()
  {
    return _sqlType;
  }

  public void getSQLType( int sqlType )
  {
    _sqlType = sqlType;
  }
}
