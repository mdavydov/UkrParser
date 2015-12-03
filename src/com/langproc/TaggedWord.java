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
	private int m_sentence_pos;
	int m_hypo_index;
	String m_word_as_was_written;
	String m_word;
	String m_base_word;
	String m_ispell_dict_tags; // tags from SpellChecker dictionary
	WordTags m_tags = new WordTags();

	public TaggedWord(String word_as_was_written, String base_word,
			String ispell_dict_tags)
	{
		if (base_word==null || word_as_was_written==null)
		{
			throw new java.lang.NullPointerException();
		}
		
		m_sentence_pos = -1;
		m_word_as_was_written = word_as_was_written;
		
		if (Character.isLowerCase(base_word.charAt(0)))
		{
			m_word = word_as_was_written.toLowerCase();
		}
		else
		{
			m_word = word_as_was_written;
		}

		m_base_word = base_word;
		m_ispell_dict_tags = ispell_dict_tags;
	}
	
	public String getWord() { return m_word; }
	public String getWordAsWritten() { return m_word_as_was_written; }
	public String getBaseBaseForm() { return m_base_word; }
	
	

	public TaggedWord(String word_as_was_written, String base_word, WordTags tags)
	{
		if (base_word==null || word_as_was_written==null || tags==null)
		{
			throw new java.lang.NullPointerException();
		}
		m_sentence_pos = -1;
		m_word_as_was_written = word_as_was_written;
		m_base_word = base_word;
		m_ispell_dict_tags = "";
		if (Character.isLowerCase(base_word.charAt(0)))
		{
			m_word = word_as_was_written.toLowerCase();
		}
		else
		{
			m_word = word_as_was_written;
		}
		m_tags = tags;
	}

	void setSentencePos(int sentence_pos)
	{
		m_sentence_pos = sentence_pos;
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

	public String getFullDesc()
	{
		StringBuffer b = new StringBuffer(200);
		b.append(m_sentence_pos);
		b.append(" ");
		b.append(m_word);
		b.append(" (");
		b.append(m_base_word);
		b.append(") ");
		b.append(m_ispell_dict_tags);
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
