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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.Word;
import org.dts.spell.finder.CharSequenceWordFinder;

import com.altmann.AdjacencyList;
import com.altmann.Edmonds;
import com.altmann.Edmonds_Andre;
import com.altmann.MatrixIO;
import com.altmann.Node;
import com.altmann.SCC;
import com.altmann.TarjanSCC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.altmann.*;

class LangProc
{
	Morphology m_morphology;
	OpenOfficeSpellDictionary m_dict;
	
	LangProc()
	{
		try
		{
			m_dict = new OpenOfficeSpellDictionary("uk_UA");
			m_morphology = new UkrainianISpellMorphology(m_dict);
		}
		catch(IOException e)
		{
			System.out.println("Could not open uk_UA dictionary");
		}
	}

	private String checkGrammar(String txt, boolean use_word_weighting)
	{
		Sentence ss = m_morphology.parseSentenceMorphemes(txt);
		DependencyGrammar dg = new DependencyGrammar();
		return dg.processSentenceWithDependencyGrammar(m_morphology, ss, use_word_weighting);
	}

	private String checkGrammarAPCFG(String txt, boolean use_word_weighting)
	{
		Sentence ss = m_morphology.parseSentenceMorphemes(txt);
		APCFGUkrainian apcfg = new APCFGUkrainian();
		return apcfg.processSentenceWithAPCFG(m_morphology, ss, use_word_weighting);
	}

	private String tryFixRandom(String txt, boolean use_word_weighting)
	{
		Sentence ss = m_morphology.parseSentenceMorphemes(txt);
		return ss.tryFixUsingRandom(m_morphology, use_word_weighting);
	}

	private int getNumCorrectionChoices(String txt)
	{
		Sentence ss = m_morphology.parseSentenceMorphemes(txt);
		return ss.getNumCorrectionChoices();
	}

	public static void main1(String[] args)
	{
		// ChoiceGraph.test();
		// WeightedDirectedSparseGraph.test();

		AdjacencyList myEdges = new AdjacencyList();

		Node n0 = new Node(0);
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		Node n4 = new Node(4);

		myEdges.addEdge(n0, n1, 0);
		myEdges.addEdge(n0, n2, 0);
		myEdges.addEdge(n0, n3, 0);
		myEdges.addEdge(n0, n4, 0);

		myEdges.addEdge(n1, n2, 1);
		myEdges.addEdge(n2, n3, 1);
		myEdges.addEdge(n2, n4, 0.9);
		myEdges.addEdge(n4, n3, 1);
		myEdges.addEdge(n4, n1, 1);

		// SCC mySCC = new TarjanSCC();

		// List<Collection<Node>> sccs = mySCC.runSCCsearch(myEdges);
		// int i = 0;
		// for(Iterator<Collection<Node>> scc = sccs.iterator(); scc.hasNext();
		// ){
		// Collection<Node> dummy = scc.next();
		// LangProcOutput.print("SCC " + ++i + ":");
		// for(Iterator<Node> v = dummy.iterator(); v.hasNext(); )
		// LangProcOutput.print(" " + v.next().name);
		// LangProcOutput.println("");
		// }

		Edmonds myed = new Edmonds_Andre();
		AdjacencyList rBranch;
		rBranch = myed.getMaxBranching(n0, myEdges);

		for (com.altmann.Edge e : rBranch.getAllEdges())
		{
			LangProcOutput.println(e);
		}
	}

	public static void main2(String[] args)
	{
		WeightedDirectedSparseGraph.test();
	}

	public static void main3(String[] args)
	{
		try
		{
			java.io.OutputStream ops = new java.io.FileOutputStream("out_graph_test.txt");
			java.io.OutputStreamWriter opsr = new java.io.OutputStreamWriter(ops, "WINDOWS-1251");
			LangProcOutput.writer = new java.io.BufferedWriter(opsr);

			ChoiceGraph.test();

			LangProcOutput.writer.flush();
			ops.close();

		}
		catch (Exception e)
		{
		}

	}

	int num_pos1 = 0;
	int num_pos2 = 0;
	int num_pos3 = 0;
	int num_pos4 = 0;
	int num_single = 0;
	int num_not_possible = 0;
	int num_all = 0;
	long t1 = 0, t2 = 0, t3 = 0, t4 = 0;

