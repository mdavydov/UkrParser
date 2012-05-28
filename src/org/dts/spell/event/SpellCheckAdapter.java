/*
 * Created on 11/02/2005
 *
 */
package org.dts.spell.event;

/**
 * @author DreamTangerine
 *
 */
public class SpellCheckAdapter implements SpellCheckListener
{

  /**
   * 
   */
  public SpellCheckAdapter()
  {
    super() ;
  }

  /* (non-Javadoc)
   * @see org.dts.spell.event.SpellCheckListener#beginChecking(org.dts.spell.event.SpellCheckEvent)
   */
  public void beginChecking(SpellCheckEvent event)
  {
  }

  /* (non-Javadoc)
   * @see org.dts.spell.event.SpellCheckListener#spellingError(org.dts.spell.event.SpellCheckEvent)
   */
  public void spellingError(SpellCheckEvent event)
  {
  }

  /* (non-Javadoc)
   * @see org.dts.spell.event.SpellCheckListener#endChecking(org.dts.spell.event.SpellCheckEvent)
   */
  public void endChecking(SpellCheckEvent event)
  {
  }
}
