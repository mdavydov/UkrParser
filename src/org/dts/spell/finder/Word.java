package org.dts.spell.finder ;

/**
 * An inmutable Word, that represent a Word in th WordFinder. Based in Jazzy
 * work.
 * 
 * @see org.dts.spell.finder.WordFinder
 * @author DreamTangerine
 *  
 */
public class Word
{
  private int start ;

  private String text ;

  private boolean isStart ;

  /**
   * Creates a new Word object.
   * 
   * @param text
   *          the String representing the word.
   *          
   * @param start
   *          the start index of the word.
   *          
   * @param isStart
   * 					Set if the word is the beginnig of a sentence.
   */
  public Word(String text, int start, boolean isStart)
  {
    this.text = text ;
    this.start = start ;
    this.isStart = isStart ;
  }

  /**
   * @return the end index of the word.
   */
  public int getEnd()
  {
    return getStart() + length();
  }

  /**
   * @return the start index.
   */
  public int getStart()
  {
    return start ;
  }

  /**
   * @return the String representing the word.
   */
  public String getText()
  {
    return text ;
  }
  
  /**
   * @return the length of the word.
   */
  public int length()
  {
    return text.length() ;
  }

  /**
   * @return the text representing the word.
   */
  public String toString()
  {
    return text ;
  }

  /**
   * Nos dice si esta palabra es inicio de frase.
   * 
   * @return true if the word starts a new sentence.
   */
  public boolean isStartOfSentence()
  {
    return isStart ;
  }
  
  /**
   * Nos dice si está bien el primer carácter. Puesto que al principio de frase debe
   * de ser mayúsculas. 
   * 
   * @return Si está bien el primer carácter de la palabra.
   */
  public boolean isCorrectFirstChar()
  {
    return !isStartOfSentence() || Character.isUpperCase(text.charAt(0)) ; 
  }
}
