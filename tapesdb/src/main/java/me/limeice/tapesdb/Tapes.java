package me.limeice.tapesdb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.esotericsoftware.kryo.Serializer;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tapes,like Non-SQL DataBase
 * Created by Lime on 2018/1/1.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Tapes {

    static final String TAG = "TapesDB";

    @SuppressWarnings("SpellCheckingInspection")
    public static final String INNER_DB_NAME = "io.tapesdb";

    private static Context mAppContext;

    private static final ConcurrentHashMap<String, Track> mTracks = new ConcurrentHashMap<>();
    private static final ArrayMap<Class, Serializer> mCustomSerializers = new ArrayMap<>();

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public static void init(@NonNull Context context) {
        mAppContext = context.getApplicationContext();
    }

    /**
     * 读取默认音轨
     *
     * @return 音轨
     */
    @NonNull
    public static Track track() {
        return track(INNER_DB_NAME);
    }

    /**
     * 读取一条音轨
     *
     * @param trackName 音轨名
     * @return 音轨
     */
    @NonNull
    public static Track track(@Nullable String trackName) {
        if (mAppContext == null)
            throw new TapesDbException("Tapes.init(Context context) is not called!");
        String key = trackName;
        if (trackName == null || trackName.trim().equals(""))
            key = INNER_DB_NAME;
        synchronized (mTracks) {
            Track track = mTracks.get(key);
            if (track == null) {
                track = new Track(mAppContext, trackName, mCustomSerializers);
                mTracks.put(key, track);
            }
            return track;
        }
    }

    /**
     * 添加自定义序列化缓存，最好在第一次调用 {@link #track()} 或 {@link #track(String)} 前调用
     *
     * @param clazz      类
     * @param serializer 序列化方式
     * @param <T>        类型
     */
    public static <T> void addCustomSerializer(@NonNull Class<T> clazz, @NonNull Serializer<T> serializer) {
        mCustomSerializers.put(clazz, serializer);
    }

    /**
     * 摧毁磁带（数据库所有数据），必须在 {@link #init(Context)} 前调用
     *
     * @param context 上下文
     */
    public synchronized static void destroy(@NonNull Context context) {
        if (mAppContext != null) {
            throw new TapesDbException("Tapes.init(Context context) is called!");
        }
        DbStorage.deleteDir(new File(context.getFilesDir(), DbStorage.DBF));
    }


    /**
     * 设置日志级别 {@link com.esotericsoftware.minlog.Log}
     *
     * @param logLevel 日志等级
     * @see com.esotericsoftware.minlog.Log
     */
    public void setLogLevel(int logLevel) {
        com.esotericsoftware.minlog.Log.set(logLevel);
    }
}
