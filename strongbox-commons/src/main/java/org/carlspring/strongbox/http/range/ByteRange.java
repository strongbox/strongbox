package org.carlspring.strongbox.http.range;

/**
 * @author mtodorov
 */
public class ByteRange
{

    private long offset = 0L;

    private long limit = 0L;

    private long totalLength = 0L;


    public ByteRange()
    {
    }

    public ByteRange(long offset)
    {
        this.offset = offset;
    }

    public ByteRange(long offset, long limit)
    {
        this.offset = offset;
        this.limit = limit;
    }

    public long getOffset()
    {
        return offset;
    }

    public void setOffset(long offset)
    {
        this.offset = offset;
    }

    public long getLimit()
    {
        return limit;
    }

    public void setLimit(long limit)
    {
        this.limit = limit;
    }

    public long getTotalLength()
    {
        return totalLength;
    }

    public void setTotalLength(long totalLength)
    {
        this.totalLength = totalLength;
    }

    @Override
    public String toString()
    {
        final String prefix = "bytes=";

        if (offset == 0 && limit < 0)
        {
            if (totalLength == 0)
            {
                return prefix + (limit > 0 ? limit + "-" : "foo");
            }
            else
            {
                return prefix + (totalLength + limit - 1) + "-" + (totalLength - 1) + "/" + totalLength;
            }
        }
        else if (offset > 0 && limit == 0)
        {
            return prefix + (totalLength > 0 ? "-" + totalLength : offset + "-");
        }
        else
        {
            return prefix + offset + (limit > 0 ? "-" + limit : "") + (totalLength > 0 ? "/" + totalLength : "");
        }
    }
}
