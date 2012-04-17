package edu.northwestern.at.utils.corpuslinguistics.lexicon;

/*	Please see the license information at the end of this file. */

import java.util.*;
import java.io.*;
import java.net.URL;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.guesser.*;
import edu.northwestern.at.utils.corpuslinguistics.spellingstandardizer.*;

/**	Lexicon: stores spellings and their possible lemmata and parts of speech.
 *
 *	<p>
 *	Each line in the main lexicon file takes the following form:
 *	</p>
 *
 *	<blockquote>
 *	<p>
 *	<code>spelling countspelling pos1 lemma1 countpos1 pos2 lemma2 countpos2 ...
 *	</p>
 *	</blockquote>
 *
 *	<p>
 *	where <strong>spelling</strong> is the spelling for a word,
 *	<strong>countspelling</strong> is the number of times the spelling
 *	appeared in the training data, <strong>pos1</strong> is the tag
 *	corresponding to the most commonly occurring part of speech
 *	for this spelling, <strong>lemma1</strong> is the lemma form for
 *	this spelling, <strong>countpos1</strong> is the number
 *	of times the <strong>pos1</strong> tag appeared, and
 *	<strong>pos2</strong>, <strong>countpos2</strong>, etc.
 *	are the other possible parts of speech and their counts and lemmata.
 *	</p>
 *
 *	<p>
 *	The raw counts are stored rather than probabilities so that
 *	new training data can be used to update the lexicon easily,
 *	and so that individual part of speech taggers can apply different
 *	methods of count smoothing.
 *	</p>
 *
 *	<p>
 *	If lemmata are not available, an "*' should appear in the lemma field.
 *	</p>
 */

public interface Lexicon
{
	/**	Load entries into a lexicon.
	 *
	 *	@param	lexiconURL	URL for the file containing the lexicon.
	 *	@param	encoding	Character encoding of lexicon file text.
	 */

	public void loadLexicon( URL lexiconURL , String encoding )
		throws IOException;

	/**	Update entry count in lexicon for a given category.
	 *
	 *	@param	entry		The entry.
	 *	@param	category	The category.
	 *	@param	lemma		The lemma.
	 *	@param	entryCount	The entry count to add to the current count.
	 *							Must be positive.
	 */

	public void updateEntryCount
	(
		String entry ,
		String category ,
		String lemma ,
		int entryCount
	);

	/**	Remove given category for an entry.
	 *
	 *	@param	entry		The entry.
	 *	@param	category	The category to remove
	 */

	public void removeEntryCategory
	(
		String entry ,
		String category
	);

	/**	Remove entry.
	 *
	 *	@param	entry		The entry to remove.
	 */

	public void removeEntry
	(
		String entry
	);

	/**	Get a lexicon entry.
	 *
	 *	@param	entry	Entry for which to get lexicon information.
	 *
	 *	@return			LexiconEntry for entry, or null if not found.
	 *
	 *	<p>
	 *	Note: this does NOT call the part of speech guesser.
	 *	</p>
	 */

	public LexiconEntry getLexiconEntry( String entry );

	/**	Set a lexicon entry.
	 *
	 *	@param	entry		Entry for which to get lexicon information.
	 *	@param	entryData	The lexicon entry data.
	 *
	 *	@return				Previous lexicon data for entry, if any.
	 */

	public LexiconEntry setLexiconEntry
	(
		String entry ,
		LexiconEntry entryData
	);

	/**	Get number of entries in Lexicon.
	 *
	 *	@return		Number of entries in Lexicon.
	 */

	public int getLexiconSize();

	/**	Get the entries, sorted in ascending order.
	 *
	 *	@return		The sorted entry strings as an array of string.
	 */

	public String[] getEntries();

	/**	Get the categories, sorted in ascending order.
	 *
	 *	@return		The sorted category strings as an array of string.
	 */

	public String[] getCategories();

	/**	Checks if lexicon contains an entry.
	 *
	 *	@param	entry	Entry to look up.
	 *
	 *	@return			true if lexicon contains entry.
	 *					Only an exact match is considered.
	 */

	public boolean containsEntry( String entry );

	/**	Get categories for an entry in the lexicon.
	 *
	 *	@param	entry	Entry to look up.
	 *
	 *	@return			Set of categories.
	 *					Null if entry not found in lexicon.
	 */

	public Set<String> getCategoriesForEntry( String entry );

	/**	Get categories for an entry.
	 *
	 *	@param	entry			Entry to look up.
	 *	@param	isFirstEntry	True if entry is first in sentence.
	 *
	 *	@return			Set of categories.
	 *					Null if entry not found in lexicon.
	 */

