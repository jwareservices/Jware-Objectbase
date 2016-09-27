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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import org.jware.objectbase.core.DataHeader;
import org.jware.objectbase.core.DataDirector;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import objectbase.Person;
import objectbase.PhoneNumber;
import org.jware.objectbase.core.DataHeaderFactory;
import org.jware.objectbase.core.DataMarshaller;
import org.jware.objectbase.exception.DataDirectorException;
import org.jware.objectbase.util.Utilities;
import static org.jware.objectbase.util.Utilities.Utility;

/**
 * File: IndexedDataDirector.java Created On: 08/11/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 * @param <T> Header data to use.
 * @param <K> Key.
 */
public class IndexedDataDirector<K, T extends DataHeader> implements DataDirector<K> {

    static final int DEFAULT_LOAD_FACTOR = 5;
    static final int FRAGMENT_THRESHOLD = 5;
    static final int BAD_READ = -1;
    static final String FILE_TYPE = "JWOB";
    static final int FILE_TYPE_LENGTH = FILE_TYPE.length();
    static final int INT_SIZE = 4;
    static final int INDEX_START_OFFSET = FILE_TYPE_LENGTH + INT_SIZE;

    static final byte MAGIC = 1;

    final DataStoreDirector fileDirector;
    final DataHeaderFactory headerFactory;
    String indexFileName;
    Integer recordCount;
    Integer sizeOfIndex;
    int headerSize;
    long dataStartOffset;
    long nextIndexOffsetEntry;
    boolean initialized;
    Map<K, T> index;

    /**
     *
     * @param fileDirector
     * @param factory
     */
    public IndexedDataDirector(final DataStoreDirector fileDirector, final DataHeaderFactory factory) {
        this.fileDirector = fileDirector;
        this.indexFileName = this.fileDirector.getFileName()
                .substring(0, this.fileDirector.getFileName()
                        .indexOf(".")) + ".idx";
        index = new HashMap<>();
//        dataStartOffset = INDEX_START_OFFSET;
        dataStartOffset = 1024;
        recordCount = 0;
        sizeOfIndex = 0;
        headerFactory = factory;
        initialize(); 
    }

    private void initialize() {
        headerSize = calculateSize(allocateHeader());
 //       dataStartOffset = headerSize * recordCount + INDEX_START_OFFSET;
        try {
            fileDirector.openChannel();
            fileDirector.growFile(dataStartOffset - MAGIC);
            fileDirector.writeFile(MAGIC, dataStartOffset);
            System.out.println(fileDirector.getFileLength());
            fileDirector.closeChannel();
        } catch (IOException cause) {
            Utilities.alertDialog(cause.toString(), " Error in initialize");
        }
    }

    private void growFile() {
    }

    private void setNextIndexPosition() {

        if ((recordCount * headerSize) * DEFAULT_LOAD_FACTOR >= dataStartOffset) {
            growFile();
        }
        nextIndexOffsetEntry = INDEX_START_OFFSET + (headerSize * recordCount);
    }

    private void writeIndexEntry(DataHeader header) {
        try {
            setNextIndexPosition();
            writeData(header, nextIndexOffsetEntry);
        } catch (DataDirectorException cause) {
            System.out.println(cause.toString());
        }
    }

    private void readIndexEntries() {
        try {
            T header = allocateHeader();
            header = (T) fileDirector.readFile(INDEX_START_OFFSET, headerSize);
        } catch (IOException | ClassNotFoundException cause) {
            System.out.println(cause.toString());
        }

    }

