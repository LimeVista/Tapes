package me.limeice.tapesdb;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@SuppressWarnings("unused")
public final class TapesAsync {

    private Context mContext;

    private Semaphore semaphore;
    private final ConcurrentHashMap<String, TrackAsync> mTracks = new ConcurrentHashMap<>();
    private final ArrayMap<Class, Serializer> mCustomSerializers = Tapes.mCustomSerializers;
    private ConcurrentLinkedQueue<Kryo> mKryo;
    private ExecutorService mServices;


    public static TapesAsync create(@NonNull Context context) {
        return new TapesAsync(context, 4);
    }

    public static TapesAsync create(@NonNull Context context, int maxThreadCount) {
        maxThreadCount = maxThreadCount <= 0 ? 1 : maxThreadCount;
        return new TapesAsync(context, maxThreadCount);
    }

    /**
     * 读取默认音轨
     *
     * @return 音轨
     */
    @NonNull
    public TrackAsync track() {
        return track(Tapes.INNER_DB_NAME);
    }

    /**
     * 读取一条音轨
     *
     * @param trackName 音轨名
     * @return 音轨
     */
    @NonNull
    public TrackAsync track(@Nullable String trackName) {
        String key = trackName;
        if (trackName == null || trackName.trim().equals(""))
            key = Tapes.INNER_DB_NAME;
        synchronized (mTracks) {
            TrackAsync track = mTracks.get(key);
            if (track == null) {
                track = new TrackInner(mContext, trackName, mCustomSerializers);
                mTracks.put(key, track);
            }
            return track;
        }
    }

    private TapesAsync(Context context, int maxThreadCount) {
        mContext = context;
        semaphore = new Semaphore(maxThreadCount, true);
        mKryo = new ConcurrentLinkedQueue<>();
        mServices = Executors.newCachedThreadPool();
    }

    final class StorageHolder extends DbStorageCore {

        StorageHolder(Context context, String trackName, ArrayMap<Class, Serializer> serializers) {
            super(context, trackName, serializers);
        }

        @Override
        Kryo getKryo() {
            if (mKryo.isEmpty())
                return createKryo();
            return mKryo.poll();
        }

        <E> E get(String key, final Kryo kryo) {
            if (!exist(key)) return null;
            return read(key, originalFile(key), kryo);
        }

        <E> void set(@NonNull String key, @NonNull E content, final Kryo kryo) {
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
            write(originalFile, backupFile, new TapesWrap<>(content), kryo);
        }
    }

    final class TrackInner implements TrackAsync {

        private StorageHolder mStorage;
        private final SafeLocker mLocker = new SafeLocker();

        TrackInner(Context context, String trackName, ArrayMap<Class, Serializer> serializers) {
            mStorage = new StorageHolder(context, trackName, serializers);
        }


        @Override
        public <E> void read(@NonNull String key, @NonNull Task<E> callback) {
            try {
                semaphore.acquireUninterruptibly();
                mServices.execute(() -> {
                    E e;
                    Kryo kryo = null;
                    try {
                        mLocker.lock(key);
                        kryo = mStorage.getKryo();
                        e = mStorage.get(key, kryo);
                    } finally {
                        if (kryo != null)
                            mKryo.offer(kryo);
                        mLocker.release(key);
                    }
                    callback.callback(e);
                });
            } finally {
                semaphore.release();
            }
        }

        @Override
        public void destroy(@Nullable Task<Boolean> callback) {
            try {
                semaphore.acquireUninterruptibly();
                mServices.execute(() -> {
                    boolean b = mStorage.destroy();
                    if (callback != null)
                        callback.callback(b);
                });
            } finally {
                semaphore.release();
            }
        }

        @Override
        public <E> void write(@NonNull String key, @NonNull E value, @Nullable Task<E> callback) {
            try {
                semaphore.acquireUninterruptibly();
                mServices.execute(() -> {
                    Kryo kryo = null;
                    try {
                        mLocker.lock(key);
                        kryo = mStorage.getKryo();
                        mStorage.set(key, value, kryo);
                    } finally {
                        if (kryo != null)
                            mKryo.offer(kryo);
                        mLocker.release(key);
                    }
                    if (callback != null)
                        callback.callback(value);
                });
            } finally {
                semaphore.release();
            }
        }

        @Override
        public void exist(@NonNull String key, @NonNull Task<Boolean> callback) {
            try {
                semaphore.acquireUninterruptibly();
                mServices.execute(() -> {
                    boolean exist;
                    try {
                        mLocker.lock(key);
                        exist = mStorage.exist(key);
                    } finally {
                        mLocker.release(key);
                    }
                    callback.callback(exist);
                });
            } finally {
                semaphore.release();
            }
        }

        @Override
        public void clear(@NonNull String key, @Nullable Task<Void> callback) {
            try {
                semaphore.acquireUninterruptibly();
                mServices.execute(() -> {
                    try {
                        mLocker.lock(key);
                        mStorage.remove(key);
                    } finally {
                        mLocker.release(key);
                    }
                    if (callback != null)
                        callback.callback(null);
                });
            } finally {
                semaphore.release();
            }
        }

        @Override
        public void getAllKey(@NonNull String key, @NonNull Task<List<String>> callback) {
            try {
                semaphore.acquireUninterruptibly();
                mServices.execute(() -> callback.callback(mStorage.getAllKey()));
            } finally {
                semaphore.release();
            }
        }
    }
}
