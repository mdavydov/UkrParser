/*
 * Created on 19/02/2005
 *
 */
package org.dts.spell.examples;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JOptionPane ;
import javax.swing.text.Document;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.finder.DocumentWordFinder;

/**
 * @author DreamTangerine
 *
 */
public class Example2
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
	   
	    JButton checkButton = new JButton("Check") ;
	    
	    checkButton.setMnemonic('C') ;
	    checkButton.addActionListener(
	        new ActionListener()
	        {
            public void actionPerformed(ActionEvent e)
            {
              Document doc = textArea.getDocument() ;
              Word word = checker.checkSpell(new DocumentWordFinder(doc)) ;
              
              if (null != word)
                JOptionPane.showMessageDialog(textArea, "Bad word " + word) ;
              else
                JOptionPane.showMessageDialog(textArea, "Text is OK") ;
              
        	    textArea.requestFocusInWindow() ;              
            }
	        }
	    ) ;
	    
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE) ;
	    frame.add(checkButton, BorderLayout.NORTH) ;
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
