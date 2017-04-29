package org.carlspring.strongbox.utils;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.http.range.ByteRangeHeaderParser;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;

public class ArtifactControllerHelper
{

    public static final String HEADER_NAME_RANGE = "Range";
    private static final Logger logger = LoggerFactory.getLogger(ArtifactControllerHelper.class);

    private ArtifactControllerHelper()
    {

    }

    public static void handlePartialDownload(ArtifactInputStream is,
                                             HttpHeaders headers,
                                             HttpServletResponse response)
            throws IOException
    {
        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headers.getFirst(HEADER_NAME_RANGE));
        List<ByteRange> ranges = parser.getRanges();
        if (ranges.size() == 1)
        {
            logger.debug("Received request for a partial download with a single range.");
            handlePartialDownloadWithSingleRange(is, (ByteRange) ranges.get(0), response);
        }
        else
        {
            logger.debug("Received request for a partial download with multiple ranges.");
            handlePartialDownloadWithMultipleRanges(is, ranges, response);
        }
    }

    public static void handlePartialDownloadWithSingleRange(ArtifactInputStream is,
                                                            ByteRange byteRange,
                                                            HttpServletResponse response)
            throws IOException
    {

        long length = StreamUtils.getLength(is);
        if (byteRange.getOffset() < length)
        {
            long partialLength = calculatePartialRangeLength(byteRange, length);

            logger.debug("Calculated partial range length ->>> " + partialLength + "\n");

            StreamUtils.setCurrentByteRange(is, byteRange);

            response.setHeader("Content-Length", partialLength + "");
            response.setStatus(PARTIAL_CONTENT.value());

            prepareResponseBuilderForPartialRequest(byteRange, length, response);
        }
        else
        {
            response.setStatus(REQUESTED_RANGE_NOT_SATISFIABLE.value());
        }
    }

    public static void handlePartialDownloadWithMultipleRanges(ArtifactInputStream is,
                                                               List<ByteRange> byteRanges,
                                                               HttpServletResponse response)
            throws IOException
    {
        // XXX: this is not implemented yet
        response.setStatus(REQUESTED_RANGE_NOT_SATISFIABLE.value());
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

    public static void prepareResponseBuilderForPartialRequest(ByteRange br,
                                                               long length,
                                                               HttpServletResponse response)
    {
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range",
                           "bytes " + br.getOffset() + "-" + (length - 1L) + "/" + length);

        logger.debug("Content-Range HEADER ->>> " + response.getHeader("Content-Range"));
        response.setHeader("Pragma", "no-cache");
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
                    headers.getFirst(HEADER_NAME_RANGE) != null ? headers.getFirst(HEADER_NAME_RANGE) : null;
            return contentRange != null && !"0/*".equals(contentRange) && !"0-".equals(contentRange) &&
                   !"0".equals(contentRange);
        }
    }

    public static void setHeadersForChecksums(ArtifactInputStream ais,
                                              HttpServletResponse response)
    {
        ais.getHexDigests()
           .forEach((k,
                     v) -> response.setHeader(String.format("Checksum-%s",
                                                            k.toUpperCase()
                                                             .replaceAll("-", "")),
                                              v));

    }

    public static void setHeadersForChecksums(ArtifactInputStream ais,
                                              HttpHeaders headers)
    {
        ais.getHexDigests()
           .forEach((k, v) -> headers.add(String.format("Checksum-%s", k.toUpperCase()
                                                                        .replaceAll("-", "")), v));

        ArtifactCoordinates artifactCoordinates = ais.getArtifactCoordinates();
        if (artifactCoordinates != null)
        {
            headers.add("strongbox-layout", artifactCoordinates.getClass()
                                                               .getSimpleName());
        }
    }

}
