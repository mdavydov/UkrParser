/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

/**
 * @author DreamTangerine
 *
 */
public class GuessWord
{
  GuessWord(String word, boolean allow)
  {
    this.word = word ;
    this.allow = allow ;
  }
  
  String word ;
  boolean allow ;
}
