package org.carlspring.strongbox.test.service;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

public class IdBlockQueueTestService
{

    @Inject
    private TransactionalTestService testService;

    @Transactional("gtmtTransactionManagerFirst")
    public Long createWithGtmtTransactionManagerFirst()
    {
        return testService.createVertexWithCommit();
    }

    @Transactional("gtmtTransactionManagerSecond")
    public Long createWithgtmtTransactionManagerSecond()
    {
        return testService.createVertexWithCommit();
    }

}
