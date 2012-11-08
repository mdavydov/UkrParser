package com.langproc;

import java.util.List;
import java.util.Vector;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.Word;
import org.dts.spell.finder.CharSequenceWordFinder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WT
{
	public static final long PLURAL	= (1L<<0);
	public static final long SINGLE	= (1L<<1);
	public static final long COUNT_MASK	= PLURAL | SINGLE;
	
	public static final long PERSON1 = (1L<<2); 
	public static final long PERSON2 = (1L<<3); 
	public static final long PERSON3 = (1L<<4);
	public static final long PERSON_MASK	= PERSON1 | PERSON2 | PERSON3;
	
	public static final long MALE	= (1L<<5);
	public static final long FEMALE	= (1L<<6);
	public static final long NEUTRAL	= (1L<<7);
	public static final long GENDER_MASK = MALE | FEMALE | NEUTRAL;
	
	public static final long CASUS1 =  (1L<<8);
	public static final long CASUS2 =  (1L<<9);
	public static final long CASUS3 =  (1L<<10);
	public static final long CASUS4 =  (1L<<11);
	public static final long CASUS5 =  (1L<<12);
	public static final long CASUS6 =  (1L<<13);
	public static final long CASUS7 =  (1L<<14);
	
	public static final long CASUS_MASK = CASUS1 | CASUS2 | CASUS3 | CASUS4 | CASUS5 | CASUS6 | CASUS7;
	
	public static final long NOUN = (1L<<15);
	public static final long VERB = (1L<<16);
	public static final long ADV =  (1L<<17);	// adverb
	public static final long ADJ =  (1L<<18);	// adjective
	public static final long PRONOUN = (1L<<19); // pronoun (I, you, they)
	public static final long NEGATION =  (1L<<20);
	public static final long PUNCT = (1L<<21);
	public static final long CONJ = (1L<<22);		// Conjunction
	public static final long NUMERAL = (1L<<23);	// Numeral
	public static final long PARTICLE = (1L<<24);
	public static final long ADVPART =  (1L<<25); // Adverbial participle
	public static final long ADJPART =  (1L<<26); // Adjective participle
	public static final long PREPOS =  (1L<<27); // preposition
	public static final long HELPWORD =  (1L<<28); // parenthesis words
	
	public static final long PART_OF_SPEECH_MASK = NOUN | VERB | ADV | ADJ | PRONOUN | NEGATION |
					PUNCT | CONJ | NUMERAL | PARTICLE | ADVPART | ADJPART;
	
	public static final long PERFECT = (1L<<29);
	public static final long SIMPLE =  (1L<<30);
	public static final long PERFECTION_MASK = PERFECT | SIMPLE;
	
	
	public static final long PAST = (1L<<31);
	public static final long PRESENT = (1L<<32);
	public static final long FUTURE =  (1L<<33);
	public static final long TIME_MASK = PAST | PRESENT | FUTURE;
}

class WordTags
{
	long m_tags;
	
	WordTags() {m_tags = 0;}
	WordTags(WordTags other) { m_tags = other.m_tags; }
	WordTags(long tags) { m_tags = tags; }
	
	public void setTags(long tags) {m_tags |= tags;}
	public boolean hasAllTags(long tags) { return 0==(~m_tags & tags);}
	public boolean hasTag(long tag) { return 0==(~m_tags & tag);}
	public boolean hasSomeTags(long tags) { return 0!=(m_tags & tags);}
	
	public void setTags(WordTags t) {m_tags |= t.m_tags;}
	public boolean hasAllTags(WordTags t) { return 0==(~m_tags & t.m_tags);}
	public boolean hasSomeTags(WordTags t) { return 0!=(m_tags & t.m_tags);}
	
	public long getPartOfSpeech() { return m_tags & WT.PART_OF_SPEECH_MASK; }
	public long getGender() { return m_tags & WT.GENDER_MASK; }
	public long getCount() { return m_tags & WT.COUNT_MASK; }
	public long getPerson() { return m_tags & WT.PERSON_MASK; }
	public long getCasus() { return m_tags & WT.CASUS_MASK; }
	public long getTime() { return m_tags & WT.TIME_MASK; }
	public long getPerfection() { return m_tags & WT.PERFECTION_MASK;}
	
