/*
 * Created on 24/02/2005
 *
 */
package org.dts.spell.examples;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.swing.event.ErrorMarkerListener;
import org.dts.spell.swing.finder.DocumentWordFinder;

/**
 * @author DreamTangerine
 *
 */
public class Example4
{
  public static void main(String[] args)
  {
    try
    {
      // Allow paint while resize :D
      Toolkit.getDefaultToolkit().setDynamicLayout(true) ;
      
	    SpellDictionary dict = new OpenOfficeSpellDictionary(args[0]) ;
	    final SpellChecker checker = new SpellChecker(dict) ;
	    
	    JFrame frame = new JFrame("Check Speller") ;
	    final JTextArea textArea = new JTextArea() ;
	    
	    textArea.setWrapStyleWord(true) ;
	    textArea.setLineWrap(true) ;
	   
	    JButton markButton = new JButton("Mark errors") ;
	    final ErrorMarkerListener listener = new ErrorMarkerListener() ;
	    
	    markButton.setMnemonic('M') ;
	    markButton.addActionListener(
	        new ActionListener()
	        {
            public void actionPerformed(ActionEvent e)
            {
              Document doc = textArea.getDocument() ;

        	    listener.setTextComponent(textArea) ;
              
              checker.check(new DocumentWordFinder(doc), listener) ;
              textArea.requestFocusInWindow() ;
            }
	        }
	    ) ;
	    
	    JButton unMarkButton = new JButton("Unmark errors") ;
	    
	    unMarkButton.setMnemonic('U') ;
	    unMarkButton.addActionListener(
	        new ActionListener()
	        {
            public void actionPerformed(ActionEvent e)
            {
        	    listener.unMarkErrors() ;
              textArea.requestFocusInWindow() ;
            }
	        }
	    ) ;
	        
	    JPanel buttonsPanel = new JPanel() ;
	    
	    buttonsPanel.add(markButton) ;
	    buttonsPanel.add(unMarkButton) ;
	    
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE) ;
	    frame.add(buttonsPanel, BorderLayout.NORTH) ;
	    frame.add(new JScrollPane(textArea), BorderLayout.CENTER) ;
	    
	    frame.setSize(640, 480) ;
	    frame.setVisible(true) ;
	    
	    textArea.requestFocusInWindow() ;
    }
    catch(Exception ex)
    {
      ex.printStackTrace() ;
    }
  }
}
 