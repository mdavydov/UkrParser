/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author DreamTangerine
 *
 */
public class AffixMgr
{
  private AffEntry[] pStart = new AffEntry[Utils.SETSIZE] ;
  private AffEntry[] sStart = new AffEntry[Utils.SETSIZE] ;
  private AffEntry[] pFlag = new AffEntry[Utils.SETSIZE] ;
  private AffEntry[] sFlag = new AffEntry[Utils.SETSIZE] ;
  private HashMap pHMgr ;
  private String trystring = null ;
  private String encoding  = null ;
  private String compound  = null ;
  private int cpdmin = 3 ;
  private ReplEntry[] reptable = null ;
  private MapEntry[] maptable = null ;
  private boolean nosplitsugs = false ;
  
  public AffixMgr(String affpath, HashMap ptr) throws IOException
  {
    pHMgr = ptr ;

    parse_file(affpath) ;
  }

  public HEntry affix_check(String word)
  {
    HEntry rv = null ;

    // check all prefixes (also crossed with suffixes if allowed)
    rv = prefix_check(word) ;
    if (rv != null)
      return rv ;

    // if still not found check all suffixes
    rv = suffix_check(word, 0, null) ;
    return rv ;
  }
  
  public void affix_check_list(String word, List<HEntry> list)
  {
    // check all prefixes (also crossed with suffixes if allowed)
    prefix_check_list(word, list) ;

    suffix_check_list(word, 0, null, list) ;
  }
  
  public HEntry prefix_check(String  word)
  {
    HEntry rv = null ;

    // first handle the special case of 0 length prefixes
    PfxEntry pe = (PfxEntry) pStart[0] ;

    while (pe != null)
    {
      rv = pe.check(word) ;

      if (rv != null)
        return rv ;

      pe = pe.getNext() ;
    }

    // now handle the general case
    int sp = 0 ;
    PfxEntry pptr = (PfxEntry) pStart[word.charAt(sp)] ;

    while (pptr != null)
    {
      if (Utils.isSubset(pptr.getKey(), word))
      {
        rv = pptr.check(word) ;
        if (rv != null)
          return rv ;
        
        pptr = pptr.getNextEQ() ;
      }
      else
        pptr = pptr.getNextNE() ;
    }

    return null ;    
  }
  
  public void prefix_check_list(String  word, List<HEntry> result)
  {
    HEntry rv = null ;

    // first handle the special case of 0 length prefixes
    PfxEntry pe = (PfxEntry) pStart[0] ;

    while (pe != null)
    {
      rv = pe.check(word) ;

      if (rv != null)
      {
    	result.add(rv);
        rv=null;
      }

      pe = pe.getNext() ;
    }

    // now handle the general case
    int sp = 0 ;
    PfxEntry pptr = (PfxEntry) pStart[word.charAt(sp)] ;

    while (pptr != null)
    {
      if (Utils.isSubset(pptr.getKey(), word))
      {
        rv = pptr.check(word) ;
        if (rv != null)
        {
        	result.add(rv);
        	rv=null;
        }
        
        pptr = pptr.getNextEQ() ;
      }
      else
        pptr = pptr.getNextNE() ;
    }
  }
  
  public HEntry suffix_check(String word, int sfxopts, AffEntry ppfx)
  {
    HEntry rv = null ;

    // first handle the special case of 0 length suffixes
    SfxEntry se = (SfxEntry) sStart[0] ;
    while (se != null)
    {
      rv = se.check(word, sfxopts, ppfx) ;
      if (rv != null)
        return rv ;
      se = se.getNext() ;
    }

    // now handle the general case
    String tmpword = Utils.myRevStrDup(word) ;
    char sp = tmpword.charAt(0) ;
    SfxEntry sptr = (SfxEntry) sStart[sp] ;

    while (sptr != null)
    {
      if (Utils.isSubset(sptr.getKey(), tmpword))
      {
        rv = sptr.check(word, sfxopts, ppfx) ;
        if (rv != null)
          return rv ;

        sptr = sptr.getNextEQ() ;
      }
      else
        sptr = sptr.getNextNE() ;

    }

    return null ;    
  }
  
