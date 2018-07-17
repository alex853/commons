package net.simforge.commons.io;

import net.simforge.commons.misc.Misc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

public class IOHelper {
    private static final Logger logger = LoggerFactory.getLogger(IOHelper.class.getName());

    public static void saveFile(File file, String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        fos.write(content.getBytes());
        fos.close();
    }

    public static void appendFile(File file, String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(content.getBytes());
        fos.close();
    }

    public static String loadFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return readInputStream(fis);
    }

    public static byte[] loadFileAsBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return readInputStreamToByteArrayOutputStream(fis).toByteArray();
    }

    public static String download(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        URLConnection urlConnx = url.openConnection();
        urlConnx.setConnectTimeout(120000);
        urlConnx.setReadTimeout(120000);
        InputStream urlInputStream = urlConnx.getInputStream();
        return readInputStreamWithTimeout(urlInputStream);
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = readInputStreamToByteArrayOutputStream(inputStream);
        return os.toString();
    }

    private static ByteArrayOutputStream readInputStreamToByteArrayOutputStream(InputStream inputStream) throws IOException {
        int bufSize = 8192;
        byte[] buf = new byte[bufSize];
        InputStream is = new BufferedInputStream(inputStream, bufSize);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while (true) {
            int actualRead = is.read(buf, 0, bufSize);
            if (actualRead == -1) {
                break;
            }
            os.write(buf, 0, actualRead);
        }
        inputStream.close();
        return os;
    }

    private static String readInputStreamWithTimeout(InputStream inputStream) throws IOException {
        long timeout = 30000; // 30 secs

        ReadingRunnable read = new ReadingRunnable(inputStream);
        Thread thread = new Thread(read);
        thread.start();
        while (thread.isAlive()) {
            if (read.getLastTs() + timeout < System.currentTimeMillis()) {
                throw new IOException("Stream reading has timed out");
            }
            Misc.sleep(100);
        }
        if (read.getIOException() != null)
            throw read.getIOException();
        inputStream.close();
        return read.getOutputStream().toString();
    }

    public static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        int bufSize = 8192;
        byte[] buf = new byte[bufSize];

        while (true) {
            int actualRead = inputStream.read(buf, 0, bufSize);
            if (actualRead == -1) {
                break;
            }
            outputStream.write(buf, 0, actualRead);
        }
    }

    private static class ReadingRunnable implements Runnable {
        private InputStream inputStream;
        private ByteArrayOutputStream outputStream;
        private IOException ioException;
        private long lastTs = System.currentTimeMillis();

        public ReadingRunnable(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            int bufSize = 8192;
            InputStream is = new BufferedInputStream(inputStream, bufSize);
            byte[] buf = new byte[bufSize];
            outputStream = new ByteArrayOutputStream();

            while (true) {
                lastTs = System.currentTimeMillis();
                int actualRead;
                try {
                    actualRead = is.read(buf, 0, bufSize);
                } catch (IOException e) {
                    ioException = e;
                    break;
                }
                if (actualRead == -1) {
                    break;
                }
                outputStream.write(buf, 0, actualRead);
            }
        }

        public ByteArrayOutputStream getOutputStream() {
            return outputStream;
        }

        public IOException getIOException() {
            return ioException;
        }

        public long getLastTs() {
            return lastTs;
        }
    }

    public static String resourceToPath(Class clazz, String resource) {
        URL url = clazz.getClassLoader().getResource(resource);
        String fn = url.getFile();
        try {
            fn = URLDecoder.decode(fn, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Can't decode file name", e);
            throw new IllegalStateException(e);
        }
        if (fn.toLowerCase().startsWith("/c:")
                || fn.toLowerCase().startsWith("/d:")) {
            fn = fn.substring(1);
        }
        return fn;
    }
}
