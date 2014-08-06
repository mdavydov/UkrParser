package com.langproc;

import java.util.List;
import java.util.regex.Pattern;

import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;

class IntervalCounter
{
	int m_num_repeat=0;
	int m_num_words=0;
	
	int m_num_base_repeat=0;
	int m_num_bases=0;
	
	int m_interval_length;
	
	java.util.HashMap<String, Integer> m_word_in_interval_count = new java.util.HashMap<String, Integer>();
	java.util.HashMap<String, Integer> m_base_in_interval_count = new java.util.HashMap<String, Integer>();
	
	java.util.List<String> m_word_sequence = new java.util.LinkedList<String>();
	
	java.util.List< java.util.HashSet<String> > m_base_sequence =
		new java.util.LinkedList< java.util.HashSet<String> >();
		
	
	IntervalCounter(int interval_length)
	{
		m_interval_length = interval_length;
	}
	
	int getIntervalLength() { return m_interval_length; }
	
	void nextBases(java.util.HashSet<String> bases)
	{
		m_base_sequence.add(bases);
		
		for(String word : bases)
		{
			Integer i = m_base_in_interval_count.get(word);
			if (i==null)
			{
				m_base_in_interval_count.put(word, new Integer(1));
			}
			else
			{
				m_base_in_interval_count.put(word, new Integer(i.intValue()+1));
			}
		}
		
		if (m_base_sequence.size() > m_interval_length)
		{
			java.util.HashSet<String> base_remove = m_base_sequence.remove(0);
			
			for(String word_remove : base_remove)
			{
				Integer i = m_base_in_interval_count.get(word_remove);
				if (i==null)
				{
					// should not occure
				}
				else
				{
					if (i.intValue()==1)
					{
						m_base_in_interval_count.remove(word_remove);
					}
					else
					{
						m_base_in_interval_count.put(word_remove, new Integer(i.intValue()-1));
					}
				}
			}
			
			// calculate stats
			
			int all_bases = 0;
			for(Integer num : m_base_in_interval_count.values())
			{
				all_bases += num.intValue();
				if (num.intValue() >= 2)
				{
					m_num_base_repeat += num.intValue(); // number of words repeated in interval
				}
			}
			m_num_bases += all_bases;
		}
	}

	
	void nextWord(String word)
	{
		m_word_sequence.add(word);
		Integer i = m_word_in_interval_count.get(word);
		if (i==null)
		{
			m_word_in_interval_count.put(word, new Integer(1));
		}
		else
		{
			m_word_in_interval_count.put(word, new Integer(i.intValue()+1));
		}
		
		if (m_word_sequence.size() > m_interval_length)
		{
			String word_remove = m_word_sequence.remove(0);
			
			i = m_word_in_interval_count.get(word_remove);
			if (i==null)
			{
				// should not occure
			}
			else
			{
				if (i.intValue()==1)
				{
					m_word_in_interval_count.remove(word_remove);
				}
				else
				{
					m_word_in_interval_count.put(word_remove, new Integer(i.intValue()-1));
				}
			}
			
			// calculate stats
			
			int all_w = 0;
			for(Integer num : m_word_in_interval_count.values())
			{
				all_w += num.intValue();
				if (num.intValue() >= 2)
				{
					m_num_repeat += num.intValue(); // number of words repeated in interval
				}
			}
			assert(all_w == m_interval_length);
			m_num_words += m_interval_length;
		}
	}
	
	float getRepeatProbability()
	{
		return (float)m_num_repeat / m_num_words;
	}
	
	float getBaseRepeatProbability()
	{
		return (float)m_num_base_repeat / m_num_bases;
	}
}

public class WordStatisticsCounter
{
	LangProc m_langproc=null;
	
	java.util.HashMap<String, WordStat> m_word_form_stat = new java.util.HashMap<String, WordStat>();
	java.util.HashMap<String, WordStat> m_word_base_stat = new java.util.HashMap<String, WordStat>();
	int m_word_counter = 0;
	
	java.util.Vector<IntervalCounter> m_interval_counters = new java.util.Vector<IntervalCounter>();
	
	WordStatisticsCounter(LangProc langproc)
	{
		m_langproc = langproc;
	}
	

	private void buildStatisticalTextModel(String txt)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(
				Pattern.compile(
				"[ÀÁÂÃ¥ÄÅªÆÇ²ÉÈ¯ÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÜÞßàáâã´äåºæç³éè¿êëìíîïðñòóôõö÷øùüþÿ'’]+")
				.matcher(txt));
		
