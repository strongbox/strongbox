package org.carlspring.strongbox.data.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public abstract class EntitySerializer<T extends GenericEntity> implements StreamSerializer<T>
{

    private Pool<Kryo> kryoPool;

    public EntitySerializer()
    {
        super();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //Log.TRACE();

        kryoPool = new Pool<Kryo>(true, false, 150)
        {
            protected Kryo create()
            {
                return kryoSerializer(classLoader);
            }
        };

    }

    private Kryo kryoSerializer(ClassLoader classLoader)
    {
        Kryo kryo = new Kryo();
        kryo.setClassLoader(classLoader);
        kryo.setRegistrationRequired(false);

        init(kryo);

        return kryo;
    }

    protected void init(Kryo kryo)
    {
        
    }

    protected Kryo getKryo()
    {
        return kryoPool.obtain();
    }

    protected void releaseKryo(Kryo kryo)
    {
        kryoPool.free(kryo);
    }

    @Override
    public void write(ObjectDataOutput objectDataOutput,
                      T object)
        throws IOException
    {
        Kryo kryo = getKryo();

        try
        {
            Output output = new Output((OutputStream) objectDataOutput);
            kryo.writeObject(output, object);
            output.flush();
        } 
        finally
        {
            releaseKryo(kryo);
        }

    }

    @Override
    public T read(ObjectDataInput objectDataInput)
        throws IOException
    {
        Kryo kryo = getKryo();

        try
        {
            InputStream in = (InputStream) objectDataInput;
            Input input = new Input(in);
            T result = kryo.readObject(input, getEntityClass());
            return result;
        } 
        finally
        {
            releaseKryo(kryo);
        }
    }

    @Override
    public void destroy()
    {

    }

    public abstract Class<T> getEntityClass();
}
