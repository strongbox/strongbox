package org.carlspring.strongbox.services;

import java.util.ArrayList;
import java.util.List;

public class ArtifactStoreResult
{

    private long size;

    private List<Runnable> events = new ArrayList<>();

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public List<Runnable> getEvents()
    {
        return events;
    }

    public void setEvents(List<Runnable> events)
    {
        this.events = events;
    }

}
