package org.carlspring.strongbox.utils;

/**
 * Created by yury on 8/8/16.
 */

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.http.range.ByteRangeHeaderParser;
import org.carlspring.commons.io.ByteRangeInputStream;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;

public class ByteRangeRequestHandler
{

    private static final Logger logger = LoggerFactory.getLogger(
            org.carlspring.commons.http.range.ByteRangeRequestHandler.class);

    public ByteRangeRequestHandler()
    {
    }

    public static ByteRangeInputStream handlePartialDownload(ByteRangeInputStream bris,
                                                             HttpHeaders headers,
                                                             HttpServletResponse response)
            throws IOException
    {
        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headers.getFirst("Range"));
        List ranges = parser.getRanges();
        if (ranges.size() == 1)
        {
            logger.debug("Received request for a partial download with a single range.");
            return handlePartialDownloadWithSingleRange(bris, (ByteRange) ranges.get(0), response);
        }
        else
        {
            logger.debug("Received request for a partial download with multiple ranges.");
            return handlePartialDownloadWithMultipleRanges(bris, ranges, response);
        }
    }

    public static ByteRangeInputStream handlePartialDownloadWithSingleRange(ByteRangeInputStream bris,
                                                                            ByteRange byteRange,
                                                                            HttpServletResponse response)
            throws IOException
    {


        if (byteRange.getOffset() < bris.getLength())
        {
            long partialLength = calculatePartialRangeLength(byteRange, bris.getLength());
            System.out.println("\nCalculated partial range length ->>> " + partialLength + "\n");

            bris.setCurrentByteRange(byteRange);
            bris.skip(byteRange.getOffset());
            response.setHeader("Content-Length", partialLength + "");
            response.setStatus(PARTIAL_CONTENT.value());
            return prepareResponseBuilderForPartialRequest(bris, response);
        }
        else
        {
            response.setStatus(REQUESTED_RANGE_NOT_SATISFIABLE.value());
            return bris;
        }
    }

    public static ByteRangeInputStream handlePartialDownloadWithMultipleRanges(ByteRangeInputStream bris,
                                                                               List<ByteRange> byteRanges,
                                                                               HttpServletResponse response)
            throws IOException
    {
        if (bris.getCurrentByteRange().getOffset() >= bris.getLength())
        {
            return prepareResponseBuilderForPartialRequest(bris, response);
        }
        else
        {
            response.setStatus(REQUESTED_RANGE_NOT_SATISFIABLE.value());
            return bris;
        }
    }

    public static long calculatePartialRangeLength(ByteRange byteRange,
                                                   long length)
    {
        if (byteRange.getLimit() > 0L && byteRange.getOffset() > 0L)
        {
            logger.debug("Partial content byteRange.getOffset: " + byteRange.getOffset());
            logger.debug("Partial content byteRange.getLimit: " + byteRange.getLimit());
            logger.debug("Partial content length: " + (byteRange.getLimit() - byteRange.getOffset()));
            return byteRange.getLimit() - byteRange.getOffset();
        }
        else if (length > 0L && byteRange.getOffset() > 0L && byteRange.getLimit() == 0L)
        {
            logger.debug("Partial content length: " + (length - byteRange.getOffset()));
            return length - byteRange.getOffset();
        }
        else
        {
            return -1L;
        }
    }

    public static ByteRangeInputStream prepareResponseBuilderForPartialRequest(ByteRangeInputStream bris,
                                                                               HttpServletResponse response)
    {
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range",
                           "bytes " + bris.getCurrentByteRange().getOffset() + "-" + (bris.getLength() - 1L) + "/" +
                           bris.getLength());
        System.out.println("Content-Range HEADER ->>> " + response.getHeader("Content-Range"));
        response.setHeader("Pragma", "no-cache");
        return bris;
    }

    public static boolean isRangedRequest(HttpHeaders headers)
    {
        if (headers == null)
        {
            return false;
        }
        else
        {
            String contentRange =
                    headers.getFirst("Range") != null ? headers.getFirst("Range") : null;
            return contentRange != null && !contentRange.equals("0/*") && !contentRange.equals("0-") &&
                   !contentRange.equals("0");
        }
    }
}
