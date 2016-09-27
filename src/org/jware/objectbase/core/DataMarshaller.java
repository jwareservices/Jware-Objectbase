package org.jware.objectbase.core;

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
 * File: DataMarshaller.java 
 * Created On: 08/17/2015
 * 
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose: Marshall data from a given type to bytes, back again.
 * 
 * @param <T> The type of data to be used.
 */
public interface DataMarshaller<T> {
    
    /*
    * Read from bytes, marshall to T
    */
    public T readData(byte[] data);
    /*
    * Write from T, marshall to bytes
    */
    public byte[] writeData(T value);
    /*
     * @return Size of T.
     */
    public int getDataSize();
}
