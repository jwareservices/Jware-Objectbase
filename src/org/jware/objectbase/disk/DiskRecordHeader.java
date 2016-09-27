package org.jware.objectbase.disk;

import org.jware.objectbase.core.DataHeader;
import org.jware.objectbase.core.DataHeaderFactory;

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
 * File: DiskRecordHeader.java Created On: 02/00/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 * @param <T>
 */
public class DiskRecordHeader<T> implements DataHeader<T> {

    protected T key;
    protected int dataSize;
    protected Long dataStartPtr;
    protected Long dataEndPtr;

    public DiskRecordHeader() {
    }

    public DiskRecordHeader(final T key, final int dataSize, final long dataStartPtr, final long dataEndPtr) {
        this.key = key;
        this.dataSize = dataSize;
        this.dataStartPtr = dataStartPtr;
        this.dataEndPtr = dataEndPtr;
    }

    @Override
    public T key() {
        return key;
    }

    @Override
    public void setKey(final T value) {
        key = value;
    }

    @Override
    public int dataSize() {
        return dataSize;
    }

    @Override
    public Long dataEndPtr() {
        return dataEndPtr;
    }

    @Override
    public Long dataStartPtr() {
        return dataStartPtr;
    }

    @Override
    public void setDataEndPtr(final long size) {
        dataEndPtr = size;
    }

    @Override
    public void setDataStartPtr(final long offset) {
        dataStartPtr = offset;
    }

    @Override
    public void setSize(final int size) {
        dataSize = size;
    }

    public final String toString() {
        final StringBuilder string = new StringBuilder();

        string.append("Key["+ key + "], ");
        string.append("Size[" + dataSize + "], ");
        string.append("Start Ptr[" + dataStartPtr + "], ");
        string.append("Ending Ptr[" + dataEndPtr + "], ");
        string.append("Slot Size[" + (dataEndPtr - dataStartPtr) + "]");

        return string.toString();
    }

}
