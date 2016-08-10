package org.carlspring.strongbox.rest;

/**
 * Created by yury on 8/8/16.
 */

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.http.range.ByteRangeHeaderParser;
import org.carlspring.commons.io.ByteRangeInputStream;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class ByteRangeRequestHandler
{

    private static final Logger logger = LoggerFactory.getLogger(
            org.carlspring.commons.http.range.ByteRangeRequestHandler.class);

    public ByteRangeRequestHandler()
    {
    }

    public static ResponseEntity handlePartialDownload(ByteRangeInputStream bris,
                                                       HttpHeaders headers)
            throws IOException
    {
        ByteRangeHeaderParser parser = new ByteRangeHeaderParser((String) headers.getFirst("Range"));
        List ranges = parser.getRanges();
        if (ranges.size() == 1)
        {
            logger.debug("Received request for a partial download with a single range.");
            return handlePartialDownloadWithSingleRange(bris, (ByteRange) ranges.get(0));
        }
        else
        {
            logger.debug("Received request for a partial download with multiple ranges.");
            return handlePartialDownloadWithMultipleRanges(bris, ranges);
        }
    }

    public static ResponseEntity handlePartialDownloadWithSingleRange(ByteRangeInputStream bris,
                                                                      ByteRange byteRange)
            throws IOException
    {
        if (byteRange.getOffset() < bris.getLength())
        {
            bris.setCurrentByteRange(byteRange);
            bris.skip(byteRange.getOffset());
            MultiValueMap<String, Long> responseHeaders = new LinkedMultiValueMap<String, Long>();
            responseHeaders.set("Content-Length",
                                Long.valueOf(calculatePartialRangeLength(byteRange, bris.getLength())));
            ResponseEntity responseBuilder = new ResponseEntity(prepareResponseBuilderForPartialRequest(bris),
                                                                responseHeaders, HttpStatus.PARTIAL_CONTENT);
            return responseBuilder;
        }
        else
        {
            return new ResponseEntity(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
        }
    }

    public static ResponseEntity handlePartialDownloadWithMultipleRanges(ByteRangeInputStream bris,
                                                                         List<ByteRange> byteRanges)
            throws IOException
    {
        if (bris.getCurrentByteRange().getOffset() >= bris.getLength())
        {
            ResponseEntity responseBuilder = prepareResponseBuilderForPartialRequest(bris);
            return responseBuilder;
        }
        else
        {
            return new ResponseEntity(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
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

    public static ResponseEntity prepareResponseBuilderForPartialRequest(ByteRangeInputStream bris)
    {
        MultiValueMap<String, String> responseHeaders = new LinkedMultiValueMap<String, String>();
        responseHeaders.set("Accept-Ranges", "bytes");
        responseHeaders.set("Content-Range",
                            "bytes " + bris.getCurrentByteRange().getOffset() + "-" + (bris.getLength() - 1L) + "/" +
                            bris.getLength());
        responseHeaders.set("Pragma", "no-cache");
        ResponseEntity responseBuilder = new ResponseEntity(bris, responseHeaders, HttpStatus.PARTIAL_CONTENT);
        return responseBuilder;
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
                    headers != null && headers.getFirst("Range") != null ? (String) headers.getFirst("Range") : null;
            return contentRange != null && !contentRange.equals("0/*") && !contentRange.equals("0-") &&
                   !contentRange.equals("0");
        }
    }
}
