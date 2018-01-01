package me.limeice.tapesdb;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Track 磁带音轨
 * Created by Lime on 2018/1/1.
 */

@SuppressWarnings({"unused", "SameParameterValue", "WeakerAccess"})
public interface TrackAsync {

    /**
     * 任务
     *
     * @param <IN>
     */
    interface Task<IN> {
        void callback(IN val);
    }

    /**
     * 读取指定键值，并赋值默认值
     *
     * @param key      键
     * @param callback 参数回调
     * @param <E>      值类型
     */
    <E> void read(@NonNull String key, @NonNull Task<E> callback);

    /**
     * 销毁当前音轨（数据库页）
     *
     * @param callback 参数回调
     */
    void destroy(@Nullable Task<Boolean> callback);

    /**
     * 写入指定键的值
     *
     * @param key      键
     * @param value    值
     * @param <E>      值类型
     * @param callback 参数回调
     */
    <E> void write(@NonNull String key, @NonNull E value, @Nullable Task<E> callback);

    /**
     * 检查指定键是否存在值
     *
     * @param key      键
     * @param callback 参数回调
     */
    void exist(@NonNull String key, @NonNull Task<Boolean> callback);

    /**
     * 清除指定键的值
     *
     * @param callback 参数回调
     * @param key      键
     */
    void clear(@NonNull String key, @Nullable Task<Void> callback);

    /**
     * 获得当前音轨（数据库页）所有键
     *
     * @param callback 参数回调
     */
    void getAllKey(@NonNull String key, @NonNull Task<List<String>> callback);
}
