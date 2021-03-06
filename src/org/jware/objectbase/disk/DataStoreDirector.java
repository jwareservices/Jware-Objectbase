package org.jware.objectbase.disk;

/*
 * Copyright (C) 2015 J. Paul Jackson <jwareservices@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import objectbase.Person;
import objectbase.PhoneNumber;
import org.jware.objectbase.util.Utilities;
import org.jware.objectbase.core.DataMarshaller;
import static org.jware.objectbase.util.Utilities.Utility;

/**
 * File: DataStoreDirector.java Created On: 08/17/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose: The DataStoreDirector is the main file handler in the library. It
 * handles or directs all access to the underlying file system.
 * DataStoreDirector follows the singleton patten and uses a single instance to
 * allow access to the class. All access to the file goes through the single
 * instance of the class.
 * <p>
 * This class uses an instance of a <code>DataMarshaller</code> to serialize and
 * de-serialize a given data type into the raw bytes to be read and written to
 * the file.
 * <p>
 * The class holds a File object from which it obtains a channel to do the
 * reading and writing of the data. The channel is obtained by calling
 * RandomAccessFile.getChannel(). You must use the open channel method to set up
 * the channel for I/O, and closeChannel <b>should</b> be called to avoid a
 * IOException at subsequent reads and writes.
 *
 * @see ObjectDataMarshaller
 */
public final class DataStoreDirector {

    public static DataStoreDirector instance = null;

    static DataMarshaller marshaller;
    static File file = null;
    static FileChannel channel = null;
    RandomAccessFile raf;
    long fileSize = 0L;

    private DataStoreDirector() {
    }

    /**
     * @param fileName
     * @param _marshaller
     * @return
     */
    public static DataStoreDirector createDataStoreDirector(final String fileName, final DataMarshaller _marshaller) {
        if (null == instance) {
            instance = new DataStoreDirector();
            marshaller = _marshaller;
            file = new File(fileName);
        }
        return instance;
    }

    /**
     *
     * @param _file
     */
    protected void setFile(final File _file) {
        Utilities.assertNotNull(file, "Invalid file.");
        file = _file;
    }

    protected final String getFileName() {
        Utilities.assertNotNull(file, "Invalid file.");
        return file.getName();
    }

    public final long getFileLength() {
        Utilities.assertNotNull(file, "Invalid file.");
        return file.length();
    }

    public void growFile(final long size) throws IOException {
        Utilities.assertNotNull(file, "Invalid file.");
        Utilities.assertNotNull(channel, "Invalid channel.");
        if (size > getFileLength()) {
            channel.position(size);
        }
    }

    public void setPosition(long offset) throws IOException {
        Utilities.assertNotNull(channel, "Invalid channel.");
        channel.position(offset);
    }

    public void closeChannel() throws IOException {
        Utilities.assertNotNull(channel, "Invalid channel.");
        channel.close();
    }

    public void openChannel() throws IOException {
        Utilities.assertNotNull(file, "Invalid file.");
        channel = new RandomAccessFile(file, "rw").getChannel();
    }

    public final int writeFile(final Object data, final long offset) throws IOException {
        channel.position(offset);
        return channel.write(ByteBuffer.wrap(marshaller.writeData(data)));
    }

    public Object readFile(final long offset, final int size) throws IOException, ClassNotFoundException {
        final byte[] data = new byte[size];
        channel.position(offset);
        channel.read(ByteBuffer.wrap(data));
        return marshaller.readData(data);
    }

    public DataMarshaller getMarshaller() {
        return marshaller;
    }

    public void deleteFile() {
        if (file.exists()) {
            file.delete();
        }
    }

    public static void main(String[] args) {
        DataMarshaller<Person> _marshaller = new ObjectDataMarshaller();
        DataStoreDirector dir = DataStoreDirector.createDataStoreDirector("Temp.jwob", _marshaller);
        int tot = 100;
        int i = 0;
        long[] length = new long[tot];
        Utilities.Clock clock = Utility.getTimer();
        clock.startTime();
        try {
            length[0] = 1;
            dir.openChannel();
            long offset = 0;
            for (; i < tot; i++) {
                Person p = new Person(i, "Paul", "Jackson",
                        new PhoneNumber(Utilities.getRandom(111, 900), Utilities.getRandom(111, 888), Utilities.getRandom(1111, 9000)), null);

                length[i] = dir.writeFile(p, i * offset);
                offset = _marshaller.getDataSize();
            }
            System.out.println("\nTime for (" + i + ") inserts: " + clock.step() / 1000f + " ms");
            clock.startTime();
            for (i = 0; i < tot; i++) {
                Person p = (Person) dir.readFile(i * offset, (int) offset);
            }
            System.out.println("\nTime for (" + tot + ") reads: " + clock.step() / 1000f + " ms");

            dir.closeChannel();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

    }
}

/*
 Time for (5000) inserts: 0.305 ms

 Time for (5000) reads: 1.053 ms

 Time for (10000) inserts: 0.35 ms

 Time for (10000) reads: 1.088 ms


 Time for (1000) inserts: 0.113 ms

 Time for (1000) reads: 0.249 ms

 Time for (1000) inserts: 0.152 ms

 Time for (1000) reads: 0.476 ms
 hs
 Time for (1000) inserts: 0.127 ms

 Time for (1000) reads: 4.392 ms


 Time for (10000) inserts: 0.358 ms

 Time for (10000) reads: 1.458 ms

 Time for (10000) inserts: 0.339 ms

 Time for (10000) reads: 1.013 ms


 Time for (10000) inserts: 0.353 ms

 Time for (10000) reads: 0.989 ms

 Time for (10000) inserts: 0.444 ms

 Time for (10000) reads: 1.386 ms

 Time for (4000000) inserts: 32.17 ms

 Time for (4000000) reads: 105.637 ms


Time for (4000000) inserts: 29.481 ms

Time for (4000000) reads: 72.728 ms



*/
