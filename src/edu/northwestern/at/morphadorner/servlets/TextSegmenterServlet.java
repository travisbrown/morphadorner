package edu.northwestern.at.morphadorner.servlets;

/*	Please see the license information at the end of this file. */

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.ServletConfig;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.html.*;
import edu.northwestern.at.utils.corpuslinguistics.lexicon.*;
import edu.northwestern.at.utils.corpuslinguistics.postagger.guesser.*;
import edu.northwestern.at.utils.corpuslinguistics.sentencemelder.*;
import edu.northwestern.at.utils.corpuslinguistics.sentencesplitter.*;
import edu.northwestern.at.utils.corpuslinguistics.textsegmenter.*;
import edu.northwestern.at.utils.corpuslinguistics.tokenizer.*;
import edu.northwestern.at.utils.servlets.*;

/**	Segments text linearly by topic.
 */

public class TextSegmenterServlet extends BaseAdornerServlet
{
	/**	Initialize the servlet.
	 *
	 *	@param	config	Servlet configuration.
	 *
	 *	@throws			ServletException
	 */

	public void init( ServletConfig config ) throws ServletException
	{
								//	Start main initialization.
		initialize( config );
	}

	/**	Handle servlet request.
	 *
	 *	@param	request		Servlet request.
	 *	@param	response	Servlet response.
	 *
	 *	@return				Servlet results.
	 */

	protected ServletResult handleRequest
	(
		HttpServletRequest request ,
		HttpServletResponse response
	)
		throws ServletException, java.io.IOException
	{
		ServletResult result	= null;

								//	Determine if we got here from a form
								//	submission or an SSI include.

		boolean clear				= ( request.getParameter( "clear" ) != null );
		boolean segment				= ( request.getParameter( "segment" ) != null );
		boolean fromForm			= clear || segment;
		String adornerName			= request.getParameter( "adornername" );

		String segmenterName		=
			request.getParameter( "segmentername" );

		int c99MaskSize				=
			getIntValue( request.getParameter( "c99masksize" ) , 11 );

		int c99SegmentsWanted		=
			getIntValue( request.getParameter( "c99segmentswanted" ) , -1 );

		int tilerSlidingWindowSize			=
			getIntValue( request.getParameter( "tilerslidingwindowsize" ) , 10 );

		int tilerStepSize		=
			getIntValue( request.getParameter( "tilerstepsize" ) , 100 );

								//	Get which adorner to use.

		AdornerInfo adornerInfo	= getAdornerInfo( adornerName );

								//	Get servlet session.

		HttpSession session = request.getSession( true );

								//	See if we're just returning the
								//	output of the previous invocation.

		String textSegmenterResults	=
			(String)session.getAttribute( "textsegmenterresults" );

								//	If so, just pipe the output back
								//	to the client.

		if ( ( textSegmenterResults != null ) && !fromForm )
		{
			session.setAttribute( "textsegmenterresults" , null );

			result	=
				new ServletResult
				(
					false ,
					textSegmenterResults ,
					"Text Segmenter Example" ,
					"/morphadorner/textsegmenter/example/" ,
					"textsegmenterresults"
				);
		}
		else
		{
								//	Get string output writer.

			StringPrintWriter out	= new StringPrintWriter();

								//	See if we're splitting text.
								//	If not, clear the text and
								//	return an empty form.

			String text						= "";
			List<List<String>> sentences	= null;
			List<Integer> segments			= null;

			if ( segment )
			{
								//	See if we have a text to split into
								//	sentences.

				text	= request.getParameter( "text" );

				if ( text == null )
				{
					text	= "";
				}
				else
				{
					text	= unTag( text );
				}
								//	Segment text.

				if ( text.length() > 0 )
				{
					sentences	=
						adornerInfo.extractor.extractSentences( text );

					TextSegmenter textSegmenter;

					if ( segmenterName != null )
					{
						if ( segmenterName.equals( "C99" ) )
						{
							textSegmenter	= new C99TextSegmenter();

							((C99TextSegmenter)textSegmenter).setSegmentsWanted(
								c99SegmentsWanted );

							((C99TextSegmenter)textSegmenter).setMaskSize( c99MaskSize );
						}
						else
						{
							textSegmenter	= new TextTilingTextSegmenter();

							((TextTilingTextSegmenter)textSegmenter).setSlidingWindowSize(
								tilerSlidingWindowSize );

							((TextTilingTextSegmenter)textSegmenter).setStepSize(
								tilerStepSize );
						}

						segments	=
							textSegmenter.getSegmentPositions( sentences );
					}
				}
			}
								//	Output form.

			outputForm
			(
				out ,
				text ,
				adornerName ,
				segmenterName ,
				c99SegmentsWanted ,
				c99MaskSize ,
				tilerSlidingWindowSize ,
				tilerStepSize
			);
								//	Output segments.

			outputSegmentedText( out , sentences , segments , segmenterName );

								//	Create results.
			result	=
				new ServletResult
				(
					fromForm ,
					out.getString() ,
					"Text Segmenter Example" ,
					"/morphadorner/textsegmenter/example/" ,
					"textsegmenterresults"
				);
		}

		return result;
	}

