/*
 * Created on 31/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

import java.util.HashMap ;
import java.util.List ;
import java.util.Iterator ;

/**
 * 
 * TODO : Change StringBuffer with StringBuilder when SDK 5.0 is ready for Apple.
 * 
 * @author DreamTangerine
 *
 */
public class SuggestMgr
{
  // For now is not used
  //private static final int MAXSWL = 100 ;

  private static final int MAX_ROOTS = 10 ;

  // For now is not used  
  //private static final int MAX_WORDS = 500 ;

  private static final int MAX_GUESS = 10 ;

  // For now is not used
  //private static final int NGRAM_IGNORE_LENGTH = 0 ;

  private static final int NGRAM_LONGER_WORSE = 1 ;

  private static final int NGRAM_ANY_MISMATCH = 2 ;

  private static final char[] EMPTY_TRY = new char[0] ; 
  
  private char[] ctry ;

  private AffixMgr pAMgr ;

  private int maxSug ;

  private boolean nosplitsugs ;

  public SuggestMgr(String tryme, int maxn, AffixMgr aptr)
  {
    // register affix manager and check in string of chars to 
    // try when building candidate suggestions
    pAMgr = aptr ;
    
    if (tryme != null)
      ctry = tryme.toCharArray() ;
    else
      ctry = EMPTY_TRY ;

    maxSug = maxn ;
    nosplitsugs = false ;

    if (pAMgr != null)
      nosplitsugs = pAMgr.get_nosplitsugs() ;
  }

  public List suggest(List wlst, String word)
  {
    int nsug = wlst.size() ;

    // perhaps we made chose the wrong char from a related set
    if (nsug < maxSug)
      nsug = mapchars(wlst, word) ;

    // perhaps we made a typical fault of spelling
    if (nsug < maxSug)
      nsug = replchars(wlst, word) ;

    // did we forget to add a char
    if (nsug < maxSug)
      nsug = forgotchar(wlst, word) ;

    // did we swap the order of chars by mistake
    if (nsug < maxSug)
      nsug = swapchar(wlst, word) ;

    // did we add a char that should not be there
    if (nsug < maxSug)
      nsug = extrachar(wlst, word) ;

    // did we just hit the wrong key in place of a good char
    if (nsug < maxSug)
      nsug = badchar(wlst, word) ;

    // perhaps we forgot to hit space and two words ran together
    if (!nosplitsugs && (nsug < maxSug))
      twowords(wlst, word) ;

    return wlst ;
  }

  public boolean check(String word)
  {
    HEntry rv = null ;

    if (pAMgr != null)
    {
      rv = pAMgr.lookup(word) ;

      if (rv == null)
        rv = pAMgr.affix_check(word) ;
    }

    return rv != null ;
  }

  public List ngsuggest(List wlst, String word, HashMap pHMgr)
  {
    int i, j ;
    int lval ;
    int sc ;
    int lp ;

    if (pHMgr == null)
      return wlst ;

    // exhaustively search through all root words
    // keeping track of the MAX_ROOTS most similar root words
    HEntry[] roots = new HEntry[MAX_ROOTS] ;
    int[] scores = new int[MAX_ROOTS] ;

    for (i = 0 ; i < MAX_ROOTS ; i++)
    {
      // Is automatic by VM
      //roots[i] = null ; 
      scores[i] = -100 * i ;
    }

    lp = MAX_ROOTS - 1 ;

    int n = word.length() ;

    Iterator it = pHMgr.values().iterator() ;

    while (it.hasNext())
    {
      HEntry hp = (HEntry) it.next() ;

      sc = ngram(3, word, hp.word, NGRAM_LONGER_WORSE) ;

      if (sc > scores[lp])
      {
        scores[lp] = sc ;
        roots[lp] = hp ;
        int lval2 = sc ;

        for (j = 0 ; j < MAX_ROOTS ; j++)
          if (scores[j] < lval2)
          {
            lp = j ;
            lval2 = scores[j] ;
          }
      }
    }

    // find minimum threshhold for a passable suggestion
    // mangle original word three differnt ways
    // and score them to generate a minimum acceptable score
    int thresh = 0 ;
    char[] mw = null ;

    for (int sp = 1 ; sp < 4 ; sp++)
    {
      mw = word.toCharArray() ;

      for (int k = sp ; k < n ; k += 4)
        mw[k] = '*' ;

      thresh = thresh + ngram(n, word, new String(mw), NGRAM_ANY_MISMATCH) ;
      mw = null ;
    }

    mw = null ;
    thresh = thresh / 3 ;
    thresh-- ;

    // now expand affixes on each of these root words and
    // and use length adjusted ngram scores to select
    // possible suggestions
    String[] guess = new String[MAX_GUESS] ;
    int[] gscore = new int[MAX_GUESS] ;

    for (i = 0 ; i < MAX_GUESS ; i++)
    {
      // Is automatic by VM      
      //guess[i] = null;
      gscore[i] = -100 * i ;
    }

    lp = MAX_GUESS - 1 ;

    for (i = 0 ; i < MAX_ROOTS ; i++)
    {
      if (roots[i] != null)
      {
        HEntry rp = roots[i] ;
        List glst = pAMgr.expand_rootword(rp.word, rp.astr) ;

        it = glst.iterator() ;

        while (it.hasNext())
        {
          GuessWord glstK = (GuessWord) it.next() ;
          sc = ngram(n, word, glstK.word, NGRAM_ANY_MISMATCH) ;

          if (sc > thresh)
          {
            if (sc > gscore[lp])
            {
              gscore[lp] = sc ;
              guess[lp] = glstK.word ;
              lval = sc ;

              for (j = 0 ; j < MAX_GUESS ; j++)
                if (gscore[j] < lval)
                {
                  lp = j ;
                  lval = gscore[j] ;
                }
            }
          }
        }
      }
    }

    // now we are done generating guesses
    // sort in order of decreasing score and copy over

    bubblesort(guess, gscore) ;

    for (i = 0 ; i < MAX_GUESS ; i++)
    {
      if (guess[i] != null)
      {
        boolean unique = true ;

        for (j = i + 1 ; j < MAX_GUESS && unique ; j++)
          unique = !guess[i].equals(guess[j]) ;

        if (unique)
          wlst.add(guess[i]) ;
      }
    }

    return wlst ;
  }

