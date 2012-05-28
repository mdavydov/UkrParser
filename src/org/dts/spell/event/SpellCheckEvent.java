package org.dts.spell.event ;

import java.util.EventObject ;

import org.dts.spell.SpellChecker ;
import org.dts.spell.dictionary.SpellDictionary ;
import org.dts.spell.finder.Word ;
import org.dts.spell.finder.WordFinder;

/**
 * This event is fired off by the SpellChecker and is passed to the registered
 * SpellCheckListeners
 * 
 * @author DreamTangerine (DreamTangerine@hotmail.com)
 */
public class SpellCheckEvent extends EventObject
{
  private WordFinder finder ;  

  private boolean cancelSpell = false ;

  public SpellCheckEvent(SpellChecker spellChecker, WordFinder finder)
  {
    super(spellChecker) ;

    this.finder = finder ;
  }

  public SpellChecker getSpellChecker()
  {
    return (SpellChecker) getSource() ;
  }

  public SpellDictionary getDictionary()
  {
    return this.getSpellChecker().getDictionary() ;
  }

  public WordFinder getWordFinder()
  {
    return finder ;
  }
  
  /** 
   * Returns the currently misspelt word 
   * 
   */
  public Word getInvalidWord()
  {
    return getWordFinder().current() ;
  }

  /**
   * Set the action to terminate processing of the spellchecker.
   */
  public void cancel()
  {
    cancelSpell = true ;
  }

  public boolean isCancel()
  {
    return cancelSpell ;
  }
}