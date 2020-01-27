package org.carlspring.strongbox.config.gremlin.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.cache.OLocalRecordCache;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.script.OCommandScriptException;
import com.orientechnologies.orient.core.config.OContextConfiguration;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.conflict.ORecordConflictStrategy;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.ODatabaseListener;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OLiveQueryMonitor;
import com.orientechnologies.orient.core.db.OLiveQueryResultListener;
import com.orientechnologies.orient.core.db.OSharedContext;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.RecordReader;
import com.orientechnologies.orient.core.db.record.OCurrentStorageComponentsFactory;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.dictionary.ODictionary;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.exception.OTransactionException;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.hook.ORecordHook.HOOK_POSITION;
import com.orientechnologies.orient.core.hook.ORecordHook.RESULT;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.intent.OIntent;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.iterator.ORecordIteratorCluster;
import com.orientechnologies.orient.core.metadata.OMetadataInternal;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OToken;
import com.orientechnologies.orient.core.query.OQuery;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.OBlob;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.executor.OExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.storage.OBasicTransaction;
import com.orientechnologies.orient.core.storage.ORecordCallback;
import com.orientechnologies.orient.core.storage.ORecordMetadata;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.storage.OStorage.LOCKING_STRATEGY;
import com.orientechnologies.orient.core.storage.ridbag.sbtree.OSBTreeCollectionManager;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE;
import com.orientechnologies.orient.core.tx.OTransactionInternal;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * @author sbespalov
 */
public class ODatabaseSessionDelegate implements ODatabaseDocumentInternal
{

    private final OObjectDatabaseTx target;

    public ODatabaseSessionDelegate(OObjectDatabaseTx oObjectDatabaseTx)
    {
        this.target = oObjectDatabaseTx;
    }

    public OStorage getStorage()
    {
        return target.getUnderlying().getStorage();
    }

    public List<String> backup(OutputStream out,
                               Map<String, Object> options,
                               Callable<Object> callable,
                               OCommandOutputListener iListener,
                               int compressionLevel,
                               int bufferSize)
        throws IOException
    {
        return target.getUnderlying().backup(out, options, callable, iListener, compressionLevel, bufferSize);
    }

    public void setUser(OSecurityUser user)
    {
        target.getUnderlying().setUser(user);
    }

    public void replaceStorage(OStorage iNewStorage)
    {
        target.getUnderlying().replaceStorage(iNewStorage);
    }

    public <V> V callInLock(Callable<V> iCallable,
                            boolean iExclusiveLock)
    {
        return target.getUnderlying().callInLock(iCallable, iExclusiveLock);
    }

    public void resetInitialization()
    {
        target.getUnderlying().resetInitialization();
    }

    public ODatabaseInternal<?> getDatabaseOwner()
    {
        return target.getUnderlying().getDatabaseOwner();
    }

    public ORecordIteratorClass<ODocument> browseClass(String iClassName)
    {
        return target.getUnderlying().browseClass(iClassName);
    }

    public ODatabaseInternal<?> setDatabaseOwner(ODatabaseInternal<?> iOwner)
    {
        return target.getUnderlying().setDatabaseOwner(iOwner);
    }

    public <DB extends ODatabase> DB getUnderlying()
    {
        return target.getUnderlying().getUnderlying();
    }

    public OCurrentStorageComponentsFactory getStorageVersions()
    {
        return target.getUnderlying().getStorageVersions();
    }

    public void setInternal(ATTRIBUTES attribute,
                            Object iValue)
    {
        target.getUnderlying().setInternal(attribute, iValue);
    }

    public void restore(InputStream in,
                        Map<String, Object> options,
                        Callable<Object> callable,
                        OCommandOutputListener iListener)
        throws IOException
    {
        target.getUnderlying().restore(in, options, callable, iListener);
    }

    public ORecordIteratorClass<ODocument> browseClass(String iClassName,
                                                       boolean iPolymorphic)
    {
        return target.getUnderlying().browseClass(iClassName, iPolymorphic);
    }

    public <DB extends ODatabase> DB open(OToken iToken)
    {
        return target.getUnderlying().open(iToken);
    }

    public OSBTreeCollectionManager getSbTreeCollectionManager()
    {
        return target.getUnderlying().getSbTreeCollectionManager();
    }

    public OBinarySerializerFactory getSerializerFactory()
    {
        return target.getUnderlying().getSerializerFactory();
    }

