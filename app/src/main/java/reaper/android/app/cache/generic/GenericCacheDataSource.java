package reaper.android.app.cache.generic;

public interface GenericCacheDataSource
{
    void write(String key, String value);

    String read(String key);

    void delete(String key);
}
