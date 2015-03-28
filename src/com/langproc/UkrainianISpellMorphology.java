package com.langproc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;

public class UkrainianISpellMorphology implements Morphology
{
	final String m_word_pattern = "[АБВГҐДЕЄЖЗІЙИЇКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзійиїклмнопрстуфхцчшщьюяЫЪЭЁыъэё'’-]+|,|\\.|\\?|!|\"|\'|;|:|\\)|\\(|«[^»]*»|\"[^\"]*\"";

	OpenOfficeSpellDictionary m_dict;
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

	List<TagRule> m_tag_rules = new java.util.LinkedList<TagRule>();

	WordStatisticsCounter m_word_stat_counter = null;
	
	

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

	void ApplyRules(TaggedWord w)
	{
		boolean applyed;
		do
		{
			applyed = false;
			for (TagRule r : m_tag_rules)
			{
				if (r.applyRule(w)) applyed = true;
			}
		} while (applyed);
	}

	static UkrainianISpellMorphology m_singleton;
	
	static UkrainianISpellMorphology singleton() { return m_singleton; }
	
	UkrainianISpellMorphology(OpenOfficeSpellDictionary dict)
	{
		m_singleton = this;
		
		m_dict = dict;
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
		fillSet(m_modal_verbs, "подобатися хочеться");

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

		// m_tag_rules.add(new TagRule(".*оя", ".*оя", ".*", ".*", WT.ADJ,
		// WT.CASUS1 | WT.SINGLE | WT.ANY_GENDER ));
		// m_tag_rules.add(new TagRule(".*оя", ".*оєї", ".*", ".*", WT.ADJ,
		// WT.CASUS2 | WT.SINGLE | WT.FEMALE ));
		// m_tag_rules.add(new TagRule(".*оя", ".*оїй", ".*", ".*", WT.ADJ,
		// WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.FEMALE ));
		// m_tag_rules.add(new TagRule(".*оя", ".*ою", ".*", ".*", WT.ADJ,
		// WT.CASUS4 | WT.SINGLE | WT.FEMALE ));
		// m_tag_rules.add(new TagRule(".*оя", ".*єю", ".*", ".*", WT.ADJ,
		// WT.CASUS5 | WT.SINGLE | WT.FEMALE ));
		//
		// m_tag_rules.add(new TagRule(".*оя", ".*ого", ".*", ".*", WT.ADJ,
		// WT.CASUS2 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));
		// m_tag_rules.add(new TagRule(".*оя", ".*оєму", ".*", ".*", WT.ADJ,
		// WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));
		// m_tag_rules.add(new TagRule(".*оя", ".*ій", ".*", ".*", WT.ADJ,
		// WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL ));

		m_tag_rules.add(new TagRule(".*", "зроблений", ".*", ".*", WT.ADJ, WT.ADJPART));

		m_tag_rules.add(new TagRule(".*[иі]й", ".*", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*[ая]", ".*", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*[еє]", ".*", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*і", ".*", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule(".*ого", ".*", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*ої", ".*ий", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*[иі]х", ".*", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule(".*ому", ".*", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*ій", ".*", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*[иі]м", ".*", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.PLURAL | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule(".*[ую]", ".*", ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.FEMALE));

		m_tag_rules.add(new TagRule(".*[иі]м", ".*", ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*ою", ".*", ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*[иі]ми", ".*", ".*", ".*", WT.ADJ, WT.CASUS5 | WT.PLURAL | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule(".*ім", ".*", ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*ому", ".*", ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*[иі]х", ".*", ".*", ".*", WT.ADJ, WT.CASUS6 | WT.PLURAL | WT.MALE));

		m_tag_rules.add(new TagRule(".*ей", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*я", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*е", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.FEMALE));

		m_tag_rules.add(new TagRule(".*ього", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*ієї", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.SINGLE | WT.FEMALE));

		m_tag_rules.add(new TagRule(".*ьому", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.MALE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*ій", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.SINGLE | WT.FEMALE));

		m_tag_rules.add(new TagRule(".*ю", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS4 | WT.SINGLE | WT.NEUTRAL));

		m_tag_rules.add(new TagRule(".*ими", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS5 | WT.SINGLE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*их", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS6 | WT.SINGLE | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule(".*их", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*им", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS3 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*і", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS4 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*ими", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS5 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*их", ".*ей", ".*", ".*", WT.ADJ, WT.CASUS6 | WT.PLURAL | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule("я|мо.*|мн.*", ".*", ".*", "", WT.PRONOUN, WT.PERSON1));
		m_tag_rules.add(new TagRule("т.*", ".*", ".*", "", WT.PRONOUN, WT.PERSON2));
		m_tag_rules.add(new TagRule("[вй].*", ".*", ".*", "", WT.PRONOUN | WT.SINGLE, WT.PERSON3));

		m_tag_rules.add(new TagRule("ми|н.*", ".*", ".*", "", WT.PRONOUN, WT.PERSON1));
		m_tag_rules.add(new TagRule("ви|ва.*", ".*", ".*", "", WT.PRONOUN | WT.PLURAL, WT.PERSON2));
		m_tag_rules.add(new TagRule("ї.*|во.*", ".*", ".*", "", WT.PRONOUN | WT.PLURAL, WT.PERSON3));

		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[ая]", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "а", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[ую]", WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[ое]м", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[уі]", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
		
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[иі]", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "ів", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[ая]м", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[ая]ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "e.*", "[ая]х", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
		
//			m_tag_rules.add(new TagRule(".*", ".*", "ehp?", "", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
//			m_tag_rules.add(new TagRule(".*", ".*", "ehp?", "[ая]", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
//			m_tag_rules.add(new TagRule(".*", ".*", "ehp?", "[ую]", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
//			m_tag_rules.add(new TagRule(".*", ".*", "ehp?", "[ое]м", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
//			m_tag_rules.add(new TagRule(".*", ".*", "ehp?", "[уі]", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
//
//			m_tag_rules.add(new TagRule(".*", ".*", "ef.*", "и", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.MALE | WT.PERSON3));
//			m_tag_rules.add(new TagRule(".*", ".*", "ef.*", "ів", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
//			m_tag_rules.add(new TagRule(".*", ".*", "ef.*", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
//			m_tag_rules.add(new TagRule(".*", ".*", "ef.*", "и", WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.MALE));
//			m_tag_rules.add(new TagRule(".*", ".*", "ef.*", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
//			m_tag_rules.add(new TagRule(".*", ".*", "ef.*", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));

		m_tag_rules.add(new TagRule(".*", ".*", "b", "[іи]?", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.ANY_GENDER | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "b", "ей", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", "b", "і?в", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", "b", "ь", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", "b", "[яа]м", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", "b", "[ьая]?ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", "b", "[ая]х", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.MALE | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "и", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "ці", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.MALE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "у", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "ою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "и", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.MALE | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "ок|", WT.NOUN, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.MALE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "abd?", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE | WT.FEMALE));

		m_tag_rules.add(new TagRule(".*", ".*", "abc", "", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*[ая]", ".*", "abc", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*[ую]", ".*", "abc", ".*", WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*я", ".*", "abc", ".*", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*[ое]м", ".*", "abc", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*і", ".*", "abc", ".*", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));

		m_tag_rules.add(new TagRule(".*(ці|и)", ".*", "abc", ".*", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*ів", ".*", "abc", ".*", WT.NOUN, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*[ая]м", ".*", "abc", ".*", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*ми", ".*", "abc", ".*", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*[ая]х", ".*", "abc", ".*", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));

		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "и", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "ц?і", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "у", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "ою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "и", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "ер|ей|", WT.NOUN, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[od]+", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.FEMALE));

		m_tag_rules.add(new TagRule(".*", ".*", "adp", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "и", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "і", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "у", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "ою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));

		m_tag_rules.add(new TagRule(".*", ".*", "adp", "и", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*[^а]", ".*", "adp", "", WT.NOUN, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "ів", WT.NOUN, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "adp", "ах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));

		m_tag_rules.add(new TagRule(".*", ".*", "ab", "", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?у", WT.NOUN, WT.CASUS2 | WT.CASUS6 | WT.CASUS3 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?ові", WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?ом", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
		
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?и", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?ів", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "ок", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "оків", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?ам", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?ами", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab", "(ьор)?(к)?ках", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));

		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "", 	 	  WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[ая]",  WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[ую]",  WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[ую]",  WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[ео]м", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[ії]",  WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
		
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[іи]", 	WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?ей", 	WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[ая]м", 	WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));	
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?ей", 		WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?ь?ми",	WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "ab.*", "(он)?[ая]х", 	WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
		
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "", 	 	  WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "і",  		  WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "і",  WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "[ео][ую]",  WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "[ая]м", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "[і]",  WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.FEMALE));
		
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "[іи]", 	WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*[^a]", ".*а", "ab.*", "", 	WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "[ая]м", 	WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.FEMALE));	
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "і", 		WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "ми",	WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*а", "ab.*", "я?х", 	WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.FEMALE));
		
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "", 		 WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[їіи]",  WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[їіи]",  WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[юу]",   WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[єео]?ю",WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[ії]", 	 WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.FEMALE));
		
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[іи]", 	 WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.FEMALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[ьй]|іт", 	 WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[ая]?м", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.FEMALE));	
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "", 		 WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[ая]?ми",WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", "a[^b]*", "[ая]?х", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.FEMALE));




		m_tag_rules.add(new TagRule(".*", ".*", "lm", "", WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "т", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "ті", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "", WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "тою", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.NEUTRAL | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "т", WT.NOUN, WT.CASUS2 | WT.CASUS4 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "там", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "lm", "тах", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.NEUTRAL));

		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "", 		WT.NOUN, WT.CASUS1 | WT.SINGLE | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "я", 	WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "ю|еві",	WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "я",		WT.NOUN, WT.CASUS4 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "ем", 	WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "і|еві", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));
		
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "і",		WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "ів", 	WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "ям", 	WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "ів", 	WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "ями", 	WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", "lmq", "ях", 	WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));
		
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
		m_tag_rules.add(new TagRule(".*[аяі]", ".*", "i.*", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "[юу]", WT.NOUN, WT.CASUS3 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "м", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "ами", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "і", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.NEUTRAL));

		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "і", WT.NOUN, WT.CASUS1 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "ей", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "ям", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "і", WT.NOUN, WT.CASUS4 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "ями", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "i.*", "ях", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.NEUTRAL));

		m_tag_rules
				.add(new TagRule("що", ".*", ".*", ".*", WT.PRONOUN, WT.CASUS1 | WT.CASUS4 | WT.CASUS6 | WT.SINGLE | WT.PLURAL | WT.ANY_GENDER | WT.PERSON3));

		m_tag_rules.add(new TagRule("ніщо", ".*", ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
		m_tag_rules.add(new TagRule("нічого", ".*", ".*", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule("нічому", ".*", ".*", ".*", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule("нічим", ".*", ".*", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));

		m_tag_rules.add(new TagRule("щось", ".*", ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.NEUTRAL | WT.PERSON3));
		m_tag_rules.add(new TagRule("чогось", ".*", ".*", ".*", WT.NOUN, WT.CASUS2 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule("чомусь", ".*", ".*", ".*", WT.NOUN, WT.CASUS3 | WT.CASUS6 | WT.SINGLE | WT.NEUTRAL));
		m_tag_rules.add(new TagRule("чимось", ".*", ".*", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.NEUTRAL));

		m_tag_rules.add(new TagRule("сон", ".*", ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.SINGLE | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule("сну", ".*", ".*", ".*", WT.NOUN, WT.CASUS2 | WT.CASUS3 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule("сном", ".*", ".*", ".*", WT.NOUN, WT.CASUS5 | WT.SINGLE | WT.MALE));
		m_tag_rules.add(new TagRule("сні", ".*", ".*", ".*", WT.NOUN, WT.CASUS6 | WT.SINGLE | WT.MALE));

		m_tag_rules.add(new TagRule("сни", ".*", ".*", ".*", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.MALE | WT.PERSON3));
		m_tag_rules.add(new TagRule("снів", ".*", ".*", ".*", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule("снам", ".*", ".*", ".*", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule("снами", ".*", ".*", ".*", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.MALE));
		m_tag_rules.add(new TagRule("снах", ".*", ".*", ".*", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.MALE));

		// ТОDO - fork to several hypotheses
		m_tag_rules.add(new TagRule(".*", ".*", "ij", "а", WT.NOUN, WT.CASUS1 | WT.CASUS4 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule("о", ".*т", "ij", "", WT.NOUN, WT.CASUS2 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "ij", "м", WT.NOUN, WT.CASUS3 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "ij", "ми", WT.NOUN, WT.CASUS5 | WT.PLURAL | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", "ij", "х", WT.NOUN, WT.CASUS6 | WT.PLURAL | WT.NEUTRAL));

		/* VERBS */

		m_tag_rules.add(new TagRule(".+ти", ".*", ".*", "", WT.VERB, WT.INFINITIVE));
		m_tag_rules.add(new TagRule(".+тися", ".*", ".*", ".*", WT.VERB, WT.INFINITIVE));
		m_tag_rules.add(new TagRule(".+тись", ".*", ".*", ".*", WT.VERB, WT.INFINITIVE));

		m_tag_rules.add(new TagRule(".+[^т]и", ".*", ".*", "и?", WT.VERB, WT.IMPERATIVE));
		m_tag_rules.add(new TagRule(".+р", ".*", ".*", "", WT.VERB, WT.IMPERATIVE));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "дь", WT.VERB, WT.IMPERATIVE));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "ж?іть", WT.VERB, WT.IMPERATIVE));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "те", WT.VERB, WT.IMPERATIVE));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "ри", WT.VERB, WT.IMPERATIVE));

		m_tag_rules.add(new TagRule("міг", ".*", ".*", "", WT.VERB, WT.ANY_PERSON | WT.SINGLE | WT.PAST | WT.MALE));

		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[лчжвд']?[ую](с[ья])?", WT.VERB, WT.PERSON1 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[лчжвд']?[єе]ш(с[ья])?", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "ї?ш(с[ья])?", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "е?[лчжврд']?[єе](тьс[ья])?", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "(ит)?ь(с[ья])?", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[лчжвд']?[єеи]?мо(с[ья])?", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[лчжвд']?[єеиї]те(с[ья])?", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "е(с[ья])?", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[лчжвд']?[уюя]ть(с[ья])?", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.PRESENT | WT.ANY_GENDER));

		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[шо]*в(с[ья])?", WT.VERB, WT.ANY_PERSON | WT.SINGLE | WT.PAST | WT.MALE));
		m_tag_rules.add(new TagRule(".*із", ".*", ".*", ".*(с[ья])?", WT.VERB, WT.ANY_PERSON | WT.SINGLE | WT.PAST | WT.MALE));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[ш]?ла(с[ья])?", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PAST | WT.FEMALE));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[ш]?ло(с[ья])?", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.PAST | WT.NEUTRAL));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "[ш]?ли(с[ья])?", WT.VERB, WT.ANY_PERSON | WT.PLURAL | WT.PAST));

		m_tag_rules.add(new TagRule(".*", ".*", ".*", "му(с[ья])?", WT.VERB, WT.PERSON1 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "меш(с[ья])?", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "ме(тьс[ья])?", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule("о.*", ".*", ".*", "ь", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule("о.*", ".*", ".*", "де", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "емо(с[ья])?", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "мете(с[ья])?", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", ".*", ".*", "муть(с[ья])?", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.FUTURE | WT.ANY_GENDER));


		m_tag_rules.add(new TagRule(".*", "бути", ".*", "ду", WT.VERB, WT.PERSON1 | WT.SINGLE | WT.FUTURE));
		m_tag_rules.add(new TagRule(".*", "бути", ".*", "деш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.FUTURE));
		m_tag_rules.add(new TagRule(".*", "бути", ".*", "де", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.FUTURE));
		m_tag_rules.add(new TagRule(".*", "бути", ".*", "демо", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.FUTURE));
		m_tag_rules.add(new TagRule(".*", "бути", ".*", "дете", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.FUTURE));
		m_tag_rules.add(new TagRule(".*", "бути", ".*", "дуть", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.FUTURE));

		m_tag_rules.add(new TagRule(".*", "йти", ".*", "ду", WT.VERB, WT.PERSON1 | WT.SINGLE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", "йти", ".*", "деш", WT.VERB, WT.PERSON2 | WT.SINGLE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", "йти", ".*", "де", WT.VERB, WT.PERSON3 | WT.SINGLE | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", "йти", ".*", "демо", WT.VERB, WT.PERSON1 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", "йти", ".*", "дете", WT.VERB, WT.PERSON2 | WT.PLURAL | WT.ANY_GENDER));
		m_tag_rules.add(new TagRule(".*", "йти", ".*", "дуть", WT.VERB, WT.PERSON3 | WT.PLURAL | WT.ANY_GENDER));
		
		m_tag_rules.add(new TagRule(".*", ".*", ".*MODAL.*", ".*", WT.VERB, WT.MODAL));

		m_tag_rules.add(new TagRule(".*", ".*", ".*[abcdefghijklmnoqp].*", ".*", 0, WT.NOUN));
		m_tag_rules.add(new TagRule(".*", ".*", ".*[ABCDEFGH].*", ".*", 0, WT.VERB));
		//m_tag_rules.add(new TagRule(".*", ".+ти", ".*", ".*", 0, WT.VERB));
		m_tag_rules.add(new TagRule(".*ся", ".*", ".*", ".*", WT.VERB, WT.REVERSE));
		m_tag_rules.add(new TagRule(".*сь", ".*", ".*", ".*", WT.VERB, WT.REVERSE));
		
		m_tag_rules.add(new TagRule(".*", ".*ий", ".*", ".*", 0, WT.ADJ));
		m_tag_rules.add(new TagRule(".*", ".*ий", ".*", "о", 0, WT.ADV));
		m_tag_rules.add(new TagRule(".*", ".*оя", ".*", ".*", 0, WT.ADJ));
		m_tag_rules.add(new TagRule(".*", ".*ко|.*но", ".*", ".*", 0, WT.ADV));
		m_tag_rules.add(new TagRule(".*", ".*но", ".*", ".*", 0, WT.VERB | WT.PERSONLESS | WT.ANY_COUNT));
		m_tag_rules.add(new TagRule("нема", ".*", ".*", ".*", 0, WT.VERB | WT.PERSONLESS | WT.ANY_COUNT));
		m_tag_rules.add(new TagRule("треба", ".*", ".*", ".*", 0, WT.VERB | WT.PERSONLESS | WT.ANY_COUNT | WT.MODAL));
		m_tag_rules.add(new TagRule("є", ".*", ".*", ".*", 0, WT.VERB | WT.PERSONLESS | WT.ANY_PERSON | WT.ANY_COUNT));
		m_tag_rules.add(new TagRule("добре", ".*", ".*", ".*", 0, WT.ADV));
		m_tag_rules.add(new TagRule(".*", ".*чи", ".*", ".*", 0, WT.ADVPART));

	}

	Matcher createMatcher(CharSequence text, String regexp, int flags)
	{
		return Pattern.compile(regexp, flags).matcher(text);
	}

	private void addWordHypotheses(SentenceWord sw, int index, String sentence_form, HEntry dictionary_form)
	{
		// if (LangProcSettings.DEBUG_OUTPUT)
		// {
		// LangProcOutput.println("Add hypo " + index + " " + base_form + " " +
		// s.word + " " + s.astr);
		// }

		boolean starts_uppercase = Character.isUpperCase(sentence_form.charAt(0));

		TaggedWord w = new TaggedWord(index, sentence_form, dictionary_form.word, dictionary_form.astr);
		TaggedWord w1 = null;
		
		if (sentence_form.equals("діти"))
		{
			w.addTags(WT.NOUN | WT.CASUS1 | WT.PLURAL);
			sw.addHypothesis(w);
			w = new TaggedWord(index, sentence_form, dictionary_form.word, dictionary_form.astr);
		}

		// if (word.equals(word.toUpperCase())) w.addTag("Cap");
		// if (Character.isUpperCase(word.charAt(0))) w.addTag("StartCap");
		// if (word.equals(word.toLowerCase())) w.addTag("Low");
		// if (word.toLowerCase().equals(s.word)) w.addTag("Base");

		// LangProcOutput.print( "   " + s.word + " " + s.astr + " " );

		if (m_pronoun_S_C1.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS1);
		if (m_pronoun_S_C2.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS2);
		if (m_pronoun_S_C3.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS3);
		if (m_pronoun_S_C4.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS4);
		if (m_pronoun_S_C5.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS5);
		if (m_pronoun_S_C6.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.SINGLE | WT.CASUS6);

		if (m_pronoun_M_C1.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS1);
		if (m_pronoun_M_C2.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS2);
		if (m_pronoun_M_C3.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS3);
		if (m_pronoun_M_C4.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS4);
		if (m_pronoun_M_C5.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS5);
		if (m_pronoun_M_C6.contains(dictionary_form.word)) w.addTags(WT.PRONOUN | WT.PLURAL | WT.CASUS6);

		if (m_pronoun_male.contains(dictionary_form.word)) w.addTags(WT.MALE);
		if (m_pronoun_female.contains(dictionary_form.word)) w.addTags(WT.FEMALE);
		if (m_pronoun_neutral.contains(dictionary_form.word)) w.addTags(WT.NEUTRAL);

		if (m_special_pronouns.containsKey(dictionary_form.word))
		{
			w.addTags(WT.PRONOUN);
			w.m_base_word = m_special_pronouns.get(dictionary_form.word);
		}

		if (m_indacative_pronous.contains(w.m_base_word))
		{
			w.addTags(WT.PRONOUN | WT.INDICATIVE);
		}
		if (m_question_pronous.contains(w.m_base_word))
		{
			w.addTags(WT.PRONOUN | WT.QUESTION);
		}

		if (w.hasAllTags(WT.PRONOUN) && !w.hasSomeTags(WT.ANY_GENDER)) w.addTags(WT.ANY_GENDER);

		if (w.hasSomeTags(WT.PRONOUN))
		{
			w1 = w;
			w = new TaggedWord(index, sentence_form, dictionary_form.word, dictionary_form.astr);
		}

		if (m_pronoun_ADJ_S_C1.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS1);
		if (m_pronoun_ADJ_S_C2.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS2);
		if (m_pronoun_ADJ_S_C3.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS3);
		if (m_pronoun_ADJ_S_C4.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS4);
		if (m_pronoun_ADJ_S_C5.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS5);
		if (m_pronoun_ADJ_S_C6.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.SINGLE | WT.CASUS6);

		if (m_pronoun_ADJ_M_C1.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS1);
		if (m_pronoun_ADJ_M_C2.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS2);
		if (m_pronoun_ADJ_M_C3.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS3);
		if (m_pronoun_ADJ_M_C4.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS4);
		if (m_pronoun_ADJ_M_C5.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS5);
		if (m_pronoun_ADJ_M_C6.contains(dictionary_form.word)) w.addTags(WT.ADJ | WT.PLURAL | WT.CASUS6);

		if (m_pronoun_ADJ_male.contains(dictionary_form.word)) w.addTags(WT.MALE);
		if (m_pronoun_ADJ_female.contains(dictionary_form.word)) w.addTags(WT.FEMALE);
		if (m_pronoun_ADJ_neutral.contains(dictionary_form.word)) w.addTags(WT.NEUTRAL);

		if (w.hasAllTags(WT.ADJ) && !w.hasSomeTags(WT.ANY_GENDER)) w.addTags(WT.ANY_GENDER);

		if (w.hasSomeTags(WT.ADJ))
		{
			if (w1 != null)
			{
				ApplyRules(w1);
				sw.addHypothesis(w1);
			}
		}
		else if (w1 != null)
		{
			w = w1;
		}

		if (m_prepositions.containsKey(dictionary_form.word)) w.addTags(WT.PREPOSITION);
		if (m_parenthesis_words.contains(dictionary_form.word)) w.addTags(WT.ADV);
		if (m_particles.contains(dictionary_form.word)) w.addTags(WT.PARTICLE);
		if (m_negations.contains(dictionary_form.word)) w.addTags(WT.NEGATION);
		if (m_conjunction.contains(dictionary_form.word)) w.addTags(WT.CONJ);
		if (m_question_adv.contains(dictionary_form.word)) w.addTags(WT.ADV);
		if (m_adverbs.contains(dictionary_form.word)) w.addTags(WT.ADV);
		if (dictionary_form.astr.indexOf('V') != -1) w.addTags(WT.ADJ);

		if (m_special_verbs.containsKey(dictionary_form.word))
		{
			w.addTags(WT.VERB);
			w.m_base_word = m_special_verbs.get(dictionary_form.word);
		}
		if (m_modal_verbs.contains(w.m_base_word)) w.addTags(WT.MODAL);
		if (m_state_words.contains(w.m_base_word)) w.addTags(WT.STATE);

		if (m_special_nouns.containsKey(dictionary_form.word))
		{
			w.addTags(WT.NOUN);
			w.m_base_word = m_special_nouns.get(dictionary_form.word);
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

		ApplyRules(w);

		if (w.hasSomeTags(WT.NOUN | WT.ADJ) && starts_uppercase)
		{
			w.addTags(WT.PROPERNAME);
			// if upper-case in the middle of the sentence -> can't be adjective
			if (index > 0) w.m_tags.removeTags(WT.ADJ);
		}

		sw.addHypothesis(w);

		// LangProcOutput.print("|" + w);
	}

	public int getWordStatisticalWeight(String word, String base_form)
	{
		if (m_word_stat_counter == null) return 0;
		return m_word_stat_counter.getWordStatisticalWeight(word, base_form);
	}
	
	public void setWordStatisticsCounter(WordStatisticsCounter wsc)
	{
		m_word_stat_counter = wsc;
	}

	private void addWordForms(Sentence ss, String word)
	{
		// LangProcOutput.println("addWordForms " + word);
		int index = ss.numWords();
		SentenceWord sw = new SentenceWord(index);

		List<HEntry> list = m_dict.checkList(word);

		if (list.size() == 0)
		{
			// try upper case if it was the first word and it can't be found
			list = m_dict.checkList(word.toLowerCase());
		}

		if (word.charAt(0) == '\"' || word.charAt(0) == '«')
		{
			TaggedWord w = new TaggedWord(index, word, word, "");
			w.addTags(WT.NOUN | WT.PROPERNAME | WT.ANY_GENDER | WT.ANY_COUNT | WT.CASUS1 | WT.CASUS4);
			sw.addHypothesis(w);
		}
		else if (list.size() == 0)
		{
			int hyphen_ind = word.indexOf('-');
			if (hyphen_ind != -1)
			{
				String part1 = word.substring(0, hyphen_ind);
				String part2 = word.substring(hyphen_ind + 1);
				if (part2.startsWith("пре") && part1.equals(part2.substring(3)))
				{
					list = m_dict.checkList(part1);
					java.util.HashSet<String> proc = new java.util.HashSet<String>();
					for (HEntry s : list)
					{
						s.word = s.word + "-пре" + s.word;
						String def = s.word + "(" + s.astr + ")";
						if (!proc.contains(def))
						{
							addWordHypotheses(sw, index, word, s);
							proc.add(def);
						}
					}
				}

			}

			if (LangProcSettings.GENERATE_SUGGESTIONS)
			{
				List su_list = m_dict.getSuggestions(word);

				if (su_list.size() == 0)
				{
					TaggedWord w = new TaggedWord(index, word, word, "");
					if (word.equals(".") || word.equals("?") || word.equals("!") || word.equals(";"))
					{
						w.addTags(WT.SENTENCE_END);
					}
					else
					{
						w.addTags(WT.COMMA);
					}
					sw.addHypothesis(w);
				}
				else
				{
					for (Object o : su_list)
					{
						// LangProcOutput.print(o.toString() + " ");

						List<HEntry> alt_list = m_dict.checkList(o.toString().toLowerCase());
						for (HEntry alt_s : alt_list)
						{
							addWordHypotheses(sw, index, o.toString().toLowerCase(), alt_s);
						}
					}
					// LangProcOutput.println("");
				}
			}
			else
			{
				if (Character.isUpperCase(word.charAt(0)))
				{
					TaggedWord w = new TaggedWord(index, word, word, "");
					w.addTags(WT.NOUN);
					sw.addHypothesis(w);
				}
				else
				{
					TaggedWord w = new TaggedWord(index, word, word, "");
					if (word.equals(".") || word.equals("?") || word.equals("!") || word.equals(";"))
					{
						w.addTags(WT.SENTENCE_END);
					}
					else
					{
						w.addTags(WT.COMMA);
					}
					sw.addHypothesis(w);
				}
			}
		}
		else
		{
			java.util.HashSet<String> proc = new java.util.HashSet<String>();
			for (HEntry s : list)
			{
				String def = s.word + "(" + s.astr + ")";
				if (!proc.contains(def))
				{
					addWordHypotheses(sw, index, word, s);
					proc.add(def);
				}

			}
		}
		ss.addWord(sw);
	}
	
	private void test(SpellChecker checker, String txt)
	{
		Word badWord = checker.checkSpell(txt);

		if (badWord == null) LangProcOutput.println("All OK!!!");
		else
		{
			LangProcOutput.println("Bad words: " + badWord);
			List<String> list = checker.getDictionary().getSuggestions(badWord);
			List<String> wl = list;
			for (String s : wl)
			{
				LangProcOutput.println(s);
			}
		}
	}

	public Sentence parseSentenceMorphemes(String txt)
	{
		CharSequenceWordFinder wf = new CharSequenceWordFinder(Pattern.compile(m_word_pattern).matcher(txt));
		Sentence ss = new Sentence();
		// SentenceProcessor sp = new SentenceProcessor();
		while (wf.hasNext())
		{
			Word w = wf.next();
			// LangProcOutput.print(w.toString() + " ");
			//System.out.println("Next word " + w.toString());
			// LangProcOutput.print(w.toString());
			// int s = w.toString().length();
			// for(int i= 20; i>s; --i) LangProcOutput.print(" ");
			addWordForms(ss, w.toString());
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

}