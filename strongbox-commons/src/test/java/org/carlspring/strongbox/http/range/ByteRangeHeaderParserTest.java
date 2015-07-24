package org.carlspring.strongbox.http.range;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author mtodorov
 */
public class ByteRangeHeaderParserTest
{


    @Test
    public void testParsingWithOffsetOnly()
            throws Exception
    {
        String headerContents = "bytes=500-";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        assertFalse(ranges.isEmpty());
        assertEquals("Parsed incorrect number of ranges!", 1, ranges.size());

        ByteRange range = ranges.get(0);

        assertEquals("Parsed an incorrect offset value!", 500, range.getOffset());
        assertEquals("Parsed an incorrect end value!", 0, range.getLimit());

        assertEquals("bytes=500-", range.toString());
    }

    @Test
    public void testParsingWithEndOnly()
            throws Exception
    {
        String headerContents = "bytes=-500";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        assertFalse(ranges.isEmpty());
        assertEquals("Parsed incorrect number of ranges!", 1, ranges.size());

        long totalLength = 1001L;
        ByteRange range = ranges.get(0);
        range.setTotalLength(totalLength);

        assertEquals("Parsed an incorrect offset value!", 0, range.getOffset());
        assertEquals("Parsed an incorrect end value!", -500, range.getLimit());

        assertEquals("bytes=500-1000/1001", range.toString());
    }

    @Test
    public void testParsingWithOffsetAndEnd()
            throws Exception
    {
        String headerContents = "bytes=500-1000";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        assertFalse(ranges.isEmpty());
        assertEquals("Parsed incorrect number of ranges!", 1, ranges.size());

        long totalLength = 1001L;
        ByteRange range = ranges.get(0);
        range.setTotalLength(totalLength);

        assertEquals("Parsed an incorrect offset value!", 500, range.getOffset());
        assertEquals("Parsed an incorrect end value!", 1000, range.getLimit());

        assertEquals("bytes=500-1000/1001", range.toString());
    }

    @Test
    public void testToStringWithWildcardLength1()
    {
        String headerContents = "bytes=500-1000/*";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        ByteRange range = ranges.get(0);

        assertEquals("Failed to parse offset!", 500, range.getOffset());
        assertEquals("Failed to parse end!", 1000, range.getLimit());
        assertEquals("Failed to parse length!", 0, range.getTotalLength());
    }

    @Test
    public void testToStringWithWildcardLength2()
    {
        String headerContents = "bytes=500/*";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        ByteRange range = ranges.get(0);

        assertEquals("Failed to parse offset!", 500, range.getOffset());
        assertEquals("Failed to parse length!", 0, range.getTotalLength());
    }

    @Test
    public void testToStringWithWildcardLength3()
    {
        String headerContents = "bytes=-500/*";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        ByteRange range = ranges.get(0);

        assertEquals("Failed to parse offset!", 0, range.getOffset());
        assertEquals("Failed to parse limit!", -500, range.getLimit());
        assertEquals("Failed to parse length!", 0, range.getTotalLength());
    }

}
