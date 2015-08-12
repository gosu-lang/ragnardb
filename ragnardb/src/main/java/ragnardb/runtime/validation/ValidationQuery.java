package ragnardb.runtime.validation;

import ragnardb.RagnarDB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by klu on 8/11/2015.
 */
public class ValidationQuery {
  private String sql;
  private List<Object> bindVals;

  public ValidationQuery(String s, List<Object> vals){
    sql = s;
    bindVals = vals;
  }

  public boolean unique(){
    try {
      PreparedStatement preparedStatement = RagnarDB.prepareStatement(sql, bindVals);
      ResultSet resultSet = preparedStatement.executeQuery();
      return !resultSet.next();
    } catch (SQLException e) {
      return false;
    }

  }


}
