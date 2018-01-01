package me.limeice.tapesdb;

/**
 * 数据代理存储
 *
 * @param <T> 被代理类类型
 */
class TapesWrap<T> {

    T content;

    TapesWrap(T content) {
        this.content = content;
    }

    @SuppressWarnings("unused")
    TapesWrap() {
    }
}
