package org.dts.spell.finder ;

/**
 * <p>
 * An interface for iterates through the words.
 * </p>
 * 
 * <p>
 * When the object is instantiated, and before the first call to <CODE>next()
 * </CODE> is made, the following methods should throw a <CODE>
 * WordNotFoundException</CODE>:<br>
 * <CODE>current()</CODE>,<CODE>startsSentence()</CODE> and <CODE>
 * replace()</CODE>.
 * </p>
 * 
 * <p>
 * A call to <CODE>next()</CODE> when <CODE>hasMoreWords()</CODE> returns
 * false should throw a <CODE>WordNotFoundException</CODE>.
 * </p>
 * 
 * @author Jason Height (jheight@chariot.net.au)
 * @author DreamTangerine (DreamTangerine@hotmail.com)
 */

public interface WordFinder
{
  /**
   * This method should return the Word object representing the current word in
   * the iteration. This method should not affect the state of the WordFinder
   * object.
   * 
   * @return the current Word object.
   * @throws WordNotFoundException
   *           current word has not yet been set.
   */
  public Word current() ;

  /**
   * Tests the finder to see if any more words are available.
   * 
   * @return true if more words are available.
   */
  public boolean hasNext() ;

  /**
   * This method should return the Word object representing the next word in the
   * iteration (the first word if next() has not yet been called.)
   * 
   * @return the next Word in the iteration.
   * @throws WordNotFoundException
   *           search string contains no more words.
   */
  public Word next() ;

  /**
   * This method should replace the current Word object with a new string
   * 
   * @param newWord
   *          the word to replace the current word with.
   * @throws WordNotFoundException
   *           current word has not yet been set.
   */
  public void replace(String newWord) ;
}
