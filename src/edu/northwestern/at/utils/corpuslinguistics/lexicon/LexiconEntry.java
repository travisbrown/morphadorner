package edu.northwestern.at.utils.corpuslinguistics.lexicon;

/*	Please see the license information at the end of this file. */

import java.util.*;
import java.io.*;
import java.net.URL;

import edu.northwestern.at.utils.*;

/**	LexiconEntry: A lexicon entry.
 *
 *	<p>
 *	A LexiconEntry contains the following information about a
 *	particular spelling.
 *	</p>
 *
 *	<ul>
 *	</ul>
 */

public class LexiconEntry implements Serializable, XCloneable
{
	/**	The lexicon entry string. */

	public String entry;

	/**	Standardized lexicon entry string. */

	public String standardEntry;

	/**	Map with categories as keys and lemmatized entries as values. */

	public Map<String, String> lemmata;

	/**	The spelling count. */

	public int entryCount;

	/**	Map with categories as keys and counts as values. */

	public Map<String, MutableInteger> categoriesAndCounts;

	/**	Category with largest count. */

	public String largestCategory;

	/**	Count for largest category. */

	public int largestCategoryCount;

	/**	Create a LexiconEntry.
	 */

	public LexiconEntry
	(
		String entry ,
		String standardEntry ,
		int entryCount ,
		Map<String, MutableInteger> categoriesAndCounts ,
		Map<String, String> lemmata
	)
	{
		this.entry					= entry;
		this.standardEntry			= standardEntry;
		this.entryCount				= entryCount;
		this.categoriesAndCounts	= categoriesAndCounts;
		this.lemmata				= lemmata;

		String largestCategory		= "";
		int largestCategoryCount		= 0;

		determineLargestCategory();
	}

	/**	Find the category with the largest count.
	 */

	public void determineLargestCategory()
	{
		for ( String category : categoriesAndCounts.keySet() )
		{
			MutableInteger count	= categoriesAndCounts.get( category );

			if ( count.intValue() > largestCategoryCount )
			{
				largestCategoryCount	= count.intValue();
				largestCategory			= category;
			}
		}
	}

	/**	Add or update entry in categories and counts map.
	 *
	 *	@param	category	Category for which to add/update count.
	 *	@param	count		Category count to add to entry.
	 *							May be negative.
	 */

	public void updateCategoryAndCount( String category , int count )
	{
		MutableInteger currentCount	= categoriesAndCounts.get( category );

		if ( ( currentCount == null ) && ( count > 0 ) )
		{
			categoriesAndCounts.put( category , new MutableInteger( count ) );
		}
		else
		{
			currentCount.setValue( currentCount.intValue() + count );

			if ( currentCount.intValue() <= 0 )
			{
				categoriesAndCounts.remove( category );

				determineLargestCategory();
			}
		}
	}

	/**	Get category count.
	 *
	 *	@param	category	Get number of times category appears
	 *							in this lexicon entry..
	 *
	 *	@return					Category count.
	 */

	public int getCategoryCount( String category )
	{
		int result	= 0;

		if ( categoriesAndCounts.get( category ) != null )
		{
			result	=
				categoriesAndCounts.get( category ).intValue();
		}

		return result;
	}

	/**	Get the categories, sorted in ascending order.
	 *
	 *	@return		The sorted category strings as an array of string.
	 */

	public String[] getCategories()
	{
								//	Get category strings.

		Set<String> categorySet	= categoriesAndCounts.keySet();

								//	Store categories in a String array.

		String[] categories		=
			(String[])categorySet.toArray(
				new String[ categorySet.size() ] );

								//	Sort the categories.

		Arrays.sort( categories );

								//	Return sorted categories.
		return categories;
	}

	/**	Add/update lemma for a category.
	 *
	 *	@param	category	Category for which to add lemma.
	 *	@param	lemma		Lemma.
	 */

	public void updateLemma( String category , String lemma )
	{
		if ( lemma != null ) lemmata.put( category , lemma );
	}

	/**	Get lemma for a category.
	 *
	 *	@param	category	Category for which to add lemma.
	 *
	 *	@return				The lemma.
	 */

	public String getLemma( String category )
	{
		String result	= lemmata.get( category );

		if ( ( result == null ) || ( result.length() == 0 ) )
		{
			result	= "*";
		}

		return result;
	}

	/**	Get String array containing lexicon data suitable for output.
	 *
	 *	@return		String array containing lexicon data items.
	 *
	 *	<p>
	 *	The result String array contains the following entries:
	 *	</p>
	 *
	 *	<p>
	 *	<code>
	 *	result[0]	: entry<br />
	 *	result[1]	: entry count<br />
	 *	result[2]	: first category tag<br />
	 *	result[3]	: first category lemma<br />
	 *	result[4]	: first category count<br />
	 *	result[5]	: second category tag, if any<br />
	 *	result[6]	: second category lemma<br />
	 *	result[7]	: second category count, if any<br />
	 *	...
	 *	</code>
	 *	</p>
	 */

	public String[] getLexiconEntryData()
	{
		String[] result	=
			new String[ 3 * categoriesAndCounts.keySet().size() + 2 ];

		result[ 0 ]	= entry;
		result[ 1 ]	= entryCount + "";
		result[ 2 ]	= largestCategory;
		result[ 3 ]	= getLemma( largestCategory );
		result[ 4 ]	= largestCategoryCount + "";

		int k	= 5;

		for ( String category : categoriesAndCounts.keySet() )
		{
			if ( !category.equals( largestCategory ) )
			{
				MutableInteger count	=
					categoriesAndCounts.get( category );

				result[ k++ ]	= category;
				result[ k++ ]	= getLemma( category );
				result[ k++ ]	= count + "";
			}
		}

		return result;
	}

	/**	Deep clone of categories and counts map.
	 *
	 *	@return			Deep clone of the categories and counts map.
	 */

	protected Map<String, MutableInteger> categoriesAndCountsClone()
	{
		Map<String, MutableInteger> result	=
			MapFactory.createNewMap(
				categoriesAndCounts.size() );

		for ( String category : categoriesAndCounts.keySet() )
		{
			MutableInteger count	= categoriesAndCounts.get( category );

			result.put
			(
				new String( category ) ,
				new MutableInteger( count.intValue() )
			);
		}

		return result;
	}

	/**	Deep clone of lemmata map.
	 *
	 *	@return			Deep clone of the lemmata map.
	 */

	protected Map<String, String> lemmataClone()
	{
		Map<String, String> result	=
			MapFactory.createNewMap( lemmata.size() );

		for ( String key : lemmata.keySet() )
		{
			String data	= lemmata.get( key );

			result.put( new String( key ) , new String( data ) );
		}

		return result;
	}

	/**	Clone this lexicon entry.
	 *
	 *	@return		A deep clone of this lexicon entry.
	 */

	public Object clone()
	{
		return
			new LexiconEntry
			(
				entry ,
				standardEntry ,
				entryCount ,
				categoriesAndCountsClone(),
				lemmataClone()
			);
	}

	/**	Deep clone of this lexicon entry.
	 *
	 *	@return		A deep clone of this lexicon entry.
	 */

	public LexiconEntry deepClone()
	{
		return (LexiconEntry)clone();
	}
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