	public Set<String> getCategoriesForEntry
	(
		String entry ,
		boolean isFirstEntry
	);

	/**	Get categories for an entry in a sentence.
	 *
	 *	@param	sentence	List of entries in sentence.
	 *	@param	entryIndex	Index within sentence (0-based) of entry.
	 *
	 *	@return				Set of categories.
	 *						Null if entry not found in lexicon.
	 */

	public Set<String> getCategoriesForEntry
	(
		List<String> sentence ,
		int entryIndex
	);

	/**	Get number of categories for an entry.
	 *
	 *	@param	entry	Entry for which to find number of categories.
	 *
	 *	@return			Number of categories for entry.
	 */

	public int getNumberOfCategoriesForEntry( String entry );

	/**	Get category counts for an entry.
	 *
	 *	@param	entry	Entry to look up.
	 *
	 *	@return			Map of counts for each category.  String keys are
	 *					tags, Integer counts are values.
	 *
	 *					Null if entry not found in lexicon.
	 */

	public Map<String, MutableInteger> getCategoryCountsForEntry
	(
		String entry
	);

	/**	Get category with largest count for an entry.
	 *
	 *	@param	entry	Entry to look up.
	 *
	 *	@return			Category with largest count.
	 *					Null if entry not found in lexicon.
	 */

	public String getLargestCategory( String entry );

	/**	Get count for an entry in a specific category.
	 *
	 *	@param	entry		Entry to look up.
	 *	@param	category	Category for which to retrieve count.
	 *
	 *	@return				Number of occurrences of entry in category.
	 */

	public int getCategoryCount( String entry , String category );

	/**	Get lemma for an entry.
	 *
	 *	@param	entry		Entry to look up.
	 *
	 *	@return				Lemma form of entry.
	 */

	public String getLemma( String entry );

	/**	Get all lemmata for an entry.
	 *
	 *	@param	entry		Entry to look up.
	 *
	 *	@return				Lemmata forms of entry.
	 */

	public String[] getLemmata( String entry );

	/**	Get lemma for an entry in a specific category.
	 *
	 *	@param	entry		Entry to look up.
	 *	@param	category	Category for which to retrieve lemma.
	 *
	 *	@return				Lemma form of entry.
	 */

	public String getLemma( String entry , String category );

	/**	Get total count for an entry.
	 *
	 *	@param	entry		Entry to look up.
	 *
	 *	@return				Count of occurrences of entry.
	 */

	public int getEntryCount( String entry );

	/**	Get category count.
	 *
	 *	@param	category	Get number of times category appears in lexicon.
     *
	 *	@return				Category count.
	 */

	public int getCategoryCount( String category );

	/**	Get category counts.
	 *
	 *	@return		Category counts map.
	 */

	public Map<String, MutableInteger> getCategoryCounts();

	/**	Get number of categories.
	 *
	 *	@return		Number of categories.
	 */

	public int getNumberOfCategories();

	/**	Save lexicon to a file.
	 *
	 *	@param	lexiconFileName	File containing the lexicon.
	 *	@param	encoding			Character encoding of lexicon file text.
	 */

	public void saveLexiconToTextFile
	(
		String lexiconFileName ,
		String encoding
	)
		throws IOException;

	/**	Get the part of speech tags list used by the lexicon.
	 *
	 *	@return		Part of speech tags list.
	 */

	public PartOfSpeechTags getPartOfSpeechTags();

	/**	Set the part of speech tags list used by the lexicon.
	 *
	 *	@param	partOfSpeechTags	Part of speech tags list.
	 */

	public boolean setPartOfSpeechTags
	(
		PartOfSpeechTags partOfSpeechTags
	);

	/**	Get the longest entry length in the lexicon.
	 *
	 *	@return		The longest entry length in the lexicon.
	 */

	public int getLongestEntryLength();

	/**	Get the shortest entry length in the lexicon.
	 *
	 *	@return		The shortest entry length in the lexicon.
	 */

	public int getShortestEntryLength();
}

/*
Copyright (c) 2008, 2009 by Northwestern University.
All rights reserved.

Developed by:
   Academic and Research Technologies
   Northwestern University
   http://www.it.northwestern.edu/about/departments/at/

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal with the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimers.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimers in the documentation and/or other materials provided
      with the distribution.

    * Neither the names of Academic and Research Technologies,
      Northwestern University, nor the names of its contributors may be
      used to endorse or promote products derived from this Software
      without specific prior written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*/



