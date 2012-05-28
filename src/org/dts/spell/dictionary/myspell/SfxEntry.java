/*
 * Created on 29/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

/**
 * @author DreamTangerine
 *  
 */
public class SfxEntry extends AffEntry
{
  AffixMgr pmyMgr ;

  String rappnd ;

  SfxEntry next ;

  SfxEntry nexteq ;

  SfxEntry nextne ;

  SfxEntry flgnxt ;

  public SfxEntry(AffixMgr pmgr, AffEntry dp)
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

    rappnd = Utils.myRevStrDup(appnd) ;
  }

  public HEntry check(String word, int optflags, AffEntry ppfx)
  {
    int len = word.length() ;
    int appndl = appnd.length() ;
    int stripl = strip.length() ;

    int tmpl ; // length of tmpword
    int cond ; // condition beng examined
    HEntry he ; // hash entry pointer
    int cp ;
    String tmpword ;
    PfxEntry ep = (PfxEntry) ppfx ;

    // if this suffix is being cross checked with a prefix
    // but it does not support cross products skip it

    if ((optflags & Utils.XPRODUCT) != 0 && (xpflg & Utils.XPRODUCT) == 0)
      return null ;

    // upon entry suffix is 0 length or already matches the end of the word.
    // So if the remaining root word has positive length
    // and if there are enough chars in root word and added back strip chars
    // to meet the number of characters conditions, then test it

    tmpl = len - appndl ;

    if ((tmpl > 0) && (tmpl + stripl >= numconds))
    {
      // generate new root word by removing suffix and adding
      // back any characters that would have been stripped or
      // or null terminating the shorter string

      cp = tmpl + stripl ;
      tmpword = word.substring(0, tmpl) + strip ;

      // now make sure all of the conditions on characters
      // are met. Please see the appendix at the end of
      // this file for more info on exactly what is being
      // tested

      for (cond = numconds ; --cond >= 0 ;)
        if ((conds[tmpword.charAt(--cp)] & (1 << cond)) == 0)
          break ;

      // if all conditions are met then check if resulting
      // root word in the dictionary

      if (cond < 0)
      {
        if ((he = pmyMgr.lookup(tmpword)) != null)
        {
          if (Utils.TestAff(he.astr, achar, he.astr.length())
              && ((optflags & Utils.XPRODUCT) == 0 || Utils.TestAff(he.astr, ep
                  .getFlag(), he.astr.length())))
            return he ;
        }
      }
    }

    return null ;
  }

  public boolean allowCross()
  {
    return ((xpflg & Utils.XPRODUCT) != 0) ;
  }

  public char getFlag()
  {
    return achar ;
  }

  public String getKey()
  {
    return rappnd ;
  }

  public String add(String word)
  {
    int len = word.length() ;
    int stripl = strip.length() ;

    int cond ;
    //String tword ;

    /* make sure all conditions match */
    if ((len > stripl) && (len >= numconds))
    {
      int cp = len ;

      for (cond = numconds ; --cond >= 0 ;)
        if ((conds[word.charAt(--cp)] & (1 << cond)) == 0)
          break ;

      /* we have a match so add suffix */
      if (cond < 0)
        return word.substring(0, len - stripl) + appnd ;
    }

    return null ;
  }

  public SfxEntry getNext()
  {
    return next ;
  }

  public SfxEntry getNextNE()
  {
    return nextne ;
  }

  public SfxEntry getNextEQ()
  {
    return nexteq ;
  }

  public SfxEntry getFlgNxt()
  {
    return flgnxt ;
  }

  public void setNext(SfxEntry ptr)
  {
    next = ptr ;
  }

  public void setNextNE(SfxEntry ptr)
  {
    nextne = ptr ;
  }

  public void setNextEQ(SfxEntry ptr)
  {
    nexteq = ptr ;
  }

  public void setFlgNxt(SfxEntry ptr)
  {
    flgnxt = ptr ;
  }
}
