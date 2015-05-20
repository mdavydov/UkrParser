package com.langproc;

import java.util.regex.Pattern;

import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;

public abstract class UkrainianMorphologyCommons implements Morphology
{
	final String m_word_pattern = "[АБВГҐДЕЄЖЗІЙИЇКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзійиїклмнопрстуфхцчшщьюяЫЪЭЁыъэё0123456789'’-]+"+
					"|,|\\.|\\?|!|\"|\'|;|:|\\)|\\(|«[^»]*»|\"[^\"]*\""+
					"|[A-Za-z'’-]+";

	java.util.HashSet<String> m_pronoun_S_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_S_C6 = new java.util.HashSet<String>();

	java.util.HashSet<String> m_pronoun_M_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_M_C6 = new java.util.HashSet<String>();

	java.util.HashSet<String> m_pronoun_male = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_female = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_neutral = new java.util.HashSet<String>();

	java.util.HashSet<String> m_pronoun_ADJ_S_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_S_C6 = new java.util.HashSet<String>();

	java.util.HashSet<String> m_pronoun_ADJ_M_C1 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C2 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C3 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C4 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C5 = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_M_C6 = new java.util.HashSet<String>();

	java.util.HashSet<String> m_pronoun_ADJ_male = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_female = new java.util.HashSet<String>();
	java.util.HashSet<String> m_pronoun_ADJ_neutral = new java.util.HashSet<String>();

	// the map from prepositions to possible CASUSES
	java.util.HashMap<String, WordTags> m_prepositions = new java.util.HashMap<String, WordTags>();
	java.util.HashSet<String> m_parenthesis_words = new java.util.HashSet<String>();
	java.util.HashSet<String> m_particles = new java.util.HashSet<String>();
	java.util.HashSet<String> m_negations = new java.util.HashSet<String>();
	java.util.HashSet<String> m_conjunction = new java.util.HashSet<String>();
	java.util.HashSet<String> m_question_adv = new java.util.HashSet<String>();
	java.util.HashSet<String> m_adverbs = new java.util.HashSet<String>();

	// words that lack base form in the dictionary
	java.util.HashMap<String, String> m_special_nouns = new java.util.HashMap<String, String>();
	java.util.HashMap<String, String> m_special_pronouns = new java.util.HashMap<String, String>();
	java.util.HashSet<String> m_indacative_pronous = new java.util.HashSet<String>();
	java.util.HashSet<String> m_question_pronous = new java.util.HashSet<String>();
	java.util.HashMap<String, String> m_special_verbs = new java.util.HashMap<String, String>();
	java.util.HashSet<String> m_modal_verbs = new java.util.HashSet<String>();
	java.util.HashSet<String> m_state_words = new java.util.HashSet<String>();

	java.util.HashMap<String, WordTags> m_countable = new java.util.HashMap<String, WordTags>();
	java.util.HashMap<String, WordTags> m_countable_req_nom = new java.util.HashMap<String, WordTags>();
	
