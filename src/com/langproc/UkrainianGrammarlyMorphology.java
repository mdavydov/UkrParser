package com.langproc;

public class UkrainianGrammarlyMorphology
{
	java.util.HashMap<String, String> m_hash_map;
	
	java.util.Set<String> m_unknown_set = new java.util.HashSet<String>();
	
	boolean addTag(WordTags wt, String desc)
	{
		// part of speech
		if (desc.equals("verb")) wt.setTags(WT.VERB);
		else if (desc.equals("noun")) wt.setTags(WT.NOUN);
		else if (desc.equals("adj")) wt.setTags(WT.ADJ);
		else if (desc.equals("pronoun")) wt.setTags(WT.PRONOUN);
		else if (desc.equals("advp")) wt.setTags(WT.ADVPART);
		else if (desc.equals("adv")) wt.setTags(WT.ADV);
		else if (desc.equals("pron")) wt.setTags(WT.PRONOUN);
		else if (desc.equals("prep")) wt.setTags(WT.PREPOSITION);
		else if (desc.equals("numr")) wt.setTags(WT.NUMERAL);
		else if (desc.equals("excl")) wt.setTags(WT.EXCLAMATION);
		else if (desc.equals("insert")) wt.setTags(WT.HELPWORD);
		// gender
		else if (desc.equals("n")) wt.setTags(WT.NEUTRAL);
		else if (desc.equals("m")) wt.setTags(WT.MALE);
		else if (desc.equals("f")) wt.setTags(WT.FEMALE);
		// casus
		else if (desc.equals("v_naz")) wt.setTags(WT.CASUS1);
		else if (desc.equals("v_rod")) wt.setTags(WT.CASUS2);
		else if (desc.equals("v_dav")) wt.setTags(WT.CASUS3);
		else if (desc.equals("v_zna")) wt.setTags(WT.CASUS4);
		else if (desc.equals("v_oru")) wt.setTags(WT.CASUS5);
		else if (desc.equals("v_mis")) wt.setTags(WT.CASUS6);
		else if (desc.equals("v_kly")) wt.setTags(WT.CASUS7);
		
		// required casus (has sense for prepositions only!!!)
		else if (desc.equals("rv_naz")) wt.setTags(WT.CASUS1);
		else if (desc.equals("rv_rod")) wt.setTags(WT.CASUS2);
		else if (desc.equals("rv_dav")) wt.setTags(WT.CASUS3);
		else if (desc.equals("rv_zna")) wt.setTags(WT.CASUS4);
		else if (desc.equals("rv_oru")) wt.setTags(WT.CASUS5);
		else if (desc.equals("rv_mis")) wt.setTags(WT.CASUS6);
		else if (desc.equals("rv_kly")) wt.setTags(WT.CASUS7);
		
		else if (desc.equals("perf")) wt.setTags(WT.PERFECT);
		else if (desc.equals("imperf")) wt.setTags(WT.IMPERFECT);
		
		else if (desc.equals("inf")) wt.setTags(WT.INFINITIVE);
		else if (desc.equals("ist")) wt.setTags(WT.ALIVE);
		
		else if (desc.equals("impers")) wt.setTags(WT.PERSONLESS);
		else if (desc.equals("subord")) wt.setTags(WT.SUBORD_CONJ);
		else if (desc.equals("dial")) {} //ignore, DIALECT
		else if (desc.equals("nv")) wt.setTags(WT.ANY_CASUS);
		else if (desc.equals("coord"))  wt.setTags(WT.COORD_CONJ);

		else if (desc.equals("obs")) {} //ignore
		else if (desc.equals("rare")) {} //ignore
		else if (desc.equals("coll")) {} //ignore, розмовне
		else if (desc.equals("adjp")) wt.setTags(WT.ADJPART);
		else if (desc.equals("abbr")) wt.setTags(WT.ABBREVIATED);
		else if (desc.equals("bad")) {} //ignore

		else if (desc.equals("compr")) wt.setTags(WT.COMPARE);
		else if (desc.equals("super")) wt.setTags(WT.COMPARESUPER);


		// person
		else if (desc.equals("1")) wt.setTags(WT.PERSON1);
		else if (desc.equals("2")) wt.setTags(WT.PERSON2);
		else if (desc.equals("3")) wt.setTags(WT.PERSON3);

		else if (desc.equals("rev")) wt.setTags(WT.REVERSE);

		else if (desc.equals("conj")) wt.setTags(WT.CONJ);
		else if (desc.equals("pers")) wt.setTags(WT.PERS_PRON);
		else if (desc.equals("v-u")) {}	// ignore
		else if (desc.equals("alt")) {}	// ignore
		else if (desc.equals("compb")) {}	// ignore
		else if (desc.equals("impr")) wt.setTags(WT.IMPERATIVE);
		else if (desc.equals("mis")) {}	// ignore
		else if (desc.equals("predic")) wt.setTags(WT.PREDICATIVE_WORD);
		else if (desc.equals("actv")) wt.setTags(WT.ACTIVE);
		else if (desc.equals("pasv")) wt.setTags(WT.PASSIVE);
		else if (desc.equals("&adj")) {}	// ignore
		else if (desc.equals("&pron"))  wt.setTags(WT.ADJ_PRON);
		else if (desc.equals("unknown")) {}	// ignore
		// singular/plural
		else if (desc.equals("s")) wt.setTags(WT.SINGLE);
		else if (desc.equals("p")) wt.setTags(WT.PLURAL);
		
		else if (desc.equals("part")) wt.setTags(WT.PARTICLE);
		// time
		else if (desc.equals("past")) wt.setTags(WT.PAST);
		else if (desc.equals("pres")) wt.setTags(WT.PRESENT);
		else if (desc.equals("futr")) wt.setTags(WT.FUTURE);
		else
		{
			if (m_unknown_set.contains(desc)) return true;
			//m_unknown_set.add(desc);
			System.out.println("Unknown " + desc);
			return false;
		}
		return true;
	}
	
