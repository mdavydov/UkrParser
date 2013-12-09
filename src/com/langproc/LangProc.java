/*******************************************************************************
 * UkrParser
 * Copyright (c) 2013
 * Maksym Davydov
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/

package com.langproc;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.Word;
import org.dts.spell.finder.CharSequenceWordFinder;

import com.altmann.AdjacencyList;
import com.altmann.Edmonds;
import com.altmann.Edmonds_Andre;
import com.altmann.MatrixIO;
import com.altmann.Node;
import com.altmann.SCC;
import com.altmann.TarjanSCC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.altmann.*;

class LangProcSettings
{
	public static final boolean DEBUG_OUTPUT = true;
	public static final boolean SENTENCE_OUTPUT = true;
	public static final boolean GENERATE_SUGGESTIONS = false;
	public static final float OPTIONAL_WEIGHT = 0.0001f;
}

class LangProcOutput
{
	public static java.io.Writer writer = new java.io.OutputStreamWriter(System.out);
	
	public static void print(String s)
	{
		try
		{
			writer.write(s);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void println() { print("\n"); }	
	public static void println(String s) { print(s); print("\n"); }
	public static void print(Object o) { print(o.toString()); }
	public static void println(Object o) {println(o.toString());}
}

class WT
{
	public static final long PLURAL	= (1L<<0);
	public static final long SINGLE	= (1L<<1);
	public static final long COUNT_MASK	= PLURAL | SINGLE;
	
	public static final long PERSON1 = (1L<<2); 
	public static final long PERSON2 = (1L<<3); 
	public static final long PERSON3 = (1L<<4);
	public static final long PERSON_MASK	= PERSON1 | PERSON2 | PERSON3;
	public static final long ANY_PERSON	= PERSON1 | PERSON2 | PERSON3;
	
	public static final long MALE	= (1L<<5);
	public static final long FEMALE	= (1L<<6);
	public static final long NEUTRAL	= (1L<<7);
	public static final long GENDER_MASK = MALE | FEMALE | NEUTRAL;
	public static final long ANY_GENDER = GENDER_MASK;
	
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
	public static final long COMMA = (1L<<21);
	public static final long CONJ = (1L<<22);		// Conjunction
	public static final long NUMERAL = (1L<<23);	// Numeral
	public static final long PARTICLE = (1L<<24);
	public static final long ADVPART =  (1L<<25); // Adverbial participle
	public static final long ADJPART =  (1L<<26); // Adjective participle
	public static final long PREPOS =  (1L<<27); // preposition
	public static final long HELPWORD =  (1L<<28); // parenthesis words
	
	
	public static final long PART_OF_SPEECH_MASK = NOUN | VERB | ADV | ADJ | PRONOUN | NEGATION |
					COMMA | CONJ | NUMERAL | PARTICLE | ADVPART | ADJPART | PREPOS | HELPWORD;
	
	public static final long PERFECT = (1L<<29);
	public static final long SIMPLE =  (1L<<30);
	public static final long PERFECTION_MASK = PERFECT | SIMPLE;
	
	
	public static final long PAST = (1L<<31);
	public static final long PRESENT = (1L<<32);
	public static final long FUTURE =  (1L<<33);
	public static final long TIME_MASK = PAST | PRESENT | FUTURE;
	
	public static final long PERSONLESS = (1L<<34);
	public static final long INFINITIVE = (1L<<35);
	public static final long SENTENCE_END = (1L<<36);
	public static final long MODAL = (1L<<37);
	public static final long INDICATIVE = (1L<<38);
	public static final long QUESTION = (1L<<39);
	public static final long STATE = (1L<<40);
	
	public static final long ANY_NOUN = NOUN | PRONOUN | NUMERAL;
	public static final long ANY_VERB = VERB | ADVPART | ADJPART;

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
		if (hasSomeTags(WT.PERSONLESS)) b.append("p- ");
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
		if (hasSomeTags(WT.COMMA)) b.append("COMMA ");
		if (hasSomeTags(WT.CONJ)) b.append("CONJ ");
		if (hasSomeTags(WT.NUMERAL)) b.append("NUMERAL ");
		if (hasSomeTags(WT.PARTICLE)) b.append("PARTICLE ");
		if (hasSomeTags(WT.ADVPART)) b.append("ADVPART ");
		if (hasSomeTags(WT.ADJPART)) b.append("ADJPART ");
		if (hasSomeTags(WT.PREPOS)) b.append("PREPOS ");
		if (hasSomeTags(WT.HELPWORD)) b.append("HELPWORD ");
		
		if (hasSomeTags(WT.PERFECT)) b.append("PERFECT ");
		if (hasSomeTags(WT.SIMPLE)) b.append("SIMPLE ");
		if (hasSomeTags(WT.PAST)) b.append("PAST ");
		if (hasSomeTags(WT.PRESENT)) b.append("PRESENT ");
		if (hasSomeTags(WT.FUTURE)) b.append("FUTURE ");
		
		if (hasSomeTags(WT.INFINITIVE)) b.append("INF ");
		if (hasSomeTags(WT.SENTENCE_END)) b.append("S-END ");
		if (hasSomeTags(WT.MODAL)) b.append("MOD ");
		if (hasSomeTags(WT.INDICATIVE)) b.append("IND ");
		if (hasSomeTags(WT.QUESTION)) b.append("QUE ");
		if (hasSomeTags(WT.STATE)) b.append("STATE ");
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
	public boolean hasSomeTags(WordTags tags) { return m_tags.hasSomeTags(tags); }
	public boolean hasSomeTags(long tags) { return m_tags.hasSomeTags(tags); }
	
	
	public void addTags(WordTags tags) { m_tags.setTags(tags); }
	public void addTags(long tags) { m_tags.setTags(tags); }
	public void setHypotesisIndex(int hyp_index) { m_hypo_index = hyp_index; }
	
	public String toString()
	{
		return m_word + "(" + m_base_word + ")";

	}
	public String getFullFesc()
	{
		StringBuffer b = new StringBuffer(200);
		b.append(m_sentence_pos);
		b.append(" ");
		b.append(m_word);
		b.append(" (");
		b.append(m_base_word);
		b.append(") ");
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
		
		int i1=0, i2=0;
		int w1_len = w.m_word.length();
		int w2_len = w.m_base_word.length();
		
		// skip "не" prefix
		if (w.m_word.indexOf("не")==0 && w.m_base_word.indexOf("не")!=0)
		{
			i1 = 2;
		}
		
		for(; i1<w1_len && i2<w2_len && w.m_word.charAt(i1)==w.m_base_word.charAt(i2); ++i1, ++i2) {}
		
		String diff = w.m_word.substring(i1);
		
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
	
	boolean hasHypothesisWithSomeTags(long tags)
	{
		for(TaggedWord t : m_hypotheses)
		{
			if (t.hasSomeTags(tags)) return true;  
		}
		return false;
	}
	
	boolean hasHypothesisWithSomeTags(long tags_group1, long tags_group2)
	{
		for(TaggedWord t : m_hypotheses)
		{
			if (t.hasSomeTags(tags_group1) && t.hasSomeTags(tags_group2)) return true;  
		}
		return false;
	}
	
	public void print()
	{
		LangProcOutput.print(m_index);
		LangProcOutput.print(" ");
//		if (m_word!=null)
//		{
//			LangProcOutput.print("* ");
//			LangProcOutput.print(m_word);
//		}
//		else
//		{
			for(TaggedWord t : m_hypotheses)
			{
				LangProcOutput.print(t.getFullFesc());
				LangProcOutput.print(" || ");
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
	
	boolean hasWordsWithSomeTagsBetween(int index_from, int index_to, long tags)
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
			if (sw.hasHypothesisWithSomeTags(tags)) return true;
		}
		return false;
	}
	
	boolean hasWordsWithSomeTagsBetween(int index_from, int index_to, long tags_group1, long tags_group2)
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
			if (sw.hasHypothesisWithSomeTags(tags_group1, tags_group2)) return true;
		}
		return false;
	}
	
	double linkPrefClose(int sp1, int sp2)
	{
		if (sp2>sp1)
		{
			return Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 );
		}
		else
		{
			return Math.exp( (0.5 - Math.abs(sp1-sp2))*0.5 );
		}
	}
	
	double linkPrefSeq(int sp1, int sp2)
	{
		if (sp2>sp1)
		{
			return Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 );
		}
		else
		{
			return Math.exp( (0.5 - Math.abs(sp1-sp2))*0.5 );
		}
	}
	double linkPrefTogether(int sp1, int sp2)
	{
		if (sp2==sp1+1)
		{
			return 1.5*Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 );
		}
		else if (sp2>sp1)
		{
			return Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 );
		}
		else
		{
			return Math.exp( (0.5 - Math.abs(sp1-sp2))*0.5 );
		}
	}
	
	double linkHardSeq(int sp1, int sp2)
	{
		if (sp2>sp1)
		{
			return Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 );
		}
		return 0.0;
	}
	double linkGovernmentPrepos(int sp1, int sp2)
	{
		if (sp2>sp1)
		{
			return 1.0 + Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 );
		}
		return 0.0;
	}
	double linkNonGovernmentPrepos(int sp1, int sp2)
	{
		if (sp2>sp1)
		{
			return Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 )/10;
		}
		return 0.0;
	}

	
	void addPossibleRelation(ChoiceGraph cg, LangProc langproc, TaggedWord w1, TaggedWord w2)
	{
		WordTags t1 = w1.getTags();
		WordTags t2 = w2.getTags();
		
		int sp1 = w1.getSentencePos();
		int sp2 = w2.getSentencePos();
		
		if (sp1==sp2) return;
		
		double dk = Math.exp( (1.0 - Math.abs(sp1-sp2))*0.5 );
		

		if (sp2 > sp1)
		{
			if ( t2.hasSomeTags(WT.SENTENCE_END) )
			{
				if (t1.hasSomeTags(WT.VERB | WT.QUESTION) && !t1.hasSomeTags(WT.INFINITIVE))
				{
					if (w2.m_word.equals("?")) cg.addEdge("QUESTION", 0.05, w2, w1);
					else if (w2.m_word.equals("!")) cg.addEdge("EXCLAMATION", 0.05, w2, w1);
					else if (w2.m_word.equals(".")) cg.addEdge("ROOT", 0.05, w2, w1);
					else if (w2.m_word.equals(";")) cg.addEdge("ROOT", 0.05, w2, w1);
				}
				else
				{
					cg.addEdge("OPTIONAL", LangProcSettings.OPTIONAL_WEIGHT, w2, w1);
				}
			}
			
			if (t1.hasAllTags(WT.PRONOUN | WT.INDICATIVE) && t2.hasAllTags(WT.PRONOUN | WT.QUESTION) &&
					sp2 == sp1 + 2 && hasWordsWithSomeTagsBetween(sp1, sp2, WT.COMMA))
			{	// noun-to-noun relations
				cg.addEdge("INDICATES", 1.0, w1, w2);
			}
			
			
			if (t1.hasAllTags(WT.PRONOUN | WT.QUESTION) && t2.hasAllTags(WT.VERB))
			{	// noun-to-noun relations
				cg.addEdge("QUESTION", linkHardSeq(sp1, sp2), w1, w2);
			}


			if (t1.hasSomeTags(WT.ANY_NOUN) && t2.hasSomeTags(WT.ANY_NOUN))
			{	// noun-to-noun relations
				if (t1.sameCasus(t2))
				{
					// we should redo this to make it better and more universal 
					if ( (hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA) || hasWordsWithAllTagsBetween(sp1, sp2, WT.CONJ)))
					{
						cg.addEdge("HOMOG-NOUN", dk, w1, w2);
					}
					else if (sp2==sp1+1 && t1.sameCount(t2) && t1.hasTag(WT.NOUN))
					{
						cg.addEdge("APPOSITION", 1.1, w1, w2);
					}
				}
				
				if (t2.hasTag(WT.CASUS2) && t2.hasSomeTags(WT.NOUN) && !hasWordsWithAllTagsBetween(sp1, sp2, t1.getCasus()| WT.ANY_NOUN)
						&& !hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA) &&!hasWordsWithAllTagsBetween(sp1, sp2, WT.CONJ))
				{
					if (w2.hasSomeTags(WT.NOUN) && !w1.hasAllTags(WT.NUMERAL))
					{
						cg.addEdge("BELONG-TO", linkPrefSeq(sp1, sp2), w1, w2);
					}
				}
			}
			
			if (t1.hasTag(WT.VERB) && t2.hasTag(WT.PARTICLE))
			{
				if (m_words.get(sp2-1).hasHypothesisWithAllTags(WT.COMMA))
				{
					if (w2.m_word.equals("тому"))
					{
						cg.addEdge("RESULT", 10, w2, w1);
					}
					if (w2.m_word.equals("оскільки"))
					{
						cg.addEdge("REASON", 10, w2, w1);
					}
				}
			}
			
			if (sp1>=2 && t1.hasTag(WT.PARTICLE) && t2.hasTag(WT.VERB))
			{
				if (m_words.get(sp1-1).hasHypothesisWithAllTags(WT.COMMA))
				{
					if (w1.m_word.equals("тому"))
					{
						cg.addEdge("REASON", 10, w1, w2);
					}
					if (w1.m_word.equals("оскільки"))
					{
						cg.addEdge("RESULT", 10, w1, w2);
					}
				}
			}
			
			if (t1.hasTag(WT.VERB) && t2.hasTag(WT.VERB))
			{	// verb-to-verb relations
				if (t1.samePerson(t2) && t1.sameCount(t2) )
				{
					cg.addEdge("HOMOG-VERB", dk, w1, w2);
				}
			}		
						

			
			if (t1.hasSomeTags(WT.ADV) && t2.hasSomeTags(WT.ADV))
			{	// noun-to-noun relations
				// we should redo this to make it better and more universal 
				if ( (hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA) || hasWordsWithAllTagsBetween(sp1, sp2, WT.CONJ)))
				{
					cg.addEdge("HOMOG-ADV", dk, w1, w2);
				}
			}
			if (t1.hasSomeTags(WT.ADJ) && t2.hasSomeTags(WT.ADJ))
			{	// noun-to-noun relations
				if (t1.sameCasus(t2) && t1.sameCount(t2) && t1.sameGender(t2))
				{
					// we should redo this to make it better and more universal 
					if ( (hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA) || hasWordsWithAllTagsBetween(sp1, sp2, WT.CONJ)))
					{
						cg.addEdge("HOMOG-ADJ", dk, w1, w2);
					}
				}
			}
		}
		
		if (t1.hasTag(WT.NUMERAL) && t2.hasTag(WT.NOUN))
		{	// verb-to-verb relations
			if (t1.hasTag(WT.CASUS1) && langproc.m_countable_req_nom.containsKey(w1.m_word))
			{
				if (t2.hasAllTags(WT.CASUS1 | WT.PLURAL))
				{
					cg.addEdge("COUNT", linkPrefSeq(sp1, sp2), w1, w2);
				}
				else if (t2.hasAllTags(WT.CASUS2 | WT.PLURAL))
				{
					cg.addEdge("COUNT-BAD", linkPrefSeq(sp1, sp2)/2, w1, w2);
				}
			}
			else if (t1.hasTag(WT.CASUS1))
			{
				if (t2.hasAllTags(WT.CASUS2 | WT.PLURAL))
				{
					cg.addEdge("COUNT", linkPrefSeq(sp1, sp2), w1, w2);
				}
			}
			else if (t1.sameCount(t2) && t1.sameCasus(t1) )
			{
				cg.addEdge("COUNT", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}
		
		if (t1.hasTag(WT.STATE) && t2.hasTag(WT.VERB))
		{
			if (w2.m_word.equals("було"))
			{
				cg.addEdge("TIME-MOD", linkPrefSeq(sp1, sp2), w1, w2);
			}
			else if (w2.m_word.equals("буде"))
			{
				cg.addEdge("TIME-MOD", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}

		
		if (t1.hasTag(WT.VERB) && t2.hasSomeTags(WT.PREPOS))
		{	// verb-to-verb relations		
			cg.addEdge("IND-OBJ", linkPrefSeq(sp1, sp2), w1, w2);
		}

		if (t1.hasTag(WT.MODAL) && t2.hasTag(WT.INFINITIVE))
		{	// verb-to-verb relations		
			cg.addEdge("MOD-VERB", linkPrefSeq(sp1, sp2), w1, w2);
		}

			
		if (t1.hasTag(WT.VERB) && t2.hasSomeTags(WT.ANY_NOUN))
		{	// verb-to-verb relations
			if ( t1.samePerson(t2) && t1.sameCount(t2) && t1.sameGender(t2) && t2.hasTag(WT.CASUS1) )
			{
				cg.addEdge("SUBJECT", linkPrefSeq(sp2, sp1), w1, w2);
			}
			
			if ( t2.hasTag(WT.CASUS4) )
			{
				cg.addEdge("OBJECT", linkPrefSeq(sp1, sp2), w1, w2);
			}
			
			if ( t2.hasSomeTags(WT.CASUS2 | WT.CASUS3 | WT.CASUS5 | WT.CASUS6 ) )
			{
				cg.addEdge("IND-OBJ", linkPrefSeq(sp1, sp2), w1, w2);
			}	
		}
		
		if (t1.hasTag(WT.VERB) && t2.hasTag(WT.ADJ))
		{	// verb-to-verb relations
			if ( t2.hasTag(WT.CASUS5) ) //"Зробити веселим"
			{
				cg.addEdge("ADVERBIAL", linkPrefSeq(sp1, sp2), w1, w2);
			}
			
			if ( t2.hasTag(WT.CASUS4) )
			{
				cg.addEdge("OBJECT", linkPrefSeq(sp1, sp2), w1, w2);
			}
			
			if ( t2.hasSomeTags(WT.CASUS2 | WT.CASUS3 | WT.CASUS5 | WT.CASUS6 ) )
			{
				cg.addEdge("IND-OBJ", linkPrefSeq(sp1, sp2), w1, w2);
			}	
		}
		
		if (t1.hasTag(WT.ADVPART) && t2.hasSomeTags(WT.ANY_NOUN))
		{	// verb-to-verb relations		
			if ( t2.hasTag(WT.CASUS4) )
			{
				cg.addEdge("OBJECT", linkPrefSeq(sp1, sp2), w1, w2);
			}
			
			if ( t2.hasSomeTags(WT.CASUS2 | WT.CASUS3 | WT.CASUS5 | WT.CASUS6 ) )
			{
				cg.addEdge("IND-OBJ", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}

		
		if (t1.hasSomeTags(WT.ANY_NOUN) && t2.hasTag(WT.ADJ))
		{
			if ( t1.sameCount(t2) && t1.sameGender(t2) && t1.sameCasus(t2) )
			{
				cg.addEdge("ADJ", linkPrefTogether(sp2, sp1), w1, w2);
			}
		}
		
		if (t1.hasTag(WT.PREPOS) && t2.hasSomeTags(WT.ANY_NOUN))
		{
			WordTags req_tags = langproc.m_prepositions.get(w1.m_base_word);
			
			if (t2.hasSomeTags( req_tags )
					&& !hasWordsWithSomeTagsBetween(sp1, sp2, WT.ANY_NOUN, req_tags.m_tags) )
			{
				cg.addEdge("PREPOS", linkGovernmentPrepos(sp1,sp2), w1, w2);
			}
			else
			{
				// less link with word that does not meet requires casus 
				cg.addEdge("PREPOS", linkNonGovernmentPrepos(sp1,sp2), w1, w2);
			}
		}
		
		if (t1.hasSomeTags(WT.ANY_VERB) && t2.hasTag(WT.ADV))
		{
			cg.addEdge("ADV", linkPrefSeq(sp1, sp2), w1, w2);
		}
		
		if (t1.hasTag(WT.VERB) && t2.hasTag(WT.ADVPART))
		{
			cg.addEdge("V-ADVPART", linkPrefClose(sp1, sp2), w1, w2);
		}
		
		if (t2.hasTag(WT.NEGATION))
		{
			if (sp1==sp2+1)
			{
				cg.addEdge("NEG", 1, w1, w2);
			}
		}
	}
	
	public void processSentence(LangProc langproc)
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
				float w = 1.0f;
				//if (tw.m_base_word.equals("робот")) w = 0.9f;
				if (tw.m_base_word.equals("робота")) w = 0.9f;
				cg.addVertex(tw, w, i==0);
				all_words.addElement(tw);
			}
		}
		
//		for(int i=0;i<all_words.size();++i)
//		{
//			TaggedWord tw = all_words.get(i);
//			LangProcOutput.println("\\node[main node] ("+ i +") at ("+ (135-i*305/all_words.size())+":7cm) {" + tw.m_word + "\\\\" 
//					+ (new WordTags(tw.m_tags.getPartOfSpeech()).toString() + "};") );
//		}

		//LangProcOutput.println("\\path[every node/.style={font=\\sffamily\\small}]");

		for(TaggedWord w1 : all_words) for(TaggedWord w2 : all_words)
		{
			addPossibleRelation(cg, langproc, w1, w2);
		}
		//LangProcOutput.println(";");
		//cg.print();
		

		//cg.print_dependencies();
		
		LangProcOutput.print("\n\\hspace{1em}\n");			

		//Subtree st = cg.growingTreesSearch();
		
		Subtree st = null;

		if (cg.getComplexity() < 1000 )
		{
			st = cg.ExhaustiveEdmondSearch();
		}
		else
		{
			st = cg.randomizedEdmondSearch(400, true, false);
		}
		
		if (st!=null)
		{
			LangProcOutput.println("\\resizebox{\\columnwidth}{!}{");
			LangProcOutput.println("\\begin{tikzpicture}[sibling distance=30pt]");
			LangProcOutput.println("\\Tree ");
			st.print_qtree(cg);
			LangProcOutput.println();
			LangProcOutput.println("\\end{tikzpicture}");
			LangProcOutput.println("}");
			LangProcOutput.println("\n\\hspace{1em}\n");
		}
	}

	
	public void print()
	{
		for(SentenceWord w:m_words)
		{
			w.print();
			LangProcOutput.println();
			LangProcOutput.println();
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
	java.util.HashSet<String> m_pronoun_S_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C6 = new java.util.HashSet<String>();
	
	java.util.HashSet<String> m_pronoun_M_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C6 = new java.util.HashSet<String>();
	
	java.util.HashSet<String> m_pronoun_male = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_female = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_neutral = new java.util.HashSet<String>();
	
	java.util.HashSet<String> m_pronoun_ADJ_S_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C6 = new java.util.HashSet<String>();
	
	java.util.HashSet<String> m_pronoun_ADJ_M_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C6 = new java.util.HashSet<String>();

	java.util.HashSet<String> m_pronoun_ADJ_male = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_female = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_neutral = new java.util.HashSet<String>();

	// the map from prepositions to possible CASUSES 
	java.util.HashMap<String, WordTags> m_prepositions = new java.util.HashMap<String, WordTags>();
	java.util.HashSet<String> m_parenthesis_words = new java.util.HashSet<String>();
	java.util.HashSet<String> m_particles = new java.util.HashSet<String>();
	java.util.HashSet<String> m_negations = new java.util.HashSet<String>();
	java.util.HashSet<String> m_conjunction = new java.util.HashSet<String>();
	java.util.HashSet<String> m_question_adv = new java.util.HashSet<String>();
	java.util.HashSet<String> m_adverbs = new java.util.HashSet<String>();
	
	// words that lack base form in the dictionary
	java.util.HashMap<String, String> m_special_nouns = new java.util.HashMap<String, String>();
	java.util.HashMap<String, String> m_special_pronouns = new java.util.HashMap<String, String>();
	java.util.HashSet<String> m_indacative_pronous = new java.util.HashSet<String>();
	java.util.HashSet<String> m_question_pronous = new java.util.HashSet<String>();
	java.util.HashMap<String, String> m_special_verbs = new java.util.HashMap<String, String>();
	java.util.HashSet<String> m_modal_verbs = new java.util.HashSet<String>();
	java.util.HashSet<String> m_state_words = new java.util.HashSet<String>();
	
	java.util.HashMap<String, WordTags> m_countable = new java.util.HashMap<String, WordTags>();
	java.util.HashMap<String, WordTags> m_countable_req_nom = new java.util.HashMap<String, WordTags>();

	
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
	
	static void fillMap(java.util.HashMap<String, String> map, String s_from, String s_to)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(s_from);
		while(wf.hasNext())
		{
			Word w = wf.next();
			map.put( w.toString(), s_to );
		}
	}
	
	static void fillMapTags(java.util.HashMap<String, WordTags> map, String s_from, long wt)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(s_from);
		while(wf.hasNext())
		{
			Word w = wf.next();
			map.put( w.toString(), new WordTags(wt) );
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
	  m_prepositions.put("перед", new WordTags(WT.CASUS5) ); // add "переді мною"
	  m_prepositions.put("як", new WordTags(WT.CASUS4));
	  m_prepositions.put("між", new WordTags(WT.CASUS5));
	  m_prepositions.put("за", new WordTags(WT.CASUS5));
	  m_prepositions.put("після", new WordTags(WT.CASUS2));
	  m_prepositions.put("над", new WordTags(WT.CASUS5));
	  m_prepositions.put("під", new WordTags(WT.CASUS5));
	  m_prepositions.put("через", new WordTags(WT.CASUS4));
	  m_prepositions.put("поза", new WordTags(WT.CASUS5));
	  m_prepositions.put("без", new WordTags(WT.CASUS2));
	  m_prepositions.put("на", new WordTags(WT.CASUS4 | WT.CASUS6));
	  m_prepositions.put("в", new WordTags(WT.CASUS4 | WT.CASUS6));
	  m_prepositions.put("у", new WordTags(WT.CASUS4 | WT.CASUS6));
	  m_prepositions.put("від", new WordTags(WT.CASUS2));
	  m_prepositions.put("для", new WordTags(WT.CASUS2));
	  m_prepositions.put("по", new WordTags(WT.CASUS6));
	  m_prepositions.put("через", new WordTags(WT.CASUS4));
	  m_prepositions.put("при", new WordTags(WT.CASUS3));
	  m_prepositions.put("про", new WordTags(WT.CASUS4));
	  m_prepositions.put("над", new WordTags(WT.CASUS5));
	  m_prepositions.put("під", new WordTags(WT.CASUS5));
	  m_prepositions.put("до", new WordTags(WT.CASUS2));
	  m_prepositions.put("з", new WordTags(WT.CASUS2 | WT.CASUS5));
	  m_prepositions.put("ради", new WordTags(WT.CASUS2));
	  m_prepositions.put("заради", new WordTags(WT.CASUS2));
	  m_prepositions.put("задля", new WordTags(WT.CASUS2));
	  m_prepositions.put("попри", new WordTags(WT.CASUS4));
	  
	  m_prepositions.put("поза", new WordTags(WT.CASUS5));
	  m_prepositions.put("щодо", new WordTags(WT.CASUS4));
	  m_prepositions.put("поміж", new WordTags(WT.CASUS5));
	  m_prepositions.put("близько", new WordTags(WT.CASUS2));
	  m_prepositions.put("внаслідок", new WordTags(WT.CASUS2));
	  m_prepositions.put("після", new WordTags(WT.CASUS2));
	  m_prepositions.put("поруч", new WordTags(WT.CASUS2)); // TODO add "поруч з із у в на під"
	  m_prepositions.put("перед", new WordTags(WT.CASUS5));
	  m_prepositions.put("протягом", new WordTags(WT.CASUS2));
	  m_prepositions.put("під час", new WordTags(WT.CASUS2));
	  m_prepositions.put("з допомогою", new WordTags(WT.CASUS2));
	  m_prepositions.put("у зв’язку з", new WordTags(WT.CASUS5));
	  m_prepositions.put("завдяки", new WordTags(WT.CASUS5));
	  m_prepositions.put("незважаючи на", new WordTags(WT.CASUS4));
	  m_prepositions.put("з-за", new WordTags(WT.CASUS2));
	  m_prepositions.put("з-над", new WordTags(WT.CASUS5));
	  m_prepositions.put("з-поза", new WordTags(WT.CASUS2));
	  m_prepositions.put("з-під", new WordTags(WT.CASUS2));
	  m_prepositions.put("з-попід", new WordTags(WT.CASUS2));
	  m_prepositions.put("з-серед", new WordTags(WT.CASUS2));
	  m_prepositions.put("із-за", new WordTags(WT.CASUS2));
	  m_prepositions.put("в силу", new WordTags(WT.CASUS2));
	  m_prepositions.put("згідно з", new WordTags(WT.CASUS5));
	  
	  
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
	  
	  fillSet(m_pronoun_S_C1, "котрий котра котре який яка яке той та те цей це ця я ти він вона воно");
	  fillSet(m_pronoun_S_C2, "котрого котрої якого того тієї тої цього цієї мене тебе його нього її неї себе");
	  fillSet(m_pronoun_S_C3, "котрому котрій якому тому тій цьому цій мені тобі йому їй собі");
	  fillSet(m_pronoun_S_C4, "котрого котру котре якого яке яку той ту те цей це цю мене тебе його нього її неї себе");
	  fillSet(m_pronoun_S_C5, "котрим котрою яким тим тією тою цим цією мною тобою ним нею собою");
	  fillSet(m_pronoun_S_C6, "котрому котрім котрій якому якій тому тім тій цьому цім цій мені тобі ньому ній собі");

	  fillSet(m_pronoun_M_C1, "котрі які ті ці ми ви вони");
	  fillSet(m_pronoun_M_C2, "котрих яких тих цих нас вас їх них");
	  fillSet(m_pronoun_M_C3, "котрому яким тим цим нам вам їм");
	  fillSet(m_pronoun_M_C4, "котрих яких ті ці нас вас їх них");
	  fillSet(m_pronoun_M_C5, "котрими якими тими цими нами вами ними");
	  fillSet(m_pronoun_M_C6, "яких тих цих нас вас них");
	  
	  fillSet(m_pronoun_male, "котрий котрого котрому якого який той цей він його йому ним ньому");
	  fillSet(m_pronoun_female, "котра котрої котрій яку яка якій та тій ця вона її їй нею ній");
	  fillSet(m_pronoun_neutral, "котре котрого котрому яке те це воно його йому ним ньому");
	  
	  
	  fillSet(m_pronoun_ADJ_S_C1, "мій моя моє твій твоя твоє його її наш наша наше ваш ваша ваше їхній їхня їхнє свій своя своє");
	  fillSet(m_pronoun_ADJ_S_C2, "мого моєї твого твоєї його її нашого нашої вашого вашої їхнього їхньої свого своєї");
	  fillSet(m_pronoun_ADJ_S_C3, "моєму моїй твоєму твоїй його її нашому нашій вашому вашій їхньому їхній своєму своїй");
	  fillSet(m_pronoun_ADJ_S_C4, "мій мою моє твій твою твоє його її наш нашу наше ваш вашу ваше їхній їхню їхнє свій свою своє");
	  fillSet(m_pronoun_ADJ_S_C5, "моїм моєю твоїм твоєю його її нашим нашою вашим вашою їхнім їхньою своїм своєю");
	  fillSet(m_pronoun_ADJ_S_C6, "моїм моїй твоїм твоїй його її нашім нашій вашім вашїй їхнім їхній своїм своїй");

	  fillSet(m_pronoun_ADJ_M_C1, "мої твої його її свої наші ваші їхні");
	  fillSet(m_pronoun_ADJ_M_C2, "моїх твоїх його її своїх наших ваших їхніх");
	  fillSet(m_pronoun_ADJ_M_C3, "моїм твоїм його її своїм нашим вашим їхнім");
	  fillSet(m_pronoun_ADJ_M_C4, "мої твої його її свої наші ваші їхні");
	  fillSet(m_pronoun_ADJ_M_C5, "моїми твоїми його її своїми нашими вашими їхніми");
	  fillSet(m_pronoun_ADJ_M_C6, "моїх твоїх своїх наших ваших їхніх");
	  
	  fillSet(m_pronoun_ADJ_male,
			  	"той цей мій твій наш ваш їхній свій "+
	  			"того цього мого твого нашого вашого їхнього свого "+
	  			"тому цьому моєму твоєму нашому вашому їхньому своєму "+
	  			"той цей мій твій наш ваш їхній свій "+
	  			"тим цим моїм твоїм нашим вашим їхнім своїм "+
	  			"тому тім цьому цім моїм твоїм нашім вашім їхнім своїм ");
	  
	  fillSet(m_pronoun_ADJ_female,
			  	"та ця моя твоя наша ваша їхня своя "+
	  			"тієї тої цією моєї твоєї нашої вашої їхньої своєї "+
	  			"тій цій моїй твоїй нашій вашій їхній своїй "+
	  			"ту цю мою твою нашу вашу їхню свою "+
	  			"тією тою цією моєю твоєю нашою вашою їхньою своєю "+
	  			"тій цій моїй твоїй нашій вашїй їхній своїй ");
	  
	  fillSet(m_pronoun_ADJ_neutral,
			  	"те це моє твоє наше ваше їхнє своє "+
	  			"того цього мого твого нашого вашого їхнього свого "+
	  			"тому цьому моєму твоєму нашому вашому їхньому своєму "+
	  			"те це моє твоє наше ваше їхнє своє "+
	  			"тим цим моїм твоїм нашим вашим їхнім своїм "+
	  			"тому цьому цім моїм твоїм нашім вашім їхнім своїм ");

	  
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
	  
	  fillSet(m_conjunction, "і або й та але а проте" );
	  
	  fillSet(m_question_adv, "коли, чому, скільки, як, навіщо ніколи");
	  
	  
	  fillSet(m_adverbs, "додому завтра сьогодні вчора позавчора післязавтра");
	  fillSet(m_adverbs, "наліво направо назад вперед вниз донизу нагору вгору додолу");
	  
	  fillMap(m_special_verbs, "могти міг могла могло могли можу можеш може можемо можете можуть", "могти" );
	  fillMap(m_special_verbs, "хотіти хотів хотіла хотіло хотіли хочу хочеш хоче хочемо хочете хочуть", "хотіти" );
	  	  
	  fillSet(m_modal_verbs, "могти хотіти бажати збирати намагатися пропонувати задумати треба важливо необхідно" );
	  fillSet(m_modal_verbs, "заохочувати забороняти провокувати мусити зобов'язаний зобов'язати" );
	  
	  fillSet(m_state_words, "треба важливо необхідно" );
	  
	  
	  
	  fillMap(m_special_nouns, "ніщо нічого нічому нічим", "ніщо" );
	  fillMap(m_special_nouns, "щось чогось чомусь чимось", "щось" );
	  fillMap(m_special_nouns, "сну сном сні сни снів снам снами снах сон", "сон" );
	  
	  fillMap(m_special_pronouns, "котрий котра котре котрого котрої котрому котрій котру котрим котрою котрім котрій", "котрий" );
	  fillMap(m_special_pronouns, "який яка яке якого якої якому якій яку яким якою ятякій", "який" );
	  fillMap(m_special_pronouns, "той та те того тої тому тій ту тим тою тім тій", "той" );
	  fillMap(m_special_pronouns, "що", "що" );
	  
	  fillSet(m_indacative_pronous, "цей, оцей, сей, той, стільки, такий, отакий" );
	  fillSet(m_question_pronous, "що хто скільки який чий котрий" );

	  fillSet(m_pronoun_M_C1, "котрі які ті ці ми ви вони");
	  fillSet(m_pronoun_M_C2, "котрих яких тих цих нас вас їх них");
	  fillSet(m_pronoun_M_C3, "котрому яким тим цим нам вам їм");
	  fillSet(m_pronoun_M_C4, "котрих яких ті ці нас вас їх них");
	  fillSet(m_pronoun_M_C5, "котрими якими тими цими нами вами ними");
	  fillSet(m_pronoun_M_C6, "яких тих цих нас вас них");
	  
	  fillMapTags(m_countable_req_nom, "два три чотири обидва", WT.PLURAL | WT.CASUS1 | WT.CASUS4 );
	  fillMapTags(m_countable, "двох трьох чотирьох обидвох", WT.PLURAL | WT.CASUS2 | WT.CASUS6);
	  fillMapTags(m_countable, "двом трьом чотирьом обидвом", WT.PLURAL | WT.CASUS3 );
	  fillMapTags(m_countable, "двома трьома чотирма обидвома", WT.PLURAL | WT.CASUS5 );
	  
	  
	  fillMapTags(m_countable, "п'ять шість сім вісім дев'ять", WT.PLURAL | WT.CASUS1 | WT.CASUS4 );
	  fillMapTags(m_countable, "п'яти шести семи восьми дев'яти", WT.PLURAL | WT.CASUS2 | WT.CASUS6 );
	  fillMapTags(m_countable, "п'ятьом шістьом сімом вісьмом дев'ятьом", WT.PLURAL | WT.CASUS3 );
	  fillMapTags(m_countable, "п'ятьма шістьма сімома вісьмома дев'ятьма", WT.PLURAL | WT.CASUS5 );
	  
	  fillMapTags(m_countable, "десять одинядцять дванадцять тринадцять", WT.PLURAL | WT.CASUS1 | WT.CASUS4 );
	  fillMapTags(m_countable, "чотирнадцять п'ятнадцять шістнадцять сімнадцять", WT.PLURAL | WT.CASUS1 | WT.CASUS4 );
	  fillMapTags(m_countable, "вісімнадцять дев'ятнадцять двадцять тридцять", WT.PLURAL | WT.CASUS1 | WT.CASUS4 );
	  
	  fillMapTags(m_countable, "скільки стільки багато мало достатньо недостатньо немало небагато", WT.PLURAL | WT.CASUS1 | WT.CASUS4 );
	  fillMapTags(m_countable, "скількох стількох багатьох", WT.PLURAL | WT.CASUS2 | WT.CASUS6 );
	  fillMapTags(m_countable, "скільком стільком багатьом небагатьом", WT.PLURAL | WT.CASUS3 );
	  fillMapTags(m_countable, "скількома стількома багатьма небагатьма", WT.PLURAL | WT.CASUS5 );

	   
//	  m_tag_rules.add(new TagRule(".*оя", ".*оя", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.SINGLE | WT.ANY_GENDER ));
//	  m_tag_rules.add(new TagRule(".*оя", ".*оєї", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.FEMALE ));
//	  m_tag_rules.add(new TagRule(".*оя", ".*оїй", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.FEMALE ));
//	  m_tag_rules.add(new TagRule(".*оя", ".*ою", ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.FEMALE ));
//	  m_tag_rules.add(new TagRule(".*оя", ".*єю", ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.FEMALE ));
//	  
//	  m_tag_rules.add(new TagRule(".*оя", ".*ого", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));
//	  m_tag_rules.add(new TagRule(".*оя", ".*оєму", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));
//	  m_tag_rules.add(new TagRule(".*оя", ".*ій", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));
	  
	  m_tag_rules.add(new TagRule(".*", "зроблений",  ".*", ".*", WT.ADJ, WT.ADJPART));

	  m_tag_rules.add(new TagRule(".*[иі]й", ".*",  ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*[ая]", ".*", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.SINGLE | WT.FEMALE ));
	  m_tag_rules.add(new TagRule(".*[еє]", ".*",  ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*і", ".*", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.ANY_GENDER ));
	  
	  m_tag_rules.add(new TagRule(".*ого", ".*", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));
	  m_tag_rules.add(new TagRule(".*ої", ".*",  ".*", ".*",WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*[иі]х", ".*",  ".*", ".*",WT.ADJ, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.ANY_GENDER));
	  
	  m_tag_rules.add(new TagRule(".*ому", ".*",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*ій", ".*",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*[иі]м", ".*",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.PLURAL | WT.ANY_GENDER));
	  
	  m_tag_rules.add(new TagRule(".*[ую]", ".*",  ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.FEMALE)); 
	  
	  m_tag_rules.add(new TagRule(".*[иі]м", ".*",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*ою", ".*",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*[иі]ми", ".*",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.PLURAL | WT.ANY_GENDER));
	  
	  m_tag_rules.add(new TagRule(".*ім", ".*",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*ому", ".*",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*[иі]х", ".*",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.PLURAL | WT.MALE));

	
	  

	  
	  
	  m_tag_rules.add(new TagRule(".*ей", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*я", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*е", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.FEMALE));
	  
	  
	  m_tag_rules.add(new TagRule(".*ього", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*ієї", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
	  
	  m_tag_rules.add(new TagRule(".*ьому", ".*ей",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*ій", ".*ей",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.FEMALE));
	  
	  m_tag_rules.add(new TagRule(".*ю", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.NEUTRAL));
	  
	  m_tag_rules.add(new TagRule(".*ими", ".*ей",  ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*их", ".*ей",  ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.ANY_GENDER));
	  
	  m_tag_rules.add(new TagRule(".*их", ".*ей",  ".*", ".*", WT.ADJ, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*им", ".*ей",  ".*", ".*", WT.ADJ, WT.CASUS3 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*і", ".*ей",  ".*", ".*",  WT.ADJ, WT.CASUS4 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*ими", ".*ей",  ".*", ".*",WT.ADJ, WT.CASUS5 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*их", ".*ей",  ".*", ".*",WT.ADJ, WT.CASUS6 | WT.PLURAL | WT.ANY_GENDER));
	  

	  m_tag_rules.add(new TagRule("я|мо.*|мн.*", ".*",  ".*", "", WT.PRONOUN, WT.PERSON1));
	  m_tag_rules.add(new TagRule("т.*", ".*",  ".*", "", WT.PRONOUN, WT.PERSON2));
	  m_tag_rules.add(new TagRule("[вй].*", ".*",  ".*", "", WT.PRONOUN | WT.SINGLE, WT.PERSON3));
	  
	  m_tag_rules.add(new TagRule("ми|н.*", ".*",  ".*", "", WT.PRONOUN, WT.PERSON1));
	  m_tag_rules.add(new TagRule("ва.*", ".*",  ".*", "", WT.PRONOUN | WT.PLURAL, WT.PERSON2));
	  m_tag_rules.add(new TagRule("ї.*|во.*", ".*",  ".*", "", WT.PRONOUN | WT.PLURAL, WT.PERSON3));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "", WT.NOUN, WT.CASUS1 |  WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg", "а", WT.NOUN, WT.CASUS2 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef", "у", WT.NOUN, WT.CASUS2 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "у", WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "ом", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef", "у", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "efg?", "і", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "и", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "ів", WT.NOUN, WT.CASUS2 |  WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "и", WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ef.*", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "b", "и", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.ANY_GENDER | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "b", "ей", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  "b", "ів", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  "b", "ь", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  "b", "ям", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  "b", "ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  "b", "[ая]х", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.ANY_GENDER));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "и", WT.NOUN, WT.CASUS2 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "ці", WT.NOUN, WT.CASUS3 | WT.CASUS6 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "у", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "ою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "и", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "", WT.NOUN, WT.CASUS2 | WT.CASUS4 |  WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abd?", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "abc", "", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*[ая]", ".*",  "abc", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*[ую]", ".*",  "abc", ".*", WT.NOUN, WT.CASUS3 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*я", ".*",  "abc", ".*", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*[ое]м", ".*",  "abc", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*і", ".*",  "abc", ".*", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*(ці|и)", ".*",  "abc", ".*", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*ів",     ".*",  "abc", ".*", WT.NOUN, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*[ая]м",  ".*",  "abc", ".*", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*ми",     ".*",  "abc", ".*", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*[ая]х",  ".*",  "abc", ".*", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
	  
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.FEMALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "и", WT.NOUN, WT.CASUS2 |  WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "ці", WT.NOUN, WT.CASUS3 | WT.CASUS6 |  WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "у", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "ою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "и", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.FEMALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "", WT.NOUN, WT.CASUS2 | WT.CASUS4 |  WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ad", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.FEMALE));

	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "и", WT.NOUN, WT.CASUS2 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "і", WT.NOUN, WT.CASUS3 | WT.CASUS6 |  WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "у", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "ою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "и", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*[^а]", ".*",  "adp", "", WT.NOUN, WT.CASUS2 | WT.CASUS4 |  WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "ів", WT.NOUN, WT.CASUS2 | WT.CASUS4 |  WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "adp", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.FEMALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "[іи]", WT.NOUN, WT.CASUS2 | WT.CASUS3 |  WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "[юу]", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "[ео]?ю", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "і", WT.NOUN, WT.CASUS6 |  WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "[іи]", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.FEMALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "ь", WT.NOUN, WT.CASUS2 | WT.CASUS4 |  WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "", WT.NOUN, WT.CASUS2 | WT.CASUS4 |  WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "[ая]?м", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "[ая]?ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "abZ?", "[ая]?х", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.FEMALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.FEMALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "и", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "і", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "у", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "ою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "и", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.FEMALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "іт", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  "a", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.FEMALE));	  
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "т", WT.NOUN, WT.CASUS2 |  WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "ті", WT.NOUN, WT.CASUS3 | WT.CASUS6 |  WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "тою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "", WT.NOUN, WT.CASUS1 |  WT.PLURAL | WT.NEUTRAL | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "т", WT.NOUN, WT.CASUS2 | WT.CASUS4 |  WT.PLURAL | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "там", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "lm", "тах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.NEUTRAL));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  "ijZ?", "", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
	  m_tag_rules.add(new TagRule(".*[аяі]", ".*",  "ijZ?", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ijZ?", "[юу]", WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ijZ?", "м", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ijZ?", "ами", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ijZ?", "і", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.NEUTRAL));
	  
	  m_tag_rules.add(new TagRule("що", ".*",  ".*", ".*", WT.PRONOUN, WT.CASUS1 | WT.CASUS4 | WT.CASUS6 |
			  WT.SINGLE | WT.PLURAL | WT.ANY_GENDER | WT.PERSON3));
	  
	  m_tag_rules.add(new TagRule("ніщо", ".*",  ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
	  m_tag_rules.add(new TagRule("нічого", ".*",  ".*", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule("нічому", ".*",  ".*", ".*", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule("нічим", ".*",  ".*", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));

	  m_tag_rules.add(new TagRule("щось", ".*",  ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
	  m_tag_rules.add(new TagRule("чогось", ".*",  ".*", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule("чомусь", ".*",  ".*", ".*", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule("чимось", ".*",  ".*", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));

	  m_tag_rules.add(new TagRule("сон", ".*",  ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule("сну", ".*",  ".*", ".*", WT.NOUN, WT.CASUS2 | WT.CASUS3 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule("сном", ".*",  ".*", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
	  m_tag_rules.add(new TagRule("сні", ".*",  ".*", ".*", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
	  
	  m_tag_rules.add(new TagRule("сни", ".*",  ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.MALE | WT.PERSON3));
	  m_tag_rules.add(new TagRule("снів", ".*",  ".*", ".*", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule("снам", ".*",  ".*", ".*", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule("снами", ".*",  ".*", ".*", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
	  m_tag_rules.add(new TagRule("снах", ".*",  ".*", ".*", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
	  
	  // ТОDO - fork to several hypotheses
	  m_tag_rules.add(new TagRule(".*", ".*",  "ij", "м", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ij", "ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  "ij", "х", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.NEUTRAL));
	  
	  /* VERBS */
	  
	  m_tag_rules.add(new TagRule(".*ти", ".*",  ".*", "",  WT.VERB, WT.INFINITIVE));
	  m_tag_rules.add(new TagRule(".*тися", ".*",  ".*", "",  WT.VERB, WT.INFINITIVE));
	  
	  m_tag_rules.add(new TagRule("міг", ".*",  ".*", "", WT.VERB, WT.ANY_PERSON | WT.SINGLE | WT.PAST | WT.MALE));
	  
	  m_tag_rules.add(new TagRule(".*[юу]сь", ".*",  ".*", ".*",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*шся", ".*",  ".*", ".*", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*[єе]ться", ".*",  ".*", ".*",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*мося", ".*",  ".*", ".*",  WT.VERB, WT.PERSON1 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*теся", ".*",  ".*", ".*", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*[юя]ться", ".*",  ".*", ".*",  WT.VERB, WT.PERSON3 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));

	  
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[лчж]?[ую]",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[лчж]?[єе]ш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[лчж]?[єе]",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[лчж]?[єе]мо",  WT.VERB, WT.PERSON1 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[лчж]?[єе]те", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[лчж]?[уюя]ть",  WT.VERB, WT.PERSON3 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));

	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[шо]*в",  WT.VERB, WT.ANY_PERSON | WT.SINGLE | WT.PAST | WT.MALE));
	  m_tag_rules.add(new TagRule(".*із", ".*",  ".*", ".*",  WT.VERB, WT.ANY_PERSON | WT.SINGLE | WT.PAST | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[ш]?ла", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PAST | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[ш]?ло", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PAST | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "[ш]?ли", WT.VERB, WT.ANY_PERSON | WT.PLURAL | WT.PAST));

	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "вся",  WT.VERB, WT.ANY_PERSON | WT.SINGLE | WT.PAST | WT.MALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "лася", WT.VERB, WT.ANY_PERSON | WT.PERSON1 | WT.SINGLE | WT.PAST | WT.FEMALE));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "лося", WT.VERB, WT.ANY_PERSON | WT.PERSON1 | WT.SINGLE | WT.PAST | WT.NEUTRAL));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "лися", WT.VERB, WT.ANY_PERSON | WT.PLURAL | WT.PAST));
	  
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "иму",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имеш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "име",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule("о.*", ".*",  ".*", "ь",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule("о.*", ".*",  ".*", "де",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "емо", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имете", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имуть", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
	  
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "мусь",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "имешся", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "иметься",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "мемося", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "метеся", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", ".*",  ".*", "муться", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
	  
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "ду",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "деш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "де",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "демо", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "дете", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.FUTURE));
	  m_tag_rules.add(new TagRule(".*", "бути", ".*", "дуть", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.FUTURE));
	  
	  m_tag_rules.add(new TagRule(".*", "йти", ".*", "ду",  WT.VERB, WT.PERSON1 | WT.SINGLE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", "йти", ".*", "деш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", "йти", ".*", "де",  WT.VERB, WT.PERSON3 | WT.SINGLE | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", "йти", ".*", "демо", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", "йти", ".*", "дете", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.ANY_GENDER));
	  m_tag_rules.add(new TagRule(".*", "йти", ".*", "дуть", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.ANY_GENDER));


	  m_tag_rules.add(new TagRule(".*", ".*",  ".*[abcdefghijklmnoqp].*", ".*", 0, WT.NOUN));
	  m_tag_rules.add(new TagRule(".*", ".+ти",  ".*", ".*", 0, WT.VERB));
	  m_tag_rules.add(new TagRule(".*", ".*тися",  ".*", ".*", 0, WT.VERB));
	  m_tag_rules.add(new TagRule(".*", ".*ий",  ".*", ".*", 0, WT.ADJ));
	  m_tag_rules.add(new TagRule(".*", ".*ий",  ".*", "о", 0, WT.ADV));
	  m_tag_rules.add(new TagRule(".*", ".*оя",  ".*", ".*", 0, WT.ADJ));
	  m_tag_rules.add(new TagRule(".*", ".*ко|.*но",  ".*", ".*", 0, WT.ADV));
	  m_tag_rules.add(new TagRule(".*", ".*но",  ".*", ".*", 0, WT.VERB | WT.PERSONLESS));
	  m_tag_rules.add(new TagRule("нема", ".*",  ".*", ".*", 0, WT.VERB | WT.PERSONLESS));
	  m_tag_rules.add(new TagRule("треба", ".*",  ".*", ".*", 0, WT.VERB | WT.PERSONLESS | WT.MODAL));
	  m_tag_rules.add(new TagRule("є", ".*",  ".*", ".*", 0, WT.VERB | WT.PERSONLESS | WT.ANY_PERSON));
	  m_tag_rules.add(new TagRule("добре", ".*",  ".*", ".*", 0, WT.ADV));
	  m_tag_rules.add(new TagRule(".*", ".*чи",  ".*", ".*", 0, WT.ADVPART));
	  
	}
		
	Matcher createMatcher(CharSequence text, String regexp, int flags)
	{
	   return Pattern.compile(regexp, flags).matcher(text) ;
	}
	
	private void addHypothesis(SentenceWord sw, int index, String base_form, HEntry s)
	{
//		if (LangProcSettings.DEBUG_OUTPUT)
//		{
//			LangProcOutput.println("Add hypo " + index + " " + base_form + " " + s.word + " " + s.astr);
//		}

	  String use_base = base_form;
	  if ( Character.isLowerCase(s.word.charAt(0)) ) use_base = base_form.toLowerCase();
	  
  	  TaggedWord w = new TaggedWord(index, use_base, s.word, s.astr);
	  TaggedWord w1 = null;
	  
	  //if (word.equals(word.toUpperCase())) w.addTag("Cap");
	  //if (Character.isUpperCase(word.charAt(0))) w.addTag("StartCap");
	  //if (word.equals(word.toLowerCase())) w.addTag("Low");
	  //if (word.toLowerCase().equals(s.word)) w.addTag("Base");
	  
	  //LangProcOutput.print( "   " + s.word + " " + s.astr + " " );
	  
	  if (m_pronoun_S_C1.contains(s.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS1);
	  if (m_pronoun_S_C2.contains(s.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS2);
	  if (m_pronoun_S_C3.contains(s.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS3);
	  if (m_pronoun_S_C4.contains(s.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS4);
	  if (m_pronoun_S_C5.contains(s.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS5);
	  if (m_pronoun_S_C6.contains(s.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS6);
	  
	  if (m_pronoun_M_C1.contains(s.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS1);
	  if (m_pronoun_M_C2.contains(s.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS2);
	  if (m_pronoun_M_C3.contains(s.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS3);
	  if (m_pronoun_M_C4.contains(s.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS4);
	  if (m_pronoun_M_C5.contains(s.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS5);
	  if (m_pronoun_M_C6.contains(s.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS6);
	  
	  if (m_pronoun_male.contains(s.word)) w.addTags(WT.MALE);
	  if (m_pronoun_female.contains(s.word)) w.addTags(WT.FEMALE);
	  if (m_pronoun_neutral.contains(s.word)) w.addTags(WT.NEUTRAL);
	  
	  if ( m_special_pronouns.containsKey(s.word)) { w.addTags(WT.PRONOUN); w.m_base_word = m_special_pronouns.get(s.word); }
	  
	  if (m_indacative_pronous.contains(w.m_base_word)) { w.addTags(WT.PRONOUN | WT.INDICATIVE); }
	  if (m_question_pronous.contains(w.m_base_word)) { w.addTags(WT.PRONOUN | WT.QUESTION); }

	  if (w.hasAllTags(WT.PRONOUN) && !w.hasSomeTags(WT.ANY_GENDER)) w.addTags(WT.ANY_GENDER); 
	  	    	  
	  if (w.hasSomeTags(WT.PRONOUN))
	  {
    	  w1 = w;
    	  w = new TaggedWord(index, use_base, s.word, s.astr);
	  }
	  
	  if (m_pronoun_ADJ_S_C1.contains(s.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS1);
	  if (m_pronoun_ADJ_S_C2.contains(s.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS2);
	  if (m_pronoun_ADJ_S_C3.contains(s.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS3);
	  if (m_pronoun_ADJ_S_C4.contains(s.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS4);
	  if (m_pronoun_ADJ_S_C5.contains(s.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS5);
	  if (m_pronoun_ADJ_S_C6.contains(s.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS6);
	  
	  if (m_pronoun_ADJ_M_C1.contains(s.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS1);
	  if (m_pronoun_ADJ_M_C2.contains(s.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS2);
	  if (m_pronoun_ADJ_M_C3.contains(s.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS3);
	  if (m_pronoun_ADJ_M_C4.contains(s.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS4);
	  if (m_pronoun_ADJ_M_C5.contains(s.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS5);
	  if (m_pronoun_ADJ_M_C6.contains(s.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS6);
	  
	  if (m_pronoun_ADJ_male.contains(s.word)) w.addTags(WT.MALE);
	  if (m_pronoun_ADJ_female.contains(s.word)) w.addTags(WT.FEMALE);
	  if (m_pronoun_ADJ_neutral.contains(s.word)) w.addTags(WT.NEUTRAL);
	  
	  if (w.hasAllTags(WT.ADJ) && !w.hasSomeTags(WT.ANY_GENDER)) w.addTags(WT.ANY_GENDER); 
	  
	  if (w.hasSomeTags(WT.ADJ))
	  {
		  if (w1!=null)
		  {
			  ApplyRules(w1);
			  sw.addHypothesis(w1);
		  }
	  }
	  else if (w1!=null)
	  {
		  w = w1;
	  }
	  

	  if (m_prepositions.containsKey(s.word)) w.addTags(WT.PREPOS);
	  if (m_parenthesis_words.contains(s.word)) w.addTags(WT.ADV);
	  if (m_particles.contains(s.word)) w.addTags(WT.PARTICLE);
	  if (m_negations.contains(s.word)) w.addTags(WT.NEGATION);
	  if (m_conjunction.contains(s.word)) w.addTags(WT.CONJ);
	  if (m_question_adv.contains(s.word)) w.addTags(WT.ADV);
	  if (m_adverbs.contains(s.word)) w.addTags(WT.ADV);
	  if (s.astr.indexOf('V')!=-1) w.addTags(WT.ADJ);
	  
	  if ( m_special_verbs.containsKey(s.word)) { w.addTags(WT.VERB); w.m_base_word = m_special_verbs.get(s.word); }
	  if ( m_modal_verbs.contains(w.m_base_word)) w.addTags(WT.MODAL);
	  if ( m_state_words.contains(w.m_base_word)) w.addTags(WT.STATE);
	  
	  if ( m_special_nouns.containsKey(s.word)) { w.addTags(WT.NOUN); w.m_base_word = m_special_nouns.get(s.word); }
	  
	  if ( m_countable.containsKey(w.m_base_word)) { w.addTags(WT.NUMERAL | m_countable.get(w.m_base_word).m_tags);  }
	  if ( m_countable_req_nom.containsKey(w.m_base_word)) { w.addTags(WT.NUMERAL | m_countable_req_nom.get(w.m_base_word).m_tags);  }
	  
	  
	  //if (s.word.equals("Микита")) w.addTags(WT.NOUN);
	  
	  ApplyRules(w);
	  
	  sw.addHypothesis(w);
	  
	  //LangProcOutput.print("|" + w);
	}
	
	private void addWordForms(Sentence ss, String word)
	{
		//LangProcOutput.println("addWordForms " + word);
		int index = ss.numWords();
		SentenceWord sw = new SentenceWord(index);
		
	      List<HEntry> list = m_dict.checkList(word);
	      
	      if (list.size()==0)
	      {
	    	  // try upper case if it was the first word and it can't be found 
	    	  list = m_dict.checkList(word.toLowerCase());
	      }
	      
	      if (list.size()==0)
	      {
	    	  if (LangProcSettings.GENERATE_SUGGESTIONS)
	    	  {
			      List su_list = m_dict.getSuggestions(word);
			      
			      if (su_list.size()==0)
			      {
			    	  TaggedWord w = new TaggedWord(index, word, word, "");
			    	  if (word.equals(".") || word.equals("?") || word.equals("!") || word.equals(";") )
			    	  {
			    		  w.addTags(WT.SENTENCE_END);
			    	  }
			    	  else
			    	  {
		  		  	      w.addTags(WT.COMMA);
			    	  }
			    	  sw.addHypothesis(w);
			      }
			      else
			      {
				      for(Object o:su_list)
				      {
				    	  LangProcOutput.print(o.toString() + " ");
				    	  
				    	  List<HEntry> alt_list = m_dict.checkList(o.toString().toLowerCase());
				    	  for(HEntry alt_s:alt_list)
				    	  {
				    		  addHypothesis(sw, index, o.toString().toLowerCase(), alt_s);
				    	  }
				      }
				      LangProcOutput.println("");
			      }
	    	  }
	    	  else
	    	  {
	    		  if ( Character.isUpperCase( word.charAt(0) ) )
	    		  {
			    	  TaggedWord w = new TaggedWord(index, word, word, "");
		  		  	  w.addTags(WT.NOUN);
			    	  sw.addHypothesis(w);    			  
	    		  }
	    		  else
	    		  {
			    	  TaggedWord w = new TaggedWord(index, word, word, "");
			    	  if (word.equals(".") || word.equals("?") || word.equals("!") || word.equals(";") )
			    	  {
			    		  w.addTags(WT.SENTENCE_END);
			    	  }
			    	  else
			    	  {
		  		  	      w.addTags(WT.COMMA);
			    	  }
	  		  	      sw.addHypothesis(w);
	    		  }
	    	  }
	      }
	      else
	      {
	    	  java.util.HashSet<String> proc = new java.util.HashSet<String>();
	    	  for(HEntry s:list)
	    	  {
	    		  String def = s.word + "(" + s.astr + ")";
	    		  if (!proc.contains(def))
	    		  {
	    			  addHypothesis(sw, index, word, s);
	    			  proc.add(def);
	    		  }
	    		  
	    	  }
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
			//LangProcOutput.print(w.toString() + " ");
			
			//LangProcOutput.print(w.toString());
			//int s = w.toString().length();
			//for(int i= 20; i>s; --i) LangProcOutput.print(" ");
			addWordForms(ss, w.toString());
			//LangProcOutput.print(w.toString() + " ");
			//LangProcOutput.println();
		}
		LangProcOutput.println("\n");
		if (LangProcSettings.SENTENCE_OUTPUT)
		{
			ss.print();
		}
		LangProcOutput.print("\n\\hspace{1em}\n\n");
		ss.processSentence(this);
	}
	
  private void test(SpellChecker checker, String txt)
  {
    Word badWord = checker.checkSpell(txt);

    if (badWord == null)
      LangProcOutput.println("All OK!!!");
    else
    {
      LangProcOutput.println("Bad words: " + badWord);
      List<String> list = checker.getDictionary().getSuggestions(badWord);
      List<String> wl = list;
      for (String s : wl)
      {
    	  LangProcOutput.println(s);
      }
    }
  }
  
 public static void main1(String[] args)
  {
	  //ChoiceGraph.test();
	   //WeightedDirectedSparseGraph.test();

		AdjacencyList myEdges = new AdjacencyList();
		
		
		Node n0 = new Node(0);
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		Node n4 = new Node(4);

		myEdges.addEdge(n0, n1, 0);
		myEdges.addEdge(n0, n2, 0);
		myEdges.addEdge(n0, n3, 0);
		myEdges.addEdge(n0, n4, 0);
		
		myEdges.addEdge(n1,n2, 1);
		myEdges.addEdge(n2,n3, 1);
		myEdges.addEdge(n2,n4, 0.9);
		myEdges.addEdge(n4,n3, 1);
		myEdges.addEdge(n4,n1, 1);

		//SCC mySCC = new TarjanSCC();

//		List<Collection<Node>> sccs = mySCC.runSCCsearch(myEdges);
//		int i = 0;
//		for(Iterator<Collection<Node>> scc = sccs.iterator(); scc.hasNext(); ){
//		    Collection<Node> dummy = scc.next();
//		    LangProcOutput.print("SCC " + ++i + ":");
//		    for(Iterator<Node> v = dummy.iterator(); v.hasNext(); )
//			LangProcOutput.print(" " + v.next().name);
//		    LangProcOutput.println("");
//		}
		
		
		Edmonds myed = new Edmonds_Andre();	
		AdjacencyList rBranch;
	    rBranch = myed.getMaxBranching(n0, myEdges);
	    
	    for( com.altmann.Edge e : rBranch.getAllEdges())
	    {
	    	LangProcOutput.println(e);
	    }
  }
 public static void main2(String[] args)
 {
	 WeightedDirectedSparseGraph.test();
 }
 
 public static void main4(String[] args)
 {
	 ChoiceGraph.test();
 }



  public static void main(String[] args)
  {
	  //ChoiceGraph.test();
	  
	  //final boolean from_file = false;
	  final boolean from_file = !LangProcSettings.DEBUG_OUTPUT;
	  
	  
	try
	{
		if (from_file)
		{
			LangProcOutput.println("Reading file");
			java.io.InputStream ips=new java.io.FileInputStream("test.txt"); 
			java.io.InputStreamReader ipsr=new java.io.InputStreamReader(ips, "WINDOWS-1251");
			java.io.BufferedReader reader=new java.io.BufferedReader(ipsr);
			
			java.io.OutputStream ops=new java.io.FileOutputStream("out.txt"); 
			java.io.OutputStreamWriter opsr=new java.io.OutputStreamWriter(ops, "WINDOWS-1251");
			LangProcOutput.writer=new java.io.BufferedWriter(opsr);
			
			
			

			StringBuffer full_text = new StringBuffer();
			String line = null;
			
			LangProc lp = new LangProc(new OpenOfficeSpellDictionary("uk_UA"));
			
			int sentence_n = 0;
						
			while ((line = reader.readLine()) != null)
			{
				//LangProcOutput.println("Read line " + line);
				full_text.append(line).append(" ");
				
				int i=0;
				while( i<full_text.length() )
				{
					char c = full_text.charAt(i);
					if ( (int)c == 8217 ) full_text.setCharAt(i, '\'');
					if ( (int)c == '’' ) full_text.setCharAt(i, '\'');
					
					if (c=='.' || c=='!' || c=='?' || c==';')
					{
						String substr = full_text.substring(0, i+1);
						full_text.delete(0, i+1);
						i=0;
						++sentence_n;
						LangProcOutput.println();
						
						LangProcOutput.println("" + sentence_n + ": " + substr);
						
						lp.checkGrammar(substr);
					}
					else
					{
						++i;
					}
				}				
			}
			
			LangProcOutput.writer.flush();
			ops.close();			
		}
		else
		{
			 (new LangProc(new OpenOfficeSpellDictionary("uk_UA")))
			 .checkGrammar(
			//"Жив собі в однім лісі Лис Микита, хитрий-прехитрий."
					 //"м'яса"
					 //"міг можу може можете могло хотів хочу хоче збирався збиралася намагався намагалась намагатися бажаю провокує зобов'язана зобов'язав"
					 //"ніщо нічим нічого"
					 //"стільки разів гонили його стрільці."
					//"Дійшло до того, що він у білий день вибирався на полювання й ніколи не вертавсь з порожніми руками."
			//"Скільки разів гонили його стрільці, цькували його хортами, ставили на нього капкани або підкидали йому отруєного м'яса, нічим не могли його доконати."
					 //"Лис Микита сміявся собі з них, обминав усякі небезпеки ще й інших своїх товаришів остерігав."
					// "А вже як вибереться на лови — чи то до курника, чи до комори, то не було сміливішого, вигадливішого та спритнішого злодія."
					 //" Незвичайне щастя і його хитрість зробили його страшенно гордим."
					 //"Йому здавалося, що нема нічого неможливого для нього."
					 
					 //"Але на вулиці й на базарі крик, шум, гамір, вози скриплять, колеса гуркотять, коні гримлять копитами, свині кувічуть — одним словом, клекіт такий, якого наш Микита і в сні не бачив, і в гарячці не чув."
					 "Псів уже наш Микита не одурить."
			//"До червоної я йшов скелі."+
			//"Робота зроблена вчасно, але не добре."+
			//"Робота зроблени вчасно, але не добре."+
			//"Робота зроблени вчасно."+
			//"Я йду додому."+
			//"Йдучи додому."+
			//"Робота зроблена."+
			//"Вона знята."+
			//"Роботу зроблено."+
			//"Мені цікаво."+
			//"Зроби мені його машину."+
			//"Його словник."+	  			
			//"Ти бачив його словник, йдучи додому?"+
			//"Який, котрий, котрого, якого, які, якому." +
			//"Я подивилася цікавий фільм." +
			//"Я люблю український борщ." +
			//"Я маю коричневого собаку." +
			//"Маленька дівчинка годує жовтих курчат." +
			//"Я знаю українську мову добре." +
			//"Чоловік купив машину?. " +
			//"Коли ти купив машину?" +
			//"Я ніколи не читав цей текст!" +
			//"Я дивлюсь цікавий фільм." + 
			//"Я дивитимусь цікавий фільм." +
			//"Я не читав цей текст." +
			//"Я хочу мати ровер."+
			//"Моя бабуся має зелене пальто." +
			//"Прийменники не мають самостійного лексичного значення, тому членами речення не виступають."+
			//"Належачи до іменників, числівників, займенників, вони входять до складу другорядних членів речення." + 
			//"Прийменником називається службова частина мови, яка разом з відмінковими закінченнями іменників (або займенників) служить для вираження підрядних зв’язків між словами в реченні."+
			//""
			 );
			 
			 LangProcOutput.writer.flush();
		}  
    }
    catch (Exception e)
    {
      e.printStackTrace() ;
    }
  }
}