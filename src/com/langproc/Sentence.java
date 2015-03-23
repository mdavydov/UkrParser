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

import java.util.Random;
import java.util.Vector;

public class Sentence implements java.lang.Iterable<SentenceWord>
{
	private Vector<SentenceWord> m_words = new Vector<SentenceWord>();

	public java.util.Iterator<SentenceWord> iterator()
	{
		return m_words.iterator();
	}
	public int numWords()
	{
		return m_words.size();
	}
	public SentenceWord wordAt(int index) { return m_words.elementAt(index); }

	public int numHypotheses()
	{
		int num = 0;
		for (SentenceWord w : m_words)
		{
			num += w.numHypotheses();
		}
		return num;
	}

	public void addWord(SentenceWord w)
	{
		m_words.addElement(w);
	}

	boolean hasWordsWithAllTagsBetween(int index_from, int index_to, long tags)
	{
		if (Math.abs(index_from - index_to) <= 1) return false;

		if (index_to >= m_words.size()) index_to = m_words.size() - 1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size() - 1;
		if (index_from < 0) index_from = 0;

		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;

		for (int i = index_from + dir; i != index_to - dir; i += dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			if (sw.hasHypothesisWithAllTags(tags)) return true;
		}
		return false;
	}

	boolean hasWordsWithSomeTagsBetween(int index_from, int index_to, long tags)
	{
		if (Math.abs(index_from - index_to) <= 1) return false;

		if (index_to >= m_words.size()) index_to = m_words.size() - 1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size() - 1;
		if (index_from < 0) index_from = 0;

		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;

		for (int i = index_from + dir; i != index_to - dir; i += dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			if (sw.hasHypothesisWithSomeTags(tags)) return true;
		}
		return false;
	}

	boolean hasWordsWithSomeTagsBetween(int index_from, int index_to, long tags_group1, long tags_group2)
	{
		if (Math.abs(index_from - index_to) <= 1) return false;

		if (index_to >= m_words.size()) index_to = m_words.size() - 1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size() - 1;
		if (index_from < 0) index_from = 0;

		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;

		for (int i = index_from + dir; i != index_to - dir; i += dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			if (sw.hasHypothesisWithSomeTags(tags_group1, tags_group2)) return true;
		}
		return false;
	}

	public int getNumCorrectionChoices()
	{
		int total = 1;

		for (SentenceWord sw : m_words)
		{
			int n = sw.numHypotheses();

			boolean different = false;

			if (n > 0)
			{
				String case1 = sw.getHypothesis(0).m_word;
				for (int i = 1; i < n; ++i)
				{
					TaggedWord tw = sw.getHypothesis(i);
					if (!tw.m_word.equals(case1)) different = true;
				}
			}
			total *= n == 0 || !different ? 1 : n;
		}
		// System.out.println("Total = " + total);
		return total;
	}
	
	static private Random randomGen;

	public String tryFixUsingRandom(Morphology morphology, boolean use_word_weighting)
	{
		int vertex_choices[] = new int[numWords()];

		if (randomGen !=null) randomGen = new Random();

		int i = 0;
		for (SentenceWord sw : m_words)
		{
			int n = sw.numHypotheses();
			// TODO: reevaluate word weight. Now all are 1.0f :-)

			int max_weight = 0;
			int best_val = 0;

			if (use_word_weighting)
			{
				for (int j = 0; j < n; ++j)
				{
					TaggedWord tw = sw.getHypothesis(j);

					int weight = morphology.getWordStatisticalWeight(tw.m_word, tw.m_base_word);

					// if (num_base_use!=null &&
					// num_base_use.getMeanInterval()>200) weight=0;

					if (weight > max_weight)
					{
						max_weight = weight;
						best_val = j;
					}
				}
			}

			vertex_choices[i] = max_weight > 0 ? best_val : (n > 0 ? randomGen.nextInt(n) : 0);
			++i;
		}

		StringBuffer sb = new StringBuffer(128);

		for (i = 0; i < vertex_choices.length; ++i)
		{
			if (m_words.get(i).numHypotheses() > 0)
			{
				TaggedWord tw = m_words.get(i).getHypothesis(vertex_choices[i]);
				if (i != 0 && !tw.hasSomeTags(WT.ANY_PUNCT)) sb.append(' ');
				sb.append(tw.m_word);
			}
		}
		sb.setCharAt(0, java.lang.Character.toUpperCase(sb.charAt(0)));

		return sb.toString();

	}

	public void print()
	{
		for (SentenceWord w : m_words)
		{
			w.print();
			LangProcOutput.println();
			LangProcOutput.println();
			LangProcOutput.flush();
		}
	}
}