	UkrainianMorphologyCommons()
	{
		m_prepositions.put("перед", new WordTags(WT.CASUS5)); // add
		// "переді мною"
		m_prepositions.put("як", new WordTags(WT.CASUS4));
		m_prepositions.put("між", new WordTags(WT.CASUS5));
		m_prepositions.put("за", new WordTags(WT.CASUS5));
		m_prepositions.put("після", new WordTags(WT.CASUS2));
		m_prepositions.put("над", new WordTags(WT.CASUS5));
		m_prepositions.put("під", new WordTags(WT.CASUS5));
		m_prepositions.put("через", new WordTags(WT.CASUS4));
		m_prepositions.put("поза", new WordTags(WT.CASUS5));
		m_prepositions.put("без", new WordTags(WT.CASUS2));
		m_prepositions.put("на", new WordTags(WT.CASUS4 | WT.CASUS6));
		m_prepositions.put("в", new WordTags(WT.CASUS4 | WT.CASUS6));
		m_prepositions.put("у", new WordTags(WT.CASUS4 | WT.CASUS6));
		m_prepositions.put("від", new WordTags(WT.CASUS2));
		m_prepositions.put("для", new WordTags(WT.CASUS2));
		m_prepositions.put("по", new WordTags(WT.CASUS6));
		m_prepositions.put("через", new WordTags(WT.CASUS4));
		m_prepositions.put("при", new WordTags(WT.CASUS3));
		m_prepositions.put("про", new WordTags(WT.CASUS4));
		m_prepositions.put("над", new WordTags(WT.CASUS5));
		m_prepositions.put("під", new WordTags(WT.CASUS5));
		m_prepositions.put("до", new WordTags(WT.CASUS2));
		m_prepositions.put("з", new WordTags(WT.CASUS2 | WT.CASUS5));
		m_prepositions.put("ради", new WordTags(WT.CASUS2));
		m_prepositions.put("заради", new WordTags(WT.CASUS2));
		m_prepositions.put("задля", new WordTags(WT.CASUS2));
		m_prepositions.put("попри", new WordTags(WT.CASUS4));

		m_prepositions.put("поза", new WordTags(WT.CASUS5));
		m_prepositions.put("щодо", new WordTags(WT.CASUS4));
		m_prepositions.put("поміж", new WordTags(WT.CASUS5));
		m_prepositions.put("близько", new WordTags(WT.CASUS2));
		m_prepositions.put("внаслідок", new WordTags(WT.CASUS2));
		m_prepositions.put("після", new WordTags(WT.CASUS2));
		m_prepositions.put("поруч", new WordTags(WT.CASUS2)); // TODO add
		// "поруч з із у в на під"
		m_prepositions.put("перед", new WordTags(WT.CASUS5));
		m_prepositions.put("протягом", new WordTags(WT.CASUS2));
		m_prepositions.put("під час", new WordTags(WT.CASUS2));
		m_prepositions.put("з допомогою", new WordTags(WT.CASUS2));
		m_prepositions.put("у зв’язку з", new WordTags(WT.CASUS5));
		m_prepositions.put("завдяки", new WordTags(WT.CASUS5));
		m_prepositions.put("незважаючи на", new WordTags(WT.CASUS4));
		m_prepositions.put("з-за", new WordTags(WT.CASUS2));
		m_prepositions.put("з-над", new WordTags(WT.CASUS5));
		m_prepositions.put("з-поза", new WordTags(WT.CASUS2));
		m_prepositions.put("з-під", new WordTags(WT.CASUS2));
		m_prepositions.put("з-попід", new WordTags(WT.CASUS2));
		m_prepositions.put("з-серед", new WordTags(WT.CASUS2));
		m_prepositions.put("із-за", new WordTags(WT.CASUS2));
		m_prepositions.put("в силу", new WordTags(WT.CASUS2));
		m_prepositions.put("згідно з", new WordTags(WT.CASUS5));

		m_parenthesis_words.add("напевно");
		m_parenthesis_words.add("безперечно");
		m_parenthesis_words.add("звичайно");
		m_parenthesis_words.add("може");
		m_parenthesis_words.add("мабуть");
		m_parenthesis_words.add("зрозуміло");
		m_parenthesis_words.add("дійсно");
		m_parenthesis_words.add("певне");
		m_parenthesis_words.add("певно");
		m_parenthesis_words.add("здається");
		m_parenthesis_words.add("боюся");
		m_parenthesis_words.add("сподіваюся");
		m_parenthesis_words.add("очевидно");
		m_parenthesis_words.add("по-перше");
		m_parenthesis_words.add("по-друге");
		m_parenthesis_words.add("далі");
		m_parenthesis_words.add("до речі");
		m_parenthesis_words.add("нарешті");
		m_parenthesis_words.add("на щастя");
		m_parenthesis_words.add("на жаль");
		m_parenthesis_words.add("на нещастя");
		m_parenthesis_words.add("дивна річ");
		m_parenthesis_words.add("взагалі");
		m_parenthesis_words.add("іншими словами");
		m_parenthesis_words.add("можна сказати");
		m_parenthesis_words.add("так би мовити");
		m_parenthesis_words.add("як кажуть");

		fillSet(m_pronoun_S_C1, "котрий котра котре який яка яке той та те цей це ця я ти він вона воно");
		fillSet(m_pronoun_S_C2, "котрого котрої якого того тієї тої цього цієї мене тебе його нього її неї себе");
		fillSet(m_pronoun_S_C3, "котрому котрій якому тому тій цьому цій мені тобі йому їй собі");
		fillSet(m_pronoun_S_C4, "котрого котру котре якого яке яку той ту те цей це цю мене тебе його нього її неї себе");
		fillSet(m_pronoun_S_C5, "котрим котрою яким тим тією тою цим цією мною тобою ним нею собою");
		fillSet(m_pronoun_S_C6, "котрому котрім котрій якому якій тому тім тій цьому цім цій мені тобі ньому ній собі");

		fillSet(m_pronoun_M_C1, "котрі які ті ці ми ви вони");
		fillSet(m_pronoun_M_C2, "котрих яких тих цих нас вас їх них");
		fillSet(m_pronoun_M_C3, "котрому яким тим цим нам вам їм");
		fillSet(m_pronoun_M_C4, "котрих яких ті ці нас вас їх них");
		fillSet(m_pronoun_M_C5, "котрими якими тими цими нами вами ними");
		fillSet(m_pronoun_M_C6, "яких тих цих нас вас них");

		fillSet(m_pronoun_male, "котрий котрого котрому якого який той цей він його йому ним ньому");
		fillSet(m_pronoun_female, "котра котрої котрій яку яка якій та тій ця вона її їй нею ній");
		fillSet(m_pronoun_neutral, "котре котрого котрому яке те це воно його йому ним ньому");

		fillSet(m_pronoun_ADJ_S_C1, "один одна одне мій моя моє твій твоя твоє його її наш наша наше ваш ваша ваше їхній їхня їхнє свій своя своє");
		fillSet(m_pronoun_ADJ_S_C2, "одного однієї мого моєї твого твоєї його її нашого нашої вашого вашої їхнього їхньої свого своєї");
		fillSet(m_pronoun_ADJ_S_C3, "одному однім одній моєму моїй твоєму твоїй його її нашому нашій вашому вашій їхньому їхній своєму своїй");
		fillSet(m_pronoun_ADJ_S_C4, "одного одну мій мою моє твій твою твоє його її наш нашу наше ваш вашу ваше їхній їхню їхнє свій свою своє");
		fillSet(m_pronoun_ADJ_S_C5, "одним однім однією моїм моєю твоїм твоєю його її нашим нашою вашим вашою їхнім їхньою своїм своєю");
		fillSet(m_pronoun_ADJ_S_C6, "одному однім одній моїм моїй твоїм твоїй його її нашім нашій вашім вашїй їхнім їхній своїм своїй");

		fillSet(m_pronoun_ADJ_M_C1, "одні мої твої його її свої наші ваші їхні");
		fillSet(m_pronoun_ADJ_M_C2, "одних моїх твоїх його її своїх наших ваших їхніх");
		fillSet(m_pronoun_ADJ_M_C3, "одним моїм твоїм його її своїм нашим вашим їхнім");
		fillSet(m_pronoun_ADJ_M_C4, "одні мої твої його її свої наші ваші їхні");
		fillSet(m_pronoun_ADJ_M_C5, "одними моїми твоїми його її своїми нашими вашими їхніми");
		fillSet(m_pronoun_ADJ_M_C6, "одних одніх моїх твоїх своїх наших ваших їхніх");

		fillSet(m_pronoun_ADJ_male, "один однім одним одного той цей мій твій наш ваш їхній свій " + "того цього мого твого нашого вашого їхнього свого "
				+ "тому цьому моєму твоєму нашому вашому їхньому своєму " + "той цей мій твій наш ваш їхній свій "
				+ "тим цим моїм твоїм нашим вашим їхнім своїм " + "тому тім цьому цім моїм твоїм нашім вашім їхнім своїм ");

		fillSet(m_pronoun_ADJ_female, "одна однієї одну однією одною та ця моя твоя наша ваша їхня своя "
				+ "тієї тої цією моєї твоєї нашої вашої їхньої своєї " + "тій цій моїй твоїй нашій вашій їхній своїй " + "ту цю мою твою нашу вашу їхню свою "
				+ "тією тою цією моєю твоєю нашою вашою їхньою своєю " + "тій цій моїй твоїй нашій вашїй їхній своїй ");

		fillSet(m_pronoun_ADJ_neutral, "одне одного одним те це моє твоє наше ваше їхнє своє " + "того цього мого твого нашого вашого їхнього свого "
				+ "тому цьому моєму твоєму нашому вашому їхньому своєму " + "те це моє твоє наше ваше їхнє своє "
				+ "тим цим моїм твоїм нашим вашим їхнім своїм " + "тому цьому цім моїм твоїм нашім вашім їхнім своїм ");

		fillSet(m_particles, "ось, осьде, он, от, ото, це, оце");
		fillSet(m_particles, "якраз, ледве, просто, прямо, власне, майже, саме");
		fillSet(m_particles, "ні, ані");
		fillSet(m_particles, "тільки, лише, хоч, хоч би, виключно");
		fillSet(m_particles, "і, й, та, таки, аж, навіть, вже, ж, же, бо");
		fillSet(m_particles, "хай, нехай, бодай, давай");
		fillSet(m_particles, "би, б, ну");
		fillSet(m_particles, "так, отак, еге, авжеж, отож, гаразд");
		fillSet(m_particles, "чи, невже, хіба, та ну, що за");
		fillSet(m_particles, "мов, мовби, немов, наче, неначе, начебто, ніби, нібито");
		fillSet(m_particles, "як, що за тому");

		fillSet(m_negations, "не");

		fillSet(m_conjunction, "і або й та але а проте");

		fillSet(m_question_adv, "коли, чому, скільки, як, навіщо ніколи");

		fillSet(m_adverbs, "додому завтра сьогодні вчора позавчора післязавтра колись");
		fillSet(m_adverbs, "наліво направо назад вперед вниз донизу нагору вгору додолу");

		fillMap(m_special_verbs, "могти міг могла могло могли можу можеш може можемо можете можуть", "могти");
		fillMap(m_special_verbs, "хотіти хотів хотіла хотіло хотіли хочу хочеш хоче хочемо хочете хочуть", "хотіти");

		fillSet(m_modal_verbs, "могти хотіти бажати збирати намагатися пропонувати задумати треба важливо необхідно");
		fillSet(m_modal_verbs, "заохочувати забороняти провокувати мусити зобов'язаний зобов'язати");
		fillSet(m_modal_verbs, "почати закінчити розпочати кінчити");
		fillSet(m_modal_verbs, "подобатися хочеться любити");

		fillSet(m_state_words, "треба важливо необхідно");

		fillMap(m_special_nouns, "ніщо нічого нічому нічим", "ніщо");
		fillMap(m_special_nouns, "щось чогось чомусь чимось", "щось");
		fillMap(m_special_nouns, "сну сном сні сни снів снам снами снах сон", "сон");

		fillMap(m_special_pronouns, "котрий котра котре котрого котрої котрому котрій котру котрим котрою котрім котрій", "котрий");
		fillMap(m_special_pronouns, "який яка яке якого якої якому якій яку яким якою ятякій", "який");
		fillMap(m_special_pronouns, "той та те того тої тому тій ту тим тою тім тій", "той");

		fillMap(m_special_pronouns, "що", "що");

		fillSet(m_indacative_pronous, "цей, оцей, сей, той, стільки, такий, отакий");
		fillSet(m_question_pronous, "що хто скільки який чий котрий де коли");

		fillSet(m_pronoun_M_C1, "котрі які ті ці ми ви вони");
		fillSet(m_pronoun_M_C2, "котрих яких тих цих нас вас їх них");
		fillSet(m_pronoun_M_C3, "котрому яким тим цим нам вам їм");
		fillSet(m_pronoun_M_C4, "котрих яких ті ці нас вас їх них");
		fillSet(m_pronoun_M_C5, "котрими якими тими цими нами вами ними");
		fillSet(m_pronoun_M_C6, "яких тих цих нас вас них");

		fillMapTags(m_countable_req_nom, "два три чотири обидва", WT.PLURAL | WT.CASUS1 | WT.CASUS4);
		fillMapTags(m_countable, "двох трьох чотирьох обидвох", WT.PLURAL | WT.CASUS2 | WT.CASUS6);
		fillMapTags(m_countable, "двом трьом чотирьом обидвом", WT.PLURAL | WT.CASUS3);
		fillMapTags(m_countable, "двома трьома чотирма обидвома", WT.PLURAL | WT.CASUS5);

		fillMapTags(m_countable, "п'ять шість сім вісім дев'ять", WT.PLURAL | WT.CASUS1 | WT.CASUS4);
		fillMapTags(m_countable, "п'яти шести семи восьми дев'яти", WT.PLURAL | WT.CASUS2 | WT.CASUS6);
		fillMapTags(m_countable, "п'ятьом шістьом сімом вісьмом дев'ятьом", WT.PLURAL | WT.CASUS3);
		fillMapTags(m_countable, "п'ятьма шістьма сімома вісьмома дев'ятьма", WT.PLURAL | WT.CASUS5);

		fillMapTags(m_countable, "десять одинядцять дванадцять тринадцять", WT.PLURAL | WT.CASUS1 | WT.CASUS4);
		fillMapTags(m_countable, "чотирнадцять п'ятнадцять шістнадцять сімнадцять", WT.PLURAL | WT.CASUS1 | WT.CASUS4);
		fillMapTags(m_countable, "вісімнадцять дев'ятнадцять двадцять тридцять", WT.PLURAL | WT.CASUS1 | WT.CASUS4);

		fillMapTags(m_countable, "скільки стільки багато мало достатньо недостатньо немало небагато", WT.PLURAL | WT.CASUS1 | WT.CASUS4);
		fillMapTags(m_countable, "скількох стількох багатьох", WT.PLURAL | WT.CASUS2 | WT.CASUS6);
		fillMapTags(m_countable, "скільком стільком багатьом небагатьом", WT.PLURAL | WT.CASUS3);
		fillMapTags(m_countable, "скількома стількома багатьма небагатьма", WT.PLURAL | WT.CASUS5);
	}
	
