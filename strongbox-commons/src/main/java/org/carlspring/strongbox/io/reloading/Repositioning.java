package org.carlspring.strongbox.io.reloading;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface Repositioning
{

    /**
     * Reposition automatically based on the list of byte ranges.
     *
     * @throws IOException
     */
    void reposition() throws IOException;

    /**
     * Reposition manually.
     *
     * @param skipBytes
     * @throws IOException
     */
    void reposition(long skipBytes) throws IOException;

    boolean hasMoreByteRanges();

}
