/*
 * Created on 31/12/2004
 *
 */
package org.dts.spell.dictionary.myspell ;

import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.BufferedReader ;
import java.io.InputStreamReader ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Collections ;
import java.util.LinkedList ;
import java.util.ListIterator ;

/**
 * @author DreamTangerine
 *  
 */
public class MySpell
{
  public static final int NOCAP = 0 ;

  public static final int INITCAP = 1 ;

  public static final int ALLCAP = 2 ;

  public static final int HUHCAP = 3 ;

  private AffixMgr pAMgr ;

  private HashMap pHMgr ;

  private SuggestMgr pSMgr ;

  private String encoding ;

  private int maxSug ;

  public MySpell(String affpath, String dpath) throws IOException
  {
    encoding = AffixMgr.readEncoding(affpath) ;

    /* first set up the hash manager */
    pHMgr = load_tables(dpath) ;

    /* next set up the affix manager */
    /* it needs access to the hash manager lookup methods */
    pAMgr = new AffixMgr(affpath, pHMgr) ;

    /* get the preferred try string and the dictionary */
    /* encoding from the Affix Manager for that dictionary */
    String try_string = pAMgr.get_try_string() ;
    //encoding = pAMgr.get_encoding();

    /* and finally set up the suggestion manager */
    maxSug = 15 ;
    pSMgr = new SuggestMgr(try_string, maxSug, pAMgr) ;
  }

  public List suggest(String word)
  {
    String wspace ;

    if (pSMgr == null)
      return Collections.EMPTY_LIST ;

    int[] captype = new int[1] ;
    boolean[] abbv = new boolean[1] ;
    String cw = cleanword(word, captype, abbv) ;
    int wl = cw.length() ;

    if (wl == 0)
      return Collections.EMPTY_LIST ;

    //int ns = 0 ;
    List wlst = new LinkedList() ;

    switch (captype[0])
    {
      case NOCAP:
      {
        wlst = pSMgr.suggest(wlst, cw) ;
        break ;
      }

      case INITCAP:
      {
        wspace = cw.toLowerCase() ;

        pSMgr.suggest(wlst, wspace) ;

        ListIterator it = wlst.listIterator() ;

        while (it.hasNext())
          it.set(Utils.mkInitCap((String) it.next())) ;

        pSMgr.suggest(wlst, cw) ;
        break ;
      }

      case HUHCAP:
      {
        pSMgr.suggest(wlst, cw) ;
        wspace = cw.toLowerCase() ;
        pSMgr.suggest(wlst, wspace) ;
        break ;
      }

      case ALLCAP:
      {
        wspace = cw.toLowerCase() ;
        pSMgr.suggest(wlst, wspace) ;

        ListIterator it = wlst.listIterator() ;

        while (it.hasNext())
          it.set(((String) it.next()).toUpperCase()) ;

        pSMgr.suggest(wlst, cw) ;
        break ;
      }
    }

    if (!wlst.isEmpty())
      return wlst ;

    // try ngram approach since found nothing
    pSMgr.ngsuggest(wlst, cw, pHMgr) ;

    if (!wlst.isEmpty())
    {
      switch (captype[0])
      {
        case NOCAP:
          break ;

        case HUHCAP:
          break ;

        case INITCAP:
        {
          ListIterator it = wlst.listIterator() ;

          while (it.hasNext())
            it.set(Utils.mkInitCap((String) it.next())) ;
        }
          break ;

        case ALLCAP:
        {
          ListIterator it = wlst.listIterator() ;

          while (it.hasNext())
            it.set(((String) it.next()).toUpperCase()) ;
        }
          break ;
      }
    }

    return wlst ;
  }

  public boolean spell(String word)
  {
    String rv = null ;

    String cw ;
    String wspace ;

    //int wl = word.length();
    //if (wl > (MAXWORDLEN - 1)) return 0;
    int[] captype = new int[1] ;
    boolean[] abbv = new boolean[1] ;

    cw = cleanword(word, captype, abbv) ;
    int wl = cw.length() ;

    if (wl == 0)
      return true ;

    switch (captype[0])
    {
      case HUHCAP:
      case NOCAP:
      {
        rv = check(cw) ;
        if ((abbv[0]) && (rv == null))
        {
          cw += '.' ;
          rv = check(cw) ;
        }
        break ;
      }

      case ALLCAP:
      {
        wspace = cw.toLowerCase() ;
        rv = check(wspace) ;

        if (rv == null)
        {
          rv = check(Utils.mkInitCap(wspace)) ;
        }
        if (rv == null)
          rv = check(cw) ;

        if (abbv[0] && (rv == null))
        {
          wspace = cw ;
          wspace += '.' ;
          rv = check(wspace) ;
        }
        break ;
      }
      case INITCAP:
      {
        wspace = cw.toLowerCase() ;
        rv = check(wspace) ;
        if (rv == null)
          rv = check(cw) ;

        if (abbv[0] && (rv == null))
        {
          wspace = cw ;
          wspace += '.' ;
          rv = check(wspace) ;
        }
        break ;
      }
    }

    return rv != null ;
  }

