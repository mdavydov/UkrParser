/*
 * Created on 11/02/2005
 *
 */
package org.dts.spell.finder;

import java.util.regex.Matcher;

/**
 * TODO : Cuando se porte todo al 5.0 Utilizar un template para el StringBuilder.
 * 
 * @author DreamTangerine
 *
 */
public class StringBufferWordFinder extends CharSequenceWordFinder
{
  private StringBuffer buffer ;
  
  /**
   * @param matcher
   */
  public StringBufferWordFinder(StringBuffer text, Matcher matcher)
  {
    super(matcher) ;
    setBuffer(text) ;
  }

  /**
   * @param text
   * @param spaceChars
   */
  public StringBufferWordFinder(StringBuffer text, String spaceChars)
  {
    super(text, spaceChars) ;
    setBuffer(text) ;    
  }

  /**
   * @param text
   */
  public StringBufferWordFinder(StringBuffer text)
  {
    super(text) ;
    setBuffer(text) ;    
  }

  protected void replace(String newWord, Word currentWord)
  {
    buffer.replace(currentWord.getStart(), currentWord.getEnd(), newWord) ;
    
    setBuffer(buffer) ;
  }
  
  private void setBuffer(StringBuffer buffer)
  {
    Matcher matcher = getMatcher() ;
    
    matcher.reset(buffer) ;
    this.buffer = buffer ;
  }
}
