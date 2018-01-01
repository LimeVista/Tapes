package me.limeice.tapesdb;

import android.support.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public final class TapesDbException extends RuntimeException {

    public TapesDbException(@NonNull String msg) {
        super(msg);
    }

    public TapesDbException(@NonNull String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