	public boolean hasPartOfSpeech() { return 0!=(m_tags & WT.PART_OF_SPEECH_MASK); }
	public boolean hasGender() { return 0!=(m_tags & WT.GENDER_MASK); }
	public boolean hasCount() { return 0!=(m_tags & WT.COUNT_MASK); }
	public boolean hasPerson() { return 0!=(m_tags & WT.PERSON_MASK); }
	public boolean hasCasus() { return 0!=(m_tags & WT.CASUS_MASK); }
	public boolean hasTime() { return 0!=(m_tags & WT.TIME_MASK); }
	public boolean hasPerfection() { return 0!=(m_tags & WT.PERFECTION_MASK);}
	
	public boolean samePart(WordTags o) { return 0!=(m_tags & o.m_tags & WT.PART_OF_SPEECH_MASK); }
	public boolean sameGender(WordTags o) { return 0!=(m_tags & o.m_tags & WT.GENDER_MASK); }
	public boolean sameCount(WordTags o) { return 0!=(m_tags & o.m_tags & WT.COUNT_MASK); }
	public boolean samePerson(WordTags o) { return 0!=(m_tags & o.m_tags & WT.PERSON_MASK); }
	public boolean sameCasus(WordTags o) { return 0!=(m_tags & o.m_tags & WT.CASUS_MASK); }
	public boolean sameTime(WordTags o) { return 0!=(m_tags & o.m_tags & WT.TIME_MASK); }
	public boolean samePerfection(WordTags o) { return 0!=(m_tags & o.m_tags & WT.PERFECTION_MASK);}
	
	public String toString()
	{
		StringBuffer b = new StringBuffer(200);
		if (hasSomeTags(WT.PLURAL)) b.append("PL ");
		if (hasSomeTags(WT.SINGLE)) b.append("SG ");
		if (hasSomeTags(WT.PERSON1)) b.append("p1 ");
		if (hasSomeTags(WT.PERSON2)) b.append("p2 ");
		if (hasSomeTags(WT.PERSON3)) b.append("p3 ");
		if (hasSomeTags(WT.MALE)) b.append("M ");
		if (hasSomeTags(WT.FEMALE)) b.append("F ");
		if (hasSomeTags(WT.NEUTRAL)) b.append("N ");
		if (hasSomeTags(WT.CASUS1)) b.append("c1 ");
		if (hasSomeTags(WT.CASUS2)) b.append("c2 ");
		if (hasSomeTags(WT.CASUS3)) b.append("c3 ");
		if (hasSomeTags(WT.CASUS4)) b.append("c4 ");
		if (hasSomeTags(WT.CASUS5)) b.append("c5 ");
		if (hasSomeTags(WT.CASUS6)) b.append("c6 ");
		if (hasSomeTags(WT.CASUS7)) b.append("c7 ");
		if (hasSomeTags(WT.NOUN)) b.append("NOUN ");
		if (hasSomeTags(WT.VERB)) b.append("VERB ");
		if (hasSomeTags(WT.ADV)) b.append("ADV ");
		if (hasSomeTags(WT.ADJ)) b.append("ADJ ");
		if (hasSomeTags(WT.PRONOUN)) b.append("PRONOUN ");
		if (hasSomeTags(WT.NEGATION)) b.append("NEGATION ");
		if (hasSomeTags(WT.PUNCT)) b.append("PUNCT ");
		if (hasSomeTags(WT.CONJ)) b.append("CONJ ");
		if (hasSomeTags(WT.NUMERAL)) b.append("NUMERAL ");
		if (hasSomeTags(WT.PARTICLE)) b.append("PARTICLE ");
		if (hasSomeTags(WT.ADVPART)) b.append("ADVPART ");
		if (hasSomeTags(WT.ADJPART)) b.append("ADJPART ");
		if (hasSomeTags(WT.PERFECT)) b.append("PERFECT ");
		if (hasSomeTags(WT.SIMPLE)) b.append("SIMPLE ");
		if (hasSomeTags(WT.PAST)) b.append("PAST ");
		if (hasSomeTags(WT.PRESENT)) b.append("PRESENT ");
		if (hasSomeTags(WT.FUTURE)) b.append("FUTURE ");
		return b.toString();
	}
}

