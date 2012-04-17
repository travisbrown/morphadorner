package de.spieleck.app.cngram ;

/*	Please see the license information in the header below. */

/*
 NGramJ - n-gram based text classification
 Copyright (C) 2001- Frank S. Nestel (frank at spieleck.de)

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program (lesser.txt); if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 Minor modifications for inclusion in MorphAdorner by
 Philip R. Burns.  2009/04/28 .
 */

import java.io.Reader ;
import java.io.BufferedReader ;
import java.io.InputStreamReader ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.HashSet ;
import java.util.Set ;
import java.util.Arrays ;
import java.util.BitSet ;
import java.text.DecimalFormat ;

import edu.northwestern.at.utils.UnicodeReader;

/**
 * Manage a set of profiles and determine "most similar" ones
 * to a given profile. Allows access to the complete results of
 * previous last ranking. Note this uses a competetive ranking
 * approach, which is memory efficient, time efficient for not
 * too many languages and provides contextual scoring of ngrams.
 *
 * @author frank nestel
 * @author $Author: nestefan $
 * @version $Revision: 2 $ $Date: 2006-03-27 23:00:21 +0200 (Mo, 27 Mrz 2006) $ $Author: nestefan $
 */
@SuppressWarnings("unchecked")
public class NGramProfiles
{
    public final static String NOLANGNAME = "--" ;

    public final static char END_CHAR = (char)0 ;

    public final static DecimalFormat DF = new DecimalFormat( "+0.00;-0.00" ) ;

    public final static double LOWSTATSAFETY = 0.5 ; // was 0.5;

    private List profiles = null ;

    private HashSet allNGrams = new HashSet( 10000 ) ;

    private int firstNGrams ;

    private int maxLen = -1 ;

    private Trie myTrie = null ;

    private float[][] vals ;

    private int mode ;

    /** Path to language resource file list. */

    protected final static String langProfilesResourcePath = "resources/" ;

    public NGramProfiles()
        throws IOException
    {
        this( 1 ) ;
    }

    public NGramProfiles( int mode )
        throws IOException
    {
        InputStream ip =
            getClass().getResourceAsStream(
                langProfilesResourcePath + "profiles.lst" ) ;
        BufferedReader br = new BufferedReader( new UnicodeReader( ip ) ) ;

        this.mode = mode ;
        init( br ) ;
    }

    public NGramProfiles( BufferedReader br )
        throws IOException
    {
        init( br ) ;
    }

	/**	Initialize language recognizer with list of languages to recognize.
	 *
	 *	@param	langs	List of languages to recognize.
	 *
	 *	@throws	IOException	When I/O error occurs.
	 *
	 *	<p>
	 *	The list of languages references the fingerprint entries in
	 *	the resources/ directory.  Specify each language without
	 *	the ".ngp" extension, e.g., use "italian" for Italian.
	 *	</p>
	 */

    public NGramProfiles( java.util.List langs )
        throws IOException
    {
    	this.mode	= 1;

        profiles = new ArrayList() ;
        firstNGrams = 0 ;
        String line ;

        for ( int i = 0 ; i < langs.size() ; i++ )
        {
            line	= langs.get( i ).toString();

            if ( line.charAt( 0 ) == '#' )
            {
                continue ;
            }
            InputStream is =
                getClass().getResourceAsStream(
                    langProfilesResourcePath + line + "." +
                    NGramProfile.NGRAM_PROFILE_EXTENSION ) ;
            NGramProfileImpl np = new NGramProfileImpl( line ) ;

            np.load( is ) ;
            profiles.add( np ) ;
            Iterator iter = np.getSorted() ;

            while ( iter.hasNext() )
            {
                NGram ng = (NGram)iter.next() ;

                if ( ng.length() > maxLen )
                {
                    maxLen = ng.length() ;
                }
                firstNGrams++ ;
                allNGrams.add( ng ) ;
            }
        }
        myTrie = null ;
    }

