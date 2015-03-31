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

import java.util.regex.Pattern;


/*******************************************************************************
 * 
 * @author Maksym Davydov TagRule class represents single rule that can be
 *         applied to obtain more word tags
 */


public class TagRule
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

	TagRule(String word_pattern, String base_pattern, String dict_pattern,
			String diff_pattern, long req_tags, long put_tags)
	{
		m_word_pattern = Pattern.compile(word_pattern, 0);
		m_base_pattern = Pattern.compile(base_pattern, 0);
		m_dict_pattern = Pattern.compile(dict_pattern, 0);
		m_diff_pattern = Pattern.compile(diff_pattern, 0);

		m_req_tags = new WordTags(req_tags);
		m_put_tags = new WordTags(put_tags);
	}

	TagRule(String word_pattern, String base_pattern, String dict_pattern,
			String diff_pattern, WordTags req_tags, WordTags put_tags)
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

		int i1 = 0, i2 = 0;
		int w1_len = w.m_word.length();
		int w2_len = w.m_base_word.length();

		// skip "не" prefix
		if (w.m_word.indexOf("не") == 0 && w.m_base_word.indexOf("не") != 0)
		{
			i1 = 2;
		}

		for (; i1 < w1_len && i2 < w2_len
				&& w.m_word.charAt(i1) == w.m_base_word.charAt(i2); ++i1, ++i2)
		{
		}

		String diff = w.m_word.substring(i1);

		if (m_word_pattern.matcher(w.m_word).matches()
				&& m_base_pattern.matcher(w.m_base_word).matches()
				&& m_dict_pattern.matcher(w.m_ispell_dict_tags).matches()
				&& m_diff_pattern.matcher(diff).matches())
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