    public OSharedContext getSharedContext()
    {
        return target.getUnderlying().getSharedContext();
    }

    public OBasicTransaction getMicroOrRegularTransaction()
    {
        return target.getUnderlying().getMicroOrRegularTransaction();
    }

    public ORecordSerializer getSerializer()
    {
        return target.getUnderlying().getSerializer();
    }

    public void setSerializer(ORecordSerializer serializer)
    {
        target.getUnderlying().setSerializer(serializer);
    }

    public int assignAndCheckCluster(ORecord record,
                                     String iClusterName)
    {
        return target.getUnderlying().assignAndCheckCluster(record, iClusterName);
    }

    public <RET extends ORecord> RET loadIfVersionIsNotLatest(ORID rid,
                                                              int recordVersion,
                                                              String fetchPlan,
                                                              boolean ignoreCache)
        throws ORecordNotFoundException
    {
        return target.getUnderlying().loadIfVersionIsNotLatest(rid, recordVersion, fetchPlan, ignoreCache);
    }

    public <RET> RET newInstance(String iClassName)
    {
        return target.getUnderlying().newInstance(iClassName);
    }

    public String getLocalNodeName()
    {
        return target.getUnderlying().getLocalNodeName();
    }

    public void reloadUser()
    {
        target.getUnderlying().reloadUser();
    }

    public OIdentifiable beforeCreateOperations(OIdentifiable id,
                                                String iClusterName)
    {
        return target.getUnderlying().beforeCreateOperations(id, iClusterName);
    }

    public OBlob newBlob(byte[] bytes)
    {
        return target.getUnderlying().newBlob(bytes);
    }

    public OIdentifiable beforeUpdateOperations(OIdentifiable id,
                                                String iClusterName)
    {
        return target.getUnderlying().beforeUpdateOperations(id, iClusterName);
    }

    public OBlob newBlob()
    {
        return target.getUnderlying().newBlob();
    }

    public Map<String, Set<String>> getActiveClusterMap()
    {
        return target.getUnderlying().getActiveClusterMap();
    }

    public long countClass(String iClassName)
    {
        return target.getUnderlying().countClass(iClassName);
    }

    public void beforeDeleteOperations(OIdentifiable id,
                                       String iClusterName)
    {
        target.getUnderlying().beforeDeleteOperations(id, iClusterName);
    }

    public <DB extends ODatabase> DB open(String iUserName,
                                          String iUserPassword)
    {
        return target.getUnderlying().open(iUserName, iUserPassword);
    }

    public void afterUpdateOperations(OIdentifiable id)
    {
        target.getUnderlying().afterUpdateOperations(id);
    }

    public long countClass(String iClassName,
                           boolean iPolymorphic)
    {
        return target.getUnderlying().countClass(iClassName, iPolymorphic);
    }

    public void afterCreateOperations(OIdentifiable id)
    {
        target.getUnderlying().afterCreateOperations(id);
    }

    public void afterDeleteOperations(OIdentifiable id)
    {
        target.getUnderlying().afterDeleteOperations(id);
    }

    public Map<String, Set<String>> getActiveDataCenterMap()
    {
        return target.getUnderlying().getActiveDataCenterMap();
    }

    public RESULT callbackHooks(com.orientechnologies.orient.core.hook.ORecordHook.TYPE type,
                                OIdentifiable id)
    {
        return target.getUnderlying().callbackHooks(type, id);
    }

    public <RET extends ORecord> RET executeReadRecord(ORecordId rid,
                                                       ORecord iRecord,
                                                       int recordVersion,
                                                       String fetchPlan,
                                                       boolean ignoreCache,
                                                       boolean iUpdateCache,
                                                       boolean loadTombstones,
                                                       LOCKING_STRATEGY lockingStrategy,
                                                       RecordReader recordReader)
    {
        return target.getUnderlying().executeReadRecord(rid, iRecord, recordVersion, fetchPlan, ignoreCache, iUpdateCache,
                                        loadTombstones, lockingStrategy, recordReader);
    }

    public void freeze()
    {
        target.getUnderlying().freeze();
    }

    public <DB extends ODatabase> DB create()
    {
        return target.getUnderlying().create();
    }