  public void suffix_check_list(String word, int sfxopts, AffEntry ppfx, List<HEntry> result)
  {
    HEntry rv = null ;

    // first handle the special case of 0 length suffixes
    SfxEntry se = (SfxEntry) sStart[0] ;
    while (se != null)
    {
      rv = se.check(word, sfxopts, ppfx) ;
      if (rv != null)
      {
    	  result.add(rv);
    	  rv = null;
      }
      se = se.getNext() ;
    }

    // now handle the general case
    String tmpword = Utils.myRevStrDup(word) ;
    char sp = tmpword.charAt(0) ;
    SfxEntry sptr = (SfxEntry) sStart[sp] ;

    while (sptr != null)
    {
      if (Utils.isSubset(sptr.getKey(), tmpword))
      {
        rv = sptr.check(word, sfxopts, ppfx) ;
        if (rv != null)
        {
        	result.add(rv);
        	rv=null;
        }

        sptr = sptr.getNextEQ() ;
      }
      else
        sptr = sptr.getNextNE() ;
    }
  }

  
  public List expand_rootword(String ts, String ap)
  {
    //int wl = ts.length() ;
    int al = ap.length() ;

    List wlst = new LinkedList() ;
    //List wlst = new java.util.Vector();
    
    // first add root word to list
    wlst.add(new GuessWord(ts, false)) ;
    
    // handle suffixes
    for (int i = 0 ; i < al ; i++)
    {
      char c = ap.charAt(i) ;
      SfxEntry sptr = (SfxEntry) sFlag[c] ;
      while (sptr != null)
      {
        String newword = sptr.add(ts) ;
        
        if (newword != null)
          wlst.add(new GuessWord(newword, sptr.allowCross())) ; 

        sptr = sptr.getFlgNxt() ;
      }
    }

    int n = wlst.size() ;
    //TODO Changed by Bob Tantlinger. Iterator caused a comodification exception
    //Iterator it = wlst.iterator() ; it.next() ;
    
    // handle cross products of prefixes and suffixes
    for (int j = 1 ; j < n ; j++)
    {
      //GuessWord wlstJ = (GuessWord) it.next() ;
      GuessWord wlstJ = (GuessWord)wlst.get(j);
      
      if (wlstJ.allow)
      {
        for (int k = 0 ; k < al ; k++)
        {
          char c = ap.charAt(k) ;
          PfxEntry cptr = (PfxEntry) pFlag[c] ;
          
          while (cptr != null)
          {
            if (cptr.allowCross())
            {
              String newword = cptr.add(wlstJ.word) ;
              
              if (newword != null)
                wlst.add(new GuessWord(newword, cptr.allowCross())) ; 
            }
            
            cptr = cptr.getFlgNxt() ;
          }
        }
      }
    }

    // now handle pure prefixes
    for (int m = 0 ; m < al ; m++)
    {
      char c = ap.charAt(m) ;
      PfxEntry ptr = (PfxEntry) pFlag[c] ;
      
      while (ptr != null)
      {
        String newword = ptr.add(ts) ;

        if (newword != null)
          wlst.add(new GuessWord(newword, ptr.allowCross())) ;
          
        ptr = ptr.getFlgNxt() ;
      }
    }
    
    return wlst ; 
  }
  
  public HEntry compound_check(String word, char compound_flag)
  {
    int len = word.length() ;

    int i ;
    HEntry rv = null ;
    String st ;
    String wordI ;

    // handle case of string too short to be a piece of a compound word
    if (len < cpdmin)
      return null ;

    for (i = cpdmin ; i < (len - (cpdmin - 1)) ; i++)
    {
      st = word.substring(0, i) ;
      rv = lookup(st) ;
      
      if (rv == null)
        rv = affix_check(st) ;

      if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
      {
        wordI = word.substring(i, word.length()) ;
        rv = lookup(wordI) ;

        if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
          return rv ;

        rv = affix_check(wordI) ;

        if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
          return rv ;

        rv = compound_check(wordI, compound_flag) ;
       
        if (rv != null)
          return rv ;
      }
    }

    return null ;
  }
  
