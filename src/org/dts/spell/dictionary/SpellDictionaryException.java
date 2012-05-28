/*
 * Created on 15/02/2005
 *
 */
package org.dts.spell.dictionary;

/**
 * @author DreamTangerine
 *
 */
public class SpellDictionaryException extends Exception
{

  /**
   * 
   */
  public SpellDictionaryException()
  {
    super() ;
  }

  /**
   * @param message
   */
  public SpellDictionaryException(String message)
  {
    super(message) ;
  }

  /**
   * @param message
   * @param cause
   */
  public SpellDictionaryException(String message, Throwable cause)
  {
    super(message, cause) ;
  }

  /**
   * @param cause
   */
  public SpellDictionaryException(Throwable cause)
  {
    super(cause) ;
  }
}
