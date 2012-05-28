package com.langproc;

import java.util.List;


import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.Word;
import org.dts.spell.finder.CharSequenceWordFinder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TaggedWord
{
	public String m_word;
	public String m_base_word;
	public String m_dict_tags;
	public java.util.HashSet<String> m_tags = new java.util.HashSet<String>();
	
	public TaggedWord(String word, String base_word, String dict_tags)
	{
		m_word = word;
		m_base_word = base_word;
		m_dict_tags = dict_tags;
	}
	
	public boolean hasTag(String tag)
	{
		return m_tags.contains(tag);
	}
	public void addTag(String tag)
	{
		m_tags.add(tag);
	}
	
	public String toString()
	{
		StringBuffer b = new StringBuffer(200);
		b.append(m_word);
		b.append(" ");
		b.append(m_base_word);
		b.append(" ");
		b.append(m_dict_tags);
		for(String s: m_tags)
		{
			b.append(" ");
			b.append(s);
		}
		return b.toString();
	}
}

class TagRule
{
	// pattern of processed word (.*ого)
	public java.util.regex.Pattern m_word_pattern;
	// pattern of base word (.*ий)
	public java.util.regex.Pattern m_base_pattern;
	// tags required for the rule
	public java.util.regex.Pattern m_dict_pattern;
	// tags required for the rule
	public java.util.List<String> m_req_tags = new java.util.LinkedList();
	// tags to add
	public java.util.List<String> m_put_tags = new java.util.LinkedList();
	
	TagRule(String word_pattern, String base_pattern, String dict_pattern,  String req_tags, String put_tags)
	{
		m_word_pattern = Pattern.compile(word_pattern, 0);
		m_base_pattern = Pattern.compile(base_pattern, 0);
		m_dict_pattern = Pattern.compile(dict_pattern, 0);
		
		CharSequenceWordFinder wf1 = new CharSequenceWordFinder(req_tags);
		while(wf1.hasNext())
		{
			Word w = wf1.next();
			m_req_tags.add(w.toString());
		}
		
		CharSequenceWordFinder wf2 = new CharSequenceWordFinder(put_tags);
		while(wf2.hasNext())
		{
			Word w = wf2.next();
			m_put_tags.add(w.toString());
		}
	}
	
	public boolean applyRule(TaggedWord w)
	{
		for(String s : m_req_tags)
		{
			if (!w.hasTag(s)) return false;
		}
		boolean has_all = true;
		for(String s : m_put_tags)
		{
			if (!w.hasTag(s)) { has_all = false; break; }
		}
		if (has_all) return false;
		
		if (m_word_pattern.matcher(w.m_word).matches()
				&& m_base_pattern.matcher(w.m_base_word).matches()
				&& m_dict_pattern.matcher(w.m_dict_tags).matches())
		{
			for(String s : m_put_tags)
			{
				w.addTag(s);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
}

public class LangProc
{
	OpenOfficeSpellDictionary m_dict;
	java.util.HashSet<String> m_pronoun = new java.util.HashSet<String>();
	java.util.HashSet<String> m_prepositions = new java.util.HashSet<String>();
	java.util.HashSet<String> m_parenthesis_words = new java.util.HashSet<String>();
	java.util.HashSet<String> m_particles = new java.util.HashSet<String>();
	java.util.HashSet<String> m_conjunction = new java.util.HashSet<String>();
	
	java.util.List<TagRule> m_tag_rules = new java.util.LinkedList();
	
	static void fillSet(java.util.HashSet<String> set, String s)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(s);
		while(wf.hasNext())
		{
			Word w = wf.next();
			set.add(w.toString());
		}
	}
	
	void ApplyRules(TaggedWord w)
	{
		boolean applyed;
		do
		{
			applyed = false;
			for(TagRule r : m_tag_rules)
			{
				if (r.applyRule(w)) applyed = true;
			}
		} while (applyed);
	}
	
	LangProc(OpenOfficeSpellDictionary dict)
	{
	  m_dict = dict;
	  m_prepositions.add("перед");
	  m_prepositions.add("як");
	  m_prepositions.add("між");
	  m_prepositions.add("за");
	  m_prepositions.add("після");
	  m_prepositions.add("над");
	  m_prepositions.add("під");
	  m_prepositions.add("через");
	  m_prepositions.add("поза");
	  m_prepositions.add("без");
	  m_prepositions.add("в");
	  m_prepositions.add("у");
	  m_prepositions.add("від");
	  m_prepositions.add("для");
	  m_prepositions.add("по");
	  m_prepositions.add("через");
	  m_prepositions.add("при");
	  m_prepositions.add("про");
	  m_prepositions.add("над");
	  m_prepositions.add("під");
	  m_prepositions.add("до");
	  m_prepositions.add("з");
	  m_prepositions.add("ради");
	  m_prepositions.add("задля");
	  m_prepositions.add("поза");
	  m_prepositions.add("щодо");
	  m_prepositions.add("близько");
	  m_prepositions.add("внаслідок");
	  m_prepositions.add("після");
	  m_prepositions.add("поруч");
	  m_prepositions.add("перед");
	  m_prepositions.add("протягом");
	  m_prepositions.add("під час");
	  m_prepositions.add("з допомогою");
	  m_prepositions.add("у зв’язку");
	  m_prepositions.add("завдяки");
	  m_prepositions.add("незважаючи на");
	  m_prepositions.add("з-за");
	  m_prepositions.add("з-над");
	  m_prepositions.add("з-поза");
	  m_prepositions.add("з-під");
	  m_prepositions.add("з-попід");
	  m_prepositions.add("з-серед");
	  m_prepositions.add("із-за");
	  m_prepositions.add("в силу");
	  m_prepositions.add("згідно з");
	  
	  
	  m_parenthesis_words.add("напевно");
	  m_parenthesis_words.add("безперечно");
	  m_parenthesis_words.add("звичайно");
	  m_parenthesis_words.add("може");
	  m_parenthesis_words.add("мабуть");
	  m_parenthesis_words.add("зрозуміло");
	  m_parenthesis_words.add("дійсно");
	  m_parenthesis_words.add("певне");
	  m_parenthesis_words.add("певно");
	  m_parenthesis_words.add("здається");
	  m_parenthesis_words.add("боюся");
	  m_parenthesis_words.add("сподіваюся");
	  m_parenthesis_words.add("очевидно");
	  m_parenthesis_words.add("по-перше");
	  m_parenthesis_words.add("по-друге");
	  m_parenthesis_words.add("далі");
	  m_parenthesis_words.add("до речі");
	  m_parenthesis_words.add("нарешті");
	  m_parenthesis_words.add("на щастя");
	  m_parenthesis_words.add("на жаль");
	  m_parenthesis_words.add("на нещастя");
	  m_parenthesis_words.add("дивна річ");
	  m_parenthesis_words.add("взагалі");
	  m_parenthesis_words.add("іншими словами");
	  m_parenthesis_words.add("можна сказати");
	  m_parenthesis_words.add("так би мовити");
	  m_parenthesis_words.add("як кажуть");
	  
	  fillSet(m_pronoun, "я ти він вона воно ми ви вони");
	  
	  fillSet(m_particles, "ось, осьде, он, от, ото, це, оце");
	  fillSet(m_particles, "якраз, ледве, просто, прямо, власне, майже, саме");
	  fillSet(m_particles, "не, ні, ані");
	  fillSet(m_particles, "тільки, лише, хоч, хоч би, виключно");
	  fillSet(m_particles, "і, й, та, таки, аж, навіть, вже, ж, же, бо");
	  fillSet(m_particles, "хай, нехай, бодай, давай");
	  fillSet(m_particles, "би, б, ну");
	  fillSet(m_particles, "так, отак, еге, авжеж, отож, гаразд");
	  fillSet(m_particles, "чи, невже, хіба, та ну, що за");
	  fillSet(m_particles, "мов, мовби, немов, наче, неначе, начебто, ніби, нібито");
	  fillSet(m_particles, "як, що за тому");
	  
	  fillSet(m_conjunction, "і, або, й, та" );
	  
	  m_tag_rules.add(new TagRule(".*ого", ".*ий", ".*", "Прикм", "Род, Одн"));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", "Прикм", "Род, Мн"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*[aioe].*", "", "Імен"));
	  
	  m_tag_rules.add(new TagRule(".*", ".*ти",  ".*", "", "Дієсл"));
	  m_tag_rules.add(new TagRule(".*", ".*ий",  ".*", "", "Прикм"));
	  m_tag_rules.add(new TagRule(".*", ".*ко|.*но",  ".*", "", "Присл"));
	  m_tag_rules.add(new TagRule(".*", ".*чи",  ".*", "", "Дієприсл"));
	}
		
	
	Matcher createMatcher(CharSequence text, String regexp, int flags)
	{
	   return Pattern.compile(regexp, flags).matcher(text) ;
	}
	