    protected void init( BufferedReader br )
        throws IOException
    {
        profiles = new ArrayList() ;
        firstNGrams = 0 ;
        String line ;

        while ( ( line = br.readLine() ) != null )
        {
            if ( line.charAt( 0 ) == '#' )
            {
                continue ;
            }
            InputStream is =
                getClass().getResourceAsStream(
                    langProfilesResourcePath + line + "." +
                    NGramProfile.NGRAM_PROFILE_EXTENSION ) ;
            NGramProfileImpl np = new NGramProfileImpl( line ) ;

            np.load( is ) ;
            profiles.add( np ) ;
            Iterator iter = np.getSorted() ;

            while ( iter.hasNext() )
            {
                NGram ng = (NGram)iter.next() ;

                if ( ng.length() > maxLen )
                {
                    maxLen = ng.length() ;
                }
                firstNGrams++ ;
                allNGrams.add( ng ) ;
            }
        }
        myTrie = null ;
    }

    public void info()
    {
        System.err.println(
            "#profiles=" + profiles.size() + ", firstNGrams=" + firstNGrams +
            ", secondNGrams=" + allNGrams.size() + ", maxLen=" + maxLen +
            ", mode=" + mode ) ;
        CosMetric cm = new CosMetric() ;
        Iterator i1 = profiles.iterator() ;

        while ( i1.hasNext() )
        {
            NGramProfile p1 = (NGramProfile)i1.next() ;

            System.out.print( "# " + p1.getName() ) ;
            Iterator i2 = profiles.iterator() ;

            while ( i2.hasNext() )
            {
                NGramProfile p2 = (NGramProfile)i2.next() ;

                System.out.print( " " ) ;
                System.out.print( DF.format( 1.0 - cm.diff( p1 , p2 ) ) ) ;
            }
            System.out.println() ;
        }
        Ranker r = getRanker() ;

        //
        check( r , "Das ist cool men!" ) ;
        check( r , "Les Ordinateurs sont appeles a jouer un role" ) ;
        check( r , "Sein oder Nichtsein, das ist hier die Frage!" ) ;
        check( r ,
            "Zu Ihren Aufgaben z�hlen u. a. die Einf�hrung und Erprobung neuer Technologien, die Erarbeitung von Rationalisierungsl�sungen f�r die Fertigung sowie die Erarbeitung und Durchf�hrung von Technologieversuchen bei der Mustererstellung mit Hinblick auf die Serienfertigung. Desweiteren sind Sie f�r die Betreuung und Durchf�hrung von Entwicklungsprojekten sowie die Erarbeitung und konstruktive Auslegung von Werkzeugen, Vorrichtungen und Automatisierungseinrichtungen zust�ndig. Sie sind Ansprechpartner f�r die Zusammenarbeit und Betreuung von externen Lieferanten bei der Konstruktion und der Herstellung von Maschinen und Vorrichtungen. Sie leiten qualit�tsverbessernde Ma�nahmen im Prozess und in der Fertigung ein und sind f�r die kostenbewusste und zukunftsweisende Planung des Bereiches verantwortlich. Zudem f�hren Sie Ihre Mitarbeiter zielgerichtet entsprechend der INA-F�hrungsleitlinien." ) ;
        check( r ,
            "Sein oder Nichtsein, das ist hier die Frage! To be or not to be, that is the question!" ) ;
        check( r ,
            "Sein oder Nichtsein, das ist hier die Frage! To be or not to be, that is the question! Sein oder Nichtsein, das ist hier die Frage! To be or not to be, that is the question!" ) ;
        check( r , "Marcel Andre Casasola Merkle" ) ;
        check( r , "question" ) ;
        check( r , "methodist" ) ;

        /*
         */
    }

    public static void check( Ranker r , CharSequence seq )
    {
        System.out.println( "## \"" + seq + "\": " ) ;
        r.reset() ;
        long t1 = System.currentTimeMillis() ;

        r.account( seq ) ;
        long t2 = System.currentTimeMillis() ;
        RankResult rs = r.getRankResult() ;
        double sum = 0.0 ;

        for ( int i = 0 ; i < rs.getLength() ; i++ )
        {
            if ( rs.getScore( i ) > 0.0 )
            {
                System.out.println(
                    "  " + rs.getName( i ) + " " +
                    DF.format( rs.getScore( i ) ) + " " + ( i + 1 ) ) ;
                sum += rs.getScore( i ) ;
            }
        }
        System.out.println(
            "#===== took=" + ( t2 - t1 ) + "ms: " +
            DF.format( 1.0 * ( t2 - t1 ) / seq.length() ) + "ms/ch. other=" +
            DF.format( 1.0 - sum ) ) ;
    }

