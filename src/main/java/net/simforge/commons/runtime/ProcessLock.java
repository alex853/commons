package net.simforge.commons.runtime;

import net.simforge.commons.io.IOHelper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;

class ProcessLock {
    private String process;
    private FileLock fileLock;
    private FileChannel fileChannel;
    private String fileName;

    private ProcessLock(String process) {
        this.process = process;
        fileName = (new StringBuilder()).append("./").append(process).append(".lck").toString();
    }

    public static ProcessLock lock(String process) {
        ProcessLock lock = new ProcessLock(process);
        lock.lockMarker();
        return lock;
    }

    private void lockMarker() {
        try {
            RandomAccessFile raFile = new RandomAccessFile(fileName, "rw");
            fileChannel = raFile.getChannel();
            fileLock = fileChannel.tryLock();
            if (fileLock != null) {
                String s = (new StringBuilder()).append((new Date()).toString()).append("         ").append(process).append(" started").toString();
                fileChannel.write(ByteBuffer.allocate(s.length()).put(s.getBytes()));
                fileChannel.force(true);
            } else {
                throw new RuntimeException("Could not lock marker for process " + process);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            File file = new File((new StringBuilder()).append("./starts/").append(sdf.format(new Date())).append("   ").append(process).toString());
            file.getParentFile().mkdirs();
            IOHelper.saveFile(file, "");
        } catch (IOException e) {
            throw new RuntimeException("Could not lock marker for process " + process, e);
        }
    }

    public void release() {
        try {
            fileLock.release();
            fileChannel.close();
            (new File(fileName)).delete();
        } catch (IOException e) {
            throw new RuntimeException("Could not unlock marker for process " + process, e);
        }
    }
}