	WordTags getTags(String tag_list)
	{
		WordTags wt = new WordTags();
		int index = 0;
		boolean result = true;
		while(index<tag_list.length())
		{
			int index_next = tag_list.indexOf(':', index);
			if (index_next<0) index_next = tag_list.length();
			//System.out.print(" Attr=(" + tag_list.substring(index, index_next) + ")");
			if (!addTag(wt,tag_list.substring(index, index_next))) return null;
			index = index_next+1;
		}
		return wt;
	}
	
	void dump_variant(String written_word, String res)
	{
		String base;
		int index = res.indexOf(" ");
		if (index<0) index = res.length();
		base = res.substring(0, index);
		//System.out.print("Base=(" + base + ") ");
		WordTags wt = getTags(res.substring(index+1));
		if (wt==null)
		{
			System.out.println(written_word +"->"+ res);
		}
		else
		{
			System.out.println(written_word + "->" + wt);
		}
	}
	
	void dump_word(String s)
	{
		String res = m_hash_map.get(s);
		if (res==null)
		{
			System.out.println("Word "+s+" was not found");
		}
		else
		{
			System.out.println(s +"->"+ res);
			int index = 0;
			while(index<res.length())
			{
				int index_next = res.indexOf('|', index);
				if (index_next<0) index_next = res.length();
				dump_variant(s, res.substring(index, index_next));
				index = index_next+1;
			}
		}
	}
	
	UkrainianGrammarlyMorphology()
	{
		try
		{
			LangProcOutput.println("Reading file");
			java.io.InputStream ips = new java.io.FileInputStream("tagged.main.txt");
			java.io.InputStreamReader ipsr = new java.io.InputStreamReader(ips, "UTF-8");
			java.io.BufferedReader reader = new java.io.BufferedReader(ipsr);

			StringBuffer full_text = new StringBuffer();
			String line = null;

			int sentence_n = 0;
			int num_all = 0;
			
			m_hash_map = new java.util.HashMap<String, String>(2000000);
			
			while ((line = reader.readLine()) != null)
			{
				int space_ind = line.indexOf(' ');
				if (space_ind>0)
				{
					String word = line.substring(0, space_ind);
					String desc = line.substring(space_ind+1);
					//dump_variant(word, desc);
					String old_val = m_hash_map.get(word);
					if (old_val==null)
					{
						m_hash_map.put( word, desc );
					}
					else
					{
						m_hash_map.put( word, old_val + "|" + desc );
					}
				}
				//if (num_all<10) System.out.println("(" + line.substring(0, space_ind) + ")->(" + line.substring(space_ind+1) + ")");
				++num_all;
			}
			System.out.println("Read complete!!! NumWords = " + m_hash_map.size());
			ips.close();
			
			dump_word("дитині");
			dump_word("коліні");
			dump_word("загортати");
			dump_word("загорнув");
			
			for(String s : m_unknown_set)
			{
				System.out.println("Uknown tag " + s);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