    public Ranker getRanker()
    {
        int[] otherCount = new int[ maxLen + 1 ] ;

        if ( myTrie == null )
        {
            synchronized ( profiles )
            {
                if ( myTrie == null )
                {
                    // Create a reverse reference of all strings
                    // which makes it easy to create reverse Trie's
                    String[] ngs = new String[ allNGrams.size() ] ;
                    Iterator it = allNGrams.iterator() ;
                    int j = 0 ;

                    while ( it.hasNext() )
                    {
                        NGram ng = (NGram)it.next() ;

                        ngs[ j++ ] = reverse( ng ) ;
                    }
                    Arrays.sort( ngs ) ;
                    // Create Strings in correct order but sorted from reverse end.
                    String[] ng1 = new String[ allNGrams.size() ] ;

                    for ( int i = 0 ; i < ngs.length ; i++ )
                    {
                        ng1[ i ] = reverse( ngs[ i ] ) ;
                    }
                    myTrie = createTrie( ngs , 0 , 0 , ngs.length ) ;
                    vals = new float[ ngs.length ][ profiles.size() ] ;
                    int[] lengthes = new int[ ngs.length ] ;

                    for ( int k = 0 ; k < profiles.size() ; k++ )
                    {
                        NGramProfile ngp = ( (NGramProfile)profiles.get( k ) ) ;
                        double norm[] = new double[ maxLen + 1 ] ;
                        int count[] = new int[ maxLen + 1 ] ;

                        for ( int i = 0 ; i < ngs.length ; i++ )
                        {
                            NGram ng = ngp.get( ng1[ i ] ) ;

                            if ( ng != null && ng.getCount() > LOWSTATSAFETY )
                            {
                                int ngl = ng.length() ;

                                lengthes[ i ] = ngl ; // write at least once, read once :-|
                                double raw1 = ng.getCount() - LOWSTATSAFETY ;

                                count[ ngl ]++ ;
                                norm[ ngl ] += raw1 ;
                                vals[ i ][ k ] = (float)raw1 ;
                            }
                        }
                        for ( int i = 1 ; i <= maxLen ; i++ )
                        {
                            norm[ i ] *= ( 1.0 + count[ i ] ) / count[ i ] ;
                            norm[ i ] += 1.0 ;
                        }
                        for ( int i = 0 ; i < ngs.length ; i++ )
                        {
                            NGram ng = ngp.get( ng1[ i ] ) ;

                            if ( ng != null && ng.getCount() > 0 )
                            {
                                int ngl = ng.length() ;
                                double trans = vals[ i ][ k ] / norm[ ngl ] ;

                                vals[ i ][ k ] = (float)trans ;
                            }
                        }
                    }
                    // Horizontal additive zero sum + nonlinear weighting
                    for ( int i = 0 ; i < ngs.length ; i++ )
                    {
                        double sum = 0.0 ;

                        for ( int k = 0 ; k < profiles.size() ; k++ )
                        {
                            double h = vals[ i ][ k ] ;

                            sum += h ;
                        }
                        double av = sum / profiles.size() ;

                        /**
                         * Assumed minimum amount of score for significance.
                         * XXX Heuristics for the following constant:
                         * Higher means faster and less noise
                         * Lower means better adaption to mixed language text
                         */
                        double n =
                            modeTrans( av , ng1[ i ].length() ) / av / 100.0 *
                            ( -Math.log( av ) ) ;

                        for ( int k = 0 ; k < profiles.size() ; k++ )
                        {
                            vals[ i ][ k ] =
                                (float)( ( vals[ i ][ k ] - av ) * n ) ;
                        }
                    }
                }
            }
        }
        return new Ranker()
        {
            private double score[] = new double[ profiles.size() + 1 ] ;
            private double rscore[] = new double[ profiles.size() + 1 ] ;
            private boolean flushed = false ;

            {
                reset() ;
            }

            public RankResult getRankResult()
            {
                flush() ;
                double pscore[] = new double[ profiles.size() ] ;
                double sum = 0.0 ;

                for ( int i = 0 ; i <= profiles.size() ; i++ )
                {
                    sum += rscore[ i ] ;
                }
                for ( int i = 0 ; i < profiles.size() ; i++ )
                {
                    pscore[ i ] = rscore[ i ] / sum ;
                }
                return new SimpleRankResult( pscore , true ) ;
            }

            public void reset()
            {
                for ( int i = 0 ; i < score.length ; i++ )
                {
                    rscore[ i ] = score[ i ] = 0.0 ;
                }
                score[ score.length - 1 ] = 0.0 ;
                rscore[ score.length - 1 ] = 0.5 ; // 0.2 is too low;
            }

            public void flush()
            {
                if ( !flushed )
                {
                    flushed = true ;
                    double maxValue = -1.0 ;

                    for ( int i = 0 ; i < score.length ; i++ )
                    {
                        maxValue = Math.max( maxValue , score[ i ] ) ;
                    }
                    double limit = maxValue / 2.0 ;
                    double f = 1.0 / ( maxValue - limit ) ;

                    for ( int i = 0 ; i < score.length ; i++ )
                    {
                        double delta = score[ i ] - limit ;

                        if ( delta > 0.0 )
                        {
                            rscore[ i ] += delta * f ;
                        }
                        // We do not reset to zero, this makes classification contextual
                        score[ i ] /= 2.0 ;
                    }
                }
            }

            public void account( CharSequence seq , int pos )
            {
                // System.out.println("--");
                Trie currentNode = myTrie ;
                int p2 = pos ;

                while ( currentNode != null )
                {
                    char ch ;

                    if ( p2 == -1 )
                    {
                        ch = ' ' ;
                    }
                    else
                    {
                        ch = Character.toLowerCase( seq.charAt( p2 ) ) ;
                        if ( isSeparator( ch ) )
                        {
                            ch = ' ' ;
                        }
                    }
                    Trie t2 = currentNode.subtree( ch ) ;

                    if ( t2 == null )
                    {
                        break ;
                    }
                    // System.out.println("- "+(pos-p2)+"|"+ch+"|"+t2+"| t2.split="+t2.split+" t2.id="+t2.id+" ("+p2+")");
                    if ( t2.id >= 0 )
                    {
                        flushed = false ;
                        // double max = 0.0;
                        for ( int i = 0 ; i < profiles.size() ; i++ )
                        {
                            // max = Math.max(max, vals[t2.id][i]);
                            score[ i ] += vals[ t2.id ][ i ] ;
                        }

                        /*
                         if (p2 >= 0 )System.out.print("<"+seq.subSequence(p2,pos+1)+">:");else System.out.print("< "+seq.subSequence(0,pos+1)+">:");
                         int llh = pos - p2;
                         System.out.print("     ".subSequence(0,5 - pos + p2));
                         for(int i = 0; i < profiles.size(); i++)
                         {
                         System.out.print(" "+getProfileName(i)
                         +(vals[t2.id][i] == max ? '*':':')
                         +DF.format(vals[t2.id][i])
                         );
                         }
                         System.out.println();
                         */
                    }
                    if ( p2-- == -1 )
                    {
                        break ;
                    }
                    currentNode = t2.center ;
                }
                char startChar = seq.charAt( pos ) ;
                boolean startSep = isSeparator( startChar ) ;
                double max = 0.0 ;

                for ( int i = 0 ; i < score.length ; i++ )
                {
                    max = Math.max( max , score[ i ] ) ;
                }
                if ( startSep && max > 1.0 )
                {

                    /*
                     System.out.println(" - "+DF.format(max)
                     +" "+DF.format(score[score.length-1])+" "+pos
                     +" "+seq.charAt(pos-2)+seq.charAt(pos-1)+seq.charAt(pos)
                     );
                     */
                    flush() ;
                }
            }

            public void account( CharSequence seq )
            {
                for ( int i = 0 ; i < seq.length() ; i++ )
                {
                    account( seq , i ) ;
                }
            }

            public void account( Reader reader )
                throws IOException
            {
                BufferedReader br ;

                if ( reader instanceof BufferedReader )
                {
                    br = (BufferedReader)reader ;
                }
                else
                {
                    br = new BufferedReader( reader ) ;
                }
                String line ;

                while ( ( line = br.readLine() ) != null )
                {
                    account( line ) ;
                }
            }
        } ;
    }

