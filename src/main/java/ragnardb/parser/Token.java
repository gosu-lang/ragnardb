package ragnardb.parser;


public class Token
{

  private TokenType _type;

  private long _longNumber;
  private double _doubleNumber;
  private String _text;
  private int _line;
  private int _col;
  private String _casedText;

  public Token(TokenType type, int line, int col) {
    _type = type;
    _line = line;
    _col = col;
  }

  public String getText() {
    return _text;
  }

  public void setText(String text) {
    _text = text;
  }

  public int getLine() {
    return _line;
  }

  public int getCol() {
    return _col;
  }

  public TokenType getType() {
    return _type;
  }

  public void setDoubleNumber(double doubleNumber) {
    _doubleNumber = doubleNumber;
  }

  public double getDoubleNumber() {
    return _doubleNumber;
  }

  public long getLongNumber() {
    return _longNumber;
  }

  public void setLongNumber(long longNumber) {
    _longNumber = longNumber;
  }

  public String getCasedText(){
    return _casedText;
  }

  public void setCasedText(String text){
    _casedText = text;
  }



  @Override
  public String toString() {
    return _type.toString();
  }
}
