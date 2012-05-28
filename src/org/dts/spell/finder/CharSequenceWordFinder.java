/*
 * Created on 04/02/2005
 *
 */
package org.dts.spell.finder ;

import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

/**
 * @author DreamTangerine
 *  
 */
public class CharSequenceWordFinder extends AbstractWordFinder
{
  /**
   * Represent all the space chars.
   */
  public static final String SPACE_CHARS = "\\s<>=,.():" ;

  /**
   * The Matcher of word. Ideally it skips the spaces.
   */
  private Matcher matcher ;
  
  /**
   * @return Returns the matcher.
   */
  protected Matcher getMatcher()
  {
    return matcher ;
  }
  
  public CharSequenceWordFinder(Matcher matcher)
  {
    this.matcher = matcher ;

    init() ;
  }

  public CharSequenceWordFinder(CharSequence text, String spaceChars)
  {
    this(createExcludeMatcher(text, spaceChars)) ;
  }

  public CharSequenceWordFinder(CharSequence text)
  {
    this(text, SPACE_CHARS) ;
  }

  /**
   * Esta función crea un <code>{@link java.util.regex.Matcher Matcher}</code>
   * para buscar las palabras dentro de la (
   * <code>{@link java.lang.CharSequence CharSequence}</code>) dada.
   * 
   * @param text
   *          El texto donde buscar las palabras.
   * 
   * @param regexp
   *          La expresión regular a utilizar y que hará que se salten los
   *          espacios.
   * 
   * @param flags
   *          Los <em>flags</em> que se utilizan en
   *          <code>{@link java.util.regex.Pattern#compile(java.lang.String, int) Pattern.compile}</code>.
   * 
   * @return El <code>{@link java.util.regex.Matcher Matcher}</code> para
   *         obtener las palabras.
   */
  public static Matcher createMatcher(
      CharSequence text,
      String regexp,
      int flags)
  {
    return Pattern.compile(regexp, flags).matcher(text) ;
  }

  /**
   * Esta función crea un <code>{@link java.util.regex.Matcher Matcher}</code>
   * para buscar las palabras dentro de la (
   * <code>{@link java.lang.CharSequence CharSequence}</code>) dada.
   * 
   * @param text
   *          El texto donde buscar las palabras.
   * 
   * @param regexp
   *          La expresión regular a utilizar y que hará que se salten los
   *          espacios.
   * 
   * @return El <code>{@link java.util.regex.Matcher Matcher}</code> para
   *         obtener las palabras.
   */
  public static Matcher createMatcher(CharSequence text, String regexp)
  {
    return Pattern.compile(regexp).matcher(text) ;
  }

  /**
   * Esta función crea un <code>{@link java.util.regex.Matcher Matcher}</code>
   * para buscar las palabras dentro de la (
   * <code>{@link java.lang.CharSequence CharSequence}</code>) dada. Haciendo
   * que el texto que se le pasa sea el texto a excluir, es decir la expresón
   * deberían de ser los caracteres que forman los espacios entre las palabras.
   * 
   * @param text
   *          El texto donde buscar las palabras.
   * 
   * @param chars
   *          La expresión regular a utilizar y que indica cuales son los
   *          espacios entre las palabras. Es decir los carácteres que no forman
   *          parte de una palabra.
   * 
   * @return El <code>{@link java.util.regex.Matcher Matcher}</code> para
   *         obtener las palabras.
   * 
   * @see org.dts.spell.finder.CharSequenceWordFinder#SPACE_CHARS SPACE_CHARS
   */

  public static Matcher createExcludeMatcher(CharSequence text, String chars)
  {
    return Pattern.compile("[^" + chars + "]+").matcher(text) ;
      
      
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.finder.AbstractWordFinder#next(org.dts.spell.finder.Word)
   */
  protected Word next(Word currentWord)
  {
    int last ;

    if (currentWord == null)
      last = 0 ;
    else
      last = currentWord.getEnd() ;

    if (!matcher.find(last))
      return null ;
    else
      return new Word(
          matcher.group(), 
          matcher.start(), 
          last == 0 || currentWord.getText().charAt(currentWord.length() - 1) == '.') ;
  }

  /**
   * This method throw a UnsupportedOperationException because a String is
   * inmutable object. If you want replace the word see StringBufferWordFinder
   * or StringBuilderWordFinder.
   *  
   */
  protected void replace(String newWord, Word currentWord)
  {
    throw new UnsupportedOperationException() ;
  }
}
