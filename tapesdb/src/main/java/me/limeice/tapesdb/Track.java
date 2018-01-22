package me.limeice.tapesdb;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Track 磁带音轨
 * Created by Lime on 2018/1/1.
 */

@SuppressWarnings("SameParameterValue")
public interface Track {

    /**
     * 读取指定键的值
     *
     * @param key 键
     * @param <E> 值类型
     * @return 值
     */
    @Nullable
    <E> E read(@NonNull String key);

    /**
     * 读取指定键值，并赋值默认值
     *
     * @param key        键
     * @param defaultVal 值
     * @param <E>        值类型
     * @return 值
     */
    @NonNull
    <E> E read(@NonNull String key, @NonNull E defaultVal);

    /**
     * 销毁当前音轨（数据库页）
     *
     * @return {@code true} 销毁成功 {@code false} 销毁失败
     */
    boolean destroy();

    /**
     * 写入指定键的值
     *
     * @param key   键
     * @param value 值
     * @param <E>   值类型
     */
    <E> void write(@NonNull String key, @NonNull E value);

    /**
     * 检查指定键是否存在值
     *
     * @param key 键
     * @return {@code true} 存在 {@code false} 不存在
     */
    boolean exist(@NonNull String key);

    /**
     * 清除指定键的值
     *
     * @param key 键
     */
    void clear(@NonNull String key);

    /**
     * 获得当前音轨（数据库页）所有键
     *
     * @return 键集合
     */
    @NonNull
    List<String> getAllKey();
}
