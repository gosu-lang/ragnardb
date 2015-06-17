package ragnardb.plugin;

public class ColumnDefinition {
  private String _columnName;
  private int _sqlType; // see java.sql.Types: http://docs.oracle.com/javase/8/docs/api/index.html?java/sql/Types.html

  public ColumnDefinition(String columnName, int sqlType) {
    _columnName = columnName;
    _sqlType = sqlType;
  }

  public String getColumnName() {
    return _columnName;
  }

  public void setColumnName(String columnName) {
    _columnName = columnName;
  }

  public int getSQLType() {
    return _sqlType;
  }

  public void setSQLType(int sqlType) {
    _sqlType = sqlType;
  }
}
