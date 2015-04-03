package com.langproc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;

public class UkrainianISpellMorphology extends UkrainianMorphologyCommons
{
	OpenOfficeSpellDictionary m_dict;

	List<TagRule> m_tag_rules = new java.util.LinkedList<TagRule>();

	WordStatisticsCounter m_word_stat_counter = null;

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

	private void addWordHypotheses(WordHypotheses wh, String sentence_form, HEntry dictionary_form)
	{
		// if (LangProcSettings.DEBUG_OUTPUT)
		// {
		// LangProcOutput.println("Add hypo " + index + " " + base_form + " " +
		// s.word + " " + s.astr);
		// }

		boolean starts_uppercase = Character.isUpperCase(sentence_form.charAt(0));

		TaggedWord w = new TaggedWord( sentence_form, dictionary_form.word, dictionary_form.astr);
		TaggedWord w1 = null;
		
		if (sentence_form.equals("діти"))
		{
			w.addTags(WT.NOUN | WT.CASUS1 | WT.PLURAL);
			wh.addHypothesis(w);
			w = new TaggedWord( sentence_form, dictionary_form.word, dictionary_form.astr);
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

		if (w.hasAllTags(WT.PRONOUN) && !w.hasSomeTags(WT.ANY_GENDER)) w.addTags(WT.ANY_GENDER);

		if (w.hasSomeTags(WT.PRONOUN))
		{
			w1 = w;
			w = new TaggedWord( sentence_form, dictionary_form.word, dictionary_form.astr);
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
				wh.addHypothesis(w1);
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
			if (wh.getSentencePos() > 0) w.m_tags.removeTags(WT.ADJ);
		}

		wh.addHypothesis(w);

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
	
	public boolean isInDictionary(String word_to_search, boolean correct_errors)
	{
		List<HEntry> list = m_dict.checkList(word_to_search);
		if ( list !=null && list.size()>0 ) return true;
		
		if (correct_errors)
		{
			@SuppressWarnings("rawtypes")
			List su_list = m_dict.getSuggestions(word_to_search);
			return su_list.size() != 0;
		}
		
		return false;
	}
	public void addWordFormsFromDictionary(WordHypotheses wh, String word_as_written, String word_to_search, boolean correct_errors)
	{
		System.out.println("Search for <" + word_as_written + ">");
		List<HEntry> list = m_dict.checkList(word_to_search);
		if ( list !=null && list.size()>0 )
		{
			java.util.HashSet<String> proc = new java.util.HashSet<String>();
			for (HEntry s : list)
			{
				String def = s.word + "(" + s.astr + ")";
				if (!proc.contains(def))
				{
					addWordHypotheses(wh, word_as_written, s);
					proc.add(def);
				}

			}
			return;
		}
		
		if (correct_errors)
		{
			@SuppressWarnings("rawtypes")
			List su_list = m_dict.getSuggestions(word_as_written);
			if (su_list.size() != 0)
			{
				for (Object o : su_list)
				{
					// LangProcOutput.print(o.toString() + " ");

					List<HEntry> alt_list = m_dict.checkList(o.toString().toLowerCase());
					for (HEntry alt_s : alt_list)
					{
						addWordHypotheses(wh, o.toString().toLowerCase(), alt_s);
					}
				}
			}
		}
		
	}


//	private void addWordForms(Sentence ss, String word)
//	{
//		// LangProcOutput.println("addWordForms " + word);
//		int index = ss.numWords();
//		WordHypotheses sw = new WordHypotheses(index);
//
//		List<HEntry> list = m_dict.checkList(word);
//
//		if (list.size() == 0)
//		{
//			// try upper case if it was the first word and it can't be found
//			list = m_dict.checkList(word.toLowerCase());
//		}
//
//		if (word.charAt(0) == '\"' || word.charAt(0) == '«')
//		{
//			TaggedWord w = new TaggedWord(word, word, "");
//			w.addTags(WT.NOUN | WT.PROPERNAME | WT.ANY_GENDER | WT.ANY_COUNT | WT.CASUS1 | WT.CASUS4);
//			sw.addHypothesis(w);
//		}
//		else if (list.size() == 0)
//		{
//			int hyphen_ind = word.indexOf('-');
//			if (hyphen_ind != -1)
//			{
//				String part1 = word.substring(0, hyphen_ind);
//				String part2 = word.substring(hyphen_ind + 1);
//				if (part2.startsWith("пре") && part1.equals(part2.substring(3)))
//				{
//					list = m_dict.checkList(part1);
//					java.util.HashSet<String> proc = new java.util.HashSet<String>();
//					for (HEntry s : list)
//					{
//						s.word = s.word + "-пре" + s.word;
//						String def = s.word + "(" + s.astr + ")";
//						if (!proc.contains(def))
//						{
//							addWordHypotheses(sw, index, word, s);
//							proc.add(def);
//						}
//					}
//				}
//			}
//
//			if (LangProcSettings.GENERATE_SUGGESTIONS)
//			{
//				List su_list = m_dict.getSuggestions(word);
//
//				if (su_list.size() == 0)
//				{
//					TaggedWord w = new TaggedWord(index, word, word, "");
//					if (word.equals(".") || word.equals("?") || word.equals("!") || word.equals(";"))
//					{
//						w.addTags(WT.SENTENCE_END);
//					}
//					else
//					{
//						w.addTags(WT.COMMA);
//					}
//					sw.addHypothesis(w);
//				}
//				else
//				{
//					for (Object o : su_list)
//					{
//						// LangProcOutput.print(o.toString() + " ");
//
//						List<HEntry> alt_list = m_dict.checkList(o.toString().toLowerCase());
//						for (HEntry alt_s : alt_list)
//						{
//							addWordHypotheses(sw, o.toString().toLowerCase(), alt_s);
//						}
//					}
//					// LangProcOutput.println("");
//				}
//			}
//			else
//			{
//				if (Character.isUpperCase(word.charAt(0)))
//				{
//					TaggedWord w = new TaggedWord(word, word, "");
//					w.addTags(WT.NOUN);
//					sw.addHypothesis(w);
//				}
//				else
//				{
//					TaggedWord w = new TaggedWord(word, word, "");
//					if (word.equals(".") || word.equals("?") || word.equals("!") || word.equals(";"))
//					{
//						w.addTags(WT.SENTENCE_END);
//					}
//					else
//					{
//						w.addTags(WT.COMMA);
//					}
//					sw.addHypothesis(w);
//				}
//			}
//		}
//		else
//		{
//			java.util.HashSet<String> proc = new java.util.HashSet<String>();
//			for (HEntry s : list)
//			{
//				String def = s.word + "(" + s.astr + ")";
//				if (!proc.contains(def))
//				{
//					addWordHypotheses(sw, word, s);
//					proc.add(def);
//				}
//
//			}
//		}
//		ss.addWord(sw);
//	}
	
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
}