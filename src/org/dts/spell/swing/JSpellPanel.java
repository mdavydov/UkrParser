/*
 * Created on 21/02/2005
 *
 */
package org.dts.spell.swing ;

import java.awt.BorderLayout ;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener ;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter ;
import java.awt.event.MouseEvent ;

import java.beans.EventHandler; 
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
//import java.util.Locale;
//import java.util.ResourceBundle;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton ;
import javax.swing.JLabel ;
import javax.swing.JList ;
import javax.swing.JPanel ;
import javax.swing.JScrollPane ;
import javax.swing.JTextField ;
import javax.swing.ListSelectionModel ;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent ;
import javax.swing.event.ListSelectionListener ;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.SpellDictionaryException;
import org.dts.spell.finder.Word;
import org.dts.spell.finder.WordFinder;
import org.dts.spell.swing.utils.ErrorMarker;
//import org.dts.spell.swing.utils.SeparatorLineBorder;

/**
 * @author DreamTangerine
 *  
 */
public class JSpellPanel extends JPanel
{
/*  
  public static final String IGNORE_STRING = "IGNORE_STRING" ;
  public static final String IGNORE_ALL_STRING = "IGNORE_ALL_STRING" ;
  public static final String ADD_STRING = "ADD_STRING" ;
  public static final String REPLACE_STRING = "REPLACE_STRING" ;
  public static final String REPLACE_ALL_STRING = "REPLACE_ALL_STRING" ;
  public static final String CANCEL_STRING = "CANCEL_STRING" ;

  private static final String SUGGESTIONS_STRING = "SUGGESTIONS_STRING" ; 
*/

  
    /** Panel constructor */
 
    
  public static final String WRONG_STRING = 
      "<html>" + Messages.getString("INVALIDWORD") + ": <font color=\"red\"><em>{0}</em></font></html>"; 
    //"<html>La palabra <font color=\"red\"><em>{0}</em></font> no se encuentra en el diccionario." +
    //"</html>" ;

  public static final String CHANGE_BY_STRING = "To change it by";//"Cambiarla por" ;
  public static final String IGNORE_ERROR_STRING = "To ignore the error";//"Ignorar el error" ;  
  
  public static final String IGNORE_STRING = Messages.getString("IGNORE");//"Una vez" ;
  public static final String IGNORE_ALL_STRING = Messages.getString("IGNOREALL");//"Hasta el final" ;
  public static final String ADD_STRING = Messages.getString("ADD");//"Añadir al Dic." ;
  public static final String REPLACE_STRING = Messages.getString("REPLACE");//"Sólo por esta vez" ;
  public static final String REPLACE_ALL_STRING = Messages.getString("REPLACEALL");//"En todo el documento" ;
  public static final String CANCEL_STRING = Messages.getString("CANCEL");//"Cancelar" ;

  private static final String SUGGESTIONS_STRING = Messages.getString("SUGGESTIONS");
      //"It can select some of the following suggestions";
    //"Puede seleccionar alguna de las siguientes sugerencias :" ; 

  //private static final String NO_SUGGESTIONS_STRING = 
   // "No hay sugerencias" ; 
  
  private static final String NO_SUGGESTIONS_STRING = ""; //messages.getString("INVALIDWORD");
      //"There are no suggestions";
  
  public static final String CANCEL_CMD = "CANCEL_CMD" ; 
  public static final String CLOSE_CMD = "CLOSE_CMD" ;  

  private MessageFormat wrongFormater ; 
  
  private JLabel wrongWordLabel ;
  private JTextField checkText ;
  
  private JList suggestList ;
  private DefaultListModel suggestionListModel ;

  private JButton replaceButton ;
  private JButton ignoreButton ;

  private SpellChecker spellChecker ;
  private WordFinder wordFinder ;
  private String wrongWord ; 
  
  protected SpellChecker getSpellChecker()
  {
    return spellChecker ;
  }
  
  protected WordFinder getWordFinder()
  {
    return wordFinder ;
  }
  
  protected SpellDictionary getDictionary()
  {
    return getSpellChecker().getDictionary() ;
  }
  
  protected String getString(String id)
  {
    return id ;
  }

  protected boolean isEmpty(JList list)
  {
    return list.getFirstVisibleIndex() < 0 || 
    				list.getModel().getElementAt(0) == NO_SUGGESTIONS_STRING ;
  }
  
