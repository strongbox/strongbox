package org.carlspring.strongbox.http.range;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public class ByteRangeHeaderParser
{

    private String headerContents;


    public ByteRangeHeaderParser(String headerContents)
    {
        this.headerContents = headerContents;
    }

    /**
     * Returns the list of ranges denoted by the "Range:" header.
     *
     * @return
     */
    public List<ByteRange> getRanges()
    {
        List<ByteRange> byteRanges = new ArrayList<>();

        String[] ranges = headerContents.substring(headerContents.lastIndexOf('=') + 1,
                                                   headerContents.length()).split(",");

        for (String range : ranges)
        {
            long length = range.contains("/") && !range.endsWith("/*") ?
                          Long.parseLong(range.substring(range.lastIndexOf("/") + 1, range.length())) :  0;

            ByteRange byteRange = null;

            range = range.contains("/") ? range.substring(0, range.indexOf("/")) : range;

            if (range.endsWith("-"))
            {
                // Example: 1000- ; Read all bytes after 1000
                byteRange = new ByteRange(Long.parseLong(range.substring(0, range.length() - 1)));
            }
            else if (range.contains("-") && !range.startsWith("-") && !range.endsWith("-"))
            {
                // Example: 1000-2000 ; Read bytes 1000-2000 (incl.)
                String[] rangeElements = range.split("-");

                byteRange = new ByteRange(Long.parseLong(rangeElements[0]), Long.parseLong(rangeElements[1]));
            }
            else if (range.startsWith("-") && range.lastIndexOf("-") == 0)
            {
                // Example: -2000 ; Read the last 2000 bytes.
                byteRange = new ByteRange(0, Long.parseLong(range));
            }
            else if (range.endsWith("-") && range.split("-").length == 1)
            {
                // Example: 2000- ; Read after the first 2000 bytes.
                byteRange = new ByteRange(Long.parseLong(range.substring(0, range.length() - 1)));
            }
            else if (!range.contains("-") || (range.startsWith("-") && range.split("-").length == 1))
            {
                long l = Long.parseLong(range);

                // Example: 2000 ; Read after the first 2000 bytes.
                byteRange = l > 0 ? new ByteRange(l) : new ByteRange(0, l);
            }

            if (byteRange != null)
            {
                byteRange.setTotalLength(length);

                byteRanges.add(byteRange);
            }
            // TODO: The byteRange should never really be null.
            // TODO: Might want to consider throwing an exception here.
        }

        return byteRanges;
    }

}