	@Override
	public Sentence parseSentenceMorphemes(String txt)
	{
		// sentence should be normal case here!!!
		
		CharSequenceWordFinder wf = new CharSequenceWordFinder(Pattern.compile(m_word_pattern).matcher(txt));
		Sentence ss = new Sentence();
		while (wf.hasNext())
		{
			Word w = wf.next();
			// LangProcOutput.print(w.toString() + " ");
			//System.out.println("Next word " + w.toString());
			// LangProcOutput.print(w.toString());
			// int s = w.toString().length();
			// for(int i= 20; i>s; --i) LangProcOutput.print(" ");
			int word_index = ss.numWords();
			WordHypotheses wh = new WordHypotheses(word_index);
			addWordForms(wh, w.toString(), false);
			ss.addWord(wh);

			// LangProcOutput.print(w.toString() + " ");
			// LangProcOutput.println();
		}
		if (LangProcSettings.SENTENCE_OUTPUT)
		{
			LangProcOutput.println("\n");
			ss.print();
			LangProcOutput.print("\n\\hspace{1em}\n\n");
		}
		return ss;
	}
	
	private void addMissingTags(TaggedWord w)
	{
		if ( m_modal_verbs.contains(w.m_base_word) ) w.addTags(WT.MODAL);
		if (m_question_pronous.contains(w.m_base_word))
		{
			w.addTags(WT.PRONOUN | WT.QUESTION);
		}
		// in Ukrainian plural has no gender
		if (w.hasAllTags(WT.PLURAL)) w.addTags(WT.ANY_GENDER);

	}
	
