/*
 * Created on 24/02/2005
 *
 */
package org.dts.spell.examples;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.swing.event.UIErrorMarkerListener;
import org.dts.spell.swing.finder.DocumentWordFinder;

/**
 * @author DreamTangerine
 *
 */
public class Example3
{
  public static void main(String[] args)
  {
    try
    {
      // Allow paint while resize :D
      Toolkit.getDefaultToolkit().setDynamicLayout(true) ;
      
	    
        File d = new File("C:\\us\\en-US.dic");
        File a = new File("C:\\us\\en-US.aff");
        SpellDictionary dict = new OpenOfficeSpellDictionary(d.getAbsolutePath(), a.getAbsolutePath()) ;
	    final SpellChecker checker = new SpellChecker(dict) ;
	    
	    JFrame frame = new JFrame("Check Speller") ;
	    final JTextArea textArea = new JTextArea() ;
	    
	    textArea.setWrapStyleWord(true) ;
	    textArea.setLineWrap(true) ;
	   
	    JButton checkButton = new JButton("Check") ;
	    final UIErrorMarkerListener listener = new UIErrorMarkerListener() ;
	    
	    checkButton.setMnemonic('C') ;
	    checkButton.addActionListener(
	        new ActionListener()
	        {
            public void actionPerformed(ActionEvent e)
            {
              Document doc = textArea.getDocument() ;

        	    listener.setTextComponent(textArea) ;
              
              if (checker.check(new DocumentWordFinder(doc), listener))
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
