/*
 * Created on 25/02/2005
 *
 */
package org.dts.spell.swing.event;

import javax.swing.text.JTextComponent;

import org.dts.spell.event.SpellCheckAdapter;
import org.dts.spell.event.SpellCheckEvent;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.utils.ErrorMarker;

/**
 * @author DreamTangerine
 *
 */
public class ErrorMarkerListener extends SpellCheckAdapter
{
  private ErrorMarker errorMarker ; 
  
  /**
   * 
   */
  public ErrorMarkerListener()
  {
    errorMarker = new ErrorMarker() ;
    errorMarker.setSelectError(false) ;
    errorMarker.setAutoQuit(true) ;
  }

  public void setTextComponent(JTextComponent textComp) 
  {
    errorMarker.setTextComponent(textComp) ;
  }
  
  public JTextComponent getTextComponent()
  {
    return errorMarker.getTextComponent() ;
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

  public void unMarkErrors()
  {
    errorMarker.unMarkAllErrors() ;
  }
  
  public void beginChecking(SpellCheckEvent event)
  {
    unMarkErrors() ;
  }
  
  public void spellingError(SpellCheckEvent event)
  {
    try
    {
	    // Mark the error
	    Word word = event.getInvalidWord() ;
	    errorMarker.markError(word.getStart(), word.getEnd()) ;
    }
    catch(Exception ex)
    {
      // We only want to trace
      System.out.println(ex) ;
    }
  }
}
