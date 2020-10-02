package org.carlspring.strongbox.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class CronTaskContextAcceptFilter extends Filter<ILoggingEvent>
{

    public static final String STRONGBOX_CRON_CONTEXT_NAME = "strongbox-cron-context-name";

    @Override
    public FilterReply decide(ILoggingEvent event)
    {     // validating if map contains the key strongbox-cron-context-name
        if (event.getMDCPropertyMap().containsKey(STRONGBOX_CRON_CONTEXT_NAME))
        {
            return FilterReply.NEUTRAL;
        }
        ;
        return FilterReply.DENY;
    }

}
