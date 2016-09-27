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
package objectbase;

import org.jware.objectbase.exception.DataDirectorException;
import org.jware.objectbase.disk.DiskRecordHeader;
import org.jware.objectbase.disk.IndexedDataDirector;
import org.jware.objectbase.disk.ObjectDataMarshaller;
import org.jware.objectbase.disk.DataStoreDirector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import org.jware.objectbase.core.DataHeader;
import org.jware.objectbase.core.DataMarshaller;
import org.jware.objectbase.util.Utilities;
import static org.jware.objectbase.util.Utilities.Utility;
import java.util.logging.Logger;
import org.jware.objectbase.disk.DiskRecordHeaderFactory;

/**
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * A test class disk object persistence. Given a number of records it will
 * create a data file and a corresponding index file. Then they can be queried
 * to look up records. This test creates and stores random sets of Person
 * objects. The time for a given iteration is given in milliseconds. The timing
 * is not exact due to the overhead of creating the random Person objects, but
 * shows a really fast access time for retrievals. The files are deleted when
 * the program exits.
 */
public final class ObjectBaseTest {

    int numOfRecords;
    int howManyToAdd;
    int howManyToGet;
    int whichOneToDelete;
    List keyList;
    Person[] people;

    DataMarshaller<Person> marshaller = new ObjectDataMarshaller();
    DataStoreDirector dm;
    IndexedDataDirector<Integer, DiskRecordHeader> indexDirector;

    public ObjectBaseTest() {
        dm = DataStoreDirector.createDataStoreDirector("People.odb", marshaller);
        indexDirector = new IndexedDataDirector(dm, new DiskRecordHeaderFactory());
        keyList = new ArrayList();
    }

    public void reset() throws IOException {
        people = null;
        howManyToAdd = 0;
        howManyToGet = 0;
        whichOneToDelete = 0;
        keyList.clear();
        keyList = new ArrayList();
        numOfRecords = 0;
        dm = null;
        indexDirector = null;

        Path obj = Paths.get("People.odb");
        Path idx = Paths.get("People.idx");

        Files.delete(idx);
        Files.delete(obj);

        dm = DataStoreDirector.createDataStoreDirector("People.odb", marshaller);
        indexDirector = new IndexedDataDirector(dm, new DiskRecordHeaderFactory());
    }

    public void addRecords(final int howMany) {
        people = new Person[howMany];
        Utilities.Clock clock = Utility.getTimer();
        int i = 0;
        try {
            for (; i < howMany; i++) {
                String fname = Utilities.getRandomString(3);
                String lname = Utilities.getRandomString(10);
                people[i] = new Person(i, fname, lname,
                        new PhoneNumber(Utilities.getRandom(111, 900),
                                Utilities.getRandom(111, 888),
                                Utilities.getRandom(1111, 9000)), null);
                if (i % 3 == 0) {
                    people[i].setDate(LocalDate.now());
                }
                keyList.add(people[i].hashCode());
            }
            clock.startTime();
            for (i = 0; i < howMany; i++) {
                indexDirector.insertData((Integer) keyList.get(i), people[i]);
                numOfRecords++;
            }
            System.out.println("\nTime for (" + i + ") inserts: " + clock.step() + " ms");
            indexDirector.writeIndex();
        } catch (final DataDirectorException | IOException cause) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, " ", cause);
        }
    }

    public void getRecords(final int howMany) {
        try {
            Utilities.Clock clock = Utility.getTimer();
            clock.startTime();
            int i = 0;
            for (; i < howMany; i++) {
                Person p = (Person) indexDirector.retrieveData((Integer) keyList.get(i));
                System.out.println(p.toString());
            }
        } catch (final DataDirectorException | NullPointerException cause) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, " ", cause);
        }
    }

    public void deleteRecord(final int whichOne) {
        try {
            Utilities.Clock clock = Utility.getTimer();
            clock.startTime();
            System.out.println("Deleting record with key: " + keyList.get(whichOne));
            indexDirector.deleteData((Integer) keyList.remove(whichOne));
            System.out.println("\nTime for (" + whichOne + ") deletion: " + clock.step() + " ms");
            numOfRecords--;
        } catch (final DataDirectorException cause) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, " ", cause);
        }
    }

    public void updateRecord(final int update) {
        Scanner input = new Scanner(System.in);
        try {
            Person p = (Person) indexDirector.retrieveData((Integer) keyList.get(update));
            System.out.println(p.toString() + "\n");
            System.out.print("Enter: ");
            p.firstName = input.nextLine();
            System.out.print("Enter: ");
            p.lastName = input.nextLine();
            System.out.println(p.toString() + "\n");

            indexDirector.updateData((Integer) keyList.get(update), p);
        } catch (DataDirectorException cause) {
            System.out.println(cause);
        }
    }

    public void printIndex() {
        Collection list = indexDirector.getIndexValues();
        for (Iterator it = list.iterator(); it.hasNext();) {
            DataHeader rec = (DataHeader) it.next();
            System.out.println(rec);
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
        final Scanner aScanner = new Scanner(System.in);
        while (validSelection) {
            System.out.println("1. Add\t\t2. Retrieve");
            System.out.println("3. Delete\t4. Count");
            System.out.println("5. Reset\t6. Print Index");
            System.out.println("7. Index Size & File t\t8. Update");
            System.out.println("9. Quit");
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
                    if (howManyToGet <= numOfRecords) {
                        getRecords(howManyToGet);
                    } else {
                        System.out.println("Trying to get more than were added.");
                    }
                    break;
                case 3:
                    System.out.print("Which one?\n> ");
                    whichOneToDelete = aScanner.nextInt();
                    if (whichOneToDelete < numOfRecords) {
                        deleteRecord(whichOneToDelete);
                    } else {
                        System.out.println("Trying to get more than were added.");
                    }
                    break;
                case 4:
                    System.out.println("Number of records: " + indexDirector.getRecordCount());
                    break;
                case 5:
                    reset();
                    break;
                case 6:
                    printIndex();
                    break;
                case 7:
                    System.out.println("Size of index: " + indexDirector.getSizeOfIndex() + 
                            "\nFile Size: " + indexDirector.getSizeOfFile());
                    break;
                case 8:
                    System.out.print("Which one?\n> ");
                    int update = aScanner.nextInt();
                    if (update < numOfRecords) {
                        updateRecord(update);
                    } else {
                        System.out.println("Not in range.");
                    }
                    break;
                case 9:
                    validSelection = false;
                    reset();
                    break;
                default:
                    System.out.println("Input out of range \"" + selection
                            + "\". Input a number between 1-5.");
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            new ObjectBaseTest().doMenu();
        } catch (InputMismatchException | IOException cause) {
            System.out.println(cause);
        }
    }
    
}
