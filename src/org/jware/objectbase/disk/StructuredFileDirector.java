package org.jware.objectbase.disk;

import java.io.File;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import org.jware.objectbase.core.DataMarshaller;

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
 * File: StructuredFileDirector.java Created On: Sep 1, 2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 *
 */
public class StructuredFileDirector {

    class Page {

        ByteBuffer pageBuffer;
        long pageNumber;

        Page(long pageNum, int bufferSize) {
            pageNumber = pageNum;
            this.pageBuffer = ByteBuffer.allocate(bufferSize);
        }

        public final long getPageNumber() {
            return pageNumber;
        }

        public ByteBuffer getFrame(int indexInto) {
            ByteBuffer page = pageBuffer.asReadOnlyBuffer();
            int offset = indexInto * FRAME_SIZE;
            page.limit(offset + FRAME_SIZE).position(offset);

            return pageBuffer.slice();
        }

        public ByteBuffer getPage() {
            return pageBuffer.asReadOnlyBuffer();
        }
        
        public void putFrame(int indexInto, ByteBuffer buffer) {
            int offset = indexInto + FRAME_SIZE;
            ((ByteBuffer)pageBuffer.position(offset)).put(buffer);
        }

    }

    static final int PAGE_SIZE = (int) Math.pow(2, 16); // should match most underlying file systems. 64K
    static final int FRAME_SIZE = PAGE_SIZE / 32; // our data frame size. 2K
    static final int FRAMES_PER_PAGE = PAGE_SIZE / FRAME_SIZE;
    static final int DEFAULT_LOAD_FACTOR = 5;

    DataStoreDirector fileDirector;
    DataMarshaller marshaller;
    File file;
    Channel fileChannel;

    public StructuredFileDirector(String fileName) {

        file = new File(fileName);
        marshaller = new ObjectDataMarshaller();
        fileDirector = DataStoreDirector.createDataStoreDirector(fileName, marshaller);

    }

    public static void main(String[] args) {
        PrintStream out = System.out;

        out.println(PAGE_SIZE);
        out.println(FRAME_SIZE);
        out.println(FRAMES_PER_PAGE);

    }

}
