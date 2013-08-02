package org.carlspring.repositoryunit.io;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class RandomInputStreamTest
{

    
    @Test
    public void testIO()
            throws IOException
    {
        RandomInputStream ris = new RandomInputStream(10000);
        
        int total = 0;
        int len;
        int size = 4096;
        byte[] bytes = new byte[size];

        while ((len = ris.read(bytes, 0, size)) != -1)
        {
            total += len;
        }
        
        assertEquals("The number of read bytes do not match the defined size!", 10000, total);
    }

}
