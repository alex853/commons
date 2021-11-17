package net.simforge.commons.bm;

import net.simforge.commons.legacy.BM;

import java.io.Closeable;

public class BMC implements Closeable {
    private BMC() {}

    public static BMC start(String point) {
        BM.start(point);
        return new BMC();
    }

    @Override
    public void close() {
        BM.stop();
    }
}
