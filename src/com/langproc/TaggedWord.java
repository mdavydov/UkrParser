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

public class TaggedWord
{
	int m_sentence_pos;
	int m_hypo_index;
	String m_word_as_was_written;
	String m_word;
	String m_base_word;
	String m_dict_tags; // tags from SpellChecker dictionary
	WordTags m_tags = new WordTags();

	public TaggedWord(int sentence_pos, String sentence_word, String base_word,
			String dict_tags)
	{
		m_sentence_pos = sentence_pos;
		m_word_as_was_written = sentence_word;
		
		if (Character.isLowerCase(base_word.charAt(0)))
		{
			m_word = sentence_word.toLowerCase();
		}
		else
		{
			m_word = sentence_word;
		}
		
		m_base_word = base_word;
		m_dict_tags = dict_tags;
	}

	int getSentencePos()
	{
		return m_sentence_pos;
	}

	public WordTags getTags()
	{
		return m_tags;
	}

	public boolean hasAllTags(WordTags tags)
	{
		return m_tags.hasAllTags(tags);
	}

	public boolean hasAllTags(long tags)
	{
		return m_tags.hasAllTags(tags);
	}

	public boolean hasSomeTags(WordTags tags)
	{
		return m_tags.hasSomeTags(tags);
	}

	public boolean hasSomeTags(long tags)
	{
		return m_tags.hasSomeTags(tags);
	}

	public void addTags(WordTags tags)
	{
		m_tags.setTags(tags);
	}

	public void addTags(long tags)
	{
		m_tags.setTags(tags);
	}

	public void setHypotesisIndex(int hyp_index)
	{
		m_hypo_index = hyp_index;
	}

	public String toString()
	{
		return m_word_as_was_written + "(" + m_base_word + ")";

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

	public int hashCode()
	{
		return toString().hashCode();
	}

	public boolean equals(TaggedWord w)
	{
		return toString().equals(w.toString());
	}
}
