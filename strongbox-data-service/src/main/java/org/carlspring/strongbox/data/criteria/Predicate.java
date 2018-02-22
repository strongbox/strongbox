package org.carlspring.strongbox.data.criteria;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * This class is part of Criteria API which is needed to have platform
 * independent query representation.
 * Each {@link Predicate} is search conditions tree node, also have an logical
 * {@link Expression} and nested nodes joined with {@link BooleanOperator}.
 * 
 * @author sbespalov
 *
 */
public class Predicate
{

    private Expression expression;

    private BooleanOperator operator;

    private List<Predicate> childPredicateList = new ArrayList<>();

    public Predicate()
    {
        super();
    }

    public Expression getExpression()
    {
        return expression;
    }

    public BooleanOperator getOperator()
    {
        return operator;
    }

    public List<Predicate> getChildPredicateList()
    {
        return childPredicateList;
    }

    public Predicate or(Predicate p)
    {
        Assert.state(!BooleanOperator.AND.equals(this.operator), "Only disjunction allowed.");

        this.operator = BooleanOperator.OR;
        add(p);

        return this;
    }

    public Predicate and(Predicate p)
    {
        Assert.state(!BooleanOperator.OR.equals(this.operator), "Only conjunction allowed.");

        this.operator = BooleanOperator.AND;
        add(p);

        return this;
    }

    private void add(Predicate p)
    {
        if (p == this)
        {
            return;
        }
        this.childPredicateList.add(p);
    }

    public boolean isEmpty()
    {
        return expression == null && childPredicateList.isEmpty();
    }

    public static Predicate empty()
    {
        return new Predicate();
    }

    public static Predicate of(Expression e)
    {
        Predicate p = new Predicate();
        p.expression = e;
        return p;
    }

    public static enum BooleanOperator
    {
        AND, OR
    }

}
