package org.carlspring.strongbox.data.criteria;

import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * @author sbespalov
 *
 * @param <T>
 */
public interface ExpressionDialect
{

    <T extends GenericEntity> String parseProperty(String attribute);

    ExpOperator parseOperator(String operator);

    String parseValue(String value);

}
