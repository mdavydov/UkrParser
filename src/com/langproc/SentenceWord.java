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

public class SentenceWord
{
	public int m_index;
	public Vector<TaggedWord> m_hypotheses = new Vector<TaggedWord>();

	// public java.util.TreeMap<String, SentenceWord> m_dependencies = new
	// java.util.TreeMap<String, SentenceWord>();
	// public TaggedWord m_word=null;

	public SentenceWord(int index)
	{
		m_index = index; /* m_word = null; */
	}

	// public void setClosed(TaggedWord meaning) { m_word = meaning; }
	// public void setOpen() { m_word = null; m_dependencies.clear(); }
	// public void addChild(String role, SentenceWord w)
	// {
	// m_dependencies.put(role, w);
	// }

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
		LangProcOutput.print(m_index);
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
