package me.limeice.tapesdb;

import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.net.URI;

import me.limeice.tapesdb.serializer.InnerSerializer;

import static org.junit.Assert.*;
import static android.support.test.InstrumentationRegistry.getTargetContext;

public class TapesTest {

//    @Test
//    public void destroy() throws Exception {
//        Tapes.destroy(getTargetContext());
//        assertFalse(new File(getTargetContext().getFilesDir(), DbStorage.DBF).exists());
//        Log.d(TapesTest.class.getSimpleName(), "destroy() is call!");
//    }

    @Test
    public void track() throws Exception {
        Tapes.init(getTargetContext());
        Track track = Tapes.track();
        Track track1 = Tapes.track("track1");

        Track trackInner = Tapes.track(Tapes.INNER_DB_NAME);
        assertNotEquals(track, track1);
        assertEquals(track, trackInner);
    }

    @Test
    public void addCustomSerializer() throws Exception {
        Tapes.addCustomSerializer(URI.class, new InnerSerializer.URISerializer());
    }
}