	Random m_randomGen = new Random();

	void printSentencePersistence()
	{
		System.out.println("Single=" + (100.0f * num_single / (num_not_possible + num_single + num_all)) + "% NotPoss="
				+ (100.0f * num_not_possible / (num_not_possible + num_single + num_all)) + "% Positive gr=" + 100.0f * num_pos1 / num_all + "% gr_stat="
				+ 100.0f * num_pos2 / num_all + "% rand=" + 100.0f * num_pos3 / num_all + "% rand_stat=" + 100.0f * num_pos4 / num_all + "%");

		System.out.println("t1=" + t1 + " t2=" + t2 + " t3=" + t3 + " t4=" + t4);
	}

	void checkSentencePersistence(String s)
	{
		// checkGrammar(s,true);

		if (s.length() < 20 || s.length() > 150) return;
		if (s.contains("0")) return;
		if (s.contains("1")) return;
		if (s.contains("2")) return;
		if (s.contains("3")) return;
		if (s.contains("4")) return;
		if (s.contains("5")) return;
		if (s.contains("6")) return;
		if (s.contains("7")) return;
		if (s.contains("8")) return;
		if (s.contains("9")) return;
		if (s.contains("¬")) return;
		if (s.contains("(")) return;
		if (s.contains(")")) return;
		if (s.contains(";")) return;
		if (s.contains(":")) return;
		if (s.contains("-")) return;
		if (s.contains("—")) return;
		if (s.contains("•")) return;
		if (s.contains("«")) return;
		if (s.contains("»")) return;

		String s_orig = s;

		StringBuffer sb = new StringBuffer(s_orig);
		while (java.lang.Character.isWhitespace(sb.charAt(0)) || sb.charAt(0) == '-')
		{
			sb.deleteCharAt(0);
		}
		s_orig = sb.toString();

		for (int i = 0; i < 5; ++i)
		{
			sb = new StringBuffer(s_orig);

			int change_ind1 = m_randomGen.nextInt(sb.length() - 1);
			if (sb.charAt(change_ind1) == ' ') continue;
			sb.setCharAt(change_ind1, (char) ('а' + m_randomGen.nextInt(33)));

			if (getNumCorrectionChoices(sb.toString()) == 1)
			{
				num_single += 1;
				continue;
			}

			// boolean possible=false;
			// for(int j=0;j<100;++j)
			// {
			// if ( tryFixRandom(sb.toString(), false).equals(s_orig) )
			// {
			// possible = true;
			// break;
			// }
			// }
			//
			// if (!possible) { num_not_possible += 1; continue; }

			String result1, result2, result3, result4;
			try
			{
				// System.out.println(num_all);
				// System.out.println(s_orig);
				// System.out.println(sb);

				long t = 0;
				t = System.nanoTime();
				result1 = checkGrammar(sb.toString(), false);
				t1 += System.nanoTime() - t;
				// System.out.println(result1 +
				// (result1.equals(s_orig)?" +":" -"));

				t = System.nanoTime();
				result2 = checkGrammar(sb.toString(), true);
				t2 += System.nanoTime() - t;

				// System.out.println(result2 +
				// (result2.equals(s_orig)?" +":" -"));
				t = System.nanoTime();
				result3 = tryFixRandom(sb.toString(), false);
				t3 += System.nanoTime() - t;

				// System.out.println(result3 +
				// (result3.equals(s_orig)?" +":" -"));
				t = System.nanoTime();
				result4 = tryFixRandom(sb.toString(), true);
				t4 += System.nanoTime() - t;

				// System.out.println(result4 +
				// (result4.equals(s_orig)?" +":" -"));
				// System.out.println();
			}
			catch (java.lang.Exception e)
			{
				num_not_possible += 1;
				continue;
			}

			num_all += 1;

			if (result1.equals(s_orig)) ++num_pos1;
			if (result2.equals(s_orig)) ++num_pos2;
			if (result3.equals(s_orig)) ++num_pos3;
			if (result4.equals(s_orig)) ++num_pos4;
		}
	}