	private static boolean isDactylated(String s)
	{
		if (s.length()<=2) return false;
		
		for(int i=1;i<s.length();i+=2)
		{
			if (s.charAt(i)!='-') return false; 
		}
		
		return true;
	}
	
	private static String dactyToWord(String s)
	{
		if (s.length()<=2) return null;
		
		StringBuffer sb = new StringBuffer(s.length()/2 + 1 );
		
		for(int i=0;i<s.length();i+=2)
		{
			sb.append(s.charAt(i));
		}
		
		return sb.toString();
	}
	

	private void addWordForms(WordHypotheses wh, String word_as_written, boolean try_error_corrections)
	{
		// LangProcOutput.println("addWordForms " + word);
		if (word_as_written.length()==0) return;
		
		int word_index = wh.getSentencePos();
		
		String lower_word = word_as_written.toLowerCase();
		String upper_word = word_as_written.toUpperCase();
		
		if (word_as_written.equals(".") || word_as_written.equals("?") || word_as_written.equals("!") || word_as_written.equals(";"))
		{
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written, new WordTags(WT.SENTENCE_END)) );
		}
		else if ( word_as_written.equals(",") )
		{
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written, new WordTags(WT.COMMA)) );			
		}
		else if ( Character.isDigit( word_as_written.charAt(0) ) )
		{
			// TODO: add better number parser
			int len = word_as_written.length();
			boolean is_1 = word_as_written.equals('1');
			
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
					new WordTags(WT.NUMERAL | WT.ANY_CASUS |
							(is_1 ? WT.SINGLE:WT.PLURAL) | WT.ANY_GENDER)) );
		}
		else if ( isDactylated(word_as_written) )
		{
			int start_i = wh.numHypotheses();
			addWordForms(wh, dactyToWord(word_as_written), try_error_corrections);
			int end_i = wh.numHypotheses();
			
			for(int i=start_i; i< end_i; ++i)
			{
				wh.getHypothesis(i).addTags(WT.DACTYL);
			}
		}
		else if (lower_word.equals(upper_word))
		{
			// sequence without letters. Add as HELPWORD
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
					new WordTags(WT.HELPWORD)) );			
		}
		else if (word_as_written.charAt(0) == '\"' || word_as_written.charAt(0) == '«')
		{
			TaggedWord w = new TaggedWord(word_as_written, word_as_written, "");
			w.addTags(WT.NOUN | WT.PROPERNAME | WT.ANY_GENDER | WT.ANY_COUNT | WT.CASUS1 | WT.CASUS4);
			wh.addHypothesis(w);
		}
		else if (word_as_written.length()>1 &&
			upper_word.equals(word_as_written) &&
			Character.isLetter(word_as_written.charAt(0)) )
		{
			// It should be an Abbreviation. Check dictionary first
			if ( isInDictionary(word_as_written, try_error_corrections) )
			{
				addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);
			}
			else
			{
				// add abbreviation as a new hypothesis
				wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
						new WordTags(WT.NOUN | WT.ANY_GENDER | WT.ANY_CASUS | WT.ANY_COUNT | WT.ABBREVIATED)) );
			}
		}
		else if ( Character.isUpperCase( word_as_written.charAt(0) ) )
		{
			if (word_index==0)
			{
				// this can be regular word or proper name
				// if it is a known proper name, add it
				// if it is a known word (lower case) also add it
				addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);
				addWordFormsFromDictionary(wh, word_as_written, lower_word, try_error_corrections);
				addWordFormsInternal(wh,word_as_written, lower_word);
				
			}
			else
			{
				// words in the middle of sentence are added "as is"  
				addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);
				addWordFormsInternal(wh,word_as_written, word_as_written);
			}
			 // try unknown proper name if no ideas
			if (wh.numHypotheses() == 0)
			{
				wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
						new WordTags(WT.NOUN | WT.ANY_CASUS | WT.ANY_GENDER | WT.ANY_COUNT | WT.PROPERNAME) ) );
			}
		}
		else
		{
			addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);
		}
		
		if (wh.numHypotheses() == 0) // if no ideas, try compound words
		{
			int hyphen_ind = word_as_written.indexOf('-');
			if (hyphen_ind != -1)
			{
				String part1 = word_as_written.substring(0, hyphen_ind);
				String part2 = word_as_written.substring(hyphen_ind + 1);
				if (part2.startsWith("пре") && part1.equals(part2.substring(3))) // зелений-презелений
				{
					addWordFormsFromDictionary(wh, word_as_written, part1, false);
				}
				else	// like жовто-блакитний
				{
					addWordFormsFromDictionary(wh, word_as_written, part2, try_error_corrections);
				}
			}

			// no ideas at all. Add word with void tags
			if (wh.numHypotheses() == 0)
			{
				wh.addHypothesis( new TaggedWord(word_as_written, word_as_written, new WordTags() ) );
			}
		}
		
		for( TaggedWord tw : wh.m_hypotheses) addMissingTags(tw);
	}

	private void addWordFormsInternal(WordHypotheses wh, String word_as_written, String word_to_search)
	{
		// if (LangProcSettings.DEBUG_OUTPUT)
		// {
		// LangProcOutput.println("Add hypo " + index + " " + base_form + " " +
		// s.word + " " + s.astr);
		// }


		TaggedWord w = new TaggedWord( word_as_written, word_as_written, new WordTags(0));
		TaggedWord w1=null;

		if (m_pronoun_S_C1.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS1);
		if (m_pronoun_S_C2.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS2);
		if (m_pronoun_S_C3.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS3);
		if (m_pronoun_S_C4.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS4);
		if (m_pronoun_S_C5.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS5);
		if (m_pronoun_S_C6.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS6);

		if (m_pronoun_M_C1.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS1);
		if (m_pronoun_M_C2.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS2);
		if (m_pronoun_M_C3.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS3);
		if (m_pronoun_M_C4.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS4);
		if (m_pronoun_M_C5.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS5);
		if (m_pronoun_M_C6.contains(word_to_search)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS6);

		if (m_pronoun_male.contains(word_to_search)) w.addTags(WT.MALE);
		if (m_pronoun_female.contains(word_to_search)) w.addTags(WT.FEMALE);
		if (m_pronoun_neutral.contains(word_to_search)) w.addTags(WT.NEUTRAL);

		if (m_special_pronouns.containsKey(word_to_search))
		{
			w.addTags(WT.PRONOUN);
			w.m_base_word = m_special_pronouns.get(word_to_search);
		}

		if (m_indacative_pronous.contains(w.m_base_word))
		{
			w.addTags(WT.PRONOUN | WT.INDICATIVE);
		}

		if (w.hasAllTags(WT.PRONOUN) && !w.hasSomeTags(WT.ANY_GENDER)) w.addTags(WT.ANY_GENDER);

		if (w.hasSomeTags(WT.PRONOUN))
		{
			w1 = w;
			w = new TaggedWord( word_as_written, word_to_search, new WordTags(0));
		}

		if (m_pronoun_ADJ_S_C1.contains(word_to_search)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS1);
		if (m_pronoun_ADJ_S_C2.contains(word_to_search)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS2);
		if (m_pronoun_ADJ_S_C3.contains(word_to_search)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS3);
		if (m_pronoun_ADJ_S_C4.contains(word_to_search)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS4);
		if (m_pronoun_ADJ_S_C5.contains(word_to_search)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS5);
		if (m_pronoun_ADJ_S_C6.contains(word_to_search)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS6);

		if (m_pronoun_ADJ_M_C1.contains(word_to_search)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS1);
		if (m_pronoun_ADJ_M_C2.contains(word_to_search)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS2);
		if (m_pronoun_ADJ_M_C3.contains(word_to_search)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS3);
		if (m_pronoun_ADJ_M_C4.contains(word_to_search)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS4);
		if (m_pronoun_ADJ_M_C5.contains(word_to_search)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS5);
		if (m_pronoun_ADJ_M_C6.contains(word_to_search)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS6);

		if (m_pronoun_ADJ_male.contains(word_to_search)) w.addTags(WT.MALE);
		if (m_pronoun_ADJ_female.contains(word_to_search)) w.addTags(WT.FEMALE);
		if (m_pronoun_ADJ_neutral.contains(word_to_search)) w.addTags(WT.NEUTRAL);

		if (w.hasAllTags(WT.ADJ) && !w.hasSomeTags(WT.ANY_GENDER)) w.addTags(WT.ANY_GENDER);

		if (w.hasSomeTags(WT.ADJ))
		{
			w.addTags(WT.PRONOUN); // it is an adjective pronoun
			if (w1 != null)
			{
				//ApplyRules(w1);
				wh.addHypothesis(w1);
			}
		}
		else if (w1 != null)
		{
			w = w1;
		}

		if (m_prepositions.containsKey(word_to_search)) w.addTags(WT.PREPOSITION);
		if (m_parenthesis_words.contains(word_to_search)) w.addTags(WT.ADV);
		if (m_particles.contains(word_to_search)) w.addTags(WT.PARTICLE);
		if (m_negations.contains(word_to_search)) w.addTags(WT.NEGATION);
		if (m_conjunction.contains(word_to_search)) w.addTags(WT.CONJ);
		if (m_question_adv.contains(word_to_search)) w.addTags(WT.ADV);
		if (m_adverbs.contains(word_to_search)) w.addTags(WT.ADV);

		if (m_special_verbs.containsKey(word_to_search))
		{
			w.addTags(WT.VERB);
			w.m_base_word = m_special_verbs.get(word_to_search);
		}
		if (m_modal_verbs.contains(w.m_base_word)) w.addTags(WT.MODAL);
		if (m_state_words.contains(w.m_base_word)) w.addTags(WT.STATE);

		if (m_special_nouns.containsKey(word_to_search))
		{
			w.addTags(WT.NOUN);
			w.m_base_word = m_special_nouns.get(word_to_search);
		}

		if (m_countable.containsKey(w.m_base_word))
		{
			w.addTags(WT.NUMERAL | m_countable.get(w.m_base_word).m_tags);
		}
		if (m_countable_req_nom.containsKey(w.m_base_word))
		{
			w.addTags(WT.NUMERAL | m_countable_req_nom.get(w.m_base_word).m_tags);
		}

		// if (s.word.equals("Микита")) w.addTags(WT.NOUN);

//		ApplyRules(w);

//		if (w.hasSomeTags(WT.NOUN | WT.ADJ) && starts_uppercase)
//		{
//			w.addTags(WT.PROPERNAME);
//			// if upper-case in the middle of the sentence -> can't be adjective
//			if (wh.getSentencePos() > 0) w.m_tags.removeTags(WT.ADJ);
//		}

		if (w.hasSomeTags(~0))
		{
			wh.addHypothesis(w);
		}

		// LangProcOutput.print("|" + w);
	}
	
	@Override
	public void setWordStatisticsCounter(WordStatisticsCounter wsc) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWordStatisticalWeight(String word, String base_form) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	static void fillSet(java.util.HashSet<String> set, String s)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(s);
		while (wf.hasNext())
		{
			Word w = wf.next();
			set.add(w.toString());
		}
	}

	static void fillMap(java.util.HashMap<String, String> map, String s_from, String s_to)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(s_from);
		while (wf.hasNext())
		{
			Word w = wf.next();
			map.put(w.toString(), s_to);
		}
	}

	static void fillMapTags(java.util.HashMap<String, WordTags> map, String s_from, long wt)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(s_from);
		while (wf.hasNext())
		{
			Word w = wf.next();
			map.put(w.toString(), new WordTags(wt));
		}
	}

}
