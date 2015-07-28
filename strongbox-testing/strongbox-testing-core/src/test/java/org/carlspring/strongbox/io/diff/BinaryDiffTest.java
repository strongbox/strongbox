package org.carlspring.strongbox.io.diff;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class BinaryDiffTest
{


    @Test
    public void testNoDifferences()
    {
        ByteArrayInputStream bais1 = new ByteArrayInputStream("This is a test".getBytes());
        ByteArrayInputStream bais2 = new ByteArrayInputStream("This is a test".getBytes());

        BinaryDiff diff = new BinaryDiff(bais1, bais2);
        assertFalse("Reported differences where no such exit!", diff.diff());
    }

    @Test
    public void testDifferences()
    {
        ByteArrayInputStream bais1 = new ByteArrayInputStream("This is a test.".getBytes());
        ByteArrayInputStream bais2 = new ByteArrayInputStream("This is another test.".getBytes());

        BinaryDiff diff = new BinaryDiff(bais1, bais2);
        assertTrue("Reported no differences where such exit!", diff.diff());
    }

}