	public static void main4(String[] args)
	{
		// ChoiceGraph.test();

		final boolean from_file = false;
		// final boolean from_file = true; //!LangProcSettings.DEBUG_OUTPUT;

		try
		{
			LangProc lp = new LangProc();

			WordStatisticsCounter wsc = new WordStatisticsCounter(lp.m_dict);

			// lp.buildStatisticalTextModel("Жив колись змій. Він їв людей і не давав їм проходу. І врятував людей од цього змія один коваль.");
			// wsc.buildStatisticalTextModelForFile("Texts/Zakon/Pro_militsiu.txt",
			// "Texts/zakon_stats.txt");
			// wsc.buildStatisticalTextModelForFile("Texts/Zakon/law2332-14.txt",
			// "Texts/zakon_stats1.txt");
			// wsc.buildStatisticalTextModelForFile("Texts/Zakon/ryd_rights_pelipenko.txt",
			// "Texts/zakon_stats2.txt");
			wsc.buildStatisticalTextModelForFile("Texts/Proza/Fata_morgana_1375700832.txt", "Texts/proza_stats.txt");
			// wsc.buildStatisticalTextModelForFile("Texts/Nauka/inf_syst_i_tekhn_v_stat.txt",
			// "Texts/nauka_stats.txt");
			// wsc.buildStatisticalTextModelForFile("Texts/Proza/Z_Rosii_z_liuboviu.txt",
			// "Texts/new.txt");

			// lp.checkGrammar("Побачив змій коваля та почав тікати.");
			lp.m_morphology.setWordStatisticsCounter(wsc);
			// lp.checkGrammar("Я надійно зберігатиму твої маленькі таємниці.",
			// true);
			// lp.checkGrammar("Скрізь нові будинки.", true);
			LangProcOutput.flush();

			if (from_file)
			{
				LangProcOutput.println("Reading file");
				java.io.InputStream ips = new java.io.FileInputStream("Texts/Proza/Fata_morgana_1375700832.txt");
				// java.io.InputStream ips = new
				// java.io.FileInputStream("Texts/Nauka/inf_syst_i_tekhn_v_stat.txt");

				java.io.InputStreamReader ipsr = new java.io.InputStreamReader(ips, "WINDOWS-1251");
				java.io.BufferedReader reader = new java.io.BufferedReader(ipsr);

				java.io.OutputStream ops = new java.io.FileOutputStream("out.txt");
				java.io.OutputStreamWriter opsr = new java.io.OutputStreamWriter(ops, "WINDOWS-1251");
				LangProcOutput.writer = new java.io.BufferedWriter(opsr);

				StringBuffer full_text = new StringBuffer();
				String line = null;

				int sentence_n = 0;

				while ((line = reader.readLine()) != null && lp.num_all < 2000)
				{
					// LangProcOutput.println("Read line " + line);
					full_text.append(line).append(" ");

					int i = 0;
					while (i < full_text.length())
					{
						char c = full_text.charAt(i);
						if ((int) c == 8217) full_text.setCharAt(i, '\'');
						if ((int) c == '’') full_text.setCharAt(i, '\'');

						if (c == '.' || c == '!' || c == '?' || c == ';')
						{
							String substr = full_text.substring(0, i + 1);
							full_text.delete(0, i + 1);
							i = 0;
							++sentence_n;
							LangProcOutput.println();

							LangProcOutput.println("" + sentence_n + ": " + substr);

							lp.checkSentencePersistence(substr);
							// lp.checkGrammar(substr, false);

						}
						else
						{
							++i;
						}
					}
				}

				lp.printSentencePersistence();

				LangProcOutput.writer.flush();
				ops.close();
			}
			else
			{

				// System.exit(0);

				// lp.checkSentencePersistence("Тетяна знову затулилася долонями і змучено глухо відповіла.");
				// lp.checkSentencePersistence("Скрізь нові будинки.");
				// lp.checkSentencePersistence("Та чого ти крутишся по хаті мов дзига?");
				// lp.checkSentencePersistence("На щастя, надходив Андрій.");

				// lp.checkSentencePersistence("На хвилину залягла тиша і натяглася, наче струна.");
				// lp.checkSentencePersistence("Далекі дзвони гуділи в ясному повітрі тихо й мелодійно, і здавалося, що то дзвенить золото сонця.");

				lp.checkGrammar(
				// "Жив колись змой."
						"У четвертому розділі досліджено мовні моделі з використанням графів."
						// "Жив собі в однім лісі Лис Микита, хитрий-прехитрий."
						// "м'яса"
						// "міг можу може можете могло хотів хочу хоче збирався збиралася намагався намагалась намагатися бажаю провокує зобов'язана зобов'язав"
						// "ніщо нічим нічого"
						// "стільки разів гонили його стрільці."
						// "Дійшло до того, що він у білий день вибирався на полювання й ніколи не вертавсь з порожніми руками."
						// "Скільки разів гонили його стрільці, цькували його хортами, ставили на нього капкани або підкидали йому отруєного м'яса, нічим не могли його доконати."
						// "Лис Микита сміявся собі з них, обминав усякі небезпеки ще й інших своїх товаришів остерігав."
						// "А вже як вибереться на лови — чи то до курника, чи до комори, то не було сміливішого, вигадливішого та спритнішого злодія."
						// " Незвичайне щастя і його хитрість зробили його страшенно гордим."
						// "Йому здавалося, що нема нічого неможливого для нього."

						// "Але на вулиці й на базарі крик, шум, гамір, вози скриплять, колеса гуркотять, коні гримлять копитами, свині кувічуть — одним словом, клекіт такий, якого наш Микита і в сні не бачив, і в гарячці не чув."
						// "Псів уже наш Микита не одурить."
						// "До червоної я йшов скелі."+
						// "Робота зроблена вчасно, але не добре."+
						// "Робота зроблени вчасно, але не добре."+
						// "Робота зроблени вчасно."+
						// "Я йду додому."+
						// "Йдучи додому."+
						// "Робота зроблена."+
						// "Вона знята."+
						// "Роботу зроблено."+
						// "Мені цікаво."+
						// "Зроби мені його машину."+
						// "Його словник."+
						// "Ти бачив його словник, йдучи додому?"+
						// "Який, котрий, котрого, якого, які, якому." +
						// "Я подивилася цікавий фільм." +
						// "Я люблю український борщ." +
						// "Я маю коричневого собаку." +
						// "Маленька дівчинка годує жовтих курчат." +
						// "Я знаю українську мову добре." +
						// "Чоловік купив машину?. " +
						// "Коли ти купив машину?" +
						// "Я ніколи не читав цей текст!" +
						// "Я дивлюсь цікавий фільм." +
						// "Я дивитимусь цікавий фільм." +
						// "Я не читав цей текст." +
						// "Я хочу мати ровер."+
						// "Моя бабуся має зелене пальто." +
						// "Прийменники не мають самостійного лексичного значення, тому членами речення не виступають."+
						// "Належачи до іменників, числівників, займенників, вони входять до складу другорядних членів речення."
						// +
						// "Прийменником називається службова частина мови, яка разом з відмінковими закінченнями іменників (або займенників) служить для вираження підрядних зв’язків між словами в реченні."+
						// ""
						, false);

				LangProcOutput.writer.flush();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main0(String[] args)
	{
		// PCFGParser.main(args);
		return;
	}

	public static void main(String[] args)
	{
		try
		{
			LangProc lp = new LangProc();
			LangProcOutput.flush();

			// final boolean from_file = false;
			final boolean from_file = true; // !LangProcSettings.DEBUG_OUTPUT;

			LangProcOutput.flush();
			
			int num_not_parsed = 0;
			int num_parsed = 0;

			if (from_file)
			{
				LangProcOutput.println("Reading file");
				java.io.InputStream ips = new java.io.FileInputStream("test_sent.txt");
				java.io.InputStreamReader ipsr = new java.io.InputStreamReader(ips, "WINDOWS-1251");
				java.io.BufferedReader reader = new java.io.BufferedReader(ipsr);
				java.io.OutputStream ops = new java.io.FileOutputStream("out.txt");
				java.io.OutputStreamWriter opsr = new java.io.OutputStreamWriter(ops, "WINDOWS-1251");
				LangProcOutput.writer = new java.io.BufferedWriter(opsr);

				StringBuffer full_text = new StringBuffer();
				String line = null;

				int sentence_n = 0;
				while ((line = reader.readLine()) != null && lp.num_all < 2000)
				{
					// LangProcOutput.println("Read line " + line);
					full_text.append(line).append(" ");

					int i = 0;
					while (i < full_text.length())
					{
						char c = full_text.charAt(i);
						if ((int) c == 8217) full_text.setCharAt(i, '\'');
						if ((int) c == '’') full_text.setCharAt(i, '\'');

						if (c == '.' || c == '!' || c == '?' || c == ';')
						{
							String substr = full_text.substring(0, i + 1);
							full_text.delete(0, i + 1);
							i = 0;
							++sentence_n;
							LangProcOutput.println();

							LangProcOutput.println("" + sentence_n + ": " + substr);

							if (null==lp.checkGrammarAPCFG(substr, false))
							{
								++num_not_parsed;
							}
							else
							{
								++num_parsed;
							}
							// lp.checkGrammar(substr, false);

						}
						else
						{
							++i;
						}
					}
				}

				lp.printSentencePersistence();
				
				LangProcOutput.println("Parsed " + num_parsed + " from " + (num_parsed + num_not_parsed) );

				LangProcOutput.writer.flush();
				ops.close();
			}
			else
			{
				lp.checkGrammarAPCFG(
						//"Я вчу дітей математиці."
						//"Я розповідаю дітям казку."
						//"Я розповідаю дитина казку."
						"Я розповідаю столу казку."
				// "У четвертому розділі досліджено мовні моделі з використанням графів."
				// "Жив собі в однім лісі Лис Микита, хитрий-прехитрий."
						//"Хлопець несе рюкзак зі школи."
						// "Жив собі в однім лісі Лис Микита."
						// "м'яса"
						// "міг можу може можете могло хотів хочу хоче збирався збиралася намагався намагалась намагатися бажаю провокує зобов'язана зобов'язав"
						// "ніщо нічим нічого"
						// "стільки разів гонили його стрільці."
						// "Дійшло до того, що він у білий день вибирався на полювання й ніколи не вертавсь з порожніми руками."
						// "Скільки разів гонили його стрільці, цькували його хортами, ставили на нього капкани або підкидали йому отруєного м'яса, нічим не могли його доконати."
						// "Лис Микита сміявся собі з них, обминав усякі небезпеки ще й інших своїх товаришів остерігав."
						// "А вже як вибереться на лови — чи то до курника, чи до комори, то не було сміливішого, вигадливішого та спритнішого злодія."
						// " Незвичайне щастя і його хитрість зробили його страшенно гордим."
						// "Йому здавалося, що нема нічого неможливого для нього."

						// "Але на вулиці й на базарі крик, шум, гамір, вози скриплять, колеса гуркотять, коні гримлять копитами, свині кувічуть — одним словом, клекіт такий, якого наш Микита і в сні не бачив, і в гарячці не чув."
						// "Псів уже наш Микита не одурить."
						// "До червоної я йшов скелі."+
						// "Робота зроблена вчасно, але не добре."+
						// "Робота зроблени вчасно, але не добре."+
						// "Робота зроблени вчасно."+
						// "Я йду додому."+
						// "Йдучи додому."+
						// "Робота зроблена."+
						// "Вона знята."+
						// "Роботу зроблено."+
						// "Мені цікаво."+
						// "Зроби мені його машину."+
						// "Його словник."+
						// "Ти бачив його словник, йдучи додому?"+
						// "Який, котрий, котрого, якого, які, якому." +
						// "Я подивилася цікавий фільм." +
						// "Я люблю український борщ." +
						// "Я маю коричневого собаку." +
						// "Маленька дівчинка годує жовтих курчат." +
						// "Я знаю українську мову добре." +
						// "Чоловік купив машину?. " +
						// "Коли ти купив машину?" +
						// "Я ніколи не читав цей текст!" +
						// "Я дивлюсь цікавий фільм." +
						// "Я дивитимусь цікавий фільм." +
						// "Я не читав цей текст." +
						// "Я хочу мати ровер."+
						// "Моя бабуся має зелене пальто." +
						// "Прийменники не мають самостійного лексичного значення, тому членами речення не виступають."+
						// "Належачи до іменників, числівників, займенників, вони входять до складу другорядних членів речення."
						// +
						// "Прийменником називається службова частина мови, яка разом з відмінковими закінченнями іменників (або займенників) служить для вираження підрядних зв’язків між словами в реченні."+
						// ""
						, false);

			}
			LangProcOutput.writer.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}

}
