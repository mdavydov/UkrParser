/*
 * Created on 28/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

/**
 * @author DreamTangerine
 *  
 */
public class PfxEntry extends AffEntry
{
  AffixMgr pmyMgr ;

  PfxEntry next ;

  PfxEntry nexteq ;

  PfxEntry nextne ;

  PfxEntry flgnxt ;

  public PfxEntry(AffixMgr pmgr, AffEntry dp)
  {
    // register affix manager
    pmyMgr = pmgr ;

    // set up its intial values
    achar = dp.achar ; // char flag
    strip = dp.strip ; // string to strip
    appnd = dp.appnd ; // string to append
    numconds = dp.numconds ; // number of conditions to match
    xpflg = dp.xpflg ; // cross product flag

    // then copy over all of the conditions
    System.arraycopy(dp.conds, 0, conds, 0, Utils.SETSIZE) ;

    next = null ;
    nextne = null ;
    nexteq = null ;
  }

  public HEntry check(String word)
  {
    int len = word.length() ;
    int appndl = appnd.length() ;
    int stripl = strip.length() ;

    int cond ; // condition number being examined
    int tmpl ; // length of tmpword
    HEntry he ; // hash entry of root word or NULL
    int cp ;
    String tmpword = "" ;

    // on entry prefix is 0 length or already matches the beginning of the word.
    // So if the remaining root word has positive length
    // and if there are enough chars in root word and added back strip chars
    // to meet the number of characters conditions, then test it

    tmpl = len - appndl ;

    if ((tmpl > 0) && (tmpl + stripl >= numconds))
    {
      // generate new root word by removing prefix and adding
      // back any characters that would have been stripped

      tmpword = strip + word.substring(appndl) ;

      // now make sure all of the conditions on characters
      // are met. Please see the appendix at the end of
      // this file for more info on exactly what is being
      // tested

      cp = 0 ;
      for (cond = 0 ; cond < numconds ; cond++)
        if ((conds[tmpword.charAt(cp++)] & (1 << cond)) == 0)
          break ;

      // if all conditions are met then check if resulting
      // root word in the dictionary

      if (cond >= numconds)
      {
        if ((he = pmyMgr.lookup(tmpword)) != null)
        {
          if (Utils.TestAff(he.astr, achar, he.astr.length()))
            return he ;
        }

        // prefix matched but no root word was found
        // if XPRODUCT is allowed, try again but now
        // ross checked combined with a suffix

        if ((xpflg & Utils.XPRODUCT) != 0)
        {
          he = pmyMgr.suffix_check(tmpword, Utils.XPRODUCT, this) ;
          
          if (he != null)
            return he ;
        }
      }
    }

    return null ;
  }

  boolean allowCross()
  {
    return ((xpflg & Utils.XPRODUCT) != 0) ;
  }

  public char getFlag()
  {
    return achar ;
  }

  public String getKey()
  {
    return appnd ;
  }

  public String add(String word)
  {
    int len = word.length() ;
    int stripl = strip.length() ;
    int cond ;

    /* make sure all conditions match */
    if ((len > stripl) && (len >= numconds))
    {
      int cp = 0 ;

      for (cond = 0 ; cond < numconds ; cond++)
        if ((conds[word.charAt(cp++)] & (1 << cond)) == 0)
          break ;

      /* we have a match so add prefix */
      if (cond >= numconds)
        return appnd + word.substring(stripl) ;
    }

    return null ;
  }

  public PfxEntry getNext()
  {
    return next ;
  }

  public PfxEntry getNextNE()
  {
    return nextne ;
  }

  public PfxEntry getNextEQ()
  {
    return nexteq ;
  }

  public PfxEntry getFlgNxt()
  {
    return flgnxt ;
  }

  public void setNext(PfxEntry ptr)
  {
    next = ptr ;
  }

  public void setNextNE(PfxEntry ptr)
  {
    nextne = ptr ;
  }

  public void setNextEQ(PfxEntry ptr)
  {
    nexteq = ptr ;
  }

  public void setFlgNxt(PfxEntry ptr)
  {
    flgnxt = ptr ;
  }
}
