/*
 * Created on 19/02/2005
 *
 */
package org.dts.spell.examples;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.finder.Word;

/**
 * @author DreamTangerine
 *
 */
public class Example1
{
  private static void test(SpellChecker checker, String txt)
  {
    Word badWord = checker.checkSpell(txt) ;

    if (badWord == null)
      System.out.println("SI Parece correcta") ;
    else
      System.out.println("NO Parece correcta la palabra " + badWord) ;
  }

  public static void main(String[] args)
  {
    try
    {
      SpellDictionary dict = new OpenOfficeSpellDictionary(args[0]) ;
      SpellChecker checker = new SpellChecker(dict) ;

      test(checker, "Esto es una prueba de texto. Para ver como funciona") ;
      test(checker, "Esto es una prueba de texto.Para ver como funciona") ;
      test(checker, "Esto es una prueba de texto. para ver como funciona") ;      
      test(checker, "Esto es una prueba de texto.para ver como funciona") ;      
    }
    catch (Exception e)
    {
      e.printStackTrace() ;
    }
  }
}