  public void compound_check_list(String word, char compound_flag, List<HEntry> result)
  {
    int len = word.length() ;

    int i ;
    HEntry rv = null ;
    String st ;
    String wordI ;

    // handle case of string too short to be a piece of a compound word
    if (len < cpdmin)
      return;

    for (i = cpdmin ; i < (len - (cpdmin - 1)) ; i++)
    {
      st = word.substring(0, i) ;
      rv = lookup(st) ;
      
      if (rv == null)
        rv = affix_check(st) ;

      if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
      {
        wordI = word.substring(i, word.length()) ;
        rv = lookup(wordI) ;

        if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
        {
          result.add(rv);
          rv=null;
        }

        rv = affix_check(wordI) ;

        if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
        {
        	result.add(rv);
        	rv=null;
        }

        rv = compound_check(wordI, compound_flag) ;
       
        if (rv != null)
        {
        	result.add(rv);
        	rv = null;
        }
      }
    }
  }


  public HEntry lookup(String word)
  {
    if (pHMgr == null)
      return null ;

    return (HEntry) pHMgr.get(word) ;
  }
  
  public int get_numrep()
  {
    if (reptable != null)
      return reptable.length ;
    else
      return 0 ;
  }
  
  public ReplEntry[] get_reptable()
  {
    return reptable ;
  }
  
  public int get_nummap()
  {
    if (maptable != null)
      return maptable.length ;
    else
      return 0 ;
  }
  
  public MapEntry[] get_maptable()
  {
    return maptable ;
  }
  
  public String get_encoding()
  {
    if (encoding == null)
      encoding = "ISO8859-1" ;
 
    return encoding ;
  }
  
  public String get_try_string()
  {
    return trystring ;
  }
  
  public String get_compound()
  {
    return compound ; 
  }
  
  public boolean get_nosplitsugs()
  {
    return nosplitsugs;
  }

  public static String readEncoding(String affpath) throws IOException
  {
    BufferedReader rd = null ;
    
    try
    {
      // we suppose that first line is in US-ASCII
      rd = 
        new BufferedReader(
            new InputStreamReader(
                new FileInputStream(affpath), "US-ASCII")) ;
                
      String line=null;
      //skip over comments if any
      while((line = rd.readLine()) != null)
      {
          if(line.toUpperCase().startsWith("SET"))
              break;
      }
      
      //return parseEncoding(rd.readLine()) ;
      return parseEncoding(line);      
      
    }
    finally
    {
      Utils.close(rd) ;
    }
  }
  
