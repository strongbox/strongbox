package com.orientechnologies.orient.object.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import java.util.Map;
import java.util.Properties;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.jdbc.OrientDataSource;
import static com.orientechnologies.orient.core.entity.OEntityManager.getEntityManagerByDatabaseURL;

/**
 * @author Sergey Bespalov
 * @author Przemyslaw Fusik
 */
public class OJPAObjectDatabaseTxPersistenceProvider
        implements PersistenceProvider
{

    public static final String PROPERTY_AUTOMATIC_SCHEMA_GENERATION = "com.orientechnologies.orient.object.jpa.automaticSchemaGeneration";

    private static OJPAProviderUtil providerUtil = new OJPAProviderUtil();

    @Override
    public synchronized EntityManagerFactory createEntityManagerFactory(String emName,
                                                                        Map map)
    {
        throw new UnsupportedOperationException("createEntityManagerFactory");
    }

    @Override
    public synchronized EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info,
                                                                                 Map map)
    {

        Properties sourceProperties = info.getProperties();
        OJPAProperties properties = sourceProperties instanceof OJPAProperties ? (OJPAProperties) sourceProperties
                                                                               : new OJPAProperties();

        if (sourceProperties != null && !sourceProperties.equals(properties))
        {
            properties.putAll(sourceProperties);
        }

        // Override parsed properties with user specified
        if (map != null && !map.isEmpty())
        {
            properties.putAll(map);
        }

        // register entities from <class> tag
        OEntityManager entityManager = getEntityManagerByDatabaseURL(properties.getURL());
        entityManager.registerEntityClasses(info.getManagedClassNames());

        OrientDataSource dataSource = (OrientDataSource) info.getNonJtaDataSource();

        return new OJPAObjectDatabaseTxEntityManagerFactory(properties, dataSource);
    }

    @Override
    public void generateSchema(PersistenceUnitInfo info,
                               Map map)
    {
        throw new UnsupportedOperationException("generateSchema");
    }

    @Override
    public boolean generateSchema(String persistenceUnitName,
                                  Map map)
    {
        throw new UnsupportedOperationException("generateSchema");
    }

    @Override
    public ProviderUtil getProviderUtil()
    {
        return providerUtil;
    }

}
