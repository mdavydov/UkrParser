/*******************************************************************************
 * UkrParser
 * Copyright (c) 2013-2014 Maksym Davydov
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/

package com.langproc;

import java.util.Vector;

/*******************************************************************************
 * 
 * @author Maksym Davydov SentenceWord class represents a single word in a
 *         sentence with all it's possible grammar meanings (hypotheses)
 */

public class WordHypotheses
{
	private int m_sentence_position;
	public Vector<TaggedWord> m_hypotheses = new Vector<TaggedWord>();

	// public java.util.TreeMap<String, SentenceWord> m_dependencies = new
	// java.util.TreeMap<String, SentenceWord>();
	// public TaggedWord m_word=null;

	public WordHypotheses(int sentence_pos)
	{
		m_sentence_position = sentence_pos; /* m_word = null; */
	}

	public void addHypothesis(TaggedWord w)
	{
		if (w==null)
		{
			throw new java.lang.NullPointerException();
		}
		if (w.hasAllTags(WT.NOUN) && !w.hasSomeTags(WT.CASUS_MASK))
		{
			System.out.println("Morphology error: noun has no casus " + w.getFullDesc());
			return;
		}
		
		if (w.hasAllTags(WT.VERB) && !w.hasSomeTags(WT.INFINITIVE | WT.PERSON_MASK | WT.IMPERATIVE))
		{
			System.out.println("Morphology error: verb is not infinitive|person|imperative " + w.getFullDesc());
			return;
		}
		w.setSentencePos(m_sentence_position);
		w.setHypotesisIndex(m_hypotheses.size());
		m_hypotheses.addElement(w);
	}

	public int getSentencePos() { return m_sentence_position; }
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
		for (TaggedWord t : m_hypotheses)
		{
			if (t.hasAllTags(tags)) return true;
		}
		return false;
	}

	boolean hasHypothesisWithSomeTags(long tags)
	{
		for (TaggedWord t : m_hypotheses)
		{
			if (t.hasSomeTags(tags)) return true;
		}
		return false;
	}

	boolean hasHypothesisWithSomeTags(long tags_group1, long tags_group2)
	{
		for (TaggedWord t : m_hypotheses)
		{
			if (t.hasSomeTags(tags_group1) && t.hasSomeTags(tags_group2)) return true;
		}
		return false;
	}

	public void print()
	{
		LangProcOutput.print(m_sentence_position);
		LangProcOutput.print(" ");
		// if (m_word!=null)
		// {
		// LangProcOutput.print("* ");
		// LangProcOutput.print(m_word);
		// }
		// else
		// {
		for (TaggedWord t : m_hypotheses)
		{
			LangProcOutput.print(t.getFullDesc());
			LangProcOutput.print(" || ");
		}
		// }
	}
}
