package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * @author mtodorov
 */
public class RandomInputStream
        extends InputStream
{

    private long count;

    private long length;

    private Random random = new Random();


    public RandomInputStream(long length)
    {
        super();
        this.length = length;
    }

    @Override
    public int read()
            throws IOException
    {
        if (count >= length)
        {
            return -1;
        }

        count++;

        return random.nextInt();
    }

    public long getCount()
    {
        return count;
    }

    public void setCount(long count)
    {
        this.count = count;
    }

    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

}