	/**	Output form.
	 *
	 *	@param	out						PrintWriter for servlet output.
	 *	@param	text					Text to split into sentences.
	 *	@param	adornerName				Adorner name.
	 *	@param	segmenterName			Segmenter name.
	 *	@param	c99SegmentsWanted		C99 desired number of segments.
	 *	@param	c99MaskSize				C99 mask size.
	 *	@param	tilerSlidingWindowSize	TextTiler sliding window size.
	 *	@param	tilerStepSize			TextTiler step size.

	 */

	public void outputForm
	(
		java.io.PrintWriter out ,
		String text ,
		String adornerName ,
		String segmenterName ,
		int c99SegmentsWanted ,
		int c99MaskSize ,
		int tilerSlidingWindowSize ,
		int tilerStepSize
	)
	{
		out.println( "<p>" );
		out.println( "Enter text to segment in the ");
		out.println( "input field below.<br />" );
		out.println( "</p>" );

		out.println(
			"<form method=\"post\" " +
			"action=\"/morphadorner/textsegmenter/example/TextSegmenter\"" +
			" name=\"segmenter\">" );

		out.println( "<table cellpadding=\"0\" cellspacing=\"5\">" );

		out.println( "<tr>" );
		out.print  ( "<td colspan=\"2\"><textarea name=\"text\" rows=\"10\"" +
			" cols=\"50\">" );

		out.print  ( text );
		out.println( "</textarea>" );

		out.println( "</td>" );
		out.println( "</tr>" );

		outputSegmenterSelection
		(
			out ,
			"Segmenter:" ,
			segmenterName ,
			c99SegmentsWanted ,
			c99MaskSize ,
			tilerSlidingWindowSize ,
			tilerStepSize
		);

		outputAdornerSelection( out , "Lexicon:" , adornerName );

		outputSpacerRow( out , 2 );

		out.println( "<tr>" );
		out.println( "<td colspan=\"2\">" );

		out.println(
			"<input type=\"submit\" name=\"segment\" value=\"Segment\" />" );

		out.println(
			"<input type=\"submit\" name=\"clear\" value=\"Clear\" />" );

		out.println( "</td>" );
		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "</form>" );
	}

	/**	Output segmenter method form field.
	 *
	 *	@param	out				PrintWriter for servlet output.
	 *	@param	label			Column label.  May be empty.
	 *	@param	segmenterName	Segmenter name.
	 */

	public void outputSegmenterSelection
	(
		java.io.PrintWriter out ,
		String label ,
		String segmenterName ,
		int c99SegmentsWanted ,
		int c99MaskSize ,
		int tilerSlidingWindowSize ,
		int tilerStepSize
	)
	{
		out.println( "<tr>" );

		if ( ( label != null ) && ( label.length() > 0 ) )
		{
			out.println( "<td valign=\"top\">" );
			out.println( "<strong>" );
			out.print( label );
			out.println( "</strong>" );
			out.println( "</td>" );
		}

		out.println( "<td>" );

		String checkedC99			= "";
		String checkedTextTiling	= "checked=\"checked\"";

		if ( ( segmenterName != null ) && ( segmenterName.equals( "C99" ) ) )
		{
			checkedC99			= "checked=\"checked\"";
			checkedTextTiling	= "";
		}

		out.println( "<input type=\"radio\" name=\"segmentername\" " +
			"value=\"C99\"" +
			checkedC99 +
			">C99</input><br />" );

		out.println( "<table border=\"0\">" );
		out.println( "<tr>" );
		out.println( "<td>" );
		out.println( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "Mask size:" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "<input type=\"text\" name=\"c99masksize\"" +
			"size = \"5\" value=\"" + c99MaskSize + "\" /></input>" );
		out.println( "</td>" );
		out.println( "</tr>" );

		out.println( "<tr>" );
		out.println( "<td>" );
		out.println( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "Segments desired:" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "<input type=\"text\" name=\"c99segmentswanted\"" +
			"size = \"5\" value=\"" + c99SegmentsWanted + "\" /></input>" );
		out.println( "</td>" );
		out.println( "</tr>" );
		out.println( "</table>" );

		out.println( "<input type=\"radio\" name=\"segmentername\" " +
			"value=\"Text Tiling\"" +
			checkedTextTiling +
			">Text Tiling</input><br />" );

		out.println( "<table border=\"0\">" );
		out.println( "<tr>" );
		out.println( "<td>" );
		out.println( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "Sliding window size:" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "<input type=\"text\" name=\"tilerslidingwindowsize\"" +
			"size = \"5\" value=\"" + tilerSlidingWindowSize + "\" /></input>" );
		out.println( "</td>" );
		out.println( "</tr>" );

		out.println( "<tr>" );
		out.println( "<td>" );
		out.println( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "Segment size:" );
		out.println( "</td>" );
		out.println( "<td>" );
		out.println( "<input type=\"text\" name=\"tilerstepsize\"" +
			"size = \"5\" value=\"" + tilerStepSize + "\" /></input>" );
		out.println( "</td>" );
		out.println( "</tr>" );
		out.println( "</table>" );

		out.println( "</td>" );
		out.println( "</tr>" );
	}

	/**	Output the segmented text.
	 *
	 *	@param	out				PrintWriter for servlet output.
	 *	@param	sentences		List of sentences.
	 *	@param	segments		List of segment boundaries.
	 *	@param	segmenterName	Type of segmenter used.
	 */

	public void outputSegmentedText
	(
		java.io.PrintWriter out ,
		List<List<String>> sentences ,
		List<Integer> segments ,
		String segmenterName
	)
	{
		if ( sentences == null ) return;

		out.println( "<hr noshade=\"noshade\" />" );

		switch ( segments.size() )
		{
			case 0:
				out.println( "<h3>No segments found.</h3>" );
				return;

			case 1:
				out.println( "<h3>1 segment found using " + segmenterName +
					".</h3>" );
				break;

			default:
				out.println( "<h3>" + segments.size() +
					" segments found using " + segmenterName + ".</h3>" );
				break;
		}

		out.println( "<table width=\"100%\">");

		out.println( "<tr>" );
		out.println( "<th class=\"width1pct\" align=\"left\">" );
		out.println( "Segment" );
		out.println( "</th>" );

		out.println( "<th class=\"width99pct\" align=\"left\">" );
		out.println( "Text" );
		out.println( "</th>" );
		out.println( "</tr>" );

		SentenceMelder melder	= new SentenceMelder();

		int firstSentence	= 0;
		int lastSentence	= 0;

		for ( int i = 0 ; i < segments.size() ; i++ )
		{
			firstSentence	= segments.get( i );

			if ( i < ( segments.size() - 1 ) )
			{
				lastSentence	= segments.get( i + 1 );
   			}
   			else
   			{
   				lastSentence	= sentences.size();
   			}

			out.println( "<tr>" );
			out.println( "<td valign=\"top\" class=\"width1pct\">" );
			out.println( ( i + 1 ) + "" );
			out.println( "</td>" );

			out.println( "<td class=\"width99pct\">" );

			for ( 	int j = firstSentence ;
					j < lastSentence ;
					j++ )
			{
				List<String> sentence	= sentences.get( j );

				String sentenceText	=
					melder.reconstituteSentence( sentence );

				out.println( sentenceText );
				out.println( "  " );
			}

			out.println( "<br />" );
			out.println( "<hr />" );

			out.println( "</td>" );
			out.println( "</tr>" );
		}

		out.println( "</table>");
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