		while (wf.hasNext())
		{
			++m_word_counter; // current word counter

			Word w = wf.next();
			String word = w.toString();
			
			for(IntervalCounter ic : m_interval_counters)
			{
				ic.nextWord(word);
			}

			List<HEntry> list = m_langproc.m_dict.checkList(word);

			if (list.size() == 0) {
				// try upper case if it was the first word and it can't be found
				word = word.toLowerCase();
				list = m_langproc.m_dict.checkList(word);
			}

			if (list.size() == 0)
			{
				System.out.println("Not in dictionary: " + w.toString());
				
				if (!m_word_base_stat.containsKey(w.toString())) m_word_base_stat.put(w.toString(), new WordStat());
				m_word_base_stat.get(w.toString()).notifyUse(m_word_counter);
				
				for(IntervalCounter ic : m_interval_counters)
				{
					java.util.HashSet<String> bases = new java.util.HashSet<String>();
					bases.add(word);
					ic.nextBases(bases);
				}
			}
			else
			{
				java.util.HashSet<String> proc = new java.util.HashSet<String>();

				if (!m_word_form_stat.containsKey(word)) m_word_form_stat.put(word, new WordStat());
				m_word_form_stat.get(word).notifyUse(m_word_counter);


				for (HEntry s : list) {
					String def = s.word + "(" + s.astr + ")";
					if (!proc.contains(def))
					{
						//System.out.println(word + " " + def);
						if (!m_word_base_stat.containsKey(s.word)) m_word_base_stat.put(s.word, new WordStat());
						m_word_base_stat.get(s.word).notifyUse(m_word_counter);

						proc.add(def);
					}
				}
				
				for(IntervalCounter ic : m_interval_counters)
				{
					ic.nextBases(proc);
				}
			}
			//System.out.println(w.toString());
		}



		// print num of words, % words not repeated at all, graphic - interval->%repeated
	}
	
	
	public int getWordStatisticalWeight(String word, String base_form)
	{
		WordStat num_use = m_word_form_stat.get(word);
		WordStat num_base_use = m_word_base_stat.get(base_form);
		
		int use = (num_use == null ? 0 : num_use.m_repeat_count - 1);
		int base_use = (num_base_use == null ? 0 : num_base_use.m_repeat_count - 1);

		int weight = (use>=4?4:use) + (base_use>=4?4:base_use);
		
		return weight;
	}
	
	public void buildStatisticalTextModelForFile(String file_name, String out_file)
	{
		// init
		m_word_form_stat.clear();
		m_word_base_stat.clear();
		m_word_counter = 0;
		
		m_interval_counters.clear();
		
//		for(int i=25; i<=1000; i+=25) 
//		{
//			m_interval_counters.add( new IntervalCounter(i) );
//		}
		
		// calculate real data -> number of words in last "N" words

		java.io.InputStream ips;
		try {
			ips = new java.io.FileInputStream(file_name);
			java.io.InputStreamReader ipsr = new java.io.InputStreamReader( ips, "WINDOWS-1251");
			java.io.BufferedReader reader = new java.io.BufferedReader(ipsr);

			String line = null;
			while ((line = reader.readLine()) != null)
			{
				buildStatisticalTextModel(line);
			}

			java.io.OutputStream ops = new java.io.FileOutputStream(out_file);
			java.io.OutputStreamWriter opsr = new java.io.OutputStreamWriter( ops, "WINDOWS-1251");
			java.io.Writer writer = new java.io.BufferedWriter(opsr);

			writer.write("#i real_repeat repeat_exp repeat_base_exp\n");

			float tot_pr=0;
			
			for(IntervalCounter ic : m_interval_counters)
			{
				int i = ic.getIntervalLength();
				
				float tot_repeat_prob = 0;
				float tot_base_repeat_prob = 0;
				
				tot_pr = 0;
				for( WordStat ws : m_word_form_stat.values() )
				{
					float pr = ws.getProbability(m_word_counter);
					tot_pr += pr;
					tot_repeat_prob += pr * ws.getProbabilityForRepetition(i, m_word_counter); 
				}
				
				for( WordStat ws : m_word_base_stat.values() )
				{
					float pr = ws.getProbability(m_word_counter);
					//tot_pr += pr;
					tot_base_repeat_prob += pr * ws.getProbabilityForRepetition(i, m_word_counter); 
				}
				writer.write(i + " " + ic.getRepeatProbability() +
								 " " + ic.getBaseRepeatProbability() +
								 " " + tot_repeat_prob +
								 " " + tot_base_repeat_prob + "\n");
			}

			writer.write("#Tot:" + tot_pr + "\n");
			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	
}
