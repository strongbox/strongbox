package org.carlspring.strongbox.testing;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class JUnitHelper extends TestWatcher
{

    private Class<?> testClass;

    @Override
    protected void starting(Description description)
    {
        super.starting(description);
        testClass = description.getTestClass();
    }

    protected Class<?> getTestClass()
    {
        return testClass;
    }

}
