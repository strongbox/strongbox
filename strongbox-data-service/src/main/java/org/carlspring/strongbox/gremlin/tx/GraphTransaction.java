package org.carlspring.strongbox.gremlin.tx;

import java.util.Iterator;
import java.util.Optional;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.Io;
import org.apache.tinkerpop.gremlin.structure.io.Io.Builder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.opencypher.gremlin.neo4j.ogm.transaction.GremlinTransaction;
import org.springframework.data.neo4j.transaction.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Exposes current thread bound {@link Graph} transaction.
 *
 * @author sbespalov
 */
public class GraphTransaction implements Graph
{
    private static final Class<SessionHolder> sessionHolderClass = SessionHolder.class;
    private static final Class<GremlinTransaction> gremlinTransactionClass = GremlinTransaction.class;

    private final SessionFactory sessionFactory;

    public GraphTransaction(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    public Vertex addVertex(Object... keyValues)
    {
        return getCurrent().addVertex(keyValues);
    }

    public Vertex addVertex(String label)
    {
        return getCurrent().addVertex(label);
    }

    public <C extends GraphComputer> C compute(Class<C> graphComputerClass)
        throws IllegalArgumentException
    {
        return getCurrent().compute(graphComputerClass);
    }

    public GraphComputer compute()
        throws IllegalArgumentException
    {
        return getCurrent().compute();
    }

    public <C extends TraversalSource> C traversal(Class<C> traversalSourceClass)
    {
        return getCurrent().traversal(traversalSourceClass);
    }

    public GraphTraversalSource traversal()
    {
        return getCurrent().traversal();
    }

    public Iterator<Vertex> vertices(Object... vertexIds)
    {
        return getCurrent().vertices(vertexIds);
    }

    public Iterator<Edge> edges(Object... edgeIds)
    {
        return getCurrent().edges(edgeIds);
    }

    public Transaction tx()
    {
        return getCurrent().tx();
    }

    public void close()
        throws Exception
    {
        getCurrent().close();
    }

    public <I extends Io> I io(Builder<I> builder)
    {
        return getCurrent().io(builder);
    }

    public Variables variables()
    {
        return getCurrent().variables();
    }

    public Configuration configuration()
    {
        return getCurrent().configuration();
    }

    public Features features()
    {
        return getCurrent().features();
    }

    private Graph getCurrent()
    {
        return Optional.ofNullable(TransactionSynchronizationManager.getResource(sessionFactory))
                       .map(sessionHolderClass::cast)
                       .map(SessionHolder::getSession)
                       .map(Session::getTransaction)
                       .map(gremlinTransactionClass::cast)
                       .map(GremlinTransaction::getNativeTransaction)
                       .orElseThrow(() -> new IllegalStateException());
    }

}
