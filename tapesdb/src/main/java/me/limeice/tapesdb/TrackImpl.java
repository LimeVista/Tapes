package me.limeice.tapesdb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.esotericsoftware.kryo.Serializer;

import java.util.List;

/**
 * Track 磁带音轨，实现类
 * Created by Lime on 2018/1/1.
 */

@SuppressWarnings({"unused", "SameParameterValue", "WeakerAccess"})
public final class TrackImpl implements Track {

    private DbStorageCore mStorage;
    private final SafeLocker mLocker = new SafeLocker();

    TrackImpl(Context context, String trackName, ArrayMap<Class, Serializer> serializers) {
        mStorage = new DbStorage(context, trackName, serializers);
    }

    /**
     * 读取指定键的值
     *
     * @param key 键
     * @param <E> 值类型
     * @return 值
     */
    @Nullable
    @Override
    public <E> E read(@NonNull String key) {
        try {
            mLocker.lock(key);
            return mStorage.get(key);
        } finally {
            mLocker.release(key);
        }
    }

    /**
     * 读取指定键值，并赋值默认值
     *
     * @param key        键
     * @param defaultVal 值
     * @param <E>        值类型
     * @return 值
     */
    @NonNull
    @Override
    public <E> E read(@NonNull String key, @NonNull E defaultVal) {
        try {
            mLocker.lock(key);
            return mStorage.get(key, defaultVal);
        } finally {
            mLocker.release(key);
        }
    }

    /**
     * 销毁当前音轨（数据库页）
     *
     * @return {@code true} 销毁成功 {@code false} 销毁失败
     */
    @Override
    public boolean destroy() {
        return mStorage.destroy();
    }

    /**
     * 写入指定键的值
     *
     * @param key   键
     * @param value 值
     * @param <E>   值类型
     */
    @Override
    public <E> void write(@NonNull String key, @NonNull E value) {
        try {
            mLocker.lock(key);
            mStorage.set(key, value);
        } finally {
            mLocker.release(key);
        }
    }

    /**
     * 检查指定键是否存在值
     *
     * @param key 键
     * @return {@code true} 存在 {@code false} 不存在
     */
    @Override
    public boolean exist(@NonNull String key) {
        try {
            mLocker.lock(key);
            return mStorage.exist(key);
        } finally {
            mLocker.release(key);
        }
    }

    /**
     * 清除指定键的值
     *
     * @param key 键
     */
    @Override
    public void clear(@NonNull String key) {
        try {
            mLocker.lock(key);
            mStorage.remove(key);
        } finally {
            mLocker.release(key);
        }
    }

    /**
     * 获得当前音轨（数据库页）所有键
     *
     * @return 键集合
     */
    @NonNull
    @Override
    public List<String> getAllKey() {
        return mStorage.getAllKey();
    }
}
