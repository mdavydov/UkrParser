/*
 * Created on 04/03/2005
 *
 */
package org.dts.spell.swing.utils;

import javax.swing.text.Highlighter;

/**
 * @author DreamTangerine
 *
 * NOTE : The Highlighter.Highlight <em>must not</em> overlap
 */
public final class HighlightUtils
{
  private HighlightUtils()
  {
  }
  
  public static boolean isMinor(Highlighter.Highlight o1, Highlighter.Highlight o2)
  {
    return o1.getEndOffset() <= o2.getStartOffset() ;
  }
  
  public static boolean isMinor(Highlighter.Highlight h, int index)
  {
    return h.getEndOffset() < index ;
  }  

  public static boolean isMinorOrEquals(Highlighter.Highlight h, int index)
  {
    return h.getEndOffset() <= index ;
  }  
  
  
  public static boolean isMajor(Highlighter.Highlight h, int index)
  {
    return h.getStartOffset() > index ;
  }  

  public static boolean isMajorOrEquals(Highlighter.Highlight h, int index)
  {
    return h.getStartOffset() >= index ;
  }  
  
  
  public static boolean isInside(Highlighter.Highlight h, int index)
  {
    return h.getStartOffset() <= index && h.getEndOffset() >= index ;
  }
  
  public static boolean isNullRange(Highlighter.Highlight h)
  {
    return h.getStartOffset() == h.getEndOffset() ;
  }

}
