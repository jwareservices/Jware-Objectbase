package objectbase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import org.jware.objectbase.util.Utilities;
import static org.jware.objectbase.util.Utilities.Utility;

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
/**
 * File: ObjectStore.java Created On: 00/25/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 */
public class ObjectStore {

    private File file;
    private FileInputStream fis;
    private FileOutputStream fos;
    private FileChannel channel;
    private final static int _DELETE = -1;
    private final static int BSIZE = 1024;
    private final static int BSIZE_FLAG = 4;
    private long NEW_POSITION;

    public ObjectStore(String fileName) throws IOException {
        this.file = new File(fileName);
    }

    /**
     * Creates a channel to read from a file
     */
    public void read() throws IOException {
        FileInputStream fis = new FileInputStream(file);
        this.channel = fis.getChannel();
    }

    /**
     * Creates a channel to write to a file in appended mode
     */
    public void write() throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        this.channel = fos.getChannel();
    }

    /**
     * Returns the object at the given index.
     */
    public Object getObject(int index) throws IOException,
            ClassNotFoundException {
        seekToIndex(index);
        return readObject();
    }

    public void close() throws IOException {
        this.channel.close();
    }

    /**
     * Delete the object at index from the store.
     */
    public void deleteObject(int index) throws IOException {
        seekToIndex(index);

        NEW_POSITION = (long) (this.channel.position() + BSIZE_FLAG);
        this.channel.position(NEW_POSITION);

        writeDeleteFlag(_DELETE);
    }

    /**
     * Adds this object to the store. Always added at the end.
     */
    public void addObject(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        byte[] buffer = bos.toByteArray();

// move to the last position
        this.channel.position(this.channel.size());

        writeSizeInfo(buffer);

        writeDeleteFlag(0);

// Write the actual object as byteBuffer
        this.channel.write(ByteBuffer.wrap(buffer));

    }

    private void writeDeleteFlag(int flag) throws IOException {
// write an int flag to identify, whether deleted or not, if delete, its
// _DELETE, i.e -1 else 0.
        ByteBuffer flagBuffer = ByteBuffer.allocateDirect(BSIZE_FLAG);
        flagBuffer.asIntBuffer().put(flag);
        this.channel.write(flagBuffer);
    }

    private void writeSizeInfo(byte[] buffer) throws IOException {
// write the number of bytes in buffer to be written
        ByteBuffer sizeBuffer = ByteBuffer.allocate(BSIZE_FLAG);
        sizeBuffer.asIntBuffer().put(buffer.length);
        this.channel.write(sizeBuffer);
    }

    /**
     * Returns the number of objects currently in the store.
     */
    public int getStoredCount() throws IOException {

        this.channel.position(0);
        int counter = 0;
        while (this.channel.position() < this.channel.size()) {

            int skipBytes = readSizeInfo();
            int deleteFlag = readDeleteFlag();

            if (deleteFlag != _DELETE) {
                counter++;
            }

// skip number of bytes
            NEW_POSITION = (long) (this.channel.position() + skipBytes);
            this.channel.position(NEW_POSITION);
        }
        System.out.println("Total Objects Written to a file is: " + counter);
        return counter;
    }

    /**
     * @return @throws IOException
     */
    private int readDeleteFlag() throws IOException {
// read flag information
        ByteBuffer flagBuffer = ByteBuffer.allocate(BSIZE_FLAG);
        this.channel.read(flagBuffer);
        flagBuffer.rewind();
        int deleteFlag = flagBuffer.getInt();
        return deleteFlag;
    }

    /**
     * @return @throws IOException
     */
    private int readSizeInfo() throws IOException {
// read and skip size information
        ByteBuffer sizeBuffer = ByteBuffer.allocate(BSIZE_FLAG);
        this.channel.read(sizeBuffer);
        sizeBuffer.rewind();
        int skipBytes = sizeBuffer.getInt();
        return skipBytes;
    }

    /**
     * Returns all the objects in the store as a List.
     */
    public List getObjects() throws IOException, ClassNotFoundException {
        List list = new LinkedList();
        this.channel.position(0);
        while (this.channel.position() < this.channel.size()) {

            int skipBytes = readSizeInfo();
            int deleteFlag = readDeleteFlag();

            if (deleteFlag != _DELETE) {
                this.channel.position(this.channel.position() - 8);
                list.add(readObject());
            }

// skip number of bytes
            NEW_POSITION = (long) (this.channel.position() + skipBytes);
            this.channel.position(NEW_POSITION);

        }
        return list;
    }

    /**
     * Return al the objects inside specific range mentioned in the store as a
     * List
     */
    public List getObjects(int fromIndex, int toIndex) throws IOException,
            ClassNotFoundException {
        List list = new LinkedList();
        for (int i = fromIndex + 1; i < toIndex; i++) {
            list.add(getObject(i));
        }
        return list;
    }

    /**
     * Compacts the underlying file by removing all the dead space aka deleted
     * records.
     */
    public void compact() throws IOException {
// TO DO
    }

    private void seekToIndex(int index) throws IOException {
        this.channel.position(0);
        int counter = 0;
        /*
         * seek to the correct index skipping those we don't want. deleted
         * records do not count as space
         */
        while (counter < index) {
            if (this.channel.position() == this.channel.size()) {
                throw new IOException("Object " + index + " not present.");
            }

            int skipBytes = readSizeInfo();
            int deleteFlag = readDeleteFlag();

// skip number of bytes
            NEW_POSITION = (long) (this.channel.position() + skipBytes);
            this.channel.position(NEW_POSITION);

            if (deleteFlag != _DELETE) {
                counter++;
            }

        }
        /*
         * now in theory we are in the right place but this index could be
         * deleted. keep reading until we are at an existing record
         */
        boolean found = false;
        while (!found) {

            if (this.channel.position() == this.channel.size()) {
                throw new IOException("Object " + index + " not present.");
            }

            int skipBytes = readSizeInfo();
            int deleteFlag = readDeleteFlag();

            if (deleteFlag != _DELETE) {
                this.channel.position(this.channel.position() - 8);
                found = true;
            } else {
// skip number of bytes
                NEW_POSITION = (long) (this.channel.position() + skipBytes);
                this.channel.position(NEW_POSITION);
            }

        }
    }