	private void printForms(String txt)
	{
	      List<HEntry> list = m_dict.checkList(txt.toLowerCase());
	      for(HEntry s:list)
	      {
	    	  TaggedWord w = new TaggedWord(txt.toLowerCase(), s.word, s.astr);
	    	  if (txt.equals(txt.toUpperCase())) w.addTag("Cap");
	    	  if (Character.isUpperCase(txt.charAt(0))) w.addTag("StartCap");
	    	  if (txt.equals(txt.toLowerCase())) w.addTag("Low");
	    	  if (txt.toLowerCase().equals(s.word)) w.addTag("Base");
	    	  
	    	  //System.out.print( "   " + s.word + " " + s.astr + " " );

	    	  if (m_prepositions.contains(s.word)) w.addTag("Прийм");
	    	  if (m_parenthesis_words.contains(s.word)) w.addTag("Встав");
	    	  if (m_particles.contains(s.word)) w.addTag("Част");
	    	  if (m_pronoun.contains(s.word)) w.addTag("Займ");
	    	  if (m_conjunction.contains(s.word)) w.addTag("Спол");
	    	  
	    	  ApplyRules(w);
	    	  
	    	  System.out.println(" " + w);
	      }
	}

	private void checkGrammar(String txt)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(txt);
		while(wf.hasNext())
		{
			Word w = wf.next();
			System.out.println(w.toString());
			printForms(w.toString());
			
		}
	}
	
  private void test(SpellChecker checker, String txt)
  {
    Word badWord = checker.checkSpell(txt) ;

    if (badWord == null)
      System.out.println("All OK!!!");
    else
    {
      System.out.println("Bad words: " + badWord);
      List list = checker.getDictionary().getSuggestions(badWord);
      List<String> wl = list;
      for (String s : wl)
      {
    	  System.out.println(s);
      }
    }
  }
  


  public static void main(String[] args)
  {
	try
	{
	  (new LangProc(new OpenOfficeSpellDictionary("uk_UA")))
	  	.checkGrammar("Прийменники не мають самостійного лексичного значення, тому членами речення не виступають. Належачи до іменників, числівників, займенників, вони входять до складу другорядних членів речення." + 
"Прийменником називається службова частина мови, яка разом з відмінковими закінченнями іменників (або займенників) служить для вираження підрядних зв’язків між словами в реченні. Приклади прийменників:");
	  
//	  (new LangProc(new OpenOfficeSpellDictionary("uk_UA")))
//	  	.checkGrammar("Сіл селами села Почесного Тінь кінь ніч грач");
    }
    catch (Exception e)
    {
      e.printStackTrace() ;
    }
  }
}