    public <RET extends ORecord> RET executeSaveRecord(ORecord record,
                                                       String clusterName,
                                                       int ver,
                                                       OPERATION_MODE mode,
                                                       boolean forceCreate,
                                                       ORecordCallback<? extends Number> recordCreatedCallback,
                                                       ORecordCallback<Integer> recordUpdatedCallback)
    {
        return target.getUnderlying().executeSaveRecord(record, clusterName, ver, mode, forceCreate, recordCreatedCallback,
                                        recordUpdatedCallback);
    }

    public boolean isSharded()
    {
        return target.getUnderlying().isSharded();
    }

    public <DB extends ODatabase> DB create(String incrementalBackupPath)
    {
        return target.getUnderlying().create(incrementalBackupPath);
    }

    public void release()
    {
        target.getUnderlying().release();
    }

    public void executeDeleteRecord(OIdentifiable record,
                                    int iVersion,
                                    boolean iRequired,
                                    OPERATION_MODE iMode,
                                    boolean prohibitTombstones)
    {
        target.getUnderlying().executeDeleteRecord(record, iVersion, iRequired, iMode, prohibitTombstones);
    }

    public void freeze(boolean throwException)
    {
        target.getUnderlying().freeze(throwException);
    }

    public <RET extends ORecord> RET executeSaveEmptyRecord(ORecord record,
                                                            String clusterName)
    {
        return target.getUnderlying().executeSaveEmptyRecord(record, clusterName);
    }

    public <DB extends ODatabase> DB create(Map<OGlobalConfiguration, Object> iInitialSettings)
    {
        return target.getUnderlying().create(iInitialSettings);
    }

    public void setDefaultTransactionMode()
    {
        target.getUnderlying().setDefaultTransactionMode();
    }

    public OMetadataInternal getMetadata()
    {
        return target.getUnderlying().getMetadata();
    }

    public ODatabaseDocumentInternal copy()
    {
        return target.getUnderlying().copy();
    }

    public void recycle(ORecord record)
    {
        target.getUnderlying().recycle(record);
    }

    public void checkIfActive()
    {
        target.getUnderlying().checkIfActive();
    }

    public void callOnOpenListeners()
    {
        target.getUnderlying().callOnOpenListeners();
    }

    public void callOnCloseListeners()
    {
        target.getUnderlying().callOnCloseListeners();
    }

    public void callOnDropListeners()
    {
        target.getUnderlying().callOnDropListeners();
    }

    public <DB extends ODatabase> DB setCustom(String name,
                                               Object iValue)
    {
        return target.getUnderlying().setCustom(name, iValue);
    }

    public ODatabase activateOnCurrentThread()
    {
        return target.getUnderlying().activateOnCurrentThread();
    }

    public void setPrefetchRecords(boolean prefetchRecords)
    {
        target.getUnderlying().setPrefetchRecords(prefetchRecords);
    }

    public boolean isPrefetchRecords()
    {
        return target.getUnderlying().isPrefetchRecords();
    }

    public void checkForClusterPermissions(String name)
    {
        target.getUnderlying().checkForClusterPermissions(name);
    }

    public void rawBegin(OTransaction transaction)
    {
        target.getUnderlying().rawBegin(transaction);
    }

    public <REC extends ORecord> ORecordIteratorCluster<REC> browseCluster(String iClusterName)
    {
        return target.getUnderlying().browseCluster(iClusterName);
    }

    public OResultSet getActiveQuery(String id)
    {
        return target.getUnderlying().getActiveQuery(id);
    }

    public boolean isActiveOnCurrentThread()
    {
        return target.getUnderlying().isActiveOnCurrentThread();
    }

    public boolean isUseLightweightEdges()
    {
        return target.getUnderlying().isUseLightweightEdges();
    }

    public OEdge newLightweightEdge(String iClassName,
                                    OVertex from,
                                    OVertex to)
    {
        return target.getUnderlying().newLightweightEdge(iClassName, from, to);
    }

    public void reload()
    {
        target.getUnderlying().reload();
    }

    public void setUseLightweightEdges(boolean b)
    {
        target.getUnderlying().setUseLightweightEdges(b);
    }

    public boolean hide(ORID rid)
    {
        return target.getUnderlying().hide(rid);
    }

    public <REC extends ORecord> ORecordIteratorCluster<REC> browseCluster(String iClusterName,
                                                                           long startClusterPosition,
                                                                           long endClusterPosition,
                                                                           boolean loadTombstones)
    {
        return target.getUnderlying().browseCluster(iClusterName, startClusterPosition, endClusterPosition, loadTombstones);
    }

