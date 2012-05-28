/*
 * Created on 20/01/2005
 *
 */
package org.dts.spell.swing.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;


public class ErrorHighlighterPainter extends DefaultHighlightPainter  
{
  private static int[] wavePoints = new int[10] ;    
  
  private Color errorColor ; 
  private boolean highlightBack = false ;

  public ErrorHighlighterPainter()
  {
    this(Color.RED) ;    
  }
  
  public ErrorHighlighterPainter(Color color)
  {
    super(null) ;
    
    for (int i = 0 ; i < wavePoints.length ; i++)
      wavePoints[i] = (int) Math.round(Math.cos(2 * i
          * (2 * Math.PI / wavePoints.length))) ;

    setErrorColor(color) ;
  }

  public void setErrorColor(Color color)
  {
    this.errorColor = color ;
  }
  
  public Color getErrorColor()
  {
    return errorColor ;
  }

  public void setHighlightBackground(boolean hi)
  {
    highlightBack = hi ;  
  }
  
  public boolean isHighlightBackground()
  {
    return highlightBack ;
  }
  
  public static void paintWaveLine(Graphics g, int x1, int x2, int y1)
  {
    for (int x = x1 ; x < x2 ; x++)
    {
      //TODO Added by Bob. An ArrayIndexOutOfBounds was being thrown here
      try
      {
      int y = y1 - 2 - wavePoints[x % wavePoints.length];      
      g.drawLine(x, y, x, y) ;
      }
      catch(Exception ex){}
    }
  }
  
  public static void paintWaveLine(Graphics g, JTextComponent c, int p0, int p1) throws BadLocationException
  {
    Rectangle r1 = c.modelToView(p0) ;
    Rectangle r2 = c.modelToView(p1) ;      
    
    paintWaveLine(g, r1.x, r2.x, r1.y + r1.height) ;
  }
  

  private void paint(Graphics g, JTextComponent c, int offs0, int offs1)
  {
    try
    {
      g.setColor(getErrorColor()) ;        
      paintWaveLine(g, c, offs0, offs1) ;
    }
    catch (BadLocationException e)
    {
      // can't be done
      e.printStackTrace() ;
    }
  }
  
  public Shape paintLayer(
      Graphics g,
      int offs0,
      int offs1,
      Shape bounds,
      JTextComponent c,
      View view)
  {
    if (isHighlightBackground())
      super.paintLayer(g, offs0, offs1, bounds, c, view) ;

    paint(g, c, offs0, offs1) ; 
    
    return bounds ;
  }

  public void paint(Graphics g, int p0, int p1, Shape shape, JTextComponent c)
  {
    if (isHighlightBackground())
      super.paint(g, p0, p1, shape, c) ;

    paint(g, c, p0, p1) ; 
  }
}