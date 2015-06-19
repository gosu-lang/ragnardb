package ragnardb.parser;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
  ABORT("abort"), ACTION("action"), ADD("add"), AFTER("after"), ALL("all"), ALTER("alter"), ANALYZE("analyze"),
  AND("and"), ANY("any"), AS("as"), ASC("asc"), ATTACH("attach"), AUTOINCREMENT("autoincrement"), BEFORE("before"),
  BEGIN("begin"), BETWEEN("between"), BY("by"), CASCADE("cascade"), CASE("case"), CAST("cast"), CHECK("check"),
  COLLATE("collate"), COLUMN("column"), COMMIT("commit"), CONFLICT("conflict"), CONSTRAINT("constraint"),
  CREATE("create"), CROSS("cross"), CURRENT_DATE("current_date"), CURRENT_TIME("current_time"),
  CURRENT_TIMESTAMP("current_timestamp"), DATABASE("database"), DEFAULT("default"), DEFERRABLE("deferrable"),
  DEFERRED("deferred"), DELETE("delete"), DESC("desc"), DETACH("detach"), DISTINCT("distinct"), DROP("drop"),
  EACH("each"), ELSE("else"), END("end"), ESCAPE("escape"), EXCEPT("except"), EXCLUSIVE("exclusive"),
  EXISTS("exists"), EXPLAIN("explain"), FAIL("fail"), FOR("for"), FOREIGN("foreign"), FROM("from"), FULL("full"),
  GLOB("glob"), GROUP("group"), HAVING("having"), IF("if"), IGNORE("ignore"), IMMEDIATE("immediate"), IN("in"),
  INDEX("index"), INDEXED("indexed"), INITIALLY("initially"), INNER("inner"), INSERT("insert"), INSTEAD("instead"),
  INTERSECT("intersect"), INTO("into"), IS("is"), ISNULL("isnull"), JOIN("join"), KEY("key"), LEFT("left"),
  LIKE("like"), LIMIT("limit"), MATCH("match"), NATURAL("natural"), NO("no"), NOT("not"), NOTNULL("notnull"),
  NULL("null"), OF("of"), OFFSET("offset"), ON("on"), OR("or"), ORDER("order"), OUTER("outer"), PLAN("plan"),
  PRAGMA("pragma"), PRIMARY("primary"), QUERY("query"), RAISE("raise"), RECURSIVE("recursive"),
  REFERENCES("references"), REGEXP("regexp"), REINDEX("reindex"), RELEASE("release"), RENAME("rename"),
  REPLACE("replace"), RESTRICT("restrict"), RIGHT("right"), ROLLBACK("rollback"), ROW("row"), ROWID("rowid"),
  SAVEPOINT("savepoint"), SELECT("select"), SET("set"), SOME("some"), TABLE("table"), TEMP("temp"),
  TEMPORARY("temporary"), THEN("then"), TO("to"), TRANSACTION("transaction"), TRIGGER("trigger"), UNION("union"),
  UNIQUE("unique"), UPDATE("update"), USING("using"), VACUUM("vacuum"), VALUES("values"), VIEW("view"),
  VIRTUAL("virtual"), WHEN("when"), WHERE("where"), WITH("with"), WITHOUT("without"),
  IDENT("identifier"), LONG("long"), DOUBLE("double"), IDENTITY("identity"), HASH("hash"),
  LPAREN("("), RPAREN(")"), PLUS("+"), MINUS("-"), DOT("."), COMMA(","), SEMI(";"), SLASH("/"),TIMES("*"),MOD("%"),
  BAR("||"),EQ("="),NEQ("!="),GT(">"),LT("<"),GTE(">="),LTE("<="),OVL("&&"), QUESTION("?"),
  EOF("End of file"), UNKNOWN,;

  String _name;
  private static Map<String, TokenType> keyword2TokType;

  private TokenType(String str) {
    _name = str;
  }
  private TokenType() {}

  public static TokenType find(String name) {
    if(keyword2TokType == null) {
      keyword2TokType = new HashMap<String, TokenType>();
      for(TokenType t : values()) {
        keyword2TokType.put(t._name, t);
      }
    }
    return keyword2TokType.get(name);
  }

  public String getName() {
    return _name;
  }
}