    public void drop()
    {
        target.getUnderlying().drop();
    }

    public <REC extends ORecord> ORecordIteratorCluster<REC> browseCluster(String iClusterName,
                                                                           Class<REC> iRecordClass)
    {
        return target.getUnderlying().browseCluster(iClusterName, iRecordClass);
    }

    public OContextConfiguration getConfiguration()
    {
        return target.getUnderlying().getConfiguration();
    }

    public boolean declareIntent(OIntent iIntent)
    {
        return target.getUnderlying().declareIntent(iIntent);
    }

    public <REC extends ORecord> ORecordIteratorCluster<REC> browseCluster(String iClusterName,
                                                                           Class<REC> iRecordClass,
                                                                           long startClusterPosition,
                                                                           long endClusterPosition)
    {
        return target.getUnderlying().browseCluster(iClusterName, iRecordClass, startClusterPosition, endClusterPosition);
    }

    public OIntent getActiveIntent()
    {
        return target.getUnderlying().getActiveIntent();
    }

    public boolean exists()
    {
        return target.getUnderlying().exists();
    }

    public <REC extends ORecord> ORecordIteratorCluster<REC> browseCluster(String iClusterName,
                                                                           Class<REC> iRecordClass,
                                                                           long startClusterPosition,
                                                                           long endClusterPosition,
                                                                           boolean loadTombstones)
    {
        return target.getUnderlying().browseCluster(iClusterName, iRecordClass, startClusterPosition, endClusterPosition,
                                    loadTombstones);
    }

    public ODatabaseDocumentInternal cleanOutRecord(ORID rid,
                                                    int version)
    {
        return target.getUnderlying().cleanOutRecord(rid, version);
    }

    public void close()
    {
        target.close();
    }

    public void realClose()
    {
        target.getUnderlying().realClose();
    }

    public <RET extends ORecord> RET getRecord(OIdentifiable iIdentifiable)
    {
        return target.getUnderlying().getRecord(iIdentifiable);
    }

    public STATUS getStatus()
    {
        return target.getUnderlying().getStatus();
    }

    public void reuse()
    {
        target.getUnderlying().reuse();
    }

    public <DB extends ODatabase> DB setStatus(STATUS iStatus)
    {
        return target.getUnderlying().setStatus(iStatus);
    }

    public boolean sync(boolean forceDeployment,
                        boolean tryWithDelta)
    {
        return target.getUnderlying().sync(forceDeployment, tryWithDelta);
    }

    public long getSize()
    {
        return target.getUnderlying().getSize();
    }

    public byte getRecordType()
    {
        return target.getUnderlying().getRecordType();
    }

    public String getName()
    {
        return target.getUnderlying().getName();
    }

    public boolean isRetainRecords()
    {
        return target.getUnderlying().isRetainRecords();
    }

    public Map<String, Object> syncCluster(String clusterName)
    {
        return target.getUnderlying().syncCluster(clusterName);
    }

    public String getURL()
    {
        return target.getUnderlying().getURL();
    }

    public OLocalRecordCache getLocalCache()
    {
        return target.getUnderlying().getLocalCache();
    }

    public ODatabaseDocument setRetainRecords(boolean iValue)
    {
        return target.getUnderlying().setRetainRecords(iValue);
    }

    public Map<String, Object> getHaStatus(boolean servers,
                                           boolean db,
                                           boolean latency,
                                           boolean messages)
    {
        return target.getUnderlying().getHaStatus(servers, db, latency, messages);
    }

    public int getDefaultClusterId()
    {
        return target.getUnderlying().getDefaultClusterId();
    }

    public boolean removeHaServer(String serverName)
    {
        return target.getUnderlying().removeHaServer(serverName);
    }