    public double modeTrans( double x , int l )
    {
        double f ;

        switch ( mode )
        {
            case 1 :
            case 10 :
                if ( l == 1 )
                {
                    return x ;
                }
                f = 1.0 / ( l + 1 ) ;
                return Math.pow( x / f , f ) ;

            case 9 :
                f = 1.0 / ( l + 1 ) ;
                return Math.pow( x , f ) / Math.sqrt( f ) ;

            case 8 :
                f = 1.0 / ( l + 1 ) ;
                return Math.pow( x , f ) / Math.sqrt( f ) ;

            case 7 :
                f = 1.0 / ( l + 1 ) ;
                return Math.pow( x , f ) / f ;

            case 6 :
                f = 1.0 / l ;
                return Math.pow( x , f ) / Math.sqrt( f ) ;

            case 5 :
                f = 1.0 / l ;
                return Math.pow( x , f ) / f ;

            case 3 :
                f = 1.0 / l ;
                return Math.pow( x , f ) ;

            case 2 :
                f = 1.0 / l ;
                return Math.pow( x / f , f ) ;

            case 4 :
                f = 1.0 / l ;
                return Math.pow( x * f , f ) ;
        }
        return x ;
    }

    public String getProfileName( int i )
    {
        if ( i < 0 || i >= profiles.size() )
        {
            return NOLANGNAME ;
        }
        return ( (NGramProfile)profiles.get( i ) ).getName() ;
    }

