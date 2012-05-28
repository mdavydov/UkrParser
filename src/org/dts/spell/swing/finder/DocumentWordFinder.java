/*
 * Created on 17/02/2005
 *
 */
package org.dts.spell.swing.finder;

import java.util.regex.Matcher;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;

/**
 * Esta clase vale para iterar sobre un documento.
 * 
 * @author DreamTangerine
 *
 */
public class DocumentWordFinder extends CharSequenceWordFinder
{
  private Document document ;

  public DocumentWordFinder(Matcher matcher)
  {
    super(matcher) ;
    document = null ;    
  }

  /**
   * @param spaceChars
   */
  public DocumentWordFinder(String spaceChars)
  {
    super("", spaceChars) ;
    document = null ;
  }
  
  public DocumentWordFinder()
  {
    super("") ;
    document = null ;
  }
  
  
  public DocumentWordFinder(Document text, Matcher matcher)
  {
    super(matcher) ;
    setDocument(text) ;
  }

  /**
   * @param text
   * @param spaceChars
   */
  public DocumentWordFinder(Document text, String spaceChars)
  {
    super(new DocumentCharSequence(text), spaceChars) ;
    setDocument(text) ;    
  }

  /**
   * @param text
   */
  public DocumentWordFinder(Document text)
  {
    super(new DocumentCharSequence(text)) ;
    setDocument(text) ;    
  }

  protected void replace(String newWord, Word currentWord)
  {
    int start = currentWord.getStart() ;
    
    try
    {
      document.remove(start, currentWord.length()) ;
      document.insertString(start, newWord, null) ;      
    }
    catch (BadLocationException e)
    {
      throw new IndexOutOfBoundsException(e.getLocalizedMessage()) ;
    }
    
    updateDocumentMatch() ;
  }
  
  public void setDocument(Document document)
  {
    this.document = document ;
    updateDocumentMatch() ;    
    init() ;
  }
  
  public void quitDocument()
  {
    document = null ;
  }
  
  private void updateDocumentMatch()
  {
    Matcher matcher = getMatcher() ;
    
    if (null != document)
      matcher.reset(new DocumentCharSequence(document)) ;
    else
      matcher.reset("") ;      
  }
}
