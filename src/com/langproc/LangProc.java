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
	public int m_index;
	public int m_hypo_index;
	public String m_word;
	public String m_base_word;
	public String m_dict_tags;
	public java.util.HashSet<String> m_tags = new java.util.HashSet<String>();
	
	public TaggedWord(int index, String word, String base_word, String dict_tags)
	{
		m_index = index;
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
	public void setHypotesisIndex(int hyp_index)
	{
		m_hypo_index = hyp_index;
	}
	
	public String toString()
	{
		StringBuffer b = new StringBuffer(200);
		b.append(m_index);
		b.append(" ");
		b.append(m_word);
		b.append("<-");
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
	// difference between base form and corrected
	public java.util.regex.Pattern m_diff_pattern;
	// tags required for the rule
	public java.util.List<String> m_req_tags = new java.util.LinkedList<String>();
	// tags to add
	public java.util.List<String> m_put_tags = new java.util.LinkedList<String>();
	
	TagRule(String word_pattern, String base_pattern, String dict_pattern, String diff_pattern, String req_tags, String put_tags)
	{
		m_word_pattern = Pattern.compile(word_pattern, 0);
		m_base_pattern = Pattern.compile(base_pattern, 0);
		m_dict_pattern = Pattern.compile(dict_pattern, 0);
		m_diff_pattern = Pattern.compile(diff_pattern, 0);
		
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
		
		int i=0;
		int minlen = Math.min(w.m_word.length(), w.m_base_word.length());
		for(; i<minlen && w.m_word.charAt(i)==w.m_base_word.charAt(i); ++i) {}
		String diff = w.m_word.substring(i);
		
		if (m_word_pattern.matcher(w.m_word).matches()
				&& m_base_pattern.matcher(w.m_base_word).matches()
				&& m_dict_pattern.matcher(w.m_dict_tags).matches()
				&& m_diff_pattern.matcher(diff).matches() )
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

class SentenceWord
{
	public int m_index;
	public java.util.Vector<TaggedWord> m_hypotheses = new java.util.Vector<TaggedWord>();
	public java.util.TreeMap<String, SentenceWord> m_dependencies = new java.util.TreeMap<String, SentenceWord>();
	public TaggedWord m_word=null;
	
	public SentenceWord(int index) { m_index = index; m_word = null; }
	
	public void setClosed(TaggedWord meaning) { m_word = meaning; }
	public void setOpen() { m_word = null; m_dependencies.clear(); }
	public void addChild(String role, SentenceWord w)
	{
		m_dependencies.put(role, w);
	}
	
	public void addHypothesis(TaggedWord w)
	{
		w.setHypotesisIndex(m_hypotheses.size());
		m_hypotheses.addElement(w);
	}
	
	void findHypothesisWithTags(java.util.List<TaggedWord> out_list, java.util.List<String> tags)
	{
		for(TaggedWord t : m_hypotheses)
		{
			boolean good = true;
			for(String tag: tags)
			{
				if (!t.hasTag(tag)) { good=false; break; }
			}
			if (good) out_list.add(t);
		}
	}
	
	public void print()
	{
		System.out.print(m_index);
		System.out.print(" ");
		if (m_word!=null)
		{
			System.out.print("* ");
			System.out.print(m_word);
		}
		else
		{
			for(TaggedWord t : m_hypotheses)
			{
				System.out.print(t);
				
				System.out.print(" || ");
			}
		}
	}
}

class Sentence
{
	public java.util.Vector<SentenceWord> m_words = new java.util.Vector<SentenceWord>();
	
	public int numWords() { return m_words.size(); }
	
	public void addWord(SentenceWord w)
	{
		m_words.addElement(w);
	}
	
	SentenceWord sentenceWordAt(int index) { return m_words.elementAt(index); }
	
	void findOpenWordsWithTags(java.util.List<TaggedWord> out_list, int index_from, int index_to, java.util.List<String> tags)
	{	
		if (index_to >= m_words.size()) index_to = m_words.size()-1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size()-1;
		if (index_from < 0) index_from = 0;
		
		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;
		
		for(int i=index_from; i!=index_to; i+=dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			sw.findHypothesisWithTags(out_list, tags);
		}
	}
	
	public void print()
	{
		for(SentenceWord w:m_words)
		{
			w.print();
			System.out.println();
		}
	}
}

class SentenceProcessor
{
	private static java.util.List<String> stringList(String s)
	{
		java.util.List<String> sl = new java.util.Vector<String>();
		CharSequenceWordFinder wf = new CharSequenceWordFinder(s);
		while(wf.hasNext())
		{
			Word w = wf.next();
			sl.add(w.toString());
		}
		return sl;
	}

	public void parsePredicate(Sentence s, SentenceWord w)
	{
		java.util.List<TaggedWord> out_list = new java.util.Vector<TaggedWord>();
		
		// search negations
		s.findOpenWordsWithTags(out_list, w.m_index-1, w.m_index-1, stringList("Neg"));
		if (out_list.size()!=0)
		{
			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
			sw.setClosed(out_list.get(0));
			w.addChild("Neg", sw );
		}
		out_list.clear();
		
		// search same-level words. !Add this word required tags!!!
		s.findOpenWordsWithTags(out_list, w.m_index+1, s.numWords(), stringList("Дієсл"));
		if (out_list.size()!=0)
		{
			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
			sw.setClosed(out_list.get(0));
			w.addChild("Sibling", sw );
		}
		out_list.clear();
		
		// search Subject
		s.findOpenWordsWithTags(out_list, 0, w.m_index-1, stringList("Імен, c1"));
		if (out_list.size()!=0)
		{
			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
			sw.setClosed(out_list.get(0));
			w.addChild("Subj", sw );
		}
		out_list.clear();
		
		// search Object
		s.findOpenWordsWithTags(out_list, w.m_index+1, s.numWords(), stringList("Імен, c4"));
		if (out_list.size()!=0)
		{
			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
			sw.setClosed(out_list.get(0));
			w.addChild("Obj", sw );
		}
		out_list.clear();
	}
	
	public void processSentence(Sentence s)
	{
		java.util.List<TaggedWord> out_list = new java.util.Vector<TaggedWord>();
		s.findOpenWordsWithTags(out_list, 0, s.numWords(), stringList("Дієсл"));
		
		System.out.println("Search for Main Predicate (the first predicate in the sentence):");
		for(TaggedWord w : out_list)
		{
			// there should be some scoring system and choice :-) 
			System.out.println(w);
			s.sentenceWordAt(w.m_index).setClosed(w);
			
			parsePredicate(s, s.sentenceWordAt(w.m_index));
			// parsing
			s.print();
			
			s.sentenceWordAt(w.m_index).setOpen();
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
	java.util.HashSet<String> m_negations = new java.util.HashSet<String>();
	java.util.HashSet<String> m_conjunction = new java.util.HashSet<String>();
	
	java.util.List<TagRule> m_tag_rules = new java.util.LinkedList<TagRule>();
	
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
	  fillSet(m_particles, "ні, ані");
	  fillSet(m_particles, "тільки, лише, хоч, хоч би, виключно");
	  fillSet(m_particles, "і, й, та, таки, аж, навіть, вже, ж, же, бо");
	  fillSet(m_particles, "хай, нехай, бодай, давай");
	  fillSet(m_particles, "би, б, ну");
	  fillSet(m_particles, "так, отак, еге, авжеж, отож, гаразд");
	  fillSet(m_particles, "чи, невже, хіба, та ну, що за");
	  fillSet(m_particles, "мов, мовби, немов, наче, неначе, начебто, ніби, нібито");
	  fillSet(m_particles, "як, що за тому");
	  
	  fillSet(m_negations, "не");
	  
	  fillSet(m_conjunction, "і, або, й, та" );
	  
	  m_tag_rules.add(new TagRule(".*ого", ".*ий", ".*", ".*", "Прикм", "с2, Одн, M, N"));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*","Прикм", "с2, Мн, M, F"));
	  
	  m_tag_rules.add(new TagRule(".*ому", ".*ий",  ".*", ".*", "Прикм", "c3, Одн, M, N"));
	  m_tag_rules.add(new TagRule(".*ій", ".*ий",  ".*", ".*", "Прикм", "c3, Одн, F"));
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", "Прикм", "c3, Мн, M, N, F"));
	  
	  m_tag_rules.add(new TagRule(".*ий", ".*ий",  ".*", ".*", "Прикм", "c4, Одн, M"));
	  m_tag_rules.add(new TagRule(".*е", ".*ий",  ".*", ".*", "Прикм", "c4, Одн, N"));
	  m_tag_rules.add(new TagRule(".*у", ".*ий",  ".*", ".*", "Прикм", "c4, Одн, F")); 
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", "Прикм", "c4, Мн, M"));
	  
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", "Прикм", "c5, Одн, M"));
	  m_tag_rules.add(new TagRule(".*ими", ".*ий",  ".*", ".*", "Прикм", "c5, Мн, M"));
	  m_tag_rules.add(new TagRule(".*ім", ".*ий",  ".*", ".*", "Прикм", "c6, Одн, M"));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", "Прикм", "c6, Мн, M"));

	  m_tag_rules.add(new TagRule(".*ої", ".*ий", ".*", ".*", "Прикм", "с2, Одн, F"));
//	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*","Прикм", "с2, Мн"));
	  m_tag_rules.add(new TagRule(".*ому", ".*ий",  ".*", ".*", "Прикм", "c3, Одн, F"));
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", "Прикм", "c3, Мн, F"));
	  m_tag_rules.add(new TagRule(".*ий", ".*ий",  ".*", ".*", "Прикм", "c4, Одн, F"));
	  m_tag_rules.add(new TagRule(".*ого", ".*ий",  ".*", ".*", "Прикм", "c4, Одн, F"));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", "Прикм", "c4, Мн, F"));
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", "Прикм", "c5, Одн, F"));
	  m_tag_rules.add(new TagRule(".*ими", ".*ий",  ".*", ".*", "Прикм", "c5, Мн, F"));
	  m_tag_rules.add(new TagRule(".*ім", ".*ий",  ".*", ".*", "Прикм", "c6, Одн, F"));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", "Прикм", "c6, Мн, F"));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "", "Імен", "c1, c4, Одн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "а|у", "Імен", "c2, Одн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "у", "Імен", "c3, Одн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ом", "Імен", "c5, Одн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "у", "Імен", "c6, Одн, M"));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "и", "Імен", "c1, Мн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ів", "Імен", "c2, Мн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ам", "Імен", "c3, Мн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "и", "Імен", "c4, Мн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ами", "Імен", "c5, Мн, M"));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ах", "Імен", "c6, Мн, M"));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ю",  "Дієсл", "o1, Одн, Pres"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "єш", "Дієсл", "o2, Одн, Pres"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "є",  "Дієсл", "o3, Одн, Pres"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ємо",  "Дієсл", "o1, Мн, Pres"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "єте", "Дієсл", "o2, Мн, Pres"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ють",  "Дієсл", "o3, Мн, Pres"));

	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "в",  "Дієсл", "o1, o2, o3, Одн, Past"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ла", "Дієсл", "o3, Одн, Past, F"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ло", "Дієсл", "o3, Одн, Past, N"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ли", "Дієсл", "o1, o2, o3, Мн, Past"));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "иму",  "Дієсл", "o1, Одн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имеш", "Дієсл", "o2, Одн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "име",  "Дієсл", "o3, Одн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "емо", "Дієсл", "o1, Мн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имете", "Дієсл", "o2, Мн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имуть", "Дієсл", "o3, Мн, Ftr"));
	  
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "ду",  "Дієсл", "o1, Одн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "деш", "Дієсл", "o2, Одн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "де",  "Дієсл", "o3, Одн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "демо", "Дієсл", "o1, Мн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "дете", "Дієсл", "o2, Мн, Ftr"));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "дуть", "Дієсл", "o3, Мн, Ftr"));

	  m_tag_rules.add(new TagRule(".*", ".*",  ".*[aioe].*", ".*", "", "Імен"));
	  m_tag_rules.add(new TagRule(".*", ".*ти",  ".*", ".*", "", "Дієсл"));
	  m_tag_rules.add(new TagRule(".*", ".*ий",  ".*", ".*", "", "Прикм"));
	  m_tag_rules.add(new TagRule(".*", ".*ко|.*но",  ".*", ".*", "", "Присл"));
	  m_tag_rules.add(new TagRule(".*", ".*чи",  ".*", ".*", "", "Дієприсл"));
	}
		
	
	Matcher createMatcher(CharSequence text, String regexp, int flags)
	{
	   return Pattern.compile(regexp, flags).matcher(text) ;
	}
	
	private void addWordForms(Sentence ss, String word)
	{
		int index = ss.numWords();
		SentenceWord sw = new SentenceWord(index);
		
	      List<HEntry> list = m_dict.checkList(word.toLowerCase());
	      
	      if (list.size()==0)
	      {
	    	  TaggedWord w = new TaggedWord(index, word, word, "Punct");
	    	  sw.addHypothesis(w);
	      }
	      
	      for(HEntry s:list)
	      {
	    	  TaggedWord w = new TaggedWord(index, word.toLowerCase(), s.word, s.astr);
	    	  
	    	  if (word.equals(word.toUpperCase())) w.addTag("Cap");
	    	  if (Character.isUpperCase(word.charAt(0))) w.addTag("StartCap");
	    	  if (word.equals(word.toLowerCase())) w.addTag("Low");
	    	  if (word.toLowerCase().equals(s.word)) w.addTag("Base");
	    	  
	    	  //System.out.print( "   " + s.word + " " + s.astr + " " );

	    	  if (m_prepositions.contains(s.word)) w.addTag("Prep");
	    	  if (m_parenthesis_words.contains(s.word)) w.addTag("Help");
	    	  if (m_particles.contains(s.word)) w.addTag("Part");
	    	  if (m_negations.contains(s.word)) w.addTag("Neg");
	    	  if (m_pronoun.contains(s.word)) w.addTag("Pron");
	    	  if (m_conjunction.contains(s.word)) w.addTag("Conj");
	    	  
	    	  ApplyRules(w);
	    	  
	    	  sw.addHypothesis(w);
	    	  
	    	  System.out.print("|" + w);
	      }
	      ss.addWord(sw);
	}

	private void checkGrammar(String txt)
	{
		//CharSequenceWordFinder wf = new CharSequenceWordFinder(txt);
		CharSequenceWordFinder wf = new CharSequenceWordFinder( Pattern.compile("[АБВГҐДЕЄЖЗІЙИЇКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзійиїклмнопрстуфхцчшщьюя'’]+|,|\\.|\\?|!|\"|\'|;|:|\\)|\\(").matcher(txt) );
		Sentence ss = new Sentence();
		
		SentenceProcessor sp = new SentenceProcessor();
		
		while(wf.hasNext())
		{
			Word w = wf.next();
			if (w.toString().equals("."))
			{
				ss.print();
				sp.processSentence(ss);
				ss = new Sentence();
				continue;
			}
			System.out.print(w.toString());
			int s = w.toString().length();
			for(int i= 20; i>s; --i) System.out.print(" ");
			addWordForms(ss, w.toString());
			System.out.println();
		}
		ss.print();
		sp.processSentence(ss);
	}
	
  private void test(SpellChecker checker, String txt)
  {
    Word badWord = checker.checkSpell(txt) ;

    if (badWord == null)
      System.out.println("All OK!!!");
    else
    {
      System.out.println("Bad words: " + badWord);
      List<String> list = checker.getDictionary().getSuggestions(badWord);
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