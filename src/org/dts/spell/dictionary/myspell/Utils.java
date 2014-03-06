/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * TODO : Change StringBuffer with StringBuilder when SDK 5.0 is ready for Apple.
 * 
 * @author DreamTangerine
 *
 */
public final class Utils
{
  public static int SETSIZE = 16000;
  public static int MAXAFFIXES = 256 ;
  public static int MAXWORDLEN = 100 ;
  public static int XPRODUCT = (1 << 0) ;

  public static int MAXLNLEN = 1024 ;

  public static boolean TestAff(String a, char b , int c)
  {
    for (int i = 0 ; i < c ; ++i)
      if (a.charAt(i) == b)
        return true ;
    
    return false ;
  }
  
  
  public static String myRevStrDup(String s)
  {
    StringBuffer builder = new StringBuffer(s) ; 
    
    return builder.reverse().toString() ;
  }

  public static boolean isSubset(String s1, String s2)
  {
    return s2.startsWith(s1) ;
  }
  
  public static void close(Reader rd) throws IOException
  {
    if (null != rd)
      rd.close() ;
  }

  public static void close(Writer wt) throws IOException
  {
    if (null != wt)
      wt.close() ;
  }
  
  public static String mkInitCap(String word)
  {
    StringBuffer bd = new StringBuffer(word) ;
    
    bd.setCharAt(0, Character.toUpperCase(bd.charAt(0))) ;
    
    return bd.toString() ;
  }
}