    public void writeIndexHeaders() throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(indexFileName);
                final BufferedOutputStream bos = new BufferedOutputStream(fos);
                final ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(FILE_TYPE);
            oos.writeInt(recordCount);
            oos.writeLong(dataStartOffset);
            //           for (T header : index.values()) {
            index.values().stream().forEach((header) -> {
                //               writeIndexEntry(header);
                try {
                    oos.writeObject(header);
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            });
            oos.close();
            bos.close();
            fos.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    final void readIndexHeaders() throws IOException, ClassNotFoundException {
        try (final FileInputStream fis = new FileInputStream(indexFileName);
                final BufferedInputStream bis = new BufferedInputStream(fis);
                final ObjectInputStream ois = new ObjectInputStream(bis)) {
            String fileType = (String) ois.readObject();
            if (fileType.equals(FILE_TYPE)) {
                recordCount = (Integer) ois.readInt();
                dataStartOffset = (Long) ois.readLong();
                index = new HashMap(recordCount);
                for (int i = 0; i < recordCount; i++) {
                    T recordHeader = (T) ois.readObject();
                    index.put((K) recordHeader.key(), recordHeader);
                }
            } else {
                Utilities.confirmDialog("This is not an JWOB file", "Exit on Error");
            }
            ois.close();
            bis.close();
            fis.close();
//            initialize();
        } catch (ClassNotFoundException c) {
            Utilities.alertDialog(c.toString(), "Error reading index");
        }
    }

    /*
     * This method is used to locate the header record pointed to
     * by data ptr.  The header will be used to during update and
     * delete operations.
     */
    protected DataHeader locateHeader(final long dataPtr) {
        DataHeader returnValue = null;
        for (final K _key : index.keySet()) {
            DataHeader header = index.get(_key);
            Utilities.assertNotNull(header, "Header value null in method 'locateHeader'");
            if (dataPtr >= header.dataStartPtr() && dataPtr <= header.dataEndPtr()) {
                returnValue = header;
                break;
            }
        }
        return returnValue;
    }

    /*
     * Header utilitiy methods.
     */
    protected final int headerSize() {
        return headerSize;
    }

    protected T allocateHeader() {
        return (T) headerFactory.allocateHeader();
    }

    @Override
    public void insertData(final K key, final Object data) throws DataDirectorException, IllegalArgumentException {
        try {
            DataHeader header = index.get((K) key);
            Utilities.assertNull(header, " Key already exists.");
            final long eof = fileDirector.getFileLength();
            final int size = writeData(data, eof);
            header = allocateHeader();
            header.setSize(size);
            header.setDataEndPtr(eof + (long) size);
            header.setDataStartPtr(eof);
            header.setKey(key);
            index.put((K) key, (T) header);
            //           writeIndexEntry(header);
            recordCount++;
        } catch (final DataDirectorException | IllegalArgumentException cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    @Override
    public final Object retrieveData(final K key) throws DataDirectorException {
        DataHeader header = index.get(key);
        Utilities.assertNotNull(header, "Key value is null in method 'retrieveData'");
        return readData(header);
    }

    /*
     * Incomplete.
     */
    @Override
    public void deleteData(final K key) throws DataDirectorException {
        final DataHeader deleteRecord = index.get(key);
        Utilities.assertNotNull(deleteRecord, "Key value is null in method 'deleteData'");
        /*
         * Is the record to be deleted pointing to the first record
         * in the data portion of the file.  If so reclaim the space
         * by moving the data starting position to end of the records
         * block thus giving it to the index potion at the top of the file.
         */
        if (deleteRecord.dataStartPtr() == dataStartOffset) {
            dataStartOffset = deleteRecord.dataEndPtr();
        } else {
            /*
             * Locate the previous adjacent record and set its end ptr
             * to point to the deleted records end ptr, effectively
             * growing the previous records slot size by that length.
             */
            DataHeader previousRecord = locateHeader(deleteRecord.dataStartPtr() - 1);
            Utilities.assertNotNull(previousRecord, " Previous record is null in 'deleteData'");
            previousRecord.setDataEndPtr(deleteRecord.dataEndPtr());
        }
        index.remove(key);
        recordCount--;
    }

    /**
     *
     * @param key
     * @param data
     * @throws DataDirectorException
     */
    @Override
    public void updateData(final K key, Object data) throws DataDirectorException {
        final DataHeader headerRecordForKey = index.get((K) key);
        Utilities.assertNotNull(headerRecordForKey, "Key value is null in method 'updateData'");
        /*
         * If the updated data cannot fit into its current slot in the file
         * then create a new header for it, set the key value, delete the old
         * data and header information then call the insert method to set up
         * the new header data and write the udpated data to the EOF.  
         */
        if (calculateSize(data) > headerRecordForKey.dataSize()) {
            DataHeader header = allocateHeader();
            header.setKey(key);
            deleteData((K) key);
            insertData(key, data);
        } else {
            /*
             * It does fit into its current slot, so write it.
             */
            writeData(data, headerRecordForKey.dataStartPtr());
        }
    }

    protected final int writeData(final Object data, final long offset) throws DataDirectorException {
        int size = BAD_READ;
        try {
            size = fileDirector.writeFile(data, offset + dataStartOffset);
//            size = fileDirector.writeFile(data, offset + dataStartOffset);
        } catch (IOException cause) {
            throw new DataDirectorException(cause.toString());
        }
        return size;
    }

    protected Object readData(final DataHeader header) throws DataDirectorException {
        Object returnValue;
        try {
            returnValue = fileDirector.readFile(header.dataStartPtr() + dataStartOffset, header.dataSize());
 //           returnValue = fileDirector.readFile(header.dataStartPtr() + dataStartOffset, header.dataSize());
        } catch (IOException | ClassNotFoundException cause) {
            throw new DataDirectorException(cause.toString());
        }
        return returnValue;
    }

    public final int getRecordCount() {
        return recordCount;
    }

    protected final int calculateSize(Object data) {
        return ((DataMarshaller) fileDirector.getMarshaller()).writeData(data).length;
    }

    public void closeIndex() {
        try {
            writeIndexHeaders();
        } catch (IOException cause) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, " ", cause.toString() + " writing index.");
        }
    }

    public final String getfileName() {
        return indexFileName;
    }

    public void deleteFile() {
        File f = new File(indexFileName);
        if (f.exists()) {
            f.delete();
        }
    }

    public final int getSizeOfIndex() {
        sizeOfIndex = calculateSize(index);
        return sizeOfIndex;
    }

    public final long getSizeOfFile() {
        return fileDirector.getFileLength();
    }

    /*
     * These two methods may ultimately be package access only,
     * public for now to give access to test case.
     */
    public final Collection<T> getIndexValues() {
        return index.values();
    }

    public DataHeader getHeader(K record) {
        return index.get(record);
    }

    public static void main(String[] args) {
        DataMarshaller<Person> marshaller = new ObjectDataMarshaller();
        DataStoreDirector dm;
        IndexedDataDirector<Integer, DataHeader> id;
        int tot = 10;
        Person[] people = new Person[tot];
        try {
            dm = DataStoreDirector.createDataStoreDirector("Temp.jwob", marshaller);
            id = new IndexedDataDirector(dm, new DiskRecordHeaderFactory());
            Utilities.Clock clock = Utility.getTimer();
            int i = 0;
            for (; i < tot; i++) {
                people[i] = new Person(i, "Paul", "Jackson",
                        new PhoneNumber(Utilities.getRandom(111, 900), Utilities.getRandom(111, 888), Utilities.getRandom(1111, 9000)), null);
            }
            dm.openChannel();
            clock.startTime();
            i = 0;
            for (; i < tot; i++) {
                id.insertData(i, people[i]);
            }
            System.out.println("\nTime for (" + i + ") inserts: " + clock.step() / 1000f + " seconds");

            people[1].firstName = "A very much larger name";
            people[1].lastName = "Along with a much larger last name to boot";
            id.updateData(people[1].personID, people[1]);

            dm.closeChannel();

            id.writeIndexHeaders();
            clock.startTime();
            i = 0;
            dm.openChannel();

            id.readIndexHeaders();
            for (; i < tot; i++) {
                Person p = (Person) id.retrieveData(i);
 //               System.out.println(p + "\n\t : " + id.getHeader(i));
            }
            System.out.println("\nTime for (" + i + ") reads: " + clock.step() / 1000f + " seconds");
            dm.closeChannel();
            id.closeIndex();

            dm.deleteFile();
            id.deleteFile();
        } catch (IOException | DataDirectorException | IllegalArgumentException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }
    }
}
/**
 * ********************
 * Time for (1000) inserts: 5.908 seconds
 *
 * Time for (1000) inserts: 6.025 seconds Time for (1000) reads: 0.732 seconds
 *
 * Time for (1000) inserts: 5.865 seconds Time for (1000) reads: 0.156 seconds
 *
 * Time for (1000) inserts: 5.797 seconds Time for (1000) reads: 0.162 seconds
 *
 * Time for (1000) inserts: 5.299 seconds Time for (1000) reads: 0.129 seconds
 *
 * Time for (1000) inserts: 5.832 seconds Time for (1000) reads: 0.13 seconds
 *
 * Time for (1000) inserts: 6.13 seconds Time for (1000) reads: 0.152 seconds
 *
 * Time for (1000) inserts: 6.457 seconds Time for (1000) reads: 0.126 seconds
 *
 * Time for (1000) inserts: 5.151 seconds Time for (1000) reads: 0.13 seconds
 *
 * Time for (1000) inserts: 5.22 seconds Time for (1000) reads: 0.122 seconds
 *
 * Time for (1000) inserts: 4.569 seconds Time for (1000) reads: 0.123 seconds
 *
 * Time for (1000) inserts: 5.195 seconds Time for (1000) reads: 0.123 seconds
 *
 * Time for (1000) inserts: 5.051 seconds Time for (1000) reads: 0.131 seconds
 *
 * Time for (1000) inserts: 5.146 seconds Time for (1000) reads: 0.127 seconds
 *
 * Time for (1000) inserts: 5.222 seconds Time for (1000) reads: 0.133 seconds
 * ***********************
 */
//Time for (1000) inserts: 3.898 ms
/*
 RandomAccessFile memoryMappedFile = new RandomAccessFile("somefile.fil", "rw");
 //Mapping a file into memory
 MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, count);
 //Writing into Memory Mapped File
 for (int i = 0; i < count; i++) {
 out.put((byte) 'A');
 }
 System.out.println("Writing to Memory Mapped File is completed");
 //reading from memory file in Java
 for (int i = 0; i < 10 ; i++) {
 System.out.print((char) out.get(i));
 }
 System.out.println("Reading from Memory Mapped File is completed");

 Read more: http://javarevisited.blogspot.com/2012/01/memorymapped-file-and-io-in-java.html#ixzz3iGyKqcnn


 FileInputStream f = new FileInputStream( name );
 FileChannel ch = f.getChannel( );
 byte[] barray = new byte[SIZE];
 ByteBuffer bb = ByteBuffer.wrap( barray );
 long checkSum = 0L;
 int nRead;
 while ( (nRead=ch.read( bb )) != -1 )
 {
 for ( int i=0; i<nRead; i++ )
 checkSum += barray[i];
 bb.clear( );
 }

 */
