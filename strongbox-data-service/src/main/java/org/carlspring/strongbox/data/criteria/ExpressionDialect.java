package org.carlspring.strongbox.data.criteria;

import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.domain.DomainObject;

/**
 * @author sbespalov
 *
 * @param <T>
 */
public interface ExpressionDialect
{

    <T extends DomainObject> String parseProperty(String attribute);

    ExpOperator parseOperator(String operator);

    String parseValue(String value);

}
