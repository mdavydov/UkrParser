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

public class WT {
	public static final long PLURAL = (1L << 0);
	public static final long SINGLE = (1L << 1);
	public static final long COUNT_MASK = PLURAL | SINGLE;
	public static final long ANY_COUNT = PLURAL | SINGLE;

	public static final long PERSON1 = (1L << 2);
	public static final long PERSON2 = (1L << 3);
	public static final long PERSON3 = (1L << 4);
	public static final long PERSONLESS = (1L << 34);
	public static final long PERSON_MASK = PERSON1 | PERSON2 | PERSON3 | PERSONLESS;
	public static final long ANY_PERSON = PERSON1 | PERSON2 | PERSON3 | PERSONLESS;

	public static final long MALE = (1L << 5);
	public static final long FEMALE = (1L << 6);
	public static final long NEUTRAL = (1L << 7);
	public static final long GENDER_MASK = MALE | FEMALE | NEUTRAL;
	public static final long ANY_GENDER = GENDER_MASK;

	public static final long CASUS1 = (1L << 8);
	public static final long CASUS2 = (1L << 9);
	public static final long CASUS3 = (1L << 10);
	public static final long CASUS4 = (1L << 11);
	public static final long CASUS5 = (1L << 12);
	public static final long CASUS6 = (1L << 13);
	public static final long CASUS7 = (1L << 14);

	public static final long CASUS_MASK = CASUS1 | CASUS2 | CASUS3 | CASUS4
	| CASUS5 | CASUS6 | CASUS7;

	public static final long NOUN = (1L << 15);
	public static final long VERB = (1L << 16);
	public static final long ADV = (1L << 17); // adverb
	public static final long ADJ = (1L << 18); // adjective
	public static final long PRONOUN = (1L << 19); // pronoun (I, you, they)
	public static final long NEGATION = (1L << 20);
	public static final long COMMA = (1L << 21);
	public static final long CONJ = (1L << 22); // Conjunction
	public static final long NUMERAL = (1L << 23); // Numeral
	public static final long PARTICLE = (1L << 24);
	public static final long ADVPART = (1L << 25); // Adverbial participle
	public static final long ADJPART = (1L << 26); // Adjective participle
	public static final long PREPOS = (1L << 27); // preposition
	public static final long HELPWORD = (1L << 28); // parenthesis words

	public static final long PART_OF_SPEECH_MASK = NOUN | VERB | ADV | ADJ
	| PRONOUN | NEGATION | COMMA | CONJ | NUMERAL | PARTICLE | ADVPART
	| ADJPART | PREPOS | HELPWORD;

	public static final long PERFECT = (1L << 29);
	public static final long SIMPLE = (1L << 30);
	public static final long PERFECTION_MASK = PERFECT | SIMPLE;

	public static final long PAST = (1L << 31);
	public static final long PRESENT = (1L << 32);
	public static final long FUTURE = (1L << 33);
	public static final long TIME_MASK = PAST | PRESENT | FUTURE;

	public static final long INFINITIVE = (1L << 35);
	public static final long SENTENCE_END = (1L << 36);
	public static final long MODAL = (1L << 37);
	public static final long INDICATIVE = (1L << 38);
	public static final long QUESTION = (1L << 39);
	public static final long STATE = (1L << 40);
	
	public static final long PROPERNAME = (1L << 41);
	

	public static final long ANY_PUNCT = COMMA | SENTENCE_END;
	public static final long ANY_NOUN = NOUN | PRONOUN | NUMERAL;
	public static final long ANY_VERB = VERB | ADVPART | ADJPART;
}
