/*
 * Created on 19/02/2005
 *
 */
package org.dts.spell.swing ;

import java.awt.BorderLayout ;
import java.awt.Dialog ;
import java.awt.Frame ;
import java.awt.HeadlessException ;
import java.awt.KeyEventPostProcessor ;
import java.awt.KeyboardFocusManager ;
import java.awt.event.ActionListener ;
import java.awt.event.KeyEvent ;
import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;
import java.beans.EventHandler ;

import javax.swing.BorderFactory ;
import javax.swing.JDialog ;

import org.dts.spell.SpellChecker ;
import org.dts.spell.finder.WordFinder ;

/**
 * @author DreamTangerine
 *  
 */
public class JSpellDialog extends JDialog
{
  private JSpellPanel panel ;

  /**
   * The user want close the dialog because he press the cancel button o other
   * button. If wantClose is false and a windowClosing event happen the user
   * close the window with the close button of the JDialog (not the JSpellPanel)
   * and a cancel event must be fire by the JSpellPanel.
   */
  private boolean wantClose ;

  /**
   * The user want to cancel the spellcheck.
   */
  private boolean cancel ;

  private class EscKeyProcessor extends WindowAdapter implements
      KeyEventPostProcessor
  {
    public boolean postProcessKeyEvent(KeyEvent keyEvent)
    {
      if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
      {
        panel.cancel() ;
        return true ;
      }
      else
        return false ;
    }

    public void windowClosing(WindowEvent e)
    {
      if (!wantClose)
        panel.cancel() ;
    }

    public void windowActivated(WindowEvent e)
    {
      KeyboardFocusManager mng = KeyboardFocusManager
          .getCurrentKeyboardFocusManager() ;
      mng.addKeyEventPostProcessor(this) ;

      panel.initFocus() ;
    }

    public void windowDeactivated(WindowEvent e)
    {
      KeyboardFocusManager mng = KeyboardFocusManager
          .getCurrentKeyboardFocusManager() ;

      mng.removeKeyEventPostProcessor(this) ;
    }
  }

  private EscKeyProcessor escKeyProcessor = new EscKeyProcessor() ;

  private static String getDialogTitle()
  {
    return "Spell Check" ;
  }

  private void init(JSpellPanel panel)
  {
    this.panel = panel ;

    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)) ;

    getContentPane().setLayout(new BorderLayout()) ;
    getContentPane().add(panel, BorderLayout.CENTER) ;

    panel.setCloseListener((ActionListener) EventHandler.create(
        ActionListener.class, this, "close")) ;
    
    panel.setCancelListener((ActionListener) EventHandler.create(
        ActionListener.class, this, "cancel")) ;

    panel.init() ;

    addWindowListener(escKeyProcessor) ;
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE) ;
  }

  private static Frame getCurrentFrame()
  {
    Frame[] frames = Frame.getFrames() ;
    
    if (null == frames || frames.length == 0)
      return null ;
    else
      return frames[frames.length - 1] ;
  }
  
  /**
   * @throws java.awt.HeadlessException
   */
  public JSpellDialog() throws HeadlessException
  {
    this(getCurrentFrame(), new JSpellPanel()) ;
  }

  /**
   * 
   * @param panel
   * @throws HeadlessException
   */
  public JSpellDialog(JSpellPanel panel) throws HeadlessException
  {
    this(getCurrentFrame(), getDialogTitle(), panel) ;
  }

  /**
   * 
   * @param title
   * @param panel
   * @throws HeadlessException
   */
  public JSpellDialog(String title, JSpellPanel panel) throws HeadlessException
  {
    this(getCurrentFrame(), title, panel) ;
  }
  
  
  
  /**
   * @param owner
   * @throws java.awt.HeadlessException
   */
  public JSpellDialog(Frame owner) throws HeadlessException
  {
    this(owner, new JSpellPanel()) ;
  }

  /**
   * 
   * @param owner
   * @param panel
   * @throws HeadlessException
   */
  public JSpellDialog(Frame owner, JSpellPanel panel) throws HeadlessException
  {
    this(owner, getDialogTitle(), panel) ;
  }

  /**
   * @param owner
   * @param title
   * @throws java.awt.HeadlessException
   */
  public JSpellDialog(Frame owner, String title) throws HeadlessException
  {
    this(owner, title, new JSpellPanel()) ;
  }

  /**
   * 
   * @param owner
   * @param title
   * @param panel
   * @throws HeadlessException
   */
  public JSpellDialog(Frame owner, String title, JSpellPanel panel)
      throws HeadlessException
  {
    super(owner, title, true) ;    
    init(panel) ;
    
  }

  /**
   * @param owner
   * @throws java.awt.HeadlessException
   */
  public JSpellDialog(Dialog owner) throws HeadlessException
  {
    this(owner, new JSpellPanel()) ;
  }

  /**
   * 
   * @param owner
   * @param panel
   * @throws HeadlessException
   */
  public JSpellDialog(Dialog owner, JSpellPanel panel) throws HeadlessException
  {
    this(owner, getDialogTitle(), panel) ;
  }

  /**
   * @param owner
   * @param title
   * @throws java.awt.HeadlessException
   */
  public JSpellDialog(Dialog owner, String title) throws HeadlessException
  {
    this(owner, title, new JSpellPanel()) ;
  }

  /**
   * @param owner
   * @param title
   * @param panel
   * @throws HeadlessException
   */
  public JSpellDialog(Dialog owner, String title, JSpellPanel panel)
      throws HeadlessException
  {
    super(owner, title, true) ;
    init(panel) ;
  }

  public void cancel()
  {
    cancel = true ;
    close() ;
  }

  /**
   * Called to close the dialog.
   */
  public void close()
  {
    wantClose = true ;
    setVisible(false) ;
    dispose() ;
  }

  /**
   * Show the Spellcheck error dialog from the SpellChecker and WordFinder. The
   * current word in the wordfinder is the wrong word showed by the dialog. The
   * return value indicate if the user want to cancel the spellcheck.
   * 
   * @param checker
   *          The checker with the dictionary.
   * @param finder
   *          The finder with the current wrong word.
   * @return false if the user want to cancel the spellcheck true in other case.
   */
  public boolean showDialog(SpellChecker checker, WordFinder finder)
  {
    cancel = false ;
    wantClose = false ;

    panel.setWrongWord(finder, checker) ;

    setModal(true) ;
    pack() ;
    
    try
    {    
       this.setLocationRelativeTo((java.awt.Component)this.getOwner());
    }
    catch(Exception ex){}
    setResizable(false);
    setVisible(true) ;
    

    return !cancel ;
  }
}
