package me.limeice.tapesdb;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;


final class DbStorage extends DbStorageCore {

    private final ThreadLocal<Kryo> mKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return createKryo();
        }
    };

    DbStorage(Context context, String trackName, ArrayMap<Class, Serializer> serializers) {
        super(context, trackName, serializers);
    }

    @Override
    Kryo getKryo() {
        return mKryo.get();
    }
}
