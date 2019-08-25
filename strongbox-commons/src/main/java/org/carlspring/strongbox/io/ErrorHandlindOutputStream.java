/**
 * 
 */
package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.OutputStream;
//import org.eclipse.jetty.io.EofException;

import org.apache.commons.io.output.ProxyOutputStream;

/**
 * @author Bogdan Sukonnov
 *
 */
public class ErrorHandlindOutputStream extends ProxyOutputStream
{
    
    /**
     * @param proxy
     */
    public ErrorHandlindOutputStream(OutputStream proxy)
    {
        super(proxy);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void handleIOException(IOException e)
        throws IOException
    {
        System.out.println(e + " --- " + e.getClass());
        //logger.debug(e + " --- " + e.getClass());
        
        //if (e.getClass().equals(org.eclipse.jetty.io.EofException.class)) {
            
        //}
    }

}
