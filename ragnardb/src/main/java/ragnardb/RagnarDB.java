package ragnardb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RagnarDB
{
  private static String g_DBURL = "";

  public static void setDBUrl(String url) {
    g_DBURL = url;
  }

  public static String getDBUrl() {
    return g_DBURL;
  }

  public static Connection getConnection() throws SQLException
  {
    return DriverManager.getConnection(g_DBURL);
  }

  public static boolean execStatement( String setup ) throws SQLException
  {
    Connection conn = getConnection();
    PreparedStatement stmt = conn.prepareStatement( setup );
    return stmt.execute();
  }

  public static int count( String tableName ) throws SQLException
  {
    Connection conn = getConnection();
    PreparedStatement stmt = conn.prepareStatement( "SELECT COUNT(1) FROM " + tableName );
    ResultSet resultSet = stmt.executeQuery();
    resultSet.next();
    return resultSet.getInt( 1 );
  }
}
