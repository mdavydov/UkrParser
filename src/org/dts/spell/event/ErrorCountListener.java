/*
 * Created on 11/02/2005
 *
 */
package org.dts.spell.event;

/**
 * @author DreamTangerine
 *
 */
public class ErrorCountListener extends SpellCheckAdapter
{
  private int nErrors ;
  
  /**
   * 
   */
  public ErrorCountListener()
  {
    super() ;
  }

  public void beginChecking(SpellCheckEvent event)
  {
    nErrors = 0 ;
  }
  
  public void spellingError(SpellCheckEvent event)
  {
    ++nErrors ;
  }
  
  public int getErrorsCount()
  {
    return nErrors ;
  }
}
