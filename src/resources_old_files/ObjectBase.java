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

package resources_old_files;

import org.jware.objectbase.exception.DataDirectorException;
import org.jware.objectbase.disk.DiskRecordHeader;
import org.jware.objectbase.disk.IndexedDataDirector;
import resources_old_files.ByteDataMarshaller;
import org.jware.objectbase.disk.DataStoreDirector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import org.jware.objectbase.core.Person;
import org.jware.objectbase.core.PhoneNumber;
import org.jware.objectbase.disk.DataMarshaller;
import org.jware.objectbase.util.Utilities;
import static org.jware.objectbase.util.Utilities.Utility;

/**
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 * 
 * A test class disk object persistence.  Given a number of records 
 * it will create a data file and a corresponding index file.  Then 
 * they can be queried to look up records.  This test creates and stores
 * random sets of Person objects. The time for a given iteration is given 
 * in milliseconds.  The timing is not exact due to the overhead 
 * of creating the random Person objects, but shows a really fast access
 * time for retrievals.  The files are deleted when the program exits. 
 */
public class ObjectBase {

    int num;
    int howManyToAdd;
    int howManyToGet;
    String[] keys;
    Person[] people;
    DiskRecordHeader[] headers;
    
    DataMarshaller<Person> marshaller = new ByteDataMarshaller();
    DataStoreDirector dm;
    IndexedDataDirector indexDirector;
    
    public ObjectBase() {
        dm = DataStoreDirector.createDataStoreDirector("People.odb", marshaller);
        indexDirector = new IndexedDataDirector(dm);
    }

       public void reset() throws IOException {
        keys = null;
        people = null;
        headers = null;
        howManyToAdd=0;
        howManyToGet=0;

        dm = null;
        indexDirector = null;

        Path obj = Paths.get("People.odb");
        Path idx = Paths.get("People.idx");

        Files.delete(idx);
        Files.delete(obj);

        dm = DataStoreDirector.createDataStoreDirector("People.odb", marshaller);
        indexDirector = new IndexedDataDirector(dm);
    }

    public void addRecords(int howMany) {
        keys = new String[howMany];
        people = new Person[howMany];
        headers = new DiskRecordHeader[howMany];
        Utilities.Clock clock = Utility.getTimer();
        clock.startTime();
        int i = 0;
        try {
            for (; i < howMany; i++) {
                String fname = Utilities.getRandomString(6);
                String lname = Utilities.getRandomString(10);
                keys[i] = fname + lname;
                people[i] = new Person(i, fname, lname,
                        new PhoneNumber(Utilities.getRandom(111, 900),
                                Utilities.getRandom(111, 888),
                                Utilities.getRandom(1111, 9000)), null);
                headers[i] = new DiskRecordHeader();
                headers[i].setKey(keys[i]);
                indexDirector.insertData(headers[i], people[i]);
            }
            System.out.println("\nTime for (" + i + ") inserts: " + clock.step() + " ms");
            indexDirector.writeIndex();
        } catch (DataDirectorException | IOException cause) {
            System.out.println(cause);
        }
    }

    public void getRecords(int howMany) {
        try {
            indexDirector.readIndex();
            List<String> list = Arrays.asList(keys);
            Collections.shuffle(list);
            Object[] k = list.toArray();
            Utilities.Clock clock = Utility.getTimer();
            clock.startTime();
            int i = 0;
            for (; i < howMany; i++) {
                Person p = (Person) indexDirector.retrieveData(String.valueOf(k[i]));
                System.out.println(p.toString());
            }
            System.out.println("\nTime for (" + i + ") retrievals: " + clock.step() + " ms");
        } catch (IOException | ClassNotFoundException | DataDirectorException cause) {
            System.out.println(cause);
        }
    }

    /**
     *
     * @throws InputMismatchException
     * @throws java.io.IOException
     */
    public void doMenu() throws InputMismatchException, IOException {
        int selection;
        boolean validSelection = true;
        Scanner aScanner = new Scanner(System.in);
        while (validSelection) {
            System.out.println("1. Add");
            System.out.println("2. Retrieve");
            System.out.println("3. Reset");
            System.out.println("4. Quit\n");
            System.out.print("> ");

            selection = aScanner.nextInt();
            validSelection = true;
            switch (selection) {
                case 1:
                    System.out.print("How many?\n> ");
                    howManyToAdd = aScanner.nextInt();
                    addRecords(howManyToAdd);
                    break;
                case 2:
                    System.out.print("How many?\n> ");
                    howManyToGet = aScanner.nextInt();
                    if (howManyToGet <= howManyToAdd) {
                        getRecords(howManyToGet);
                    } else {
                        System.out.println("Trying to get more than were added.");
                    }
                    break;
                case 3:
                    reset();
                    break;
                case 4:
                    validSelection = false;
                    reset();
                    break;
                default:
                    System.out.println("Input out of range \"" + selection
                            + "\". Input a number between 1-4.");
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        ObjectBase t = new ObjectBase();
        try {
            t.doMenu();
        } catch (InputMismatchException | IOException cause) {
            System.out.print(cause);
        }
    }
}
