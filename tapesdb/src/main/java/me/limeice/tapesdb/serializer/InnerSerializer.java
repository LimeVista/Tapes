package me.limeice.tapesdb.serializer;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.net.URI;
import java.util.UUID;

public class InnerSerializer {

    public static class URISerializer extends Serializer<java.net.URI> {

        public URISerializer() {
            setImmutable(true);
        }

        @Override
        public void write(final Kryo kryo, final Output output, final URI uri) {
            output.writeString(uri.toString());
        }

        @Override
        public URI read(final Kryo kryo, final Input input, final Class<URI> uriClass) {
            return URI.create(input.readString());
        }
    }

    public static class UUIDSerializer extends Serializer<UUID> {

        public UUIDSerializer() {
            setImmutable(true);
        }

        @Override
        public void write(Kryo kryo, Output output, final UUID uuid) {
            output.writeLong(uuid.getMostSignificantBits());
            output.writeLong(uuid.getLeastSignificantBits());
        }

        @Override
        public UUID read(Kryo kryo, Input input, Class<UUID> type) {
            return new UUID(input.readLong(), input.readLong());
        }
    }
}
