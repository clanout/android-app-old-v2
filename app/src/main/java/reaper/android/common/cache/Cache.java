package reaper.android.common.cache;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class Cache implements Serializable, Cloneable
{
    private static Cache instance;

    private HashMap<String, Object> data;

    private Cache()
    {
        data = new HashMap<>();
    }

    public static Cache getInstance()
    {
        if (instance == null)
        {
            Log.d("[reap3r] cache", "Cache not initialized");
            throw new IllegalStateException();
        }

        return instance;
    }

    public void put(String key, Object value)
    {
        data.put(key, value);
    }

    public Object get(String key)
    {
        return data.get(key);
    }

    public void remove(String key)
    {
        data.remove(key);
    }

    public static void init(Context context, String cacheFile)
    {
        try
        {
            File file = context.getFileStreamPath(cacheFile);
            if (file.exists())
            {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                instance = (Cache) inputStream.readObject();
                inputStream.close();
            }
            else
            {
                instance = new Cache();
            }
        }
        catch (Exception e)
        {
            Log.d("[reap3r] cache", "Unable to initialize cache [" + e.getMessage() + "]");
            e.printStackTrace();
            instance = new Cache();
        }
    }

    public synchronized static void commit(Context context, String cacheFile)
    {
        try
        {
            File file = context.getFileStreamPath(cacheFile);

            FileOutputStream fileOutputStream = new FileOutputStream(file, false);

            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(instance.clone());
            outputStream.close();

            Log.d("[reap3r] cache", "Cache committed");
        }
        catch (Exception e)
        {
            Log.d("[reap3r] cache", "Unable to commit cache [" + e.getMessage() + "]");
            e.printStackTrace();
        }
    }
}