  protected void clearSuggestionWords()
  {
    suggestionListModel.clear() ;
    suggestionListModel.addElement(NO_SUGGESTIONS_STRING) ;
  }
  
  protected void setSuggestionWords(List list)
  {
    if (list.isEmpty())
      clearSuggestionWords() ;
    else
    {
      Iterator it = list.iterator() ;
      
      suggestionListModel.clear() ;

      while (it.hasNext())
        suggestionListModel.addElement(it.next()) ;
    }
  }
  
  
  protected class ListListener extends MouseAdapter implements
      ListSelectionListener, FocusListener
  {
    public void valueChanged(ListSelectionEvent e)
    {
      if (!e.getValueIsAdjusting())
      {
        JList list = (JList) e.getSource() ;
        
        if (!isEmpty(list))
        {
	        Object selectedValue = list.getSelectedValue() ;
	
	        if (selectedValue != null)
	          checkText.setText(selectedValue.toString()) ;
        }
      }
    }

    public void mouseClicked(MouseEvent e)
    {
      if (e.getClickCount() == 2)
      {
        if (!isEmpty(suggestList))
        {
	        int index = suggestList.locationToIndex(e.getPoint()) ;
	
	        if (index != -1)
	          replaceCurrent() ;
        }
      }
    }

    public void focusGained(FocusEvent e)
    {
      if (!e.isTemporary())
      {
        if (suggestList.getSelectedIndex() < 0)
          suggestList.setSelectedIndex(0) ;
      }
    }

    public void focusLost(FocusEvent e)
    {
    }
  } ;
  
  protected final ListListener listListener = new ListListener() ;

  /**
   *  
   */
  public void init()
  {
    suggestionListModel = new DefaultListModel() ;
    
    createPanels() ;
    setPreferredSize(new java.awt.Dimension(360, 230));
    initFocus() ;
  }
  
  private ActionListener cancelListener ;
  
  /**
   * This is the listener to call when the user press cancel button or ESC key. 
   * 
   * @param listener The listener. It can be null.
   */
  public void setCancelListener(ActionListener listener)
  {
    cancelListener = listener ;
  }
  
  private ActionListener closeListener ;

  public void setCloseListener(ActionListener listener)
  {
    closeListener = listener ;
  }
  
  protected void fireCancelAction()
  {
    if (null != cancelListener)
      cancelListener.actionPerformed(
          new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "cancel")) ;
  }

  protected void fireCloseAction()
  {
    if (null != closeListener)
      closeListener.actionPerformed(
          new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "close")) ;
  }
    
  protected JButton createButton(String id, final ActionListener action)
  {
    Action tempAction = new AbstractAction(getString(id))
    {
      public void actionPerformed(ActionEvent e)
      {
        action.actionPerformed(e) ;
      }
    } ;

    return createButton(tempAction) ;
  }
  
  protected JButton createButton(Action action)
  {
    return new JButton(action) ;
  }
  
	protected void setWrongWord(String txt)
	{
	  wrongWordLabel.setText(getWrongWordMessageFormat().format(new Object[] { txt })) ;
	  
	  checkText.setText(txt) ;
	  wrongWord = txt ;
	  
	  setSuggestionWords(getDictionary().getSuggestions( new Word(wrongWord,0,false) )) ;
	  initFocus() ;
	}
	
  /**
   * Set the wrong word from the current WordFinder Word.
   * 
   * @param finder
   * @param checker
   */
  public void setWrongWord(WordFinder finder, SpellChecker checker)
  {
    spellChecker = checker ;
    wordFinder = finder ;
    
    setWrongWord(finder.current().getText()) ;
  }

	protected MessageFormat createWrongWordMessageFormat()
	{
	  return new MessageFormat(getString(WRONG_STRING));
	}
	
	protected final MessageFormat getWrongWordMessageFormat()
	{
	  return wrongFormater ;
	}
	
  protected JLabel createWrongWordLabel()
  {
    JLabel result = new JLabel("") ;
    
    wrongFormater = createWrongWordMessageFormat() ;
    
    return result ;
  }

  protected JPanel createNorthPanel()
  {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)) ;

    //panel.setBorder(
    //    BorderFactory.createTitledBorder(
    //        SeparatorLineBorder.get(), 
    //        getString("¿ Que desea hacer ?"))) ;

    wrongWordLabel = createWrongWordLabel() ; 
    panel.add(wrongWordLabel) ;

    return panel ;
  }
  
  protected class CheckTextListener implements DocumentListener
  {
    public CheckTextListener(JTextComponent textComponent)
    {
      errorMarker = new ErrorMarker(textComponent, false) ;
      errorMarker.setAutoQuit(false) ;
    }
    
    public void insertUpdate(DocumentEvent e)
    {
      checkWordSpell(e.getDocument()) ;
    }

    public void removeUpdate(DocumentEvent e)
    {
      checkWordSpell(e.getDocument()) ;
    }

    public void changedUpdate(DocumentEvent e)
    {
    }
    
    private void checkWordSpell(Document doc)
    {
      try
      {
        int end = doc.getLength() ;
        boolean caseSensitive = spellChecker.isCaseSensitive() ; 
        String text = doc.getText(0, end) ;
        
        errorMarker.unMarkAllErrors() ;
        
        spellChecker.setCaseSensitive(false) ;
        
        if (!spellChecker.getDictionary().isCorrect(text))
          errorMarker.markError(0, end) ;
        
        spellChecker.setCaseSensitive(caseSensitive) ;        
      }
      catch (BadLocationException e)
      {
        // Can't happen
        e.printStackTrace() ;
      }
    }
    
    private ErrorMarker errorMarker ; 
  }
  
  protected JTextField createCheckTextField()
  {
    JTextField result  = new JTextField() ;

    // TODO : Hacer que cuando la palabra esté mal escrita salga en rojo.
    result.getDocument().addDocumentListener(new CheckTextListener(result)) ;
    
    return result ;
  }

  protected JLabel createSuggestionLabel()
  {
    return new JLabel(getString(SUGGESTIONS_STRING)) ;
  }
  
  protected JList createSuggestionList()
  {
    JList result = new JList() ;
    
    result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION) ;
    result.addListSelectionListener(listListener) ;
    result.addMouseListener(listListener) ;
    result.addFocusListener(listListener) ;
    
    result.setModel(suggestionListModel) ;
    
    return result ;
  }
/*
  protected JPanel createReplaceTextButtonsPanel()
  {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER)) ;

    replaceButton = createButton(REPLACE_STRING, 
        (ActionListener) EventHandler.create(
		        ActionListener.class, 
		        this, 
		        "replaceCurrent")) ;
    
    panel.add(replaceButton) ;
    
    JButton replaceAllButton = createButton(REPLACE_ALL_STRING, 
        (ActionListener) EventHandler.create(
		        ActionListener.class, 
		        this, 
		        "replaceAll")) ;
    
    panel.add(replaceAllButton) ;
    
    return panel ;
  }
  */
  protected JPanel createButtonsPanel()
  {
      JPanel panel = new JPanel(new GridLayout(6, 1, 5 ,5));
      
      ignoreButton = createButton(IGNORE_STRING, 
          (ActionListener) EventHandler.create(
              ActionListener.class, 
              this, 
              "ignoreCurrent")) ;
      
      panel.add(ignoreButton) ;
      
      panel.add(    
        createButton(IGNORE_ALL_STRING, 
              (ActionListener) EventHandler.create(
                  ActionListener.class, 
                  this, 
                  "ignoreAll"))) ;
      panel.add(    
        createButton(ADD_STRING, 
            (ActionListener) EventHandler.create(
  			        ActionListener.class, 
  			        this, 
  			        "addCurrent"))) ;
      
      replaceButton = createButton(REPLACE_STRING, 
          (ActionListener) EventHandler.create(
  		        ActionListener.class, 
  		        this, 
  		        "replaceCurrent")) ;
      
      panel.add(replaceButton) ;
      
      JButton replaceAllButton = createButton(REPLACE_ALL_STRING, 
          (ActionListener) EventHandler.create(
  		        ActionListener.class, 
  		        this, 
  		        "replaceAll")) ;
      
      panel.add(replaceAllButton) ;
      
      panel.add(    
        createButton(CANCEL_STRING, 
            (ActionListener) EventHandler.create(
  			        ActionListener.class, 
  			        this, 
  			        "cancel"))) ;
      
      return panel;
  }
  
  protected JPanel createSuggestionPanel()
  {
    JPanel panel = new JPanel(new BorderLayout(2, 2)) ;
    JLabel label = createSuggestionLabel() ; 

    suggestList = createSuggestionList();
    //suggestList.setPreferredSize(new java.awt.Dimension(300, 300));
    label.setLabelFor(suggestList) ;

    panel.add(label, BorderLayout.NORTH) ;
    panel.add(new JScrollPane(suggestList), BorderLayout.CENTER) ;
    panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    
    return panel ;
  }
  /*
  protected JPanel createCenterPanel()
  {
    JPanel panel = new JPanel() ;

    panel.setLayout(new BorderLayout()) ;

    panel.setBorder(
        BorderFactory.createTitledBorder(
            SeparatorLineBorder.get(), 
            getString(CHANGE_BY_STRING))) ;

    checkText = createCheckTextField() ;    
    
    panel.add(checkText, BorderLayout.NORTH) ; 
    panel.add(createSuggestionPanel(), BorderLayout.CENTER) ;
    panel.add(createReplaceTextButtonsPanel(), BorderLayout.SOUTH) ;    

    return panel ;
  }*/

  /*
  private JPanel createSouthPanel()
  {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER)) ; 

    panel.setBorder(
        BorderFactory.createTitledBorder(
            SeparatorLineBorder.get(), 
            getString(IGNORE_ERROR_STRING))) ;

    ignoreButton = createButton(IGNORE_STRING, 
        (ActionListener) EventHandler.create(
            ActionListener.class, 
            this, 
            "ignoreCurrent")) ;
    
    panel.add(ignoreButton) ;
    
    panel.add(    
      createButton(IGNORE_ALL_STRING, 
            (ActionListener) EventHandler.create(
                ActionListener.class, 
                this, 
                "ignoreAll"))) ;
    panel.add(    
      createButton(ADD_STRING, 
          (ActionListener) EventHandler.create(
			        ActionListener.class, 
			        this, 
			        "addCurrent"))) ;
    panel.add(    
      createButton(CANCEL_STRING, 
          (ActionListener) EventHandler.create(
			        ActionListener.class, 
			        this, 
			        "cancel"))) ;
    
    return panel ;
  }
  
  */
  protected void createPanels()
  {
    setLayout(new BorderLayout(5, 5)) ;

    //add(createNorthPanel(), BorderLayout.NORTH) ;
    //add(createCenterPanel(), BorderLayout.CENTER) ;
    //add(createSouthPanel(), BorderLayout.SOUTH) ;
    
    JPanel p = new JPanel(new BorderLayout());
    JPanel panel = new JPanel() ;   

    checkText = createCheckTextField() ;    
    
    p.add(checkText, BorderLayout.NORTH) ; 
    p.add(createSuggestionPanel(), BorderLayout.CENTER) ;
    
    add(createNorthPanel(), BorderLayout.NORTH);
    add(p, BorderLayout.CENTER);
    add(createButtonsPanel(), BorderLayout.EAST);
    
	  clearSuggestionWords() ;
  }

  public String getNewWord()
  {
    return checkText.getText().trim() ;
  }
  
  public void ignoreCurrent()
  {
    fireCloseAction() ;    
  }
  
  public void ignoreAll()
  {
    SpellChecker checker = getSpellChecker() ;
    
    checker.addIgnore(wrongWord) ;
    fireCloseAction() ;    
  }

  public void addCurrent()
  {
    try
    {
      SpellDictionary dict = getDictionary() ;
    
      dict.addWord(wrongWord) ;
      fireCloseAction() ;      
    }
    catch (SpellDictionaryException e)
    {
      // TODO Add A MessageBox
      e.printStackTrace();
    }
  }
  
  public void replaceCurrent()
  {
    WordFinder finder = getWordFinder() ;
    
    finder.replace(getNewWord()) ;
    fireCloseAction() ;    
  }
  
  public void replaceAll()
  {
    SpellChecker checker = getSpellChecker() ;
    WordFinder finder = getWordFinder() ;
    
    finder.replace(getNewWord()) ;
    checker.addReplace(wrongWord, getNewWord()) ;
    
    fireCloseAction() ;
  }

  public void cancel()
  {
    fireCancelAction() ;
  }

  /**
   * Init the focus to the correct control. This implementation set the focus to the
   * list of suggestions. 
   */
  public void initFocus()
  {
    boolean empty = isEmpty(suggestList) ; 
    
	  suggestList.ensureIndexIsVisible(0) ;
	  checkText.requestFocusInWindow() ;
	  
	  if (empty)
	    getRootPane().setDefaultButton(ignoreButton) ;	    
	  else
	    getRootPane().setDefaultButton(replaceButton) ;
	  
	  suggestList.setFocusable(!empty) ;
  }
}
