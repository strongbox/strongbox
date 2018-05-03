package org.carlspring.strongbox.data.server;

import com.orientechnologies.orient.core.db.OrientDB;

public interface OrientDbServer
{

    void start();

    OrientDB orientDB();

    void stop();

}
