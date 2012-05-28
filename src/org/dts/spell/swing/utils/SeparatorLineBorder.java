/*
 * Created on 23/02/2005
 *
 */
package org.dts.spell.swing.utils;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JSeparator;
import javax.swing.border.AbstractBorder;

public class SeparatorLineBorder extends AbstractBorder
{
  private static final JSeparator line = new JSeparator() ;
  private static final SeparatorLineBorder instance = new SeparatorLineBorder() ;  
  
  private SeparatorLineBorder()
  {
  }

  static public SeparatorLineBorder get()
  {
    return instance ;
  }

  public void paintBorder(
      Component c,
      Graphics g,
      int x,
      int y,
      int width,
      int height)
  {
    g.translate(x, y) ;
	    line.setSize(width, line.getPreferredSize().height) ;
	    line.paint(g) ;
    g.translate(-x, -y) ;    
  }
}