  public String get_dic_encoding()
  {
    return encoding ;
  }

  private HashMap load_tables(String tpath) throws IOException
  {
    HashMap result = null ;
    int tablesize ;

    // raw dictionary - munched file
    BufferedReader rawdict = null ;

    try
    {
      rawdict = new BufferedReader(new InputStreamReader(new FileInputStream(
          tpath), encoding)) ;

      // first read the first line of file to get hash table size
      String ts = rawdict.readLine() ;

      if (ts == null)
        throw new IOException("Hash Manager Error : 2\n") ;

      tablesize = Integer.parseInt(ts) ;

      if (tablesize == 0)
        throw new IOException("Hash Manager Error : 4\n") ;

      // allocate the hash table
      result = new HashMap(tablesize) ;

      // loop through all words on much list and add to hash
      // table and create word and affix strings

      while ((ts = rawdict.readLine()) != null)
      {
        ts = ts.trim() ;

        // split each line into word and affix char strings
        int ap = ts.indexOf('/') ;
        HEntry en ;

        if (ap != -1)
          en = new HEntry(ts.substring(0, ap), ts.substring(ap + 1)) ;
        else
          en = new HEntry(ts, "") ;

        //if (result.get(en.word)!=null)
        //{
        //	System.out.println("Duplicate entry " + en.word);
        //}
        result.put(en.word, en) ;
      }
    }
    finally
    {
      Utils.close(rawdict) ;
    }

    return result ;
  }

  private String cleanword(String src, int[] pcaptype, boolean[] pabbrev)
  {
    int p = 0 ;
    int q = 0 ;

    // first skip over any leading special characters
    while ((q < src.length()) && !Character.isLetterOrDigit(src.charAt(q)))
      q++ ;

    // now strip off any trailing special characters
    // if a period comes after a normal char record its presence
    pabbrev[0] = false ;

    int nl = src.substring(q).length() ;

    while ((nl > 0) && !Character.isLetterOrDigit(src.charAt(q + nl - 1)))
      nl-- ;

    if ((q + nl) < src.length() && src.charAt(q + nl) == '.')
      pabbrev[0] = true ;

    // if no characters are left it can't be an abbreviation and can't be
    // capitalized
    if (nl <= 0)
    {
      pcaptype[0] = NOCAP ;
      pabbrev[0] = false ;

      return "" ;
    }

    // now determine the capitalization type of the first nl letters
    int ncap = 0 ;
    int nneutral = 0 ;
    int nc = 0 ;

    p = q ;

    while (nl > 0)
    {
      nc++ ;
      char c = src.charAt(q) ;

      if (Character.isUpperCase(c))
        ncap++ ;

      if (!Character.isUpperCase(c) && !Character.isLowerCase(c))
        nneutral++ ;

      q++ ;
      nl-- ;
    }

    // now finally set the captype
    if (ncap == 0)
      pcaptype[0] = NOCAP ;
    else if ((ncap == 1) && Character.isUpperCase(src.charAt(p)))
      pcaptype[0] = INITCAP ;
    else if ((ncap == nc) || ((ncap + nneutral) == nc))
      pcaptype[0] = ALLCAP ;
    else
      pcaptype[0] = HUHCAP ;

    return src.substring(p, q) ;
  }

  public String check(String word)
  {
    HEntry he = null ;
 
    if (pHMgr != null)
      he = (HEntry) pHMgr.get(word) ;

    if ((he == null) && (pAMgr != null))
    {
      // try stripping off affixes */
      he = pAMgr.affix_check(word);

      // try check compound word
      if ((he == null) && (pAMgr.get_compound() != null))
        he = pAMgr.compound_check(word, pAMgr.get_compound().charAt(0)) ;
    }

    if (he != null)
      return he.word ;

    return null ;
  }
  
  public List<HEntry> checkList(String word)
  {
	  List<HEntry> result = new LinkedList<HEntry>();
	    HEntry he = null ;
	    
	    if (pHMgr != null)
	      he = (HEntry) pHMgr.get(word);
	    
	    if (he!=null) result.add(he);

	    if ((pAMgr != null))
	    {
	      // try stripping off affixes */
	      pAMgr.affix_check_list(word, result);

	      // try check compound word
	      if ((pAMgr.get_compound() != null))
	        pAMgr.compound_check_list(word, pAMgr.get_compound().charAt(0), result);
	    }

	    return result ;
  }
  
  /**
   * This function add a new word to the current WordManager, but this word is not
   * add permanet, that is, is not save in file.
   * 
   * @param word The word to add.
   */
  public void addCustomWord(String word)
  {
    HEntry en = new HEntry(word, "") ;

    pHMgr.put(en.word, en) ;
  }
}
