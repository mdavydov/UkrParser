/*
 * Created on 24/02/2005
 *
 */
package org.dts.spell.swing.event;

import org.dts.spell.event.SpellCheckAdapter;
import org.dts.spell.event.SpellCheckEvent ;
import org.dts.spell.swing.JSpellDialog;

/**
 * @author DreamTangerine
 *  
 */
public class UISpellCheckListener extends SpellCheckAdapter
{
  /**
   * The current JSpellDialog
   */
  private JSpellDialog spellDialog ;

  /**
   * @return Returns the spellDialog.
   */
  public JSpellDialog getSpellDialog()
  {
    return spellDialog ;
  }

  /**
   * @param spellDialog
   *          The spellDialog to set.
   */
  public void setSpellDialog(JSpellDialog spellDialog)
  {
    this.spellDialog = spellDialog ;
  }

  /**
   *  
   */
  public UISpellCheckListener()
  {
    this(null) ;
  }

  /**
   * Create a UISpellCheckListener that show a JSpellDialog for each error that was 
   * found. Tou can pass a null JSpellDialog and the UISpellCheckListener will create
   * one for you. 
   * 
   * @param spellDialog The dialog to show it can be null.
   */
  public UISpellCheckListener(JSpellDialog spellDialog)
  {
    setSpellDialog(spellDialog) ;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#beginChecking(org.dts.spell.event.SpellCheckEvent)
   */
  public void beginChecking(SpellCheckEvent event)
  {
    if (getSpellDialog() == null)
      setSpellDialog(new JSpellDialog()) ;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#spellingError(org.dts.spell.event.SpellCheckEvent)
   */
  public void spellingError(SpellCheckEvent event)
  {
    JSpellDialog dlg = getSpellDialog() ;
    
    if (!dlg.showDialog(event.getSpellChecker(), event.getWordFinder()))
      event.cancel() ;
  }
}
