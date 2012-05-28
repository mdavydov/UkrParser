/*
 * Created on 25/02/2005
 *
 */
package org.dts.spell.swing.event;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.dts.spell.event.SpellCheckEvent;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.JSpellDialog;
import org.dts.spell.swing.utils.ErrorMarker;

/**
 * @author DreamTangerine
 *
 */
public class UIErrorMarkerListener extends UISpellCheckListener
{
  private ErrorMarker errorMarker ; 
  
  /**
   * 
   */
  public UIErrorMarkerListener()
  {
    this(null) ;
  }

  /**
   * @param spellDialog
   */
  public UIErrorMarkerListener(JSpellDialog spellDialog)
  {
    super(spellDialog) ;
    errorMarker = new ErrorMarker() ;
  }
  
  public void setTextComponent(JTextComponent textComp) 
  {
    errorMarker.setTextComponent(textComp) ;
  }
  
  public void quitTextComponent()
  {
    errorMarker.quitTextComponent() ;
  }
  
  public boolean isSelectError()
  {
    return errorMarker.isSelectError() ;
  }

  /**
   * @param selectError
   *          The selectError to set.
   */
  public void setSelectError(boolean selectError)
  {
    errorMarker.setSelectError(selectError) ;
  }
  
  public void beginChecking(SpellCheckEvent event)
  {
    if (getSpellDialog() == null)
    {
      Window window = SwingUtilities.getWindowAncestor(errorMarker.getTextComponent()) ;
      JSpellDialog dlg ;
      
      if (window instanceof Frame)
        dlg = new JSpellDialog((Frame) window) ;
      else if (window instanceof Dialog)
        dlg = new JSpellDialog((Dialog) window) ;
      else 
        dlg = null ; 
      
      setSpellDialog(dlg) ;
    }
    
    super.beginChecking(event) ;
  }
  
  public void spellingError(SpellCheckEvent event)
  {
    try
    {
	    // Mark the error
	    Word word = event.getInvalidWord() ;
	    
	    errorMarker.unMarkAllErrors() ;
	    errorMarker.markError(word.getStart(), word.getEnd(), true) ;
	    
	    super.spellingError(event) ;
    }
    catch(Exception ex)
    {
      // TODO : Show a ErrorMessageBox
      System.out.println(ex) ;
      ex.printStackTrace();
    }
  }
  
  public void endChecking(SpellCheckEvent event)
  {
    errorMarker.unMarkAllErrors() ;    
    super.endChecking(event) ;
  }
}
