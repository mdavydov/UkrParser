/*
 * Created on 18/02/2005
 *
 */
package org.dts.spell.swing.finder;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class DocumentFixedCharSequence implements CharSequence
{
  private Document document ;
  private int start ;
  private int end ;

  public DocumentFixedCharSequence(Document doc)
  {
    this(doc, 0, doc.getLength()) ;
  }

  public DocumentFixedCharSequence(Document doc, int start, int end)
  {
    this.document = doc ;
    this.start = start ;
    this.end = end ;
  }
  
  public int length()
  {
    return end - start ;
  }

  // TODO : Optimizar con una caché    
  public char charAt(int index)
  {
    try
    {
      return document.getText(start + index, 1).charAt(0) ;
    }
    catch (BadLocationException e)
    {
      throw new IndexOutOfBoundsException(e.getLocalizedMessage()) ;
    }
  }

  public CharSequence subSequence(int start, int end)
  {
    return new DocumentFixedCharSequence(
        document, 
        this.start + start, 
        this.start + end) ;
  }
  
  public String toString()
  {
    try
    {
      return document.getText(start, length()) ;
    }
    catch (BadLocationException e)
    {
      throw new IndexOutOfBoundsException(e.getLocalizedMessage()) ;
    }
  }
  
  public Document getDocument()
  {
    return document ;
  }
}