  //suggestions for a typical fault of spelling, that
  //differs with more, than 1 letter from the right form.
  private int replchars(List wlst, String word)
  {
    int ns = wlst.size() ;
    String candidate ;
    int r ;
    int /*lenr,*/ lenp ;

    int wl = word.length() ;
    if (wl < 2 || pAMgr == null)
      return ns ;

    int numrep = pAMgr.get_numrep() ;
    ReplEntry[] reptable = pAMgr.get_reptable() ;

    if (reptable == null)
      return ns ;

    for (int i = 0 ; i < numrep ; i++)
    {
      r = 0 ;
      //lenr = reptable[i].replacement.length() ;
      lenp = reptable[i].pattern.length() ;

      // search every occurence of the pattern in the word
      while ((r = word.indexOf(reptable[i].pattern, r)) != -1)
      {
        candidate = word.substring(0, r) + reptable[i].replacement
            + word.substring(r + lenp) ;

        if (!wlst.contains(candidate) && check(candidate))
        {
          if (ns < maxSug)
            wlst.add(candidate) ;
          else
            return wlst.size() ;
        }

        r++ ; // search for the next letter
      }
    }

    return wlst.size() ;
  }

  //suggestions for when chose the wrong char out of a related set
  private int mapchars(List wlst, String word)
  {
    int ns = wlst.size() ;
    int wl = word.length() ;

    if (wl < 2 || pAMgr == null)
      return ns ;

    MapEntry[] maptable = pAMgr.get_maptable() ;

    if (maptable == null)
      return ns ;

    map_related(word, 0, wlst, maptable) ;

    return wlst.size() ;
  }

  private void map_related(String word, int i, List wlst, MapEntry[] maptable)
  {
    int nummap = maptable.length ;
    
    if (word.length() <= i)
    {
      if (!wlst.contains(word) && check(word))
      {
        if (wlst.size() < maxSug)
          wlst.add(word) ;
      }

      return ;
    }
    
   	char c = word.charAt(i) ;

    //if (c == 0)


    boolean in_map = false ;

    // exit as soon as posible
    for (int j = 0 ; j < nummap && wlst.size() < maxSug ; j++)
    {
      if (maptable[j].set.indexOf(c) != -1)
      {
        in_map = true ;
        StringBuffer newword = new StringBuffer(word) ;

        // exit as soon as posible
        for (int k = 0 ; k < maptable[j].set.length() && wlst.size() < maxSug ; k++)
        {
          newword.setCharAt(i, maptable[j].set.charAt(k)) ;
          map_related(newword.toString(), (i + 1), wlst, maptable) ;
        }
      }
    }

    if (!in_map)
    {
      i++ ;
      map_related(word, i, wlst, maptable) ;
    }
  }

  //error is mising a letter it needs  
  private int forgotchar(List wlst, String word)
  {
    char[] candidate = new char[word.length() + 1] ;
    int q ;

    int wl = word.length() ;
    int ctryl = ctry.length ;

    // try inserting a tryme character before every letter
    System.arraycopy(word.toCharArray(), 0, candidate, 1, wl) ;

    for (q = 0 ; q < wl ; q++)
    {
      for (int i = 0 ; i < ctryl ; i++)
      {
        candidate[q] = ctry[i] ;

        String candidateStr = new String(candidate) ;

        if (!wlst.contains(candidateStr) && check(candidateStr))
        {
          int ns = wlst.size() ;

          if (ns < maxSug)
            wlst.add(candidateStr) ;
          else
            return ns ;
        }
      }

      candidate[q] = word.charAt(q) ;
    }

    // now try adding one to end */
    for (int i = 0 ; i < ctryl ; i++)
    {
      candidate[q] = ctry[i] ;

      String candidateStr = new String(candidate) ;

      if (!wlst.contains(candidateStr) && check(candidateStr))
      {
        int ns = wlst.size() ;

        if (ns < maxSug)
          wlst.add(candidateStr) ;
        else
          return ns ;
      }
    }

    return wlst.size() ;
  }

