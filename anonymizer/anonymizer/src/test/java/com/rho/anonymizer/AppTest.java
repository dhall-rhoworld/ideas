package com.rho.anonymizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    public void testDateRegex() {
    	String datum = "Mon Feb 06 19:00:00 EST 2012";
    	assertTrue(datum.matches("^[A-Za-z]{3} [A-Za-z]{3} [0-9]{2} .*"));
    	//assertTrue(datum.matches("Mon.*"));
    	
    	Pattern pattern = Pattern.compile("[0-9]{4}$");
    	Matcher matcher = pattern.matcher(datum);
    	assertTrue(matcher.find());
    	System.out.println(datum.substring(matcher.start(), matcher.end()));
    }
}
