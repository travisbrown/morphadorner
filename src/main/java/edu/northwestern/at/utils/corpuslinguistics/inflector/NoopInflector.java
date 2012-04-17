package edu.northwestern.at.utils.corpuslinguistics.inflector;

/*	Please see the license information at the end of this file. */

import edu.northwestern.at.utils.corpuslinguistics.inflector.conjugator.*;
import edu.northwestern.at.utils.corpuslinguistics.inflector.pluralizer.*;

/** Noop inflector which returns a lemma uninflected.
 */

public class NoopInflector implements Inflector
{
	/**	Noop conjugator. */

	protected Conjugator noopConjugator	= new NoopConjugator();

	/**	Noop pluralizer. */

	protected Pluralizer noopPluralizer	= new NoopPluralizer();

	/**	Conjugate a verb from its infinitive, tense, and person.
	 *
	 *	@param	infinitive	The infinitive of the verb to inflect.
	 *	@param	tense		The verb tense to generate.
	 *	@param	person		The person (1st, 2nd, 3rd) to generate.
	 *
	 *	@return				The infinitive unchanged.
	 */

	public String conjugate
	(
		String infinitive ,
		VerbTense tense ,
		Person person
	)
	{
		return noopConjugator.conjugate( infinitive , tense , person );
	}

	/**	Pluralize a noun or pronoun.
	 *
	 *	@param	nounOrPronoun	The singular form of the noun or pronoun.
	 *
	 *	@return					The noun or pronoun unchanged.
	 */

	public String pluralize( String nounOrPronoun )
	{
		return noopPluralizer.pluralize( nounOrPronoun );
	}

	/**	Pluralize a noun or pronoun.
	 *
	 *	@param	nounOrPronoun	The singular form of the noun or pronoun.
	 *	@param	number			The number for the noun or pronoun.
	 *
	 *	@return			The form of the noun or pronoun for the specified
	 *					number.
	 */

	public String pluralize( String nounOrPronoun , int number )
	{
		return noopPluralizer.pluralize( nounOrPronoun , number );
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



