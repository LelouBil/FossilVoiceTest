package net.leloubil.fossilvoicetest;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class BlockingByteArrayOutputStream extends InputStream {

    private static final String TAG = "BlockingByteArrayOutput";

    private final LinkedBlockingQueue<Integer> buffer = new LinkedBlockingQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);


    @Override
    public int read() throws IOException {
        if (closed.get()) {
            Log.d(TAG, "Stream is closed");
            return -1;
        }
        try {
            return buffer.take();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public void write(byte[] bytes) throws IOException {
        if (closed.get()) {
            throw new IOException("Stream is closed");
        }
        for (byte b : bytes) {
            buffer.add(b & 0xFF);
        }
//        System.out.println("Write count: " + buffer.size());
    }

    public void closeInput() {
        closed.set(true);
    }
}
