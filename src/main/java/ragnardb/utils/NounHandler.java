package ragnardb.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by klu on 6/17/2015.
 * This class is used for singularizing and converting names to things that look like java class names.
 */
public class NounHandler {
  private String input;
  private HashMap<String, String> specialPlurals = new HashMap<>();


  public NounHandler(String in) {
    input = in;
    exceptionalPlurals();
  }

  public static void main(String[] args) throws IOException {
    //Testing
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String input;
    input = br.readLine();
    NounHandler n = new NounHandler(input);
    System.out.println(n.getSingular());


  }

  /**
   * Gets the final noun in a compoundName (ie compoundName goes to Name)
   *
   * @param words List of 'Words' in the initial string
   * @return the final noun in the name
   */
  private String getFinalWord(String[] words) {
    return words[words.length - 1];
  }

  private boolean isConsonant(char c) {
    return !Character.toString(c).matches("[aeiou]");
  }

  /**
   * Gets all the distinct words in a compoundName
   *
   * @param word Initial word
   * @return an array of all the nouns/words
   */
  private String[] getWords(String word) {
    String[] finalSplit = new String[1];
    String[] initialSplit = word.split("[^A-Za-z0-9']");
    ArrayList<String> currentList = new ArrayList<>(Arrays.asList(initialSplit));

    /*splits on capital letters and numbers*/
    int k = 0;
    for (int i = 0; i < initialSplit.length; i++) {
      String currentWord = initialSplit[i];
      for (int j = 0; j < currentWord.length(); j++) {
        char c = currentWord.charAt(j);
        if ('A' <= c && 'Z' >= c) {
          currentList.add(i + k + 1, currentList.get(i + k).substring(j));
          currentList.set(i + k, currentList.get(i + k).substring(0, j));
          k++;
        }
        if (Character.isDigit(c)) {
          currentList.add(i + k + 1, currentList.get(i + k).substring(j));
          currentList.set(i + k, currentList.get(i + k).substring(0, j));
          k++;
        }
      }
    }

    finalSplit = currentList.toArray(finalSplit);
    return finalSplit;
  }

  private static String[] getWordsStatic(String word) {
    String[] finalSplit = new String[1];
    String[] initialSplit = word.split("[^A-Za-z0-9']");
    ArrayList<String> currentList = new ArrayList<>(Arrays.asList(initialSplit));

    /*splits on capital letters and numbers*/
    int k = 0;
    for (int i = 0; i < initialSplit.length; i++) {
      String currentWord = initialSplit[i];
      for (int j = 0; j < currentWord.length(); j++) {
        char c = currentWord.charAt(j);
        if ('A' <= c && 'Z' >= c) {
          currentList.add(i + k + 1, currentList.get(i + k).substring(j));
          currentList.set(i + k, currentList.get(i + k).substring(0, j));
          k++;
        }
        if (Character.isDigit(c)) {
          currentList.add(i + k + 1, currentList.get(i + k).substring(j));
          currentList.set(i + k, currentList.get(i + k).substring(0, j));
          k++;
        }
      }
    }

    finalSplit = currentList.toArray(finalSplit);
    return finalSplit;
  }

  private String singularize(String word) {
    char[] chars = word.toCharArray();
    char last = chars[chars.length - 1];
    String current = word;
    try {
      Integer.parseInt(current);
      return current;
    }
    catch (Exception e){

    }

    if(current.length() == 1){
      return current.equals("s")?"":current;
    }
    for (Map.Entry<String, String> entry : specialPlurals.entrySet()) {
      String regexptest = "[A-Za-z]*" + entry.getKey();
      if (current.matches(regexptest)) {
        current = current.substring(0, current.length() - entry.getKey().length());
        current += entry.getValue();
      }
    }

    if (current.equals(word)) {
      if (last == 's') {
        current = current.substring(0, chars.length - 1);
      }
      try {
        last = chars[chars.length - 2];
        if (last == 'e') {
          char prev = chars[chars.length - 3];
          char p2 = chars[chars.length-4];
          if (prev == 'x' || prev == 's' || (prev == 'h' && (p2 == 's' || p2 == 'c'))) {
            current = current.substring(0, current.length() - 1);
          }
          if (prev == 'i') {
            current = current.substring(0, current.length() - 2);
            current += 'y';
          }
          if (prev == 'v') {
            current = current.substring(0, current.length() - 1);
            current += 'f';
            if (chars[chars.length - 4] == 'i') {
              current += 'e';
            }
          }
        }
        if (last == '\'') {
          current = current.substring(0, current.length() - 1);
        }
      } catch (ArrayIndexOutOfBoundsException e){

      }
    }
    return current;
  }

  private void exceptionalPlurals() {
    /*Handles really weird plural problems, populates specialPlurals*/
    specialPlurals.put("feet", "foot");
    specialPlurals.put("geese", "goose");
    specialPlurals.put("lice", "louse");
    specialPlurals.put("mice", "mouse");
    specialPlurals.put("teeth", "tooth");
    specialPlurals.put("men", "man");
    specialPlurals.put("children", "child");
    specialPlurals.put("brethren", "brother");
    specialPlurals.put("oxen", "ox");
    specialPlurals.put("caves", "cave");
    specialPlurals.put("saves", "save");
    specialPlurals.put("safes", "safe");
    specialPlurals.put("indices", "index");
    specialPlurals.put("matrices", "matrix");
    specialPlurals.put("vertices", "vertex");
    specialPlurals.put("axes", "axis");
    specialPlurals.put("geneses", "genesis");
    specialPlurals.put("nemeses", "nemesis");
    specialPlurals.put("crises", "crisis");
    specialPlurals.put("series", "series");
    specialPlurals.put("species", "species");
    specialPlurals.put("memoranda", "memorandum");
    specialPlurals.put("millenia", "millenium");
    specialPlurals.put("spectra", "spectrum");
    specialPlurals.put("alumni", "alumnus");
    specialPlurals.put("foci", "focus");
    specialPlurals.put("genera", "genus");
    specialPlurals.put("radii", "radius");
    specialPlurals.put("succubi", "succubus");
    specialPlurals.put("syllabi", "syllabus");
    specialPlurals.put("octopi", "octopus");
    specialPlurals.put("automata", "automaton");
    specialPlurals.put("criteria", "criterion");
    specialPlurals.put("phenomena", "phenomenon");
    specialPlurals.put("polyhedra", "polyhedron");
    specialPlurals.put("seraphim", "seraph");
    specialPlurals.put("cloves", "clove");
    specialPlurals.put("gloves", "glove");
    specialPlurals.put("abrasive", "abrasives");
    specialPlurals.put("adhesives", "adhesive");
    specialPlurals.put("administratives", "administrative");
    specialPlurals.put("affirmatives", "affirmative");
    specialPlurals.put("archives", "archive");
    specialPlurals.put("approves", "approve");
    specialPlurals.put("aves", "ave");
    specialPlurals.put("beehives", "beehive");
    specialPlurals.put("claves", "clave");
    specialPlurals.put("captives", "captive");
    specialPlurals.put("stove", "stoves");
    specialPlurals.put("coves", "cove");
    specialPlurals.put("curves", "curve");
    specialPlurals.put("raves", "rave");
    specialPlurals.put("kai", "The Creator!");
    specialPlurals.put("detectives", "detective");
    specialPlurals.put("eaves", "eave");
    specialPlurals.put("emissives", "emissive");
    specialPlurals.put("emulsives", "emulsive");
    specialPlurals.put("eves", "eve");
    specialPlurals.put("evolves", "evolve");
    specialPlurals.put("fives", "five");
    specialPlurals.put("glaives", "glaive");
    specialPlurals.put("loves", "love");
    specialPlurals.put("knaves", "knave");
    specialPlurals.put("olives", "olive");
    specialPlurals.put("shoves", "shove");
    specialPlurals.put("solves", "solve");
    specialPlurals.put("slaves", "slave");
    specialPlurals.put("sleeves", "sleeve");
    specialPlurals.put("stoves", "stove");
    specialPlurals.put("swerves", "swerve");
    specialPlurals.put("twelves", "twelve");
    specialPlurals.put("valves", "valve");
    specialPlurals.put("people", "person");
    specialPlurals.put("news", "news");
    specialPlurals.put("uises", "uise");
    specialPlurals.put("ases", "ase");
    specialPlurals.put("eses", "ese");
    specialPlurals.put("auses", "ause");
    specialPlurals.put("ouses", "ouse");
    specialPlurals.put("corpses", "corpse");
    specialPlurals.put("eases", "ease");
    specialPlurals.put("urses", "urse");
    specialPlurals.put("orses", "orse");
    specialPlurals.put("enses", "ense");
    specialPlurals.put("uses", "uses");
    specialPlurals.put("oses", "ose");
    specialPlurals.put("erses", "erse");

  }


  /**
   * Given a database table name, returns an equivalent Java class name.
   * @return A classname equivalent.
   */
  public String getSingular() {
    String[] strings = getWords(input);
    String finalword = getFinalWord(strings);
    String finalout = singularize(finalword);
    if (strings.length == 1) {
      return finalout;
    }
    String output = "";
    output += Character.toLowerCase(strings[0].charAt(0)) + (strings[0].length() > 1 ? strings[0].substring(1) : "");
    for (int i = 1; i < strings.length - 1; i++) {
      if(!strings[i].equals("")) {
        String nextpart = Character.toUpperCase(strings[i].charAt(0)) + (strings[i].length() > 1 ? strings[i].substring(1) : "");
        output += nextpart;
      }
    }
    output += Character.toUpperCase(finalout.charAt(0))+finalout.substring(1);
    return output;
  }

  public static String getCamelCased(String s) {
    if(s.equals("")){
      return s;
    }
    String[] strings = getWordsStatic(s);
    String finalword = strings[strings.length-1];
    if (strings.length == 1) {
      return finalword;
    }
    String output = "";
    output += strings[0].equals("")?"":Character.toLowerCase(strings[0].charAt(0)) + (strings[0].length() > 1 ? strings[0].substring(1) : "");
    for (int i = 1; i < strings.length - 1; i++) {
      if(!strings[i].equals("")) {
        String nextpart = Character.toUpperCase(strings[i].charAt(0)) + (strings[i].length() > 1 ? strings[i].substring(1) : "");
        output += nextpart;
      }
    }
    output += finalword.equals("")?"":(Character.toUpperCase(finalword.charAt(0))+finalword.substring(1));
    return output;
  }

  /**
   * Adds an exception to the singularizer. Only the base form is needed (ie between fighters
   * and firefighters, only adding fighters, fighter is necessary).
   * Please only use this method if the singularizer is incorrectly singularizing; the list of
   * exception words is already populated.
   * @param plural plural form
   * @param singular singular form
   */
  public void addException(String plural, String singular){
    specialPlurals.put(plural, singular);
  }
}
