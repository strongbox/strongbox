package org.carlspring.strongbox.rest;

public interface UnsafeCommand<T>
{

    T execute()
            throws Exception;
}