class TaggedWord
{
	
	int m_sentence_pos;
	int m_hypo_index;
	String m_word;
	String m_base_word;
	String m_dict_tags;	// tags from SpellChecker dictionary
	WordTags m_tags = new WordTags();
	
	public TaggedWord(int sentence_pos, String word, String base_word, String dict_tags)
	{
		m_sentence_pos = sentence_pos;
		m_word = word;
		m_base_word = base_word;
		m_dict_tags = dict_tags;
	}
	int getSentencePos() { return m_sentence_pos; }
	public WordTags getTags() { return m_tags; }
	public boolean hasAllTags(WordTags tags) { return m_tags.hasAllTags(tags); }
	public boolean hasAllTags(long tags) { return m_tags.hasAllTags(tags); }
	public void addTags(WordTags tags) { m_tags.setTags(tags); }
	public void addTags(long tags) { m_tags.setTags(tags); }
	public void setHypotesisIndex(int hyp_index) { m_hypo_index = hyp_index; }
	
	public String toString()
	{
		StringBuffer b = new StringBuffer(200);
		b.append(m_sentence_pos);
		b.append(" ");
		b.append(m_word);
		b.append("<-");
		b.append(m_base_word);
		b.append(" ");
		b.append(m_dict_tags);
		b.append(" ");	
		b.append(m_tags);
		return b.toString();
	}
	public int hashCode() { return toString().hashCode();}
	public boolean equals(TaggedWord w) { return toString().equals(w.toString());}
}

/*******************************************************************************
 * 
 * @author Maksym Davydov
 * TagRule class represents single rule that can be applied to obtain more word tags 
 */

class TagRule
{
	// pattern of processed word (.*ого)
	java.util.regex.Pattern m_word_pattern;
	// pattern of base word (.*ий)
	java.util.regex.Pattern m_base_pattern;
	// tags required for the rule
	java.util.regex.Pattern m_dict_pattern;
	// difference between base form and corrected
	java.util.regex.Pattern m_diff_pattern;
	// tags required for the rule
	
	
	WordTags m_req_tags;
	// tags to add
	WordTags m_put_tags;
	
	TagRule(String word_pattern, String base_pattern, String dict_pattern, String diff_pattern, long req_tags, long put_tags)
	{
		m_word_pattern = Pattern.compile(word_pattern, 0);
		m_base_pattern = Pattern.compile(base_pattern, 0);
		m_dict_pattern = Pattern.compile(dict_pattern, 0);
		m_diff_pattern = Pattern.compile(diff_pattern, 0);
		
		m_req_tags = new WordTags(req_tags);
		m_put_tags = new WordTags(put_tags);
	}
	TagRule(String word_pattern, String base_pattern, String dict_pattern, String diff_pattern, WordTags req_tags, WordTags put_tags)
	{
		m_word_pattern = Pattern.compile(word_pattern, 0);
		m_base_pattern = Pattern.compile(base_pattern, 0);
		m_dict_pattern = Pattern.compile(dict_pattern, 0);
		m_diff_pattern = Pattern.compile(diff_pattern, 0);
		
		m_req_tags = new WordTags(req_tags);
		m_put_tags = new WordTags(put_tags);
	}
	
	public boolean applyRule(TaggedWord w)
	{
		if (!w.hasAllTags(m_req_tags)) return false;
		if (w.hasAllTags(m_put_tags)) return false;
		
		int i=0;
		int minlen = Math.min(w.m_word.length(), w.m_base_word.length());
		for(; i<minlen && w.m_word.charAt(i)==w.m_base_word.charAt(i); ++i) {}
		String diff = w.m_word.substring(i);
		
		if (m_word_pattern.matcher(w.m_word).matches()
				&& m_base_pattern.matcher(w.m_base_word).matches()
				&& m_dict_pattern.matcher(w.m_dict_tags).matches()
				&& m_diff_pattern.matcher(diff).matches() )
		{
			w.addTags(m_put_tags);
			return true;
		}
		else
		{
			return false;
		}
	}
}

