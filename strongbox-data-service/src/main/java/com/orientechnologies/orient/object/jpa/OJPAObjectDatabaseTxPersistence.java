package com.orientechnologies.orient.object.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.object.jpa.parsing.PersistenceXmlUtil;
import static com.orientechnologies.orient.core.entity.OEntityManager.getEntityManagerByDatabaseURL;
import static com.orientechnologies.orient.object.jpa.parsing.PersistenceXmlUtil.PERSISTENCE_XML;

/**
 * @author Sergey Bespalov
 *
 */
public class OJPAObjectDatabaseTxPersistence
        implements PersistenceProvider
{

    /** the log used by this class. */
    private static Logger logger = Logger.getLogger(OJPAObjectDatabaseTxPersistence.class.getName());
    private static OJPAProviderUtil providerUtil = new OJPAProviderUtil();

    private Collection<? extends PersistenceUnitInfo> persistenceUnits = null;

    public OJPAObjectDatabaseTxPersistence()
    {
        URL persistenceXml = Thread.currentThread().getContextClassLoader().getResource(PERSISTENCE_XML);
        try
        {
            persistenceUnits = PersistenceXmlUtil.parse(persistenceXml);
        }
        catch (Exception e)
        {
            logger.log(Level.INFO, "Cannot parse '" + PERSISTENCE_XML + "' :" + e.getMessage(), e);
        }
    }

    @Override
    public synchronized EntityManagerFactory createEntityManagerFactory(String emName,
                                                                        Map map)
    {
        if (emName == null)
        {
            throw new IllegalStateException("Name of the persistence unit should not be null");
        }

        PersistenceUnitInfo unitInfo = PersistenceXmlUtil.findPersistenceUnit(emName, persistenceUnits);
        return createContainerEntityManagerFactory(unitInfo, map);
    }

    @SuppressWarnings("unchecked")
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

        return new OJPAPartitionedEntityManagerPool(properties);
    }

    @Override
    public ProviderUtil getProviderUtil()
    {
        return providerUtil;
    }

}
