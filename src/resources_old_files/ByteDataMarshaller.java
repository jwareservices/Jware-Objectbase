package resources_old_files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
 * File: ByteDataMarshaller.java Created On: 00/17/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose: Convert Java objects from and to byte arrays by serialization.
 * Objects of this class will typically be used for a single shot serialization.
 * The size member is only guaranteed to be correct if no exceptions are thrown.
 */
public final class ByteDataMarshaller<T> implements DataMarshaller<T> {

    /* 
    * Size of the current object, guarantees only to be valid for a single invocation.
    */
    int size;

    public ByteDataMarshaller() {
        size = -1;
    }

    @Override
    public final T readData(final byte[] bytes) {
        Object data = null;
        try {
            ByteArrayInputStream bais;
            ObjectInputStream ois;
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            data = ois.readObject();
        } catch (IOException | ClassNotFoundException cause) {
        }
        return (T)data;
    }

    @Override
    public final byte[] writeData(final T data) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            bytes = baos.toByteArray();
            size=bytes.length;
        } catch (IOException cause) {
        }
        return bytes;
    }

    @Override
    public final int getDataSize() {
        return size;
    }
}
