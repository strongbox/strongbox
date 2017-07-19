package org.carlspring.strongbox;

import org.hamcrest.CustomMatcher;

/**
 * @author Przemyslaw Fusik
 */
public class CustomMatchers
{

    public static org.hamcrest.Matcher<Number> eqByNumberOrToString(Number expected)
    {
        return new EqByNumberOrToStringMatcher(expected, "expected value = " + expected);
    }

    public static class EqByNumberOrToStringMatcher
            extends CustomMatcher<Number>
    {

        private Number expected;

        public EqByNumberOrToStringMatcher(Number expected,
                                           String description)
        {
            super(description);
            this.expected = expected;
        }

        @Override
        public boolean matches(Object item)
        {
            return (item instanceof Number) ? item.equals(expected) : item.toString().equals(expected.toString());
        }
    }

}
