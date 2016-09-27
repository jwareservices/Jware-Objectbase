package objectbase;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
 * File: Person.java Created On: 27/00/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 */
public class Person implements Externalizable {

    public int personID;
    public String firstName;
    public String lastName;
    public PhoneNumber phoneNumber;
    public List<LocalDate> visitDates;

    /**
     *
     * @param pID
     * @param fname
     * @param lname
     * @param pNumber
     * @param visits
     */
    public Person(final int pID, final String fname, final String lname, final PhoneNumber pNumber, final List visits) {
        personID = pID;
        firstName = fname;
        lastName = lname;
        phoneNumber = pNumber;
        if (visits == null) {
            visitDates = new ArrayList<>();
        } else {
            visitDates = visits;
        }
    }

    public Person() {

    }

    public final String getFirstName() {
        return firstName;
    }

    public final String getLastName() {
        return lastName;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setDate(LocalDate dates) {
        visitDates.add(dates);
    }

    /**
     *
     * @return The hash code for this object.
     */
//    @Override
    public final int getCustomerID() {
        return personID;
    }

    /**
     *
     * @return The representation of This person object.
     */
    @Override
    public final String toString() {

        StringBuilder string = new StringBuilder();
        string.append("PERSON [")
                .append(personID)
                .append("\t" + firstName)
                .append("\t" + lastName)
                .append("\t" + phoneNumber + "]");
        if (visitDates != null) {
            string.append("*[ " + visitDates + " ]");
        }
        return string.toString();
    }

    /**
     *
     * @param object to compare.
     * @return True if this object is equal to object.
     */
    @Override
    public final boolean equals(final Object object) {
        final Person other = (Person) object;
        boolean returnValue;

        if (object == null) {
            returnValue = false;
        } else if (!(object instanceof Person)) {
            returnValue = false;
        } else if (!Objects.equals(this.firstName, other.firstName)) {
            returnValue = false;
        } else if (!Objects.equals(this.lastName, other.lastName)) {
            returnValue = false;
        } else {
            returnValue = Objects.equals(this.phoneNumber, other.phoneNumber);
        }

        return returnValue;
    }

    /**
     *
     * @return Hash code calculated using Additive primes.
     */
    @Override
    public final int hashCode() {
        int result = 23;
        result += 17 * result + personID;
        result += 11 * result + firstName.hashCode();
        result += 7 * result + lastName.hashCode();
        result += 3 * result + phoneNumber.hashCode();
        return result;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(personID);
        out.writeUTF(firstName);
        out.writeUTF(lastName);
        out.writeObject(phoneNumber);
        out.writeObject(visitDates);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        personID = in.readInt();
        firstName = in.readUTF();
        lastName = in.readUTF();
        phoneNumber = (PhoneNumber) in.readObject();
        visitDates = (List) in.readObject();
    }
}
//            visitDates = ((ObjectInputStream) in).readBoolean();
