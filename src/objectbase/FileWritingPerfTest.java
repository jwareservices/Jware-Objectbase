package objectbase;

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
 * File: FlieWritingPerfTest.java 
 * Created On: 00/23/2015
 * 
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.jware.objectbase.util.Utilities;

public class FileWritingPerfTest {


private static final int ITERATIONS = 1;
private static final double MEG = (Math.pow(1024, 2));
private static final int RECORD_COUNT = 10000;
private static final String RECORD = "Help I am trapped in a fortune cookie factory\n";
private static final int RECSIZE = RECORD.getBytes().length;
static    Person[] people = new Person[RECORD_COUNT];
public static void main(String[] args) throws Exception {
    List<Person> records = new ArrayList<>(RECORD_COUNT);
    int size = 0;
    for (int i = 0; i < RECORD_COUNT; i++) {
                    people[i] = new Person(i, Integer.toString(i+1), Integer.toString(i+2),
                            new PhoneNumber(Utilities.getRandom(111, 900),
                                    Utilities.getRandom(111, 888),
                                    Utilities.getRandom(1111, 9000)), null);        
        records.add(people[i]);
        size += RECSIZE;
    }
    System.out.println(records.size() + " 'records'");
    System.out.println(size / MEG + " MB");

    for (int i = 0; i < ITERATIONS; i++) {
        System.out.println("\nIteration " + i);

        writeRaw(records);
        writeBuffered(records, 8192);
        writeBuffered(records, (int) MEG);
        writeBuffered(records, 4 * (int) MEG);
    }
}

private static void writeRaw(List<Person> records) throws IOException {
    File file = new File("foo.txt");
    try {
        FileWriter writer = new FileWriter(file);
        System.out.print("Writing raw... ");
        write(records, writer);
    } finally {
        // comment this out if you want to inspect the files afterward
 //       file.delete();
    }
}

private static void writeBuffered(List<Person> records, int bufSize) throws IOException {
    File file =  new File("foo.txt");
    try {
        FileWriter writer = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);

        System.out.print("Writing buffered (buffer size: " + bufSize + ")... ");
        write(records, bufferedWriter);
    } finally {
        // comment this out if you want to inspect the files afterward
 //       file.delete();
    }
}

private static void write(List<Person> records, Writer writer) throws IOException {
    long start = System.currentTimeMillis();
    for (Person record: records) {
        writer.write(record.toString());
    }
    writer.flush();
    writer.close();
    long end = System.currentTimeMillis();
    System.out.println((end - start) / 1000f + " seconds");
}
}