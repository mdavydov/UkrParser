package com.langproc;

public class TaggedWord
{
	int m_sentence_pos;
	int m_hypo_index;
	String m_word;
	String m_base_word;
	String m_dict_tags; // tags from SpellChecker dictionary
	WordTags m_tags = new WordTags();

	public TaggedWord(int sentence_pos, String word, String base_word,
			String dict_tags)
	{
		m_sentence_pos = sentence_pos;
		m_word = word;
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

	public int hashCode()
	{
		return toString().hashCode();
	}

	public boolean equals(TaggedWord w)
	{
		return toString().equals(w.toString());
	}
}