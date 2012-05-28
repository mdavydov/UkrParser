/*
 * Created on 04/01/2005
 *
 */
package org.dts.spell ;

import java.util.HashMap;
import java.util.HashSet;

import org.dts.spell.dictionary.SpellDictionary ;
import org.dts.spell.event.ErrorCountListener ;
import org.dts.spell.event.FindSpellCheckErrorListener ;
import org.dts.spell.event.SpellCheckEvent ;
import org.dts.spell.event.SpellCheckListener ;
import org.dts.spell.finder.Word ;
import org.dts.spell.finder.WordFinder ;
import org.dts.spell.finder.CharSequenceWordFinder ;

/**
 * @author DreamTangerine
 *  
 */
public class SpellChecker
{
  private HashSet ignore = new HashSet() ;
  private HashMap replace = new HashMap() ;  
  
  public SpellChecker(SpellDictionary dictionary)
  {
    this.dictionary = dictionary ;
  }

  private SpellDictionary dictionary ;

  /**
   * @param dictionary
   *          The dictionary to set.
   */
  public void setDictionary(SpellDictionary dictionary)
  {
    this.dictionary = dictionary ;
  }

  public SpellDictionary getDictionary()
  {
    return dictionary ;
  }

  /**
   * This method add teh word to the ignore table.
   * 
   * @param word
   */
  public void addIgnore(String word)
  {
    ignore.add(word.trim()) ;
  }
  
  public void resetIgnore()
  {
    ignore.clear() ;
  }
  
  /**
   * This method add a word to the replace table.
   * 
   * @param oldWord old word to replace.
   * @param newWord new word to replace
   */
  public void addReplace(String oldWord, String newWord)
  {
    replace.put(oldWord.trim(), newWord.trim()) ;
  }
  
  public void resetReplace()
  {
    replace.clear() ;  
  }
  
  public void setCaseSensitive(boolean sensitive)
  {
    caseSensitive = sensitive ;
  }
  
  public boolean isCaseSensitive()
  {
    return caseSensitive ;
  }
  
  private boolean checkCase(Word word)
  {
    return !isCaseSensitive() || word.isCorrectFirstChar() ;    
  }
  
  private boolean caseSensitive = true ; 
  
  /**
   * @return true si todo ha ido bien y no tiene errores.
   *  
   */
  public boolean isCorrect(CharSequence txt)
  {
    return isCorrect(new CharSequenceWordFinder(txt)) ;
  }

  public boolean isCorrect(WordFinder finder)
  {
    return check(finder, new FindSpellCheckErrorListener()) ;
  }

  public Word checkSpell(CharSequence txt)
  {
    return checkSpell(new CharSequenceWordFinder(txt)) ;
  }

  private static final FindSpellCheckErrorListener ERROR_FIND_LISTENER = new FindSpellCheckErrorListener() ;

  public Word checkSpell(WordFinder finder)
  {
    check(finder, ERROR_FIND_LISTENER) ;

    return ERROR_FIND_LISTENER.getInvalidWord() ;
  }

  private static final ErrorCountListener ERROR_COUNT_LISTENER = new ErrorCountListener() ;

  public int getErrorCount(CharSequence txt)
  {
    return getErrorCount(new CharSequenceWordFinder(txt)) ;
  }

  public int getErrorCount(WordFinder finder)
  {
    check(finder, ERROR_COUNT_LISTENER) ;

    return ERROR_COUNT_LISTENER.getErrorsCount() ;
  }

  public boolean check(WordFinder finder, SpellCheckListener listener)
  {
    boolean result = true ;
    boolean exit = false ;
    SpellDictionary dict = getDictionary() ;

    listener.beginChecking(new SpellCheckEvent(this, finder)) ;

    while (!exit && finder.hasNext())
    {
      Word word = finder.next() ;
      String wordText = word.getText() ;
      String newString = (String) replace.get(wordText) ; 

      if (null != newString)
      {
        finder.replace(newString) ;
      }
      else if (!ignore.contains(wordText))
      {
	      if (!dict.isCorrect(word.getText()) || !checkCase(word))
	      {
	        SpellCheckEvent event = new SpellCheckEvent(this, finder) ;
	
	        listener.spellingError(event) ;
	
	        result = false ;
	        exit = event.isCancel() ;
	      }
      }
    }

    listener.endChecking(new SpellCheckEvent(this, finder)) ;

    return result ;
  }
}