    public static boolean isSeparator( char ch )
    {
        return ( ch <= ' ' || Character.isWhitespace( ch ) ||
            Character.isDigit( ch ) || ".!?:,;".indexOf( ch ) >= 0
            ) ;
    }

    public static String reverse( CharSequence seq )
    {
        StringBuilder sb = new StringBuilder( seq.length() ) ;

        for ( int i = 0 ; i < seq.length() ; i++ )
        {
            sb.insert( 0 , seq.charAt( i ) ) ;
        }
        return sb.toString() ;
    }

    private static void printTrie( String indent , int w , Trie trie , String seq )
    {
        if ( --w <= 0 )
        {
            return ;
        }
        if ( trie == null )
        {
            return ;
        }
        if ( trie.split == END_CHAR )
        {
            System.out.println( indent + "<" + trie.split + ">[" + seq + "]" ) ;
        }
        else
        {
            System.out.println( indent + "<" + trie.split + ">" ) ;
            printTrie( indent + " c" , w , trie.center , trie.split + seq ) ;
            printTrie( indent + "l" , w , trie.left , seq ) ;
            printTrie( indent + "r" , w , trie.right , seq ) ;
        }
    }

    private static Trie createTrie( String[] array , int pos , int start , int end )
    {
        if ( start >= end )
        {
            return null ;
        }

        /*
         if ( start == end )
         {
         // XXX Some special Trie-Node and moving to getters for links
         // could save memory here!
         Trie leaf = new Trie();
         leaf.split = array[start].charAt(pos);
         leaf.id = start;
         return leaf;
         }
         */
        int mid = ( start + end ) / 2 ;
        Trie nt = new Trie() ;

        // System.out.println("=! "+start+" "+mid+"["+array[mid]+"] "+end);
        nt.split = array[ mid ].charAt( pos ) ;
        int goRight = mid ;

        while ( goRight < end && charAt( array[ goRight ] , pos ) == nt.split )
        {
            goRight++ ;
        }
        int goLeft = mid ;

        while ( goLeft > start &&
            charAt( array[ goLeft - 1 ] , pos ) == nt.split )
        {
            goLeft-- ;
        }
        // Try to move "end" nodes direktly into the id field!
        int goLeft2 = goLeft ;

        if ( array[ goLeft ].length() == pos + 1 )
        {
            // System.out.println("=# "+goLeft+" <"+nt.split+"> <"+array[goLeft]+">");
            nt.id = goLeft ;
            goLeft2++ ;
        }
        // System.out.println("== "+start+" "+goLeft+"["+array[goLeft]+"]|"+(goLeft2 < array.length ?"["+array[goLeft2]+"]":"[]")+goLeft2+" ("+mid+","+nt.split+") "+(goRight < array.length ? "["+array[goRight]+"]":"[]")+goRight+"-"+end);
        nt.center = createTrie( array , pos + 1 , goLeft2 , goRight ) ;
        nt.left = createTrie( array , pos , start , goLeft ) ;
        nt.right = createTrie( array , pos , goRight , end ) ;
        return nt ;
    }

