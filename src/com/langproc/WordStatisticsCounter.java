package com.langproc;

import java.util.List;
import java.util.regex.Pattern;

import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;


public class WordStatisticsCounter
{
	LangProc m_langproc=null;
	
	java.util.HashMap<String, WordStat> m_word_form_stat = new java.util.HashMap<String, WordStat>();
	java.util.HashMap<String, WordStat> m_word_base_stat = new java.util.HashMap<String, WordStat>();
	int m_word_counter = 0;
	
	WordStatisticsCounter(LangProc langproc)
	{
		m_langproc = langproc;
	}
	
	public int getWordStatisticalWeight(String word, String base_form)
	{
		WordStat num_use = m_word_form_stat.get(word);
		WordStat num_base_use = m_word_base_stat.get(base_form);

		int weight = (num_use == null ? 0 : num_use.m_repeat_count - 1) +
				(num_base_use == null ? 0 : num_base_use.m_repeat_count - 1);
		
		return weight;
	}

	
	public void nextWord(String word)
	{
		
	}

	public void buildStatisticalTextModelForFile(String file_name, String out_file)
	{
		// init
		m_word_form_stat.clear();
		m_word_base_stat.clear();
		m_word_counter = 0;
		
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

			writer.write("#i;repeat_prob\n");

			float tot_pr=0;
			for(int i=0;i<=1000;i+=50)
			{
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
				writer.write(i + " " + tot_repeat_prob + " " + tot_base_repeat_prob + "\n");
			}

			writer.write("#Tot:" + tot_pr + "\n");
			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			

			List<HEntry> list = m_langproc.m_dict.checkList(word);

			if (list.size() == 0) {
				// try upper case if it was the first word and it can't be found
				word = word.toLowerCase();
				list = m_langproc.m_dict.checkList(word);
			}

			if (list.size() == 0)
			{
				System.out.println("Not in dictionary: " + w.toString());
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
			}
			//System.out.println(w.toString());
		}



		// print num of words, % words not repeated at all, graphic - interval->%repeated
	}

	
}
