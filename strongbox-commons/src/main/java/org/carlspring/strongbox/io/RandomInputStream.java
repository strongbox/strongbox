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

    private int length;

    private Random random = new Random();


    public RandomInputStream(int length)
    {
        super();
        this.length = length;
    }

    public RandomInputStream(boolean randomSize, int sizeLimit)
    {
        super();
        if (randomSize)
        {
            this.length = getRandomSize(sizeLimit);
        }
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

    public int getRandomSize(int max)
    {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive

        return random.nextInt(max + 1);
    }

    public long getCount()
    {
        return count;
    }

    public void setCount(long count)
    {
        this.count = count;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

}