    public final static char charAt( CharSequence cs , int pos )
    {
        if ( pos < cs.length() )
        {
            return cs.charAt( pos ) ;
        }
        return END_CHAR ;
    }

    private final static class Trie
    {
        static int count = 0 ;
        char split ;
        Trie left ;
        Trie right ;
        Trie center ;
        int id = -1 ;

        public Trie()
        {
            count++ ;
        }

        public Trie subtree( char c )
        {
            Trie current = this ;

            do
            {
                if ( c == current.split )
                {
                    return current ;
                }
                else if ( c > current.split )
                {
                    current = current.right ;
                }
                else
                {
                    current = current.left ;
                }
            } while ( current != null ) ;
            return null ;
        }
    }

    public static char sc( char c )
    {
        return c == END_CHAR ? '?' : c ;
    }

    /**
     * Note this class returns a complete match result, for the
     * sake of thread safety!
     */
    public RankResult rank( NGramMetric metric , NGramProfile profile )
    {
        Iterator it = profiles.iterator() ;
        String res = null ;
        double[] scores = new double[ profiles.size() ] ;

        for ( int i = 0 ; i < profiles.size() ; i++ )
        {
            scores[ i ] =
                metric.diff( profile , (NGramProfile)( profiles.get( i ) ) ) ;
        }
        return new SimpleRankResult( scores , false ) ;
    }

    private class SimpleRankResult
        implements RankResult
    {
        private double scores[] ;
        private NGramProfile[] profs ;
        private double remain ;

        public SimpleRankResult( double[] scorex , boolean inverse )
        {
            scores = new double[ scorex.length ] ;
            System.arraycopy( scorex , 0 , scores , 0 , scorex.length ) ;
            profs = new NGramProfile[ scores.length ] ;
            remain = 1.0 ;
            for ( int i = 0 ; i < scores.length ; i++ )
            {
                NGramProfile prof = ( (NGramProfile)profiles.get( i ) ) ;
                double m = scores[ i ] ;

                remain -= m ;
                int j = i ;

                while ( --j >= 0 && ( inverse ^ ( m < scores[ j ] ) ) )
                {
                    scores[ j + 1 ] = scores[ j ] ;
                    profs[ j + 1 ] = profs[ j ] ;
                }
                scores[ j + 1 ] = m ;
                profs[ j + 1 ] = prof ;
            }
        }

        public NGramProfiles getProfiles()
        {
            return NGramProfiles.this ;
        }

        public double getScore( int pos )
        {
            if ( pos == getLength() )
            {
                return remain ;
            }
            if ( pos < 0 )
            {
                pos += getLength() ;
            }
            return scores[ pos ] ;
        }

        public String getName( int pos )
        {
            if ( pos == getLength() )
            {
                return NOLANGNAME ;
            }
            else if ( pos < 0 )
            {
                pos += getLength() ;
            }
            return profs[ pos ].getName() ;
        }

        public int getLength()
        {
            return profs.length ;
        }
    }

    public int getProfileCount()
    {
        return profiles.size() ;
    }

    public Set getAllNGrams()
    {
        // XXX make this read only or is this slowing down too much?
        return allNGrams ;
    }

    public interface RankResult
    {
        public NGramProfiles getProfiles() ;
        public int getLength() ;
        public double getScore( int pos ) ;
        public String getName( int pos ) ;
    }

    public interface Ranker
    {
        public RankResult getRankResult() ;
        public void reset() ;
        public void flush() ;
        public void account( CharSequence seq , int pos ) ;
        public void account( CharSequence seq ) ;
        public void account( Reader reader )
            throws IOException ;
    }
}