    public OResultSet queryOnNode(String nodeName,
                                  OExecutionPlan executionPlan,
                                  Map<Object, Object> inputParameters)
    {
        return target.getUnderlying().queryOnNode(nodeName, executionPlan, inputParameters);
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(ResourceGeneric resourceGeneric,
                                                           String resourceSpecific,
                                                           int iOperation)
    {
        return target.getUnderlying().checkSecurity(resourceGeneric, resourceSpecific, iOperation);
    }

    public int getClusters()
    {
        return target.getUnderlying().getClusters();
    }

    public boolean existsCluster(String iClusterName)
    {
        return target.getUnderlying().existsCluster(iClusterName);
    }

    public Collection<String> getClusterNames()
    {
        return target.getUnderlying().getClusterNames();
    }

    public void internalCommit(OTransactionInternal transaction)
    {
        target.getUnderlying().internalCommit(transaction);
    }

    public int getClusterIdByName(String iClusterName)
    {
        return target.getUnderlying().getClusterIdByName(iClusterName);
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(ResourceGeneric iResourceGeneric,
                                                           int iOperation,
                                                           Object iResourceSpecific)
    {
        return target.getUnderlying().checkSecurity(iResourceGeneric, iOperation, iResourceSpecific);
    }

    public boolean isClusterVertex(int cluster)
    {
        return target.getUnderlying().isClusterVertex(cluster);
    }

    public boolean isClusterEdge(int cluster)
    {
        return target.getUnderlying().isClusterEdge(cluster);
    }

    public String getClusterNameById(int iClusterId)
    {
        return target.getUnderlying().getClusterNameById(iClusterId);
    }

    public long getClusterRecordSizeByName(String iClusterName)
    {
        return target.getUnderlying().getClusterRecordSizeByName(iClusterName);
    }

    public long getClusterRecordSizeById(int iClusterId)
    {
        return target.getUnderlying().getClusterRecordSizeById(iClusterId);
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(ResourceGeneric iResourceGeneric,
                                                           int iOperation,
                                                           Object... iResourcesSpecific)
    {
        return target.getUnderlying().checkSecurity(iResourceGeneric, iOperation, iResourcesSpecific);
    }

    public boolean isClosed()
    {
        return target.getUnderlying().isClosed();
    }

    public void truncateCluster(String clusterName)
    {
        target.getUnderlying().truncateCluster(clusterName);
    }

    public long countClusterElements(int iCurrentClusterId)
    {
        return target.getUnderlying().countClusterElements(iCurrentClusterId);
    }

    public long countClusterElements(int iCurrentClusterId,
                                     boolean countTombstones)
    {
        return target.getUnderlying().countClusterElements(iCurrentClusterId, countTombstones);
    }

    public long countClusterElements(int[] iClusterIds)
    {
        return target.getUnderlying().countClusterElements(iClusterIds);
    }

    public boolean isValidationEnabled()
    {
        return target.getUnderlying().isValidationEnabled();
    }

    public <DB extends ODatabaseDocument> DB setValidationEnabled(boolean iEnabled)
    {
        return target.getUnderlying().setValidationEnabled(iEnabled);
    }

    public long countClusterElements(int[] iClusterIds,
                                     boolean countTombstones)
    {
        return target.getUnderlying().countClusterElements(iClusterIds, countTombstones);
    }

    public long countClusterElements(String iClusterName)
    {
        return target.getUnderlying().countClusterElements(iClusterName);
    }

    public int addCluster(String iClusterName,
                          Object... iParameters)
    {
        return target.getUnderlying().addCluster(iClusterName, iParameters);
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(String iResource,
                                                           int iOperation)
    {
        return target.getUnderlying().checkSecurity(iResource, iOperation);
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(String iResourceGeneric,
                                                           int iOperation,
                                                           Object iResourceSpecific)
    {
        return target.getUnderlying().checkSecurity(iResourceGeneric, iOperation, iResourceSpecific);
    }

    public Set<Integer> getBlobClusterIds()
    {
        return target.getUnderlying().getBlobClusterIds();
    }

    public int addCluster(String iClusterName,
                          int iRequestedId,
                          Object... iParameters)
    {
        return target.getUnderlying().addCluster(iClusterName, iRequestedId, iParameters);
    }

    public boolean dropCluster(String iClusterName,
                               boolean iTruncate)
    {
        return target.getUnderlying().dropCluster(iClusterName, iTruncate);
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(String iResourceGeneric,
                                                           int iOperation,
                                                           Object... iResourcesSpecific)
    {
        return target.getUnderlying().checkSecurity(iResourceGeneric, iOperation, iResourcesSpecific);
    }

    public boolean dropCluster(int iClusterId,
                               boolean iTruncate)
    {
        return target.getUnderlying().dropCluster(iClusterId, iTruncate);
    }

    public Object setProperty(String iName,
                              Object iValue)
    {
        return target.getUnderlying().setProperty(iName, iValue);
    }

    public Object getProperty(String iName)
    {
        return target.getUnderlying().getProperty(iName);
    }

    public boolean isPooled()
    {
        return target.getUnderlying().isPooled();
    }

    public Iterator<Entry<String, Object>> getProperties()
    {
        return target.getUnderlying().getProperties();
    }

    public int addBlobCluster(String iClusterName,
                              Object... iParameters)
    {
        return target.getUnderlying().addBlobCluster(iClusterName, iParameters);
    }

    public Object get(ATTRIBUTES iAttribute)
    {
        return target.getUnderlying().get(iAttribute);
    }

    public OElement newElement()
    {
        return target.getUnderlying().newElement();
    }

    public OElement newElement(String className)
    {
        return target.getUnderlying().newElement(className);
    }

    public OEdge newEdge(OVertex from,
                         OVertex to)
    {
        return target.getUnderlying().newEdge(from, to);
    }

    public <DB extends ODatabase> DB set(ATTRIBUTES iAttribute,
                                         Object iValue)
    {
        return target.getUnderlying().set(iAttribute, iValue);
    }

    public OEdge newEdge(OVertex from,
                         OVertex to,
                         OClass type)
    {
        return target.getUnderlying().newEdge(from, to, type);
    }

    public void registerListener(ODatabaseListener iListener)
    {
        target.getUnderlying().registerListener(iListener);
    }

    public OEdge newEdge(OVertex from,
                         OVertex to,
                         String type)
    {
        return target.getUnderlying().newEdge(from, to, type);
    }

    public void unregisterListener(ODatabaseListener iListener)
    {
        target.getUnderlying().unregisterListener(iListener);
    }

    public ORecordMetadata getRecordMetadata(ORID rid)
    {
        return target.getUnderlying().getRecordMetadata(rid);
    }

    public OVertex newVertex()
    {
        return target.getUnderlying().newVertex();
    }

    public OVertex newVertex(OClass type)
    {
        return target.getUnderlying().newVertex(type);
    }

    public OVertex newVertex(String type)
    {
        return target.getUnderlying().newVertex(type);
    }

    public OClass createVertexClass(String className)
        throws OSchemaException
    {
        return target.getUnderlying().createVertexClass(className);
    }

    public boolean isFrozen()
    {
        return target.getUnderlying().isFrozen();
    }

    public OClass createEdgeClass(String className)
    {
        return target.getUnderlying().createEdgeClass(className);
    }

    public OClass createClassIfNotExist(String className,
                                        String... superclasses)
        throws OSchemaException
    {
        return target.getUnderlying().createClassIfNotExist(className, superclasses);
    }

    public <RET> RET newInstance()
    {
        return target.getUnderlying().newInstance();
    }

    public ODictionary<ORecord> getDictionary()
    {
        return target.getUnderlying().getDictionary();
    }

    public OSecurityUser getUser()
    {
        return target.getUnderlying().getUser();
    }

    public OClass getClass(String className)
    {
        return target.getUnderlying().getClass(className);
    }

    public OClass createClass(String className,
                              String... superclasses)
        throws OSchemaException
    {
        return target.getUnderlying().createClass(className, superclasses);
    }

    public <RET extends ORecord> RET load(ORecord iObject)
    {
        return target.getUnderlying().load(iObject);
    }

    public <RET extends ORecord> RET load(ORecord iObject,
                                          String iFetchPlan)
    {
        return target.getUnderlying().load(iObject, iFetchPlan);
    }

    public <RET extends ORecord> RET load(ORecord iObject,
                                          String iFetchPlan,
                                          boolean iIgnoreCache,
                                          boolean loadTombstone,
                                          LOCKING_STRATEGY iLockingStrategy)
    {
        return target.getUnderlying().load(iObject, iFetchPlan, iIgnoreCache, loadTombstone, iLockingStrategy);
    }

    public <RET extends ORecord> RET load(ORecord iObject,
                                          String iFetchPlan,
                                          boolean iIgnoreCache,
                                          boolean iUpdateCache,
                                          boolean loadTombstone,
                                          LOCKING_STRATEGY iLockingStrategy)
    {
        return target.getUnderlying().load(iObject, iFetchPlan, iIgnoreCache, iUpdateCache, loadTombstone, iLockingStrategy);
    }

    public <RET extends ORecord> RET load(ORecord iObject,
                                          String iFetchPlan,
                                          boolean iIgnoreCache)
    {
        return target.getUnderlying().load(iObject, iFetchPlan, iIgnoreCache);
    }

    public <RET extends ORecord> RET reload(ORecord iObject,
                                            String iFetchPlan,
                                            boolean iIgnoreCache)
    {
        return target.getUnderlying().reload(iObject, iFetchPlan, iIgnoreCache);
    }

    public <RET extends ORecord> RET reload(ORecord iObject,
                                            String iFetchPlan,
                                            boolean iIgnoreCache,
                                            boolean force)
    {
        return target.getUnderlying().reload(iObject, iFetchPlan, iIgnoreCache, force);
    }

    public <RET extends ORecord> RET load(ORID recordId)
    {
        return target.getUnderlying().load(recordId);
    }

    public <RET extends ORecord> RET load(ORID iRecordId,
                                          String iFetchPlan)
    {
        return target.getUnderlying().load(iRecordId, iFetchPlan);
    }

    public <RET extends ORecord> RET load(ORID iRecordId,
                                          String iFetchPlan,
                                          boolean iIgnoreCache)
    {
        return target.getUnderlying().load(iRecordId, iFetchPlan, iIgnoreCache);
    }

    public <RET extends ORecord> RET load(ORID iRecordId,
                                          String iFetchPlan,
                                          boolean iIgnoreCache,
                                          boolean loadTombstone,
                                          LOCKING_STRATEGY iLockingStrategy)
    {
        return target.getUnderlying().load(iRecordId, iFetchPlan, iIgnoreCache, loadTombstone, iLockingStrategy);
    }

    public <RET extends ORecord> RET load(ORID iRecordId,
                                          String iFetchPlan,
                                          boolean iIgnoreCache,
                                          boolean iUpdateCache,
                                          boolean loadTombstone,
                                          LOCKING_STRATEGY iLockingStrategy)
    {
        return target.getUnderlying().load(iRecordId, iFetchPlan, iIgnoreCache, iUpdateCache, loadTombstone, iLockingStrategy);
    }

    public <RET extends ORecord> RET save(ORecord iObject)
    {
        return target.getUnderlying().save(iObject);
    }

    public <RET extends ORecord> RET save(ORecord iObject,
                                          OPERATION_MODE iMode,
                                          boolean iForceCreate,
                                          ORecordCallback<? extends Number> iRecordCreatedCallback,
                                          ORecordCallback<Integer> iRecordUpdatedCallback)
    {
        return target.getUnderlying().save(iObject, iMode, iForceCreate, iRecordCreatedCallback, iRecordUpdatedCallback);
    }

    public <RET extends ORecord> RET save(ORecord iObject,
                                          String iClusterName)
    {
        return target.getUnderlying().save(iObject, iClusterName);
    }

    public <RET extends ORecord> RET save(ORecord iObject,
                                          String iClusterName,
                                          OPERATION_MODE iMode,
                                          boolean iForceCreate,
                                          ORecordCallback<? extends Number> iRecordCreatedCallback,
                                          ORecordCallback<Integer> iRecordUpdatedCallback)
    {
        return target.getUnderlying().save(iObject, iClusterName, iMode, iForceCreate, iRecordCreatedCallback, iRecordUpdatedCallback);
    }

    public ODatabase<ORecord> delete(ORecord iObject)
    {
        return target.getUnderlying().delete(iObject);
    }

    public ODatabase<ORecord> delete(ORID iRID)
    {
        return target.getUnderlying().delete(iRID);
    }

    public ODatabase<ORecord> delete(ORID iRID,
                                     int iVersion)
    {
        return target.getUnderlying().delete(iRID, iVersion);
    }

    public OTransaction getTransaction()
    {
        return target.getUnderlying().getTransaction();
    }

    public ODatabase<ORecord> begin()
    {
        target.begin();
        
        return target.getUnderlying();
    }

    public ODatabase<ORecord> begin(TXTYPE iStatus)
    {
        return target.getUnderlying().begin(iStatus);
    }

    public ODatabase<ORecord> begin(OTransaction iTx)
        throws OTransactionException
    {
        return target.getUnderlying().begin(iTx);
    }

    public ODatabase<ORecord> commit()
        throws OTransactionException
    {
        target.commit();
        
        return target.getUnderlying();
    }

    public ODatabase<ORecord> commit(boolean force)
        throws OTransactionException
    {
        target.commit(force);
        
        return target.getUnderlying();
    }

    public ODatabase<ORecord> rollback()
        throws OTransactionException
    {
        target.rollback();
        return target.getUnderlying();
    }

    public ODatabase<ORecord> rollback(boolean force)
        throws OTransactionException
    {
        target.rollback(force);
        
        return target.getUnderlying();
    }

    public <RET extends List<?>> RET query(OQuery<?> iCommand,
                                           Object... iArgs)
    {
        return target.getUnderlying().query(iCommand, iArgs);
    }

    public <RET extends OCommandRequest> RET command(OCommandRequest iCommand)
    {
        return target.getUnderlying().command(iCommand);
    }

    public OResultSet query(String query,
                            Object... args)
        throws OCommandSQLParsingException,
        OCommandExecutionException
    {
        return target.getUnderlying().query(query, args);
    }

    public OResultSet query(String query,
                            Map args)
        throws OCommandSQLParsingException,
        OCommandExecutionException
    {
        return target.getUnderlying().query(query, args);
    }

    public OResultSet command(String query,
                              Object... args)
        throws OCommandSQLParsingException,
        OCommandExecutionException
    {
        return target.getUnderlying().command(query, args);
    }

    public OResultSet command(String query,
                              Map args)
        throws OCommandSQLParsingException,
        OCommandExecutionException
    {
        return target.getUnderlying().command(query, args);
    }

    public OResultSet execute(String language,
                              String script,
                              Object... args)
        throws OCommandExecutionException,
        OCommandScriptException
    {
        return target.getUnderlying().execute(language, script, args);
    }

    public OResultSet execute(String language,
                              String script,
                              Map<String, ?> args)
        throws OCommandExecutionException,
        OCommandScriptException
    {
        return target.getUnderlying().execute(language, script, args);
    }

    public <DB extends ODatabase<?>> DB registerHook(ORecordHook iHookImpl)
    {
        return target.getUnderlying().registerHook(iHookImpl);
    }

    public <DB extends ODatabase<?>> DB registerHook(ORecordHook iHookImpl,
                                                     HOOK_POSITION iPosition)
    {
        return target.getUnderlying().registerHook(iHookImpl, iPosition);
    }

    public Map<ORecordHook, HOOK_POSITION> getHooks()
    {
        return target.getUnderlying().getHooks();
    }

    public <DB extends ODatabase<?>> DB unregisterHook(ORecordHook iHookImpl)
    {
        return target.getUnderlying().unregisterHook(iHookImpl);
    }

    public boolean isMVCC()
    {
        return target.getUnderlying().isMVCC();
    }

    public Iterable<ODatabaseListener> getListeners()
    {
        return target.getUnderlying().getListeners();
    }

    public <DB extends ODatabase<?>> DB setMVCC(boolean iValue)
    {
        return target.getUnderlying().setMVCC(iValue);
    }

    public String getType()
    {
        return target.getUnderlying().getType();
    }

    public ORecordConflictStrategy getConflictStrategy()
    {
        return target.getUnderlying().getConflictStrategy();
    }

    public <DB extends ODatabase<?>> DB setConflictStrategy(String iStrategyName)
    {
        return target.getUnderlying().setConflictStrategy(iStrategyName);
    }

    public <DB extends ODatabase<?>> DB setConflictStrategy(ORecordConflictStrategy iResolver)
    {
        return target.getUnderlying().setConflictStrategy(iResolver);
    }

    public String incrementalBackup(String path)
    {
        return target.getUnderlying().incrementalBackup(path);
    }

    public OLiveQueryMonitor live(String query,
                                  OLiveQueryResultListener listener,
                                  Map<String, ?> args)
    {
        return target.getUnderlying().live(query, listener, args);
    }

    public OLiveQueryMonitor live(String query,
                                  OLiveQueryResultListener listener,
                                  Object... args)
    {
        return target.getUnderlying().live(query, listener, args);
    }

    public <T> T executeWithRetry(int nRetries,
                                  Function<ODatabaseSession, T> function)
        throws IllegalStateException,
        IllegalArgumentException,
        ONeedRetryException,
        UnsupportedOperationException
    {
        return target.getUnderlying().executeWithRetry(nRetries, function);
    }

}
