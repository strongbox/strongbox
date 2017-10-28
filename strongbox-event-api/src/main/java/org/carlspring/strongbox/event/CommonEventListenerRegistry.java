package org.carlspring.strongbox.event;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonEventListenerRegistry extends AbstractEventListenerRegistry
{

    @Autowired(required = false)
    private List<CommonEventListener<?>> eventListenerList = new ArrayList<>();

    @PostConstruct
    public void init()
    {
        eventListenerList.forEach(this::addListener);
    }

}
