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

public class WordStat
{
	public int m_repeat_count = 0;
	public int m_sum_interval_close = 0;
	public int m_sum_interval_far = 0;

	public int m_rep_close_count = 0;
	public int m_rep_far_count = 0;

	// public int m_num_intervals=0; // = m_repeat_count-1
	public int m_last_entry = 0;
	public int m_last_interval = 0;

	public void notifyUse(int text_position)
	{
		++m_repeat_count;
		if (m_repeat_count >= 2)
		{
			int effective_interval = text_position - m_last_entry;

			if (m_last_interval != 0 && m_last_interval < text_position - m_last_entry)
			{
				effective_interval = m_last_interval;
			}

			if (effective_interval < 0)
			{
				m_sum_interval_close += effective_interval;
				m_rep_close_count += 1;
			}
			else
			{
				m_sum_interval_far += effective_interval;
				m_rep_far_count += 1;
			}
		}
		m_last_entry = text_position;
	}

	public boolean wasRepeated()
	{
		return m_repeat_count >= 2;
	}

	public float getProbability(int total_words)
	{
		return (float) m_repeat_count / total_words;
	}

	public float getProbabilityForRepetition(int interval_length, int total_words)
	{
		float mean_pr = getProbability(total_words);
		if (m_repeat_count <= 1) return 0;//mean_pr * interval_length;

		float m_prob_close = m_rep_close_count / (float) (m_rep_close_count + m_rep_far_count);

		float prob = 0;
		
		if (m_rep_close_count > 0)
		{
			float lambda1 = 1.0f / (m_sum_interval_close / (float) m_rep_close_count);
			prob += m_prob_close * (1.0 - (float) java.lang.Math.exp(-lambda1 * interval_length));
		}

		if (m_rep_far_count > 0)
		{
			float lambda2 = 1.0f / (m_sum_interval_far / (float) m_rep_far_count);
			prob += (1.0 - m_prob_close) * (1.0 - (float) java.lang.Math.exp(-lambda2 * interval_length));
		}

		return prob;
	}
}
