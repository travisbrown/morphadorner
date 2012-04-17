package edu.northwestern.at.utils.corpuslinguistics.postagger.trigramhybrid;

/*	Please see the license information at the end of this file. */

import java.util.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.corpuslinguistics.adornedword.*;
import edu.northwestern.at.utils.corpuslinguistics.lexicon.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.hepple.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.smoothing.contextual.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.smoothing.lexical.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.trigram.*;
import edu.northwestern.at.utils.corpuslinguistics.tokenizer.*;
import edu.northwestern.at.utils.math.*;

/**	Hybrid Trigram Part of Speech tagger.
 *
 *	<p>
 *	This trigram part of speech tagger assigns tags to words in a sentence
 *	assigning the most probable set of tags given the previous tag
 *	assignments.   The Viterbi algorithm is used to reduce the
 *	amount of computation required to find the optimal tag assignments.
 *	The Hepple rule-based tagger is used to apply corrections to each
 *	sentence after the trigram tagger has assigned the initial set of
 *	tags.
 *	</p>
 */

public class TrigramHybridTagger
	extends TrigramTagger
	implements PartOfSpeechTagger
{
	/**	Create a trigram hybrid tagger.
	 */

	public TrigramHybridTagger()
	{
		super();
								//	Get a lexical smoother.
		lexicalSmoother	=
			new LexicalSmootherFactory().newLexicalSmoother();

		lexicalSmoother.setPartOfSpeechTagger( this );

								//	Get a contextual smoother.

		contextualSmoother	=
			new ContextualSmootherFactory().newContextualSmoother();

		contextualSmoother.setPartOfSpeechTagger( this );

								//	Get a Hepple tagger as the
								//	fixup tagger.

		retagger			= new HeppleTagger();
	}

	/**	See if tagger uses contextual rules.
	 *
	 *	@return		True since hybrid tagger uses contextual rules.
	 */

	public boolean usesContextRules()
	{
		return true;
	}

	/**	Return tagger description.
	 *
	 *	@return		Tagger description.
	 */

	public String toString()
	{
		return "Trigram hybrid tagger";
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



