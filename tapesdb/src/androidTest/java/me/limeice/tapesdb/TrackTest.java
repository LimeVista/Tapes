package me.limeice.tapesdb;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;


public class TrackTest {

    private Track track;

    @Before
    public void setUp() throws Exception {
        Tapes.init(getTargetContext());
        track = Tapes.track();
        assertNotNull(track);
        assertTrue(Tapes.track().destroy());
    }

    @Test
    public void read() throws Exception {
        track.write("t_read", 3.14f);
        Float data = track.read("t_read");
        assertNotNull(data);
        assertEquals(3.14f, data, 0f);
        List<String> list = track.read("t_null");
        assertNull(list);
    }

    @Test
    public void readWithDefault() throws Exception {
        List<String> list = track.read("t_null", new ArrayList<String>());
        assertNotNull(list);
    }

    @Test
    public void destroy() throws Exception {
        track.write("t_destroy1", new Data());
        track.write("t_destroy2", 123);
        assertTrue(track.destroy());
        assertFalse(track.exist("t_destroy1"));
        assertFalse(track.exist("t_destroy2"));
    }

    @Test
    public void write() throws Exception {
        track.write("t_write", new Data());
        Data data = track.read("t_write");
        assertNotNull(data);
        assertEquals(data, new Data());
    }

    @Test
    public void exist() throws Exception {
        track.write("t_exist", new Data());
        assertTrue(track.exist("t_exist"));
        assertFalse(track.exist("t_null"));
    }

    @Test
    public void clear() throws Exception {
        track.write("t_clear", new Data());
        track.clear("t_clear");
        assertNull(track.read("t_clear"));
    }

    @Test
    public void getAllKey() throws Exception {
        track.write("t_allKey", new Data());
        track.write("t_allKey_1", 123);
        track.write("t_allKey_2", 1.23);
        List<String> list = track.getAllKey();
        System.out.println(list.toString());
    }

    public static class Data {
        String x = "123";
        int y = 2018;

        @Override
        public String toString() {
            return "Data{" +
                    "x='" + x + '\'' +
                    ", y=" + y +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Data
                    && x.equals(((Data) obj).x)
                    && y == ((Data) obj).y;
        }
    }
}