/*******************************************************************************
 * 
 * @author Maksym Davydov
 * SentenceWord class represents a single word in a sentence with all it's possible grammar meanings (hypotheses) 
 */

class SentenceWord
{
	public int m_index;
	public Vector<TaggedWord> m_hypotheses = new Vector<TaggedWord>();
	//public java.util.TreeMap<String, SentenceWord> m_dependencies = new java.util.TreeMap<String, SentenceWord>();
	//public TaggedWord m_word=null;
	
	public SentenceWord(int index) { m_index = index; /*m_word = null;*/ }
	
	//public void setClosed(TaggedWord meaning) { m_word = meaning; }
	//public void setOpen() { m_word = null; m_dependencies.clear(); }
	//public void addChild(String role, SentenceWord w)
	//{
	//	m_dependencies.put(role, w);
	//}
	
	public void addHypothesis(TaggedWord w)
	{
		w.setHypotesisIndex(m_hypotheses.size());
		m_hypotheses.addElement(w);
	}
	public int numHypotheses()
	{
		return m_hypotheses.size();
	}
	public TaggedWord getHypothesis(int hyp_i)
	{
		return m_hypotheses.get(hyp_i);
	}
	
	boolean hasHypothesisWithAllTags(long tags)
	{
		for(TaggedWord t : m_hypotheses)
		{
			if (t.hasAllTags(tags)) return true;  
		}
		return false;
	}
	
	public void print()
	{
		System.out.print(m_index);
		System.out.print(" ");
//		if (m_word!=null)
//		{
//			System.out.print("* ");
//			System.out.print(m_word);
//		}
//		else
//		{
			for(TaggedWord t : m_hypotheses)
			{
				System.out.print(t);
				
				System.out.print(" || ");
			}
//		}
	}
}

class Sentence
{
	public Vector<SentenceWord> m_words = new Vector<SentenceWord>();
	
	public int numWords() { return m_words.size(); }
	public int numHypotheses()
	{
		int num = 0;
		for(SentenceWord w : m_words)
		{
			num += w.numHypotheses();
		}
		return num;
	}
	
	public void addWord(SentenceWord w)
	{
		m_words.addElement(w);
	}
	
	SentenceWord sentenceWordAt(int index) { return m_words.elementAt(index); }
	
	boolean hasWordsWithAllTags(int index_from, int index_to, long tags)
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
			if (sw.hasHypothesisWithAllTags(tags)) return true;
		}
		return false;
	}
	boolean hasWordsWithAllTagsBetween(int index_from, int index_to, long tags)
	{	
		if (Math.abs(index_from-index_to)<=1) return false;
			
		if (index_to >= m_words.size()) index_to = m_words.size()-1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size()-1;
		if (index_from < 0) index_from = 0;
		
		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;
		
		for(int i=index_from + dir; i!=index_to-dir; i+=dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			if (sw.hasHypothesisWithAllTags(tags)) return true;
		}
		return false;
	}
	
	void addPossibleRelation(ChoiceGraph cg, TaggedWord w1, TaggedWord w2)
	{
		WordTags t1 = w1.getTags();
		WordTags t2 = w2.getTags();
		
		int sp1 = w1.getSentencePos();
		int sp2 = w2.getSentencePos();
		
		if (sp1 ==sp2) return;
		
		if (sp2 > sp1)
		{
			if (t1.hasTag(WT.NOUN) && t2.hasTag(WT.NOUN))
			{	// noun-to-noun relations
				if (t1.sameCasus(t2) && !hasWordsWithAllTagsBetween(sp1, sp2, t1.getCasus() | WT.NOUN)
						&& (hasWordsWithAllTagsBetween(sp1, sp2, WT.PUNCT) || hasWordsWithAllTagsBetween(sp1, sp2, WT.CONJ)))
				{
					cg.addEdge("HOMOG-N", 1.0f, w1, w2);
				}
			}
			
			if (t1.hasTag(WT.VERB) && t2.hasTag(WT.VERB))
			{	// verb-to-verb relations
				if (t1.samePerson(t2) && t1.sameCount(t2) && !hasWordsWithAllTagsBetween(sp1, sp2, t1.getPerson() | WT.VERB)
						&& (hasWordsWithAllTagsBetween(sp1, sp2, WT.PUNCT) || hasWordsWithAllTagsBetween(sp1, sp2, WT.CONJ)))
				{
					cg.addEdge("HOMOG-V", 1.0f, w1, w2);
				}
			}			
		}
	}
	
	public void processSentence()
	{	
		ChoiceGraph cg = new ChoiceGraph(numWords(), numHypotheses());
		
		Vector<TaggedWord> all_words = new Vector<TaggedWord>();
		
		for(SentenceWord sw : m_words)
		{
			int n = sw.numHypotheses();
			for(int i=0;i<n;++i)
			{
				// TODO: reevaluate word weight. Now all are 1.0f :-)
				TaggedWord tw = sw.getHypothesis(i);
				cg.addVertex(tw, 1.0f, i==0);
				all_words.addElement(tw);
			}
		}

		for(TaggedWord w1 : all_words) for(TaggedWord w2 : all_words)
		{
			addPossibleRelation(cg, w1, w2);
		}
		
		//cg.print();
		
		Subtree st = cg.growingTreesSearch();
		st.print(cg);
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

//class SentenceProcessor
//{
//	private static List<String> stringList(String s)
//	{
//		List<String> sl = new Vector<String>();
//		CharSequenceWordFinder wf = new CharSequenceWordFinder(s);
//		while(wf.hasNext())
//		{
//			Word w = wf.next();
//			sl.add(w.toString());
//		}
//		return sl;
//	}

//	public void parsePredicate(Sentence s, SentenceWord w)
//	{
//		List<TaggedWord> out_list = new Vector<TaggedWord>();
//		
//		// search negations
//		s.findOpenWordsWithTags(out_list, w.m_index-1, w.m_index-1, stringList("Neg"));
//		if (out_list.size()!=0)
//		{
//			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
//			sw.setClosed(out_list.get(0));
//			w.addChild("Neg", sw );
//		}
//		out_list.clear();
//		
//		// search same-level words. !Add this word required tags!!!
//		s.findOpenWordsWithTags(out_list, w.m_index+1, s.numWords(), stringList(WT.VERB));
//		if (out_list.size()!=0)
//		{
//			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
//			sw.setClosed(out_list.get(0));
//			w.addChild("Sibling", sw );
//		}
//		out_list.clear();
//		
//		// search Subject
//		s.findOpenWordsWithTags(out_list, 0, w.m_index-1, stringList("Імен, c1"));
//		if (out_list.size()!=0)
//		{
//			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
//			sw.setClosed(out_list.get(0));
//			w.addChild("Subj", sw );
//		}
//		out_list.clear();
//		
//		// search Object
//		s.findOpenWordsWithTags(out_list, w.m_index+1, s.numWords(), stringList("Імен, c4"));
//		if (out_list.size()!=0)
//		{
//			SentenceWord sw = s.sentenceWordAt(out_list.get(0).m_index);
//			sw.setClosed(out_list.get(0));
//			w.addChild("Obj", sw );
//		}
//		out_list.clear();
//	}
	
//}

public class LangProc
{
	OpenOfficeSpellDictionary m_dict;
	java.util.HashSet<String> m_pronoun = new java.util.HashSet<String>();
	java.util.HashSet<String> m_prepositions = new java.util.HashSet<String>();
	java.util.HashSet<String> m_parenthesis_words = new java.util.HashSet<String>();
	java.util.HashSet<String> m_particles = new java.util.HashSet<String>();
	java.util.HashSet<String> m_negations = new java.util.HashSet<String>();
	java.util.HashSet<String> m_conjunction = new java.util.HashSet<String>();
	
	List<TagRule> m_tag_rules = new java.util.LinkedList<TagRule>();
	
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
	  
	  m_tag_rules.add(new TagRule(".*ого", ".*ий", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*",WT.ADJ, WT.CASUS2 | WT.PLURAL | WT.MALE | WT.FEMALE));
	  
	  m_tag_rules.add(new TagRule(".*ому", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*ій", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.PLURAL | WT.GENDER_MASK));
	  
	  m_tag_rules.add(new TagRule(".*ий", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*е", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*у", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.FEMALE)); 
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.PLURAL | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*ими", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*ім", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.PLURAL | WT.MALE));

	  m_tag_rules.add(new TagRule(".*ої", ".*ий", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*ому", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*ий", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*ого", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*им", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*ими", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*ім", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*их", ".*ий",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.PLURAL | WT.FEMALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "", WT.NOUN, WT.CASUS1 |  WT.CASUS4 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "а|у", WT.NOUN, WT.CASUS2 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "у", WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ом", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "у", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "и", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ів", WT.NOUN, WT.CASUS2 |  WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "и", WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ю",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.PRESENT));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "єш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.PRESENT));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "є",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PRESENT));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ємо",  WT.VERB, WT.PERSON1 | WT.PLURAL | WT.PRESENT));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "єте", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.PRESENT));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ють",  WT.VERB, WT.PERSON3 | WT.PLURAL | WT.PRESENT));

	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "в",  WT.VERB, WT.PERSON_MASK | WT.SINGLE | WT.PAST));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ла", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PAST | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ло", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PAST | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "ли", WT.VERB, WT.PERSON_MASK | WT.PLURAL | WT.PAST));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "иму",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имеш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "име",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "емо", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имете", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имуть", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.FUTURE));
	  
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "ду",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "деш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "де",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "демо", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "дете", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "дуть", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.FUTURE));

	  m_tag_rules.add(new TagRule(".*", ".*",  ".*[aioe].*", ".*", 0, WT.NOUN));
	  m_tag_rules.add(new TagRule(".*", ".*ти",  ".*", ".*", 0, WT.VERB));
	  m_tag_rules.add(new TagRule(".*", ".*ий",  ".*", ".*", 0, WT.ADJ));
	  m_tag_rules.add(new TagRule(".*", ".*ко|.*но",  ".*", ".*", 0, WT.ADV));
	  m_tag_rules.add(new TagRule(".*", ".*чи",  ".*", ".*", 0, WT.ADVPART));
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
	    	  
	    	  //if (word.equals(word.toUpperCase())) w.addTag("Cap");
	    	  //if (Character.isUpperCase(word.charAt(0))) w.addTag("StartCap");
	    	  //if (word.equals(word.toLowerCase())) w.addTag("Low");
	    	  //if (word.toLowerCase().equals(s.word)) w.addTag("Base");
	    	  
	    	  //System.out.print( "   " + s.word + " " + s.astr + " " );

	    	  if (m_prepositions.contains(s.word)) w.addTags(WT.PREPOS);
	    	  if (m_parenthesis_words.contains(s.word)) w.addTags(WT.HELPWORD);
	    	  if (m_particles.contains(s.word)) w.addTags(WT.PARTICLE);
	    	  if (m_negations.contains(s.word)) w.addTags(WT.NEGATION);
	    	  if (m_pronoun.contains(s.word)) w.addTags(WT.PRONOUN);
	    	  if (m_conjunction.contains(s.word)) w.addTags(WT.CONJ);
	    	  
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
		
		//SentenceProcessor sp = new SentenceProcessor();
		
		while(wf.hasNext())
		{
			Word w = wf.next();
			if (w.toString().equals("."))
			{
				ss.print();
				ss.processSentence();
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
		ss.processSentence();
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
	  //ChoiceGraph.test();
	  
	try
	{
	  (new LangProc(new OpenOfficeSpellDictionary("uk_UA")))
	  	.checkGrammar(
"Прийменники не мають самостійного лексичного значення, тому членами речення не виступають. Належачи до іменників, числівників, займенників, вони входять до складу другорядних членів речення." + 
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