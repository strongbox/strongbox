package org.carlspring.strongbox.test.service;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.carlspring.strongbox.config.gremlin.dsl.EntityTraversalSource;
import org.carlspring.strongbox.config.gremlin.graph.OrientGraphFactory;
import org.carlspring.strongbox.test.domain.FooEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class TransactionalTestService
{

    private static final String VERTEX_LABEL = "TransactionPropagationTestVertex";

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Long countVertices()
    {
        return traversal().V().hasLabel(VERTEX_LABEL).count().next();
    }

    @Transactional
    public Object createVertexWithCommit()
    {
        GraphTraversalSource t = traversal();

        return t.addV(VERTEX_LABEL).next().id();
    }

    private EntityTraversalSource traversal()
    {
        OrientGraph g = OrientGraphFactory.getGraph();

        return g.traversal(EntityTraversalSource.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object createVertexWithNesteedCommit()
    {
        return createVertexWithCommit();
    }

    @Transactional
    public Object createVerticesWithException()
    {
        createVertexWithCommit();
        createVertexWithNesteedCommit();

        throw new RuntimeException();
    }

    @Transactional
    public FooEntity createObjectWithCommit()
    {
        FooEntity entity = new FooEntity();
        entity.setName("fooEntity1");
        String uuid = UUID.randomUUID().toString();
        entity.setUuid(uuid);

        OObjectDatabaseTx db = (OObjectDatabaseTx) em.getDelegate();
        
        return db.save(entity);
    }
    
    @Transactional
    public FooEntity updateWithCommit(FooEntity entity)
    {
        OObjectDatabaseTx db = (OObjectDatabaseTx) em.getDelegate();
        return db.save(entity);

//        OObjectDatabaseTx db = (OObjectDatabaseTx) em.getDelegate();
//        OResultSet query = db.query("select * from FooEntity where uuid=?", entity.getUuid());
//
//        OResult item = query.next();
//        OIdentifiable doc = item.toElement();
//
//        return (FooEntity) db.getUserObjectByRecord(doc, null);
    }

    @Transactional
    public FooEntity findById(String uuid)
    {
        OObjectDatabaseTx db = (OObjectDatabaseTx) em.getDelegate();
        OResultSet query = db.query("select * from FooEntity where uuid=?", uuid);

        OResult item = query.next();
        OIdentifiable doc = item.toElement();

        return (FooEntity) db.getUserObjectByRecord(doc, null);
    }
    
    @Transactional
    public Object createObjectWithException()
    {
        createObjectWithCommit();

        throw new RuntimeException();
    }

    @Transactional
    public Long countObjects()
    {
        OObjectDatabaseTx db = (OObjectDatabaseTx) em.getDelegate();

        return db.countClass(FooEntity.class);
    }

}
