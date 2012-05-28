/*
 * Created on 25/02/2005
 *
 */
package org.dts.spell.swing.utils ;

import java.awt.Color ;

import javax.swing.event.CaretEvent ;
import javax.swing.event.CaretListener ;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException ;
import javax.swing.text.Highlighter ;
import javax.swing.text.JTextComponent ;
import javax.swing.text.Highlighter.Highlight ;

/**
 * This class has all the error marks (the red lines) for a JTextComponent. 
 * If all the marks are removed and isAutoQuit() is true (by default is false) 
 * the ErrorMarker call quitTextComponent().
 * 
 * @author DreamTangerine
 * 
 */
public class ErrorMarker
{
  private JTextComponent textComponent = null ;
  
  private Highlighter highlighter ;

  private ErrorHighlighterPainter errorHighlighterPainter = new ErrorHighlighterPainter() ;

  /**
   * A list to the current tag returned by highlighter.addHighlight. This tag
   * are in fact Highlighter.Highlight.
   * 
   * <b>NOTE</b> : The use of Highlighter.Highlight is not documented in the
   * JDK. It known that highlighter.addHighlight return this class by the
   * inspect of the <em>SUN</em> code. So it may change in the future.
   */
  private TagList errorList = new TagList() ;

  private TagSynchronizer caretListener = new TagSynchronizer() ;

  /**
   * This flag, when set to true, allow automatic remove of caretListener if
   * errorList is empty.
   */
  private boolean autoQuitSynchronizer = false ;
  
  public ErrorMarker()
  {
    this(null, true) ;
  }

  public ErrorMarker(JTextComponent textComp)
  {
    this(textComp, true) ;
  }

  public ErrorMarker(JTextComponent textComp, boolean selectError)
  {
    setTextComponent(textComp) ;
    setSelectError(selectError) ;
  }
  
  public void setErrorMarkColor(Color color)
  {
    errorHighlighterPainter.setErrorColor(color) ;
  }

  public Color getErrorMarkColor()
  {
    return errorHighlighterPainter.getErrorColor() ;
  }

  /**
   * @return Returns the selectError.
   */
  public boolean isSelectError()
  {
    return errorHighlighterPainter.isHighlightBackground() ;
  }

  /**
   * @param selectError
   *          The selectError to set.
   */
  public void setSelectError(boolean selectError)
  {
    errorHighlighterPainter.setHighlightBackground(selectError) ;
  }

  public void setTextComponent(JTextComponent textComp)
  {
    quitTextComponent() ;
    textComponent = textComp ;

    if (null != textComponent)
    {
      textComponent.addCaretListener(caretListener) ;
      textComponent.getDocument().addDocumentListener(caretListener) ;
      
      highlighter = textComponent.getHighlighter() ;
    }
    else
      highlighter = null ;
  }

  public JTextComponent getTextComponent()
  {
    return textComponent ;
  }

  public void quitTextComponent()
  {
    if (null != textComponent)
    {
      unMarkAllErrors() ;

      textComponent.removeCaretListener(caretListener) ;
      textComponent.getDocument().removeDocumentListener(caretListener) ;
      
      textComponent = null ;
      highlighter = null ;
    }
  }

  public Object markError(int start, int end, boolean scroll)
      throws BadLocationException
  {
    Object tag = highlighter.addHighlight(start, end, errorHighlighterPainter) ;

    errorList.add(tag) ;

    if (scroll)
      textComponent.setCaretPosition(start) ;

    return tag ;
  }

  public Object markError(int start, int end) throws BadLocationException
  {
    return markError(start, end, false) ;
  }

  public void unMarkError(Object tag)
  {
    highlighter.removeHighlight(tag) ;
    errorList.remove(tag) ;
  }

  public void unMarkAllErrors()
  {
    errorList.removeAll(highlighter) ;
  }

  public boolean isAutoQuit()
  {
    return autoQuitSynchronizer ;
  }
  
  public void setAutoQuit(boolean auto)
  {
    this.autoQuitSynchronizer = auto ;
  }

  
  public String toString()
  {
    Highlight[] hl = highlighter.getHighlights() ;

    String result = "N° Highlight " + hl.length + "\n" ;

    for (int i = 0 ; i < hl.length ; ++i)
    {
      if (hl[i].getPainter() == errorHighlighterPainter)
        result += "* " ;

      result += hl[i] + "\n" ;
    }
    
    return result ;
  }

  private class TagSynchronizer implements CaretListener, DocumentListener
  {
    public void caretUpdate(CaretEvent e)
    {
      errorList.updateCurrent(e.getDot()) ;      
    }

    public void insertUpdate(DocumentEvent e)
    {
    }

    public void removeUpdate(DocumentEvent e)
    {
      int os = e.getOffset() ;
     
      errorList.removeRange(os, os + e.getLength(), highlighter) ;
      
      if (errorList.isEmpty() && isAutoQuit())
        quitTextComponent() ;
    }

    public void changedUpdate(DocumentEvent e)
    {
    }
  }
}
