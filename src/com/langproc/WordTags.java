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
	
	public boolean equals(WordTags wt)
	{
		return m_tags == wt.m_tags;
	}

	public void setTags(long tags)
	{
		m_tags |= tags;
	}
	
	public void copy(WordTags wt)
	{
		m_tags = wt.m_tags;
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
	public void removeTags(WordTags t) { m_tags &= ~t.m_tags; }
	public void removeTags(long tags)  { m_tags &= ~tags; }
	
	public boolean hasCommonTagsInAllCategories(long tags, long categories)
	{
		long res = ~categories | (m_tags & tags);
		long all = m_tags | tags;
		return  (res & WT.COUNT_MASK)!=0 &&
				(res & WT.PERSON_MASK)!=0 &&
				(res & WT.GENDER_MASK)!=0 &&
				(res & WT.CASUS_MASK)!=0 &&
				(res & WT.PERFECTION_MASK)!=0 &&
				(res & WT.TIME_MASK)!=0 &&
				(res & WT.MODAL_MASK)!=0;	
	}
	
	public boolean hasRequiredTags(WordTags wt)
	{
		long categories = wt.getCategories();
		long other_tags = wt.m_tags & ~categories;
		// has common in categories, so that could be unified
		// +has all required tags (that are not in categories)
		return hasCommonTagsInAllCategories(wt.m_tags, categories) &&
			( other_tags & (~m_tags) )==0;
	}
	
	public void limitInCategories(WordTags wt)
	{
		m_tags = m_tags&( ~wt.getCategories() | wt.m_tags );
	}
	
	public void limitInCategories(WordTags categories, WordTags tags)
	{
		m_tags = m_tags&( ~categories.getCategories() | tags.m_tags );
	}
	
	public void limitInCategories(long categories, long tags)
	{
		m_tags = m_tags&( ~categories | tags );
	}
	
	public void limitInCategories(WordTags categories, long tags)
	{
		m_tags = m_tags&( ~categories.getCategories() | tags );
	}
	public void limitInCategories(long categories, WordTags tags)
	{
		m_tags = m_tags&( ~categories | tags.m_tags );
	}

	
	public long getCategories()
	{
		long res = 0;
		if ((m_tags & WT.COUNT_MASK)!=0) res|=WT.COUNT_MASK;
		if ((m_tags & WT.PERSON_MASK)!=0) res|=WT.PERSON_MASK;
		if ((m_tags & WT.GENDER_MASK)!=0) res|=WT.GENDER_MASK;
		if ((m_tags & WT.CASUS_MASK)!=0) res|=WT.CASUS_MASK;
		if ((m_tags & WT.PERFECTION_MASK)!=0) res|=WT.PERFECTION_MASK;
		if ((m_tags & WT.TIME_MASK)!=0) res|=WT.TIME_MASK;
		if ((m_tags & WT.MODAL_MASK)!=0) res|=WT.MODAL_MASK;
		return res;
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
		if (hasSomeTags(WT.PLURAL)) b.append("PL(n*) ");
		if (hasSomeTags(WT.SINGLE)) b.append("SG(n1) ");
		if (hasSomeTags(WT.PERSON1)) b.append("p1 ");
		if (hasSomeTags(WT.PERSON2)) b.append("p2 ");
		if (hasSomeTags(WT.PERSON3)) b.append("p3 ");
		if (hasSomeTags(WT.PERSONLESS)) b.append("p- ");
		if (hasSomeTags(WT.MALE)) b.append("gm ");
		if (hasSomeTags(WT.FEMALE)) b.append("gf ");
		if (hasSomeTags(WT.NEUTRAL)) b.append("gn ");
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
		if (hasSomeTags(WT.PREPOSITION)) b.append("PREPOS ");
		if (hasSomeTags(WT.HELPWORD)) b.append("HELPWORD ");

		if (hasSomeTags(WT.PERFECT)) b.append("PERFECT ");
		if (hasSomeTags(WT.IMPERFECT)) b.append("IMPERFECT ");
		if (hasSomeTags(WT.PAST)) b.append("PAST ");
		if (hasSomeTags(WT.PRESENT)) b.append("PRESENT ");
		if (hasSomeTags(WT.FUTURE)) b.append("FUTURE ");
		if (hasSomeTags(WT.STATE)) b.append("STATE ");
		if (hasSomeTags(WT.PROPERNAME)) b.append("PROPER ");

		if (hasSomeTags(WT.INFINITIVE)) b.append("INF ");
		if (hasSomeTags(WT.SENTENCE_END)) b.append("S-END ");
		if (hasSomeTags(WT.MODAL)) b.append("MOD+ ");
		if (hasSomeTags(WT.NON_MODAL)) b.append("MOD- ");
		if (hasSomeTags(WT.INDICATIVE)) b.append("IND ");
		if (hasSomeTags(WT.QUESTION)) b.append("QUE(q) ");
		if (hasSomeTags(WT.STATE)) b.append("STATE ");
		if (hasSomeTags(WT.IMPERATIVE)) b.append("IMP ");
		if (hasSomeTags(WT.RAW)) b.append("RAW(r) ");
		if (hasSomeTags(WT.ALIVE)) b.append("ALIVE ");
		if (hasSomeTags(WT.PASSIVE)) b.append("PASSIVE ");
		if (hasSomeTags(WT.ACTIVE)) b.append("ACTIVE ");
		
		if (hasSomeTags(WT.EXCLAMATION)) b.append("EXCLAMATION ");
		if (hasSomeTags(WT.ABBREVIATED)) b.append("ABBREVIATED ");
		if (hasSomeTags(WT.COMPARE)) b.append("COMPARE ");	
		if (hasSomeTags(WT.COMPARESUPER)) b.append("COMPARESUPER ");
		
		
		if (hasSomeTags(WT.REVERSE)) b.append("REVERSE ");
		if (hasSomeTags(WT.SUBORD_CONJ)) b.append("SUBORD_CONJ ");
		if (hasSomeTags(WT.COORD_CONJ)) b.append("COORD_CONJ ");
		if (hasSomeTags(WT.ADJ_PRON)) b.append("ADJ_PRON ");
		if (hasSomeTags(WT.PERS_PRON)) b.append("PERS_PRON ");
		
		if (hasSomeTags(WT.PREDICATIVE_WORD)) b.append("PREDICATIVE_WORD ");


		return b.toString();
	}
	
	static void readAttributeString(StringBuffer inbuf, WordTags specified, WordTags unified)
	{
		specified.m_tags = 0;
		unified.m_tags = 0;
		// read one token from the string and advance string pointer p
		while(inbuf.length()>0 && Character.isWhitespace(inbuf.charAt(0)))
		{
			inbuf.deleteCharAt(0);
		}
		if (inbuf.length()==0) return;
		
		// if not "[...someTags...]" return
		if (inbuf.charAt(0)!='[') return;
		inbuf.deleteCharAt(0);
		
		while(inbuf.length()>0)
		{
			if (inbuf.charAt(0)==']')
			{
				// attribute string is closed
				inbuf.deleteCharAt(0);
				return; 
			}
			switch(inbuf.charAt(0))
			{
				case 'G': unified.m_tags |= WT.GENDER_MASK; break;
				case 'C': unified.m_tags |= WT.CASUS_MASK; break;
				case 'N': unified.m_tags |= WT.COUNT_MASK; break;
				case 'P': unified.m_tags |= WT.PERSON_MASK; break;
				case 'T': unified.m_tags |= WT.TIME_MASK; break;
				case 'F': unified.m_tags |= WT.PERFECTION_MASK; break; // finished of not
				case 'M': unified.m_tags |= WT.MODAL_MASK; break; // finished of not

				case 'q': specified.m_tags |= WT.QUESTION; break;
				case 'u': specified.m_tags |= WT.PROPERNAME; break;
				case 'i': specified.m_tags |= WT.INFINITIVE; break;
						
				case 'm': // modality
					switch(inbuf.charAt(1))
					{			
					case '+': specified.m_tags |= WT.MODAL; break;
					case '-': specified.m_tags |= WT.NON_MODAL; break;
					default: System.out.println("Unknown modality m" + inbuf.charAt(1));
					}
					inbuf.deleteCharAt(0);
					break;

				
				case 'g': // gender
					switch(inbuf.charAt(1))
					{			
					case 'm': specified.m_tags |= WT.MALE; break;
					case 'f': specified.m_tags |= WT.FEMALE; break;
					case 'n': specified.m_tags |= WT.NEUTRAL; break;
					default: System.out.println("Unknown gender g" + inbuf.charAt(1));
					}
					inbuf.deleteCharAt(0);
					break;

				case 'c':
					switch(inbuf.charAt(1))
					{			
					case '1': specified.m_tags |= WT.CASUS1; break;
					case '2': specified.m_tags |= WT.CASUS2; break;
					case '3': specified.m_tags |= WT.CASUS3; break;
					case '4': specified.m_tags |= WT.CASUS4; break;
					case '5': specified.m_tags |= WT.CASUS5; break;
					case '6': specified.m_tags |= WT.CASUS6; break;
					case '7': specified.m_tags |= WT.CASUS7; break;
					default: System.out.println("Unknown casus c" + inbuf.charAt(1));
					}
					inbuf.deleteCharAt(0);
					break;
					
				case 'n': // count
					switch(inbuf.charAt(1))
					{			
					case '1': specified.m_tags |= WT.SINGLE; break;
					case '*': specified.m_tags |= WT.PLURAL; break;
					default: System.out.println("Unknown count n" + inbuf.charAt(1));
					}
					inbuf.deleteCharAt(0);
					break;
					
				case 'p':
					switch(inbuf.charAt(1))
					{			
					case '1': specified.m_tags |= WT.PERSON1; break;
					case '2': specified.m_tags |= WT.PERSON2; break;
					case '3': specified.m_tags |= WT.PERSON3; break;
					case '-': specified.m_tags |= WT.PERSONLESS; break;
					default: System.out.println("Unknown person p" + inbuf.charAt(1));
					}
					inbuf.deleteCharAt(0);
					break;
					
				case 'r': specified.m_tags |= WT.RAW; break;
					
				case ' ': break;
					
				default:
					System.out.println("Unknown tag " + inbuf.charAt(0));
					System.exit(0);
			}
			inbuf.deleteCharAt(0);
			// one-char tokens for attributes now
		}
		return;
	}
}
