package org.carlspring.strongbox;

import org.hamcrest.CustomMatcher;

/**
 * @author Przemyslaw Fusik
 */
public class CustomMatchers
{

    public static <T> org.hamcrest.Matcher<T> equalByToString(T expected)
    {
        return new EqualByToStringMatcher(expected, "expected value = " + expected);
    }

    public static class EqualByToStringMatcher<T>
            extends CustomMatcher<T>
    {

        private T expected;

        public EqualByToStringMatcher(T expected,
                                      String description)
        {
            super(description);
            this.expected = expected;
        }

        @Override
        public boolean matches(Object item)
        {
            return item.toString().equals(expected.toString());
        }
    }

}
