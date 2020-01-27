package org.carlspring.strongbox.config.gremlin.graph;

import java.util.Optional;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphBaseFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OTransactionException;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * @author sbespalov
 */
public class OrientGraphFactory implements OrientGraphBaseFactory
{

    private final ODatabasePool databasePool;

    private static final ThreadLocal<OrientGraph> graphHolder = new ThreadLocal<>();

    public OrientGraphFactory(ODatabasePool databasePool)
    {
        this.databasePool = databasePool;
    }

    public static OrientGraph getGraph()
    {
        OrientGraph result = graphHolder.get();
        if (result == null)
        {
            throw new IllegalStateException("Тo thread bound transaction found.");
        }
        return result;
    }

    @Override
    public OrientGraph getTx()
    {
        OrientGraph graph = new OrientGraph(this, Optional.of(new BaseConfiguration())
                                                          .map(OrientGraphFactory::commonConfiguration)
                                                          .map(c -> {
                                                              c.setProperty(OrientGraph.CONFIG_TRANSACTIONAL, true);
                                                              return c;
                                                          })
                                                          .get());
        // TODO: we need `org.apache.tinkerpop.gremlin.structure.Graph` proxy
        // here which will allow to manage `ThreadLocal` within transaction
        graphHolder.set(graph);
        return graph;
    }

    @Override
    public OrientGraph getNoTx()
    {
        return new OrientGraph(this, Optional.of(new BaseConfiguration())
                                             .map(OrientGraphFactory::commonConfiguration)
                                             .map(c -> {
                                                 c.setProperty(OrientGraph.CONFIG_TRANSACTIONAL, false);
                                                 return c;
                                             })
                                             .get());
    }

    public ODatabaseDocument getDatabase(boolean create,
                                         boolean open)
    {
        ODatabaseDocumentInternal db = (ODatabaseDocumentInternal) databasePool.acquire();
        return new ODatabaseSessionDelegate(new OObjectDatabaseTx(db))
        {

            @Override
            public ODatabase<ORecord> commit()
                throws OTransactionException
            {
                try
                {
                    return super.commit();
                }
                finally
                {
                    graphHolder.remove();
                }
            }

            @Override
            public ODatabase<ORecord> rollback()
                throws OTransactionException
            {
                try
                {
                    return super.rollback();
                }
                finally
                {
                    graphHolder.remove();
                }
            }

        };
    }

    private static Configuration commonConfiguration(Configuration c)
    {
        c.setProperty(Graph.GRAPH, OrientGraph.class.getName());
        c.setProperty(OrientGraph.CONFIG_CREATE, false);
        c.setProperty(OrientGraph.CONFIG_OPEN, false);
        c.setProperty(OrientGraph.CONFIG_LABEL_AS_CLASSNAME, true);

        return c;
    };
}
