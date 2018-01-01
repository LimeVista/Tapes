package me.limeice.tapesdb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.limeice.tapesdb.serializer.InnerSerializer;

@SuppressWarnings("WeakerAccess")
public final class DbStorage {

    static final String DBF = "TapesDB";
    private File mBasePath;
    private ArrayMap<Class, Serializer> mSerializers;

    private Kryo getKryo() {
        return mKryo.get();
    }

    private final ThreadLocal<Kryo> mKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return createKryo();
        }
    };

    @SuppressWarnings("ResultOfMethodCallIgnored")
    DbStorage(Context context, String trackName, ArrayMap<Class, Serializer> serializers) {
        mBasePath = new File(context.getFilesDir().getAbsolutePath() +
                File.separator + DBF + File.separator + trackName);
        if (!mBasePath.exists()) mBasePath.mkdirs();
        mSerializers = serializers;
    }

    private Kryo createKryo() {
        Kryo kryo = new Kryo();

        kryo.setReferences(false);
        kryo.register(TapesWrap.class);
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);

        kryo.register(UUID.class, new InnerSerializer.UUIDSerializer());
        kryo.register(URI.class, new InnerSerializer.URISerializer());

        for (Class<?> clazz : mSerializers.keySet())
            kryo.register(clazz, mSerializers.get(clazz));

        kryo.setInstantiatorStrategy(
                new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    }

    /**
     * 获得指定键映射值
     *
     * @param key 键
     * @param <E> 泛型，数据真正类型
     * @return 值(不存在对应值时返回空)
     */
    @Nullable
    <E> E get(String key) {
        if (!exist(key)) return null;
        return read(key, originalFile(key));
    }

    /**
     * 获得指定键映射值
     *
     * @param key        键
     * @param defaultVal 默认值
     * @param <E>        泛型，数据真正类型
     * @return 值(不存在对应值时返回默认值)
     */
    @NonNull
    <E> E get(String key, @NonNull E defaultVal) {
        if (!exist(key)) return defaultVal;
        return read(key, originalFile(key));
    }

    /**
     * 获得所有键
     *
     * @return key集合
     */
    @NonNull
    synchronized List<String> getAllKey() {
        String[] str = mBasePath.list();
        List<String> allKey = new ArrayList<>();
        if (str == null) return allKey;
        for (String s : str) {
            if (s.charAt(s.length() - 1) == 'b') {
                allKey.add(s.substring(0, s.length() - 4));
            }
        }
        return allKey;
    }

    /**
     * 所否存在制定键的值
     *
     * @param key 键
     * @return 是否存在
     */
    boolean exist(@NonNull String key) {
        final File file = originalFile(key);
        return file.exists() && file.isFile();
    }

    /**
     * 根据键读取值
     *
     * @param key          键
     * @param originalFile 源路径
     * @param <E>          泛型
     * @return 值
     */
    private <E> E read(@NonNull String key, @NonNull File originalFile) {
        Input in = null;
        try {
            in = new Input(new FileInputStream(originalFile));
            @SuppressWarnings("unchecked")
            TapesWrap<E> content = getKryo().readObject(in, TapesWrap.class);
            return content.content;
        } catch (FileNotFoundException | KryoException | ClassCastException e) {
            throw new TapesDbException("Couldn't read/deserialize file "
                    + originalFile + "; Key: " + key, e);
        } finally {
            closeIO(in);
        }
    }

    /**
     * 摧毁当前 Track
     *
     * @return 是否成功{@code true} 是，{@code false} 否
     */
    synchronized boolean destroy() {
        if (deleteDir(mBasePath)) {
            if (!mBasePath.exists())
                //noinspection ResultOfMethodCallIgnored
                mBasePath.mkdirs();
            return true;
        }
        return false;
    }

    /**
     * 删除指定键的值
     *
     * @param key 键
     */
    void remove(@NonNull String key) {
        final File originalFile = originalFile(key);
        final File backupFile = backupFile(key);
        if (originalFile.exists())
            //noinspection ResultOfMethodCallIgnored
            originalFile.delete();
        if (backupFile.exists())
            //noinspection ResultOfMethodCallIgnored
            backupFile.delete();
    }

    /**
     * 存储操作
     *
     * @param key     键
     * @param content 值（内容）
     * @param <E>     泛型，内容真正数据类型
     */
    <E> void set(@NonNull String key, @NonNull E content) {
        final File originalFile = originalFile(key);
        final File backupFile = backupFile(key);
        if (originalFile.exists()) {
            if (backupFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                originalFile.delete(); // 如果备份文件存在，那么说明刚才操作失败了
            } else {
                if (!originalFile.renameTo(backupFile))
                    throw new TapesDbException("Could't rename file "
                            + originalFile + " to backup file " + backupFile);
            }
        }
        write(originalFile, backupFile, new TapesWrap<>(content));
    }

    /**
     * 写入操作
     *
     * @param originalFile 源文件路径
     * @param backupFile   备份文件路径
     * @param content      存储内容
     * @param <E>          泛型，内容真正数据类型
     */
    private <E> void write(@NonNull File originalFile, @NonNull File backupFile,
                           @NonNull TapesWrap<E> content) {
        try {
            FileOutputStream out = new FileOutputStream(originalFile);
            final Output kryoOut = new Output(out);
            getKryo().writeObject(kryoOut, content);
            kryoOut.flush();
            out.flush();
            out.getFD().sync();
            kryoOut.close();
            //noinspection ResultOfMethodCallIgnored
            backupFile.delete();
        } catch (IOException | KryoException e) {
            //noinspection ResultOfMethodCallIgnored
            originalFile.delete();
            if (!backupFile.renameTo(originalFile))
                throw new TapesDbException("Could't rename backup file "
                        + backupFile + " to  file " + originalFile);
        }
    }

    /**
     * 获得源文件路径
     *
     * @param key 键
     * @return 源文件路径
     */
    private File originalFile(@NonNull String key) {
        return new File(mBasePath, key + ".tdb");
    }

    /**
     * 获得备份文件路径
     *
     * @param key 键
     * @return 备份文件路径
     */
    private File backupFile(@NonNull String key) {
        return new File(mBasePath, key + ".tdb.bak");
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * Close IO
     *
     * @param cs Closeable
     */
    private void closeIO(Closeable... cs) {
        if (cs == null) return;
        for (Closeable c : cs) {
            if (c == null) continue;
            try {
                c.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
}