  //error is adjacent letter were swapped  
  private int swapchar(List wlst, String word)
  {
    char[] candidate ;
    int p ;
    char tmpc ;

    int wl = word.length() ;

    // try swapping adjacent chars one by one
    candidate = word.toCharArray() ;
    for (p = 0 ; (p + 1) < wl ; p++)
    {
      tmpc = candidate[p] ;
      candidate[p] = candidate[p + 1] ;
      candidate[p + 1] = tmpc ;

      String candidateStr = new String(candidate) ;

      if (!wlst.contains(candidateStr) && check(candidateStr))
      {
        int ns = wlst.size() ;

        if (ns < maxSug)
          wlst.add(candidateStr) ;
        else
          return ns ;
      }

      tmpc = candidate[p] ;
      candidate[p] = candidate[p + 1] ;
      candidate[p + 1] = tmpc ;
    }

    return wlst.size() ;
  }

  //error is word has an extra letter it does not need   
  private int extrachar(List wlst, String word)
  {
    char[] candidate ;
    int p ;

    int wl = word.length() ;
    if (wl < 2)
      return wlst.size() ;

    candidate = new char[wl] ;

    // try omitting one char of word at a time
    System.arraycopy(word.toCharArray(), 1, candidate, 0, wl - 1) ;

    for (p = 0 ; p < wl ; p++)
    {
      String candidateStr = new String(candidate, 0, wl - 1) ;

      if (!wlst.contains(candidateStr) && check(candidateStr))
      {
        int ns = wlst.size() ;

        if (ns < maxSug)
          wlst.add(candidateStr) ;
        else
          return ns ;
      }

      candidate[p] = word.charAt(p) ;
    }

    return wlst.size() ;
  }

  //error is wrong char in place of correct one  
  private int badchar(List wlst, String word)
  {
    char tmpc ;
    char[] candidate = word.toCharArray() ;

    int wl = word.length() ;
    int ctryl = ctry.length ;

    // swap out each char one by one and try all the tryme
    // chars in its place to see if that makes a good word
    for (int i = 0 ; i < wl ; i++)
    {
      tmpc = candidate[i] ;

      for (int j = 0 ; j < ctryl ; j++)
      {
        if (ctry[j] == tmpc)
          continue ;

        candidate[i] = ctry[j] ;
        String candidateStr = new String(candidate) ;

        if (!wlst.contains(candidateStr) && check(candidateStr))
        {
          int ns = wlst.size() ;

          if (ns < maxSug)
            wlst.add(candidateStr) ;
          else
            return ns ;
        }

        candidate[i] = tmpc ;
      }
    }

    return wlst.size() ;
  }

  private int twowords(List wlst, String word)
  {
    char[] candidate ;
    int p ;

    int wl = word.length() ;

    if (wl < 3)
      return wlst.size() ;

    candidate = new char[wl + 1] ;
    System.arraycopy(word.toCharArray(), 0, candidate, 1, wl) ;

    // split the string into two pieces after every char
    // if both pieces are good words make them a suggestion
    for (p = 1 ; (p + 1) < (wl + 1) ; p++)
    {
      candidate[p - 1] = candidate[p] ;
      String candidateStr = new String(candidate, 0, p) ;

      if (check(candidateStr))
      {
        candidateStr = new String(candidate, p + 1, candidate.length - (p + 1)) ;

        if (check(candidateStr))
        {
          int ns = wlst.size() ;

          candidate[p] = ' ' ;

          if (ns < maxSug)
            wlst.add(new String(candidate)) ;
          else
            return ns ;
        }
      }
    }

    return wlst.size() ;
  }

  private int ngram(int n, String s1, String s2, int uselen)
  {
    int nscore = 0 ;
    int l1 = s1.length() ;
    int l2 = s2.length() ;
    int ns ;

    for (int j = 1 ; j <= n ; j++)
    {
      ns = 0 ;
      for (int i = 0 ; i <= (l1 - j) ; i++)
      {
        if (s2.indexOf(s1.substring(i, i + j)) != -1)
          ns++ ;
      }

      nscore = nscore + ns ;
      if (ns < 2)
        break ;
    }

    ns = 0 ;
    if (uselen == NGRAM_LONGER_WORSE)
      ns = (l2 - l1) - 2 ;

    if (uselen == NGRAM_ANY_MISMATCH)
      ns = Math.abs(l2 - l1) - 2 ;

    return (nscore - ((ns > 0) ? ns : 0)) ;
  }

  private void bubblesort(String[] rword, int[] rsc)
  {
    int n = rword.length ;
    int m = 1 ;
    while (m < n)
    {
      int j = m ;

      while (j > 0)
      {
        if (rsc[j - 1] < rsc[j])
        {
          int sctmp = rsc[j - 1] ;
          String wdtmp = rword[j - 1] ;

          rsc[j - 1] = rsc[j] ;
          rword[j - 1] = rword[j] ;
          rsc[j] = sctmp ;
          rword[j] = wdtmp ;
          j-- ;
        }
        else
          break ;
      }

      m++ ;
    }
  }
}
