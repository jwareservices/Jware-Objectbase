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

import org.jware.objectbase.core.DataMarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jware.objectbase.util.Utilities;

 
/**
 * File: ObjectDataMarshaller.java Created On: 08/17/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose: Convert Java objects from and to byte arrays by serialization.
 * Objects of this class will typically be used for a single shot serialization.
 * The size member is only guaranteed to be valid if no exceptions are thrown.
 * Objects passed in must either implement the Serializable or
 * Externalizable interface.
 * 
 * @see DataMarshaller
 * @see Utilities
 * @param <T> The data type to be Marshalled.
 */
public final class ObjectDataMarshaller<T> implements DataMarshaller<T> {

    /* 
     * Size of the current object, -1 if invalid. 
     */
    int size;

    /**
     * Construct
     */
    public ObjectDataMarshaller() {
        size = -1;
    }

    /**
     * Read in bytes and de-serialize them into an object 
     * of type T
     * 
     * @param bytes
     * @return the data object of type T
     */
    @Override
    public final T readData(final byte[] bytes) {
        Object data = null;
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            data = ois.readObject();
        } catch (final IOException | ClassNotFoundException cause) {
            Utilities.alertDialog(cause.toString(), " Marshaller read");
        }
        return (T) data;
    }

    /**
     * Convert data of type T into a byte array.
     * 
     * @param data
     * @return the serialized data in bytes
     */
    @Override
    public final byte[] writeData(final T data) {
        byte[] bytes = null;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(data);
            bytes = baos.toByteArray();
            size = bytes.length;
        } catch (IOException cause) {
            Utilities.alertDialog(cause.toString(), " Marshaller write");
        }
        return bytes;
    }

    /**
     * Return the size of the current length of the bytes being serialized.
     * 
     * @return length of byte array.
     */
    @Override
    public final int getDataSize() {
        return size;
    }
    
    
    
    // TEST //
    public static void main(String[] args) {
        
        ObjectDataMarshaller<String> dm = new ObjectDataMarshaller();
                
        byte[] bytes = dm.writeData("Hello, World");
        
        int size = dm.getDataSize();
        
        String str = dm.readData(bytes);
        
        System.out.println("Bytes: " + bytes);
        System.out.println("String: " + str);
        System.out.println("Size: " + size );
    }
  }
 