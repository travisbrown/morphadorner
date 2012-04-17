package edu.northwestern.at.utils.corpuslinguistics.partsofspeech.mapper;

/*	Please see the license information at the end of this file. */

import java.util.*;

/**	PartOfSpeechTagsMapper: Maps a part of speech tag from one tag set to another.
 */

public interface PartOfSpeechTagsMapper
{
	/**	Get destination part of speech tag for a source part of speech tag.
	 *
	 *	@param	spelling		Spelling.
	 *	@param	sourceTag		Source tag name.
	 *
	 *	@return					Destination tag name.
	 *              			Returns unknown tag value if source unrecognized.
	 *
	 *	<p>
	 *	If the source tag contains one or more tag separators,
	 *	the result will contain the respective mapped destination tags
	 *	separated by tag separators.
	 *	</p>
	 *
	 */

	public String getTag( String spelling , String sourceTag );
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