  private void parse_file(String affpath) throws IOException
  {
    BufferedReader rd = null ;
    String charSet = readEncoding(affpath) ;
    
    try
    {
      rd = 
        new BufferedReader(
          new InputStreamReader(
              new FileInputStream(affpath), charSet)) ;
    
      String line ;
      
      while ((line = rd.readLine()) != null)
      {
        // parse this affix: P - prefix, S - suffix
        if (line.startsWith("PFX"))
          parse_affix(line, 'P', rd) ;
        else if (line.startsWith("SFX"))
          parse_affix(line, 'S', rd) ;
        else if (line.startsWith("TRY"))
          parse_try(line) ;
        else if (line.startsWith("SET"))
          parse_set(line) ;        
        else if (line.startsWith("COMPOUNDFLAG"))
          parse_cpdflag(line) ;        
        else if (line.startsWith("COMPOUNDMIN"))
          parse_cpdmin(line) ;
        else if (line.startsWith("REP"))
          parse_reptable(line, rd) ;
        else if (line.startsWith("MAP"))
          parse_maptable(line, rd) ;
        else if (line.startsWith("NOSPLITSUGS")) // handle NOSPLITSUGS
          nosplitsugs = true ;
      }
      
      // now we can speed up performance greatly taking advantage of the 
      // relationship between the affixes and the idea of "subsets".

      // View each prefix as a potential leading subset of another and view
      // each suffix (reversed) as a potential trailing subset of another.

      // To illustrate this relationship if we know the prefix "ab" is found in the
      // word to examine, only prefixes that "ab" is a leading subset of need be examined.
      // Furthermore is "ab" is not present then none of the prefixes that "ab" is
      // is a subset need be examined.
      // The same argument goes for suffix string that are reversed.

      // Then to top this off why not examine the first char of the word to quickly
      // limit the set of prefixes to examine (i.e. the prefixes to examine must 
      // be leading supersets of the first character of the word (if they exist)
   
      // To take advantage of this "subset" relationship, we need to add two links
      // from entry.  One to take next if the current prefix is found (call it nexteq)
      // and one to take next if the current prefix is not found (call it nextne).

      // Since we have built ordered lists, all that remains is to properly intialize 
      // the nextne and nexteq pointers that relate them

      process_pfx_order();
      process_sfx_order();
    }
    finally
    {
      Utils.close(rd) ;
    }
  }
  
  private void parse_try(String line) throws IOException
  {
    if (trystring != null)
      throw new IOException("error: duplicate TRY strings\n") ;

    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;
    
    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;

      if (piece.length() != 0)
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;

          case 1:
            trystring = piece ;
            np++ ;
            break ;
          
          default:
            break ;
        }
        
        i++ ;
      }
    }

    if (np != 2)
      throw new IOException("error: missing TRY information\n") ;
  }

  private static String parseEncoding(String line) throws IOException
  {
    if (line == null)
      throw new IOException("error: missing SET information\n") ;
    
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;
    String result = null ;

    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;

      if (piece.length() != 0)
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;

          case 1:
            result = piece ;
            np++ ;
            break ;

          default:
            break ;
        }
        
        i++ ;
      }
    }
    
    if (np != 2)
      throw new IOException("error: missing SET information\n") ;
    
    return result ;
  }
  
  private void parse_set(String line) throws IOException
  {
    if (encoding != null)
      throw new IOException("error: duplicate SET strings\n") ;

    encoding = parseEncoding(line) ;
  }
  
  private void parse_cpdflag(String line) throws IOException
  {
    if (compound != null)
      throw new IOException("error: duplicate compound flags used\n") ;

    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;

    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;

      if (piece.length() != 0)
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;
            
          case 1:
            compound = piece ;
            np++ ;
            break ;
            
          default:
            break ;
        }
        
        i++ ;
      }
    }
    if (np != 2)
      throw new IOException("error: missing compound flag information\n") ;
  }
  
  private void parse_cpdmin(String line) throws IOException
  {
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;

    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;

      if (piece.length() != 0)
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;

          case 1:
            cpdmin = Integer.parseInt(piece) ;
            np++ ;
            break ;

          default:
            break ;
        }

        i++ ;
      }
    }

    if (np != 2)
      throw new IOException("error: missing compound min information\n") ;

    if ((cpdmin < 1) || (cpdmin > 50))
      cpdmin = 3 ;
  }
  
  private void parse_reptable(String line, BufferedReader af) throws IOException
  {
    int numrep = get_numrep() ;

    if (numrep != 0)
      throw new IOException("error: duplicate REP tables used\n") ;

    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;

    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;

      if (piece.length() != 0)
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;

          case 1:
            numrep = Integer.parseInt(piece) ;
            
            if (numrep < 1)
              throw new IOException(
                  "incorrect number of entries in replacement table\n") ;

            reptable = new ReplEntry[numrep] ;
            np++ ;
            break ;

          default:
            break ;
        }

        i++ ;
      }
    }

    if (np != 2)
      throw new IOException("error: missing replacement table information\n") ;

    /* now parse the numrep lines to read in the remainder of the table */
    for (int j = 0 ; j < numrep ; j++)
    {
      tp = new StringTokenizer(af.readLine(), " ") ;
      i = 0 ;

      reptable[j] = new ReplEntry() ;
      reptable[j].pattern = null ;
      reptable[j].replacement = null ;

      while (tp.hasMoreTokens())
      {
        piece = tp.nextToken() ;

        if (piece.length() != 0)
        {
          switch (i)
          {
            case 0:
              if (!piece.startsWith("REP"))
                throw new IOException("error: replacement table is corrupt\n") ;
              break ;

            case 1:
              reptable[j].pattern = piece ;
              break ;
              
            case 2:
              reptable[j].replacement = piece ;
              break ;
              
            default:
              break ;
          }
          
          i++ ;
        }
      }

      if ((reptable[j].pattern == null) || (reptable[j].replacement == null))
        throw new IOException("error: replacement table is corrupt\n") ;
    }
    
  }
  
  private void parse_maptable(String line, BufferedReader af) throws IOException
  {
    int nummap = get_nummap() ;

    if (nummap != 0)
      throw new IOException("error: duplicate MAP tables used\n") ;

    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;

    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;

      if (piece.length() != 0)
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;

          case 1:
            nummap = Integer.parseInt(piece) ;
            
            if (nummap < 1)
              throw new IOException(
                  "incorrect number of entries in map table\n") ;

            maptable = new MapEntry[nummap] ;
            np++ ;
            break ;

          default:
            break ;
        }
        
        i++ ;
      }
    }

    if (np != 2)
      throw new IOException("error: missing map table information\n") ;

    /* now parse the nummap lines to read in the remainder of the table */
    for (int j = 0 ; j < nummap ; j++)
    {
      tp = new StringTokenizer(line, " ") ;
      i = 0 ;

      maptable[j] = new MapEntry() ;
      maptable[j].set = null ;

      while (tp.hasMoreTokens())
      {
        piece = tp.nextToken() ;

        if (piece.length() != 0)
        {
          switch (i)
          {
            case 0:
              if (!piece.startsWith("MAP"))
                throw new IOException("error: map table is corrupt\n") ;

              break ;

            case 1:
              maptable[j].set = piece ;
              break ;
              
            default:
              break ;
          }
          
          i++ ;
        }
      }

      if ((maptable[j].set == null) || (maptable[j].set.length() == 0))
        throw new IOException("error: map table is corrupt\n") ;
    }
  }
  
  private void parse_affix(String line, char at, BufferedReader af) throws IOException
  {
    int numents = 0 ; // number of affentry structures to parse
    char achar = '\0' ; // affix char identifier
    short ff = 0 ;
    AffEntry[] ptr = null ;
    int nptr = 0 ;

    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String nl = line ;
    String piece ;
    int i = 0 ;

    // split affix header line into pieces

    int np = 0 ;

    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;

      if (piece.length() != 0)
      {
        switch (i)
        {
          // piece 1 - is type of affix
          case 0:
          {
            np++ ;
            break ;
          }

          // piece 2 - is affix char
          case 1:
          {
            np++ ;
            achar = piece.charAt(0) ;
            break ;
          }

          // piece 3 - is cross product indicator
          case 2:
          {
            np++ ;
            if (piece.charAt(0) == 'Y')
              ff = (short) Utils.XPRODUCT ;
            break ;
          }

          // piece 4 - is number of affentries
          case 3:
          {
            np++ ;
            numents = Integer.parseInt(piece) ;
            ptr = new AffEntry[numents] ;

            for (int e = 0 ; e < numents ; ++e)
              ptr[e] = new AffEntry() ;
            
            ptr[0].xpflg = ff ;
            ptr[0].achar = achar ;
            break ;
          }

          default:
            break ;
        }

        i++ ;
      }
    }

    // check to make sure we parsed enough pieces
    if (np != 4)
    {
      MessageFormat form = new MessageFormat(
          "error: affix {0} header has insufficient data in line {1}") ;

      throw new IOException(form
          .format(new Object[] { new Character(achar), nl })) ;
    }

    // store away ptr to first affentry
    nptr = 0 ;

    // now parse numents affentries for this affix
    for (int j = 0 ; j < numents ; j++)
    {
      nl = af.readLine() ;
      tp = new StringTokenizer(nl, " ") ;

      i = 0 ;
      np = 0 ;

      // split line into pieces
      while (tp.hasMoreTokens())
      {
        piece = tp.nextToken() ;

        if (piece.length() != 0)
        {
          switch (i)
          {

            // piece 1 - is type
            case 0:
              np++ ;
              if (nptr != 0)
                ptr[nptr].xpflg = ptr[0].xpflg ;
              break ;

            // piece 2 - is affix char
            case 1:
              np++ ;
              if (piece.charAt(0) != achar)
              {
                MessageFormat form = new MessageFormat(
                    "error: affix {0} is corrupt near line {1}\nerror: possible incorrect count\n") ;

                throw new IOException(form.format(new Object[] {
                    new Character(achar), nl })) ;
              }

              if (nptr != 0)
                ptr[nptr].achar = ptr[0].achar ;
              break ;

            // piece 3 - is string to strip or 0 for null
            case 2:
              np++ ;
              ptr[nptr].strip = piece ;

              if (ptr[nptr].strip.equals("0"))
                ptr[nptr].strip = "" ;

              break ;

            // piece 4 - is affix string or 0 for null
            case 3:
              np++ ;
              ptr[nptr].appnd = piece ;

              if (ptr[nptr].appnd.equals("0"))
                ptr[nptr].appnd = "" ;
              break ;

            // piece 5 - is the conditions descriptions
            case 4:
            {
              np++ ;
              encodeit(ptr[nptr], piece) ;
              break ;
            }

            default:
              break ;
          }
          i++ ;
        }
      }
      // check to make sure we parsed enough pieces
      if (np != 5)
      {
        MessageFormat form = new MessageFormat(
            "error: affix {0} is corrupt near line {1}\n") ;

        throw new IOException(form.format(new Object[] { new Character(achar),
            nl })) ;
      }

      nptr++ ;
    }

    // now create SfxEntry or PfxEntry objects and use links to
    // build an ordered (sorted by affix string) list
    nptr = 0 ;
    for (int k = 0 ; k < numents ; k++)
    {
      if (at == 'P')
      {
        PfxEntry pfxptr = new PfxEntry(this, ptr[nptr]) ;
        build_pfxlist(pfxptr) ;
      }
      else
      {
        SfxEntry sfxptr = new SfxEntry(this, ptr[nptr]) ;
        build_sfxlist(sfxptr) ;
      }
      nptr++ ;
    }      
  }

  private void encodeit(AffEntry ptr, String cs)
  {
    char c ;
    int i, j, k ;
    char[] mbr = new char[Utils.MAXLNLEN] ;

    // now clear the conditions array */
    for (i = 0 ; i < Utils.SETSIZE ; i++)
      ptr.conds[i] = 0 ;

    // now parse the string to create the conds array */
    int nc = cs.length() ;
    int neg = 0 ; // complement indicator
    int grp = 0 ; // group indicator
    int n = 0 ; // number of conditions
    int ec = 0 ; // end condition indicator
    int nm = 0 ; // number of member in group

    // if no condition just return
    if (cs.equals("."))
    {
      ptr.numconds = 0 ;
      return ;
    }

    i = 0 ;
    while (i < nc)
    {
      c = cs.charAt(i) ;

      // start group indicator
      if (c == '[')
      {
        grp = 1 ;
        c = 0 ;
      }

      // complement flag
      if ((grp == 1) && (c == '^'))
      {
        neg = 1 ;
        c = 0 ;
      }

      // end goup indicator
      if (c == ']')
      {
        ec = 1 ;
        c = 0 ;
      }

      // add character of group to list
      if ((grp == 1) && (c != 0))
      {
        mbr[nm] = c ;
        nm++ ;
        c = 0 ;
      }

      // end of condition
      if (c != 0)
        ec = 1 ;

      if (ec != 0)
      {
        if (grp == 1)
        {
          if (neg == 0)
          {
            // set the proper bits in the condition array vals for those chars
            for (j = 0 ; j < nm ; j++)
            {
              k = mbr[j] ;
              ptr.conds[k] = (char) (ptr.conds[k] | (1 << n)) ;
            }
          }
          else
          {
            // complement so set all of them and then unset indicated ones
            for (j = 0 ; j < Utils.SETSIZE ; j++)
              ptr.conds[j] = (char) (ptr.conds[j] | (1 << n)) ;
            for (j = 0 ; j < nm ; j++)
            {
              k = mbr[j] ;
              ptr.conds[k] = (char) (ptr.conds[k] & ~(1 << n)) ;
            }
          }
          
          neg = 0 ;
          grp = 0 ;
          nm = 0 ;
        }
        else
        {
          // not a group so just set the proper bit for this char
          // but first handle special case of . inside condition
          if (c == '.')
          {
            // wild card character so set them all
            for (j = 0 ; j < Utils.SETSIZE ; j++)
              ptr.conds[j] = (char) (ptr.conds[j] | (1 << n)) ;
          }
          else
            ptr.conds[c] = (char) (ptr.conds[c] | (1 << n)) ;
        }
        
        n++ ;
        ec = 0 ;
      }

      i++ ;
    }
    
    ptr.numconds = (short) n ;
  }
  
  private void build_pfxlist(AffEntry pfxptr)
  {
    PfxEntry ptr;
    PfxEntry pptr;
    PfxEntry ep = (PfxEntry) pfxptr;

    // get the right starting points
    String key = ep.getKey();
    char flg = ep.getFlag();

    // first index by flag which must exist
    ptr = (PfxEntry)pFlag[flg];
    ep.setFlgNxt(ptr);
    pFlag[flg] = ep ;


    // next index by affix string

    // handle the special case of null affix string
    if (key.length() == 0) 
    {
      // always inset them at head of list at element 0
       ptr = (PfxEntry)pStart[0];
       ep.setNext(ptr);
       pStart[0] = ep;
       return ;
    }

    // now handle the general case
    char sp = key.charAt(0) ;
    ptr = (PfxEntry)pStart[sp];
    
    /* handle the insert at top of list case */
    if ((ptr == null) || (ep.getKey().compareTo(ptr.getKey()) <= 0)) 
    {
       ep.setNext(ptr);
       pStart[sp] = ep;
       return;
    }

    /* otherwise find where it fits in order and insert it */
    pptr = null ;
    
    for (; ptr != null; ptr = ptr.getNext()) 
    {
      if (ep.getKey().compareTo(ptr.getKey()) <= 0) 
        break ;
      
      pptr = ptr;
    }
    
    pptr.setNext(ep);
    ep.setNext(ptr);
  }

  private void build_sfxlist(AffEntry sfxptr)
  {
    SfxEntry ptr;
    SfxEntry pptr;
    SfxEntry ep = (SfxEntry) sfxptr;

    /* get the right starting point */
    String key = ep.getKey();
    char flg = ep.getFlag();

    // first index by flag which must exist
    ptr = (SfxEntry)sFlag[flg];
    ep.setFlgNxt(ptr);
    sFlag[flg] = ep;


    // next index by affix string

    // handle the special case of null affix string
    if (key.length() == 0) 
    {
      // always inset them at head of list at element 0
       ptr = (SfxEntry)sStart[0];
       ep.setNext(ptr);
       sStart[0] = ep;
       return;
    }

    // now handle the normal case
    char sp = key.charAt(0) ;
    ptr = (SfxEntry)sStart[sp];
    
    /* handle the insert at top of list case */
    if ((ptr == null) || (ep.getKey().compareTo(ptr.getKey()) <= 0)) 
    {
       ep.setNext(ptr);
       sStart[sp] = ep;
       return ;
    }

    /* otherwise find where it fits in order and insert it */
    pptr = null;
    for (; ptr != null; ptr = ptr.getNext()) 
    {
      if (ep.getKey().compareTo(ptr.getKey()) <= 0) 
        break;
      pptr = ptr;
    }
    
    pptr.setNext(ep);
    ep.setNext(ptr);
  }
  
  private void process_pfx_order()
  {
    PfxEntry ptr ;

    // loop through each prefix list starting point
    for (int i = 1 ; i < Utils.SETSIZE ; i++)
    {
      ptr = (PfxEntry) pStart[i] ;

      // look through the remainder of the list
      //  and find next entry with affix that 
      // the current one is not a subset of
      // mark that as destination for NextNE
      // use next in list that you are a subset
      // of as NextEQ

      for ( ; ptr != null ; ptr = ptr.getNext())
      {
        PfxEntry nptr = ptr.getNext() ;

        for ( ; nptr != null ; nptr = nptr.getNext())
          if (!Utils.isSubset(ptr.getKey(), nptr.getKey()))
            break ;

        ptr.setNextNE(nptr) ;
        ptr.setNextEQ(null) ;

        if ((ptr.getNext() != null)
            && Utils.isSubset(ptr.getKey(), ptr.getNext().getKey()))
          ptr.setNextEQ(ptr.getNext()) ;
      }

      // now clean up by adding smart search termination strings:
      // if you are already a superset of the previous prefix
      // but not a subset of the next, search can end here
      // so set NextNE properly

      ptr = (PfxEntry) pStart[i] ;
      for ( ; ptr != null ; ptr = ptr.getNext())
      {
        PfxEntry nptr = ptr.getNext() ;
        PfxEntry mptr = null ;

        for ( ; nptr != null ; nptr = nptr.getNext())
        {
          if (!Utils.isSubset(ptr.getKey(), nptr.getKey()))
            break ;
          mptr = nptr ;
        }
        if (mptr != null)
          mptr.setNextNE(null) ;
      }
    }
  }
  
  private void process_sfx_order()
  {
    SfxEntry ptr ;

    // loop through each prefix list starting point
    for (int i = 1 ; i < Utils.SETSIZE ; i++)
    {
      ptr = (SfxEntry) sStart[i] ;

      // look through the remainder of the list
      //  and find next entry with affix that 
      // the current one is not a subset of
      // mark that as destination for NextNE
      // use next in list that you are a subset
      // of as NextEQ

      for ( ; ptr != null ; ptr = ptr.getNext())
      {
        SfxEntry nptr = ptr.getNext() ;

        for ( ; nptr != null ; nptr = nptr.getNext())
          if (!Utils.isSubset(ptr.getKey(), nptr.getKey()))
            break ;

        ptr.setNextNE(nptr) ;
        ptr.setNextEQ(null) ;

        if ((ptr.getNext() != null)
            && Utils.isSubset(ptr.getKey(), ptr.getNext().getKey()))
          ptr.setNextEQ(ptr.getNext()) ;
      }

      // now clean up by adding smart search termination strings:
      // if you are already a superset of the previous suffix
      // but not a subset of the next, search can end here
      // so set NextNE properly

      ptr = (SfxEntry) sStart[i] ;
      for ( ; ptr != null ; ptr = ptr.getNext())
      {
        SfxEntry nptr = ptr.getNext() ;
        SfxEntry mptr = null ;
        
        for ( ; nptr != null ; nptr = nptr.getNext())
        {
          if (!Utils.isSubset(ptr.getKey(), nptr.getKey()))
            break ;

          mptr = nptr ;
        }
        
        if (mptr != null)
          mptr.setNextNE(null) ;
      }
    }
  }
}
