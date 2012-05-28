/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.dictionary.myspell.MySpell;
import org.dts.spell.dictionary.myspell.Utils;
import org.dts.spell.finder.Word;

/**
 * @author DreamTangerine
 *
 */
public class OpenOfficeSpellDictionary implements SpellDictionary
{
  private File rootFile ;
  private File personalWords;
  private MySpell mySpell ;
  
  public OpenOfficeSpellDictionary(String filePath) throws IOException
  {
    initFromFiles(new File(filePath + ".dic"), new File(filePath + ".aff"), null) ;
  }  

  public OpenOfficeSpellDictionary(File file) throws IOException
  {
    String name = file.getName() ;
    File parent = file.getParentFile() ;
    initFromFiles(new File(parent, name + ".dic"), new File(parent, name + ".aff"), null) ;
  }

  public OpenOfficeSpellDictionary(String dictFileName, String affFileName) throws IOException
  {
    initFromFiles(new File(dictFileName), new File(affFileName), null) ;
  }
  
  public OpenOfficeSpellDictionary(String dictFileName, String affFileName, String userDicPath) throws IOException
  {
    initFromFiles(new File(dictFileName), new File(affFileName), new File(userDicPath)) ;
  }
  
  public OpenOfficeSpellDictionary(File dictFile, File affFile) throws IOException
  {
    initFromFiles(dictFile, affFile, null) ;
  }
  
  public OpenOfficeSpellDictionary(File dictFile, File affFile, File userDic) throws IOException
  {
    initFromFiles(dictFile, affFile, userDic) ;
  }
  
  private void initFromFiles(File dictFile, File affFile, File perFile) throws IOException
  {
    String name = dictFile.getName() ;
    int index = name.lastIndexOf('.') ;
    String rootName ;
    personalWords = perFile;
    
    if (index != -1)
      rootName = name.substring(0, index) ;
    else
      rootName = name ;
    
    rootFile = new File(dictFile.getParent(), rootName) ;
    mySpell = new MySpell(affFile.getPath(), dictFile.getPath()) ;
    
    //readPersonalWords(rootFile) ;
    readPersonalWords(personalWords);
  }
  
  public void addWord(String word) throws SpellDictionaryException
  {
    PrintWriter pw = null ;
    
    word = word.trim() ;
    
    try
    {
	    pw = new PrintWriter(
	        new OutputStreamWriter(
	            new FileOutputStream(getPersonalWordsFile(rootFile), true), 
	            										mySpell.get_dic_encoding())) ;

      mySpell.addCustomWord(word) ;
	    
	    pw.println(word) ;
    }
    catch(Exception ex)
    {
      throw new SpellDictionaryException(ex) ;
    }
    finally
    {
      try
      {
        Utils.close(pw) ;
      }
      catch (IOException e)
      {
        throw new SpellDictionaryException(e) ;
      }
    }
  }
  public String checkWord(String word)
  {
	  return mySpell.check(word);
  }
  public List<HEntry> checkList(String word)
  {
	  return  mySpell.checkList(word);
  }
  public List<HEntry> checkList(Word word)
  {
	  return  mySpell.checkList(word.getText());
  }

  public boolean isCorrect(String word)
  {
    return mySpell.spell(word) ;
  }

  public List getSuggestions(String word)
  {
    return mySpell.suggest(word) ;
  }
  public List getSuggestions(Word word)
  {
	  return mySpell.suggest(word.toString()) ;
  }
  
  private File getPersonalWordsFile(File rootFile)
  {
      if(personalWords != null)
          return personalWords;
      
      return new File(rootFile.getParent(), rootFile.getName() + ".per") ;
  }
  
  private void readPersonalWords(File rootFile) throws IOException
  {
	  if (true) return;
    BufferedReader rd = null ;

    try
    {
      File personalFile = getPersonalWordsFile(rootFile) ;
      
      if (personalFile.exists() && !personalFile.isDirectory())
      {
	      rd = new BufferedReader(
	          new InputStreamReader(
	              new FileInputStream(getPersonalWordsFile(rootFile)), 
	              										mySpell.get_dic_encoding())) ;
	      
		    String line = rd.readLine() ;
		    
		    while (line != null)
		    {
		      mySpell.addCustomWord(line.trim()) ;
		      line = rd.readLine() ;
		    }
      }
    }
    finally
    {
      Utils.close(rd) ;
    }
  }
}