// does the actual byte to Object comversion for getObject(int) and
// getObjects()
    private Object readObject() throws IOException, ClassNotFoundException {
        int bytes = readSizeInfo();
        readDeleteFlag();
        byte[] buff = new byte[bytes];
        this.channel.read(ByteBuffer.wrap(buff));
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buff));
        return ois.readObject();
    }

    public static void main(String[] args) {

        try {
            ObjectStore obs = new ObjectStore("Temp.obj");
            Utilities.Clock clock = Utility.getTimer();
            int tot = 1000;
            int str=500;
            int end=675;
            int cnt=0;
            Person[] people = new Person[tot];
            obs.write();
            clock.startTime();
            for (int i=0; i < tot; i++) {
                Person p = new Person(i, "Paul", "Jackson",
                    new PhoneNumber(Utilities.getRandom(111, 900), Utilities.getRandom(111, 888), Utilities.getRandom(1111, 9000)), null);
                obs.addObject(p);
                cnt++;
            }
            System.out.println("\nTime for (" + cnt + ") inserts: " + clock.step() / 1000f + " ms");
            obs.close();
            obs.read();
            cnt=0;
            clock.startTime();
            for (int i=0; i < tot; i++) {
                people[i] = (Person) obs.getObject(i);
                cnt++;
            }
            System.out.println("\nTime for (" + cnt + ") reads: " + clock.step() / 1000f + " ms");

            for (int i=0; i < tot; i++) {
                System.out.println(people[i]);
            }
            obs.close();

        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}
