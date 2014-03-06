package com.langproc;

public class WordTags
{
	long m_tags;

	WordTags()
	{
		m_tags = 0;
	}

	WordTags(WordTags other)
	{
		m_tags = other.m_tags;
	}

	WordTags(long tags)
	{
		m_tags = tags;
	}

	public void setTags(long tags)
	{
		m_tags |= tags;
	}

	public boolean hasAllTags(long tags)
	{
		return 0 == (~m_tags & tags);
	}

	public boolean hasTag(long tag)
	{
		return 0 == (~m_tags & tag);
	}

	public boolean hasSomeTags(long tags)
	{
		return 0 != (m_tags & tags);
	}

	public void setTags(WordTags t)
	{
		m_tags |= t.m_tags;
	}

	public boolean hasAllTags(WordTags t)
	{
		return 0 == (~m_tags & t.m_tags);
	}

	public boolean hasSomeTags(WordTags t)
	{
		return 0 != (m_tags & t.m_tags);
	}

	public long getPartOfSpeech()
	{
		return m_tags & WT.PART_OF_SPEECH_MASK;
	}

	public long getGender()
	{
		return m_tags & WT.GENDER_MASK;
	}

	public long getCount()
	{
		return m_tags & WT.COUNT_MASK;
	}

	public long getPerson()
	{
		return m_tags & WT.PERSON_MASK;
	}

	public long getCasus()
	{
		return m_tags & WT.CASUS_MASK;
	}

	public long getTime()
	{
		return m_tags & WT.TIME_MASK;
	}

	public long getPerfection()
	{
		return m_tags & WT.PERFECTION_MASK;
	}

	public boolean hasPartOfSpeech()
	{
		return 0 != (m_tags & WT.PART_OF_SPEECH_MASK);
	}

	public boolean hasGender()
	{
		return 0 != (m_tags & WT.GENDER_MASK);
	}

	public boolean hasCount()
	{
		return 0 != (m_tags & WT.COUNT_MASK);
	}

	public boolean hasPerson()
	{
		return 0 != (m_tags & WT.PERSON_MASK);
	}

	public boolean hasCasus()
	{
		return 0 != (m_tags & WT.CASUS_MASK);
	}

	public boolean hasTime()
	{
		return 0 != (m_tags & WT.TIME_MASK);
	}

	public boolean hasPerfection()
	{
		return 0 != (m_tags & WT.PERFECTION_MASK);
	}

	public boolean samePart(WordTags o)
	{
		return 0 != (m_tags & o.m_tags & WT.PART_OF_SPEECH_MASK);
	}

	public boolean sameGender(WordTags o)
	{
		return 0 != (m_tags & o.m_tags & WT.GENDER_MASK);
	}

	public boolean sameCount(WordTags o)
	{
		return 0 != (m_tags & o.m_tags & WT.COUNT_MASK);
	}

	public boolean samePerson(WordTags o)
	{
		return 0 != (m_tags & o.m_tags & WT.PERSON_MASK);
	}

	public boolean sameCasus(WordTags o)
	{
		return 0 != (m_tags & o.m_tags & WT.CASUS_MASK);
	}

	public boolean sameTime(WordTags o)
	{
		return 0 != (m_tags & o.m_tags & WT.TIME_MASK);
	}

	public boolean samePerfection(WordTags o)
	{
		return 0 != (m_tags & o.m_tags & WT.PERFECTION_MASK);
	}

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
