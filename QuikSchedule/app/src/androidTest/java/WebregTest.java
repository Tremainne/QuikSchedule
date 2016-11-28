import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Class: WebregTest
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/25/16
 *
 * Description: Class used for testing functionality of Webreg Parsing
 *
 * @author Tynan Dewes
 */
public class WebregTest {

    // Patterns used for parsing data based on HTML formatting of desired Web pages
    final static Pattern CLASS_PATTERN = Pattern.compile("\\s+(\\w.* \\- \\w+)");
    final static Pattern DAY_PATTERN = Pattern.compile(">(.*)<");
    final static Pattern LOC_PATTERN = Pattern.compile(">(.*)<");
    final static Pattern SPLIT_PATTERN = Pattern.compile("(.*) \\- (.*)");
    final static Pattern TIME_PATTERN = Pattern.compile("\\s+(\\w.*)<");
    final static Pattern SEC_PATTERN = Pattern.compile("\\((\\w+)\\) (\\w+), Section: (\\d+)");
    final static Pattern AUTHOR_PATTERN = Pattern.compile(">(\\w+)</font>");
    final static Pattern BOOK_PATTERN = Pattern.compile("-1\">(.*),");

    final static String DAY = "<h4>Monday</h4>\n";
    final static String SPLIT = "1 - 2";
    final static String TIME = "\t\t\t\t\t\t\t1:00pm - 1:50pm<br />\n";
    final static String CLASS = "\t\t\t\t\t\t\tCSE 101 - Discussion\t\t  \t\t\n";
    final static String LOC = "\t\t\t\t\t\t\t<a href=\"http://m.ucsd.edu/maps/isisCode/PETER\"" +
            "class=\"only-webkit\">PETER 102</a>\t\n";
    final static String SEC = "Computer Science & Engineering (CSE) 110, Section: 882324\n";
    final static String AUTHOR = "<td><font face=\"tahoma\" size=\"-1\">Buck</font></Td>\n";
    final static String BOOK = "<TD><FONT FACE=\"tahoma\" SIZE=\"-1\">Computer Explorations In " +
            "Signals & Systems Using Matlab, 2 Edition, 9780130421555</FONT></TD>\n";

    @Test
    public void testDayRegex() throws IOException {
        String res = "Monday";
        Matcher matcher = DAY_PATTERN.matcher( DAY );
        matcher.find();
        assertThat( matcher.group(1), is( res ) );
    }

    @Test
    public void testSplitRegex() throws IOException {
        String res1 = "1";
        String res2 = "2";
        Matcher matcher = SPLIT_PATTERN.matcher( SPLIT );
        matcher.find();
        assertThat( matcher.group(1), is( res1 ) );
        assertThat( matcher.group(2), is( res2 ) );
    }

    @Test
    public void testTimeRegex() throws IOException {
        String time1 = "1:00pm";
        String time2 = "1:50pm";
        Matcher matcher = TIME_PATTERN.matcher( TIME );
        matcher.find();

        Matcher splitter = SPLIT_PATTERN.matcher( matcher.group(1) );
        splitter.find();
        assertThat( splitter.group(1), is( time1 ) );
        assertThat( splitter.group(2), is( time2 ) );
    }

    @Test
    public void testClassRegex() throws IOException {
        String res = "CSE 101 - Discussion";
        Matcher matcher = CLASS_PATTERN.matcher( CLASS );
        matcher.find();
        assertThat( matcher.group(1), is( res ) );
    }

    @Test
    public void testLocRegex() throws IOException {
        String res = "PETER 102";
        Matcher matcher = LOC_PATTERN.matcher( LOC );
        matcher.find();
        assertThat( matcher.group(1), is( res ) );
    }

    @Test
    public void testSecRegex() throws IOException {
        String res = "882324";
        Matcher matcher = SEC_PATTERN.matcher( SEC );
        matcher.find();
        assertThat( matcher.group(3), is( res ) );
    }

    @Test
    public void testAuthorRegex() throws IOException {
        String res = "Buck";
        Matcher matcher = AUTHOR_PATTERN.matcher( AUTHOR );
        matcher.find();
        assertThat( matcher.group(1), is( res ) );
    }

    @Test
    public void testBookRegex() throws IOException {
        String res = "Computer Explorations In Signals & Systems Using Matlab, 2 Edition";
        Matcher matcher = BOOK_PATTERN.matcher( BOOK );
        matcher.find();
        assertThat( matcher.group(1), is( res ) );
    }
}
