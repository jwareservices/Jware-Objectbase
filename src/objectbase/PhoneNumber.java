package objectbase;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Logger;

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
 * File: PhoneNumber.java Created On: 27/06/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 */
public final class PhoneNumber implements Externalizable {

    short areaCode;
    short exchange;
    short extension;

    public PhoneNumber(final int areaCode, final int exchange, final int extension) {
        rangeCheck(areaCode, 999, "Area code");
        rangeCheck(exchange, 999, "Exchange");
        rangeCheck(extension, 9999, "Extension");
        this.areaCode = (short) areaCode;
        this.exchange = (short) exchange;
        this.extension = (short) extension;
    }
    
    public PhoneNumber() {
        
    }
            

    private static void rangeCheck(int arg, int max, String name) {
        if (arg < 0 || arg > max) {
            throw new IllegalArgumentException(name + ": " + arg + ", is out of range.");
        }
    }

    public final static String formatPhoneNumberString(final short areacode, final short exchange, final short extension) {
        return "(" + areacode + ") " + exchange + "-" + extension;
    }

    /**
     *
     * @param areacode
     * @param exchange
     * @param extension
     * @return
     */
    public final static PhoneNumber createPhoneNumber(final short areacode, final short exchange, final short extension) {
        return new PhoneNumber(areacode, exchange, extension);
    }

    /**
     *
     * @return The area code.
     */
    public short getAreaCode() {
        return areaCode;
    }

    /**
     *
     * @return The exchange.
     */
    public short getExchange() {
        return exchange;
    }

    /**
     *
     * @return The extension.
     */
    public short getExtension() {
        return extension;
    }

    
    /**
     *
     * @return U.S. format phone number: (XXX) YYY-ZZZZ.
     */
    @Override
    public String toString() {
        return "(" + areaCode + ") " + exchange + "-" + extension;
    }

    /**
     *
     * @param o
     * @return retVal
     */
    @Override
    public boolean equals(final Object o) {
        boolean retVal;
        PhoneNumber pn = (PhoneNumber) o;

        if (o == this) {
            retVal = true;
        } else if (!(o instanceof PhoneNumber)) {
            retVal = false;
        } else {
            retVal = pn.extension == extension
                    && pn.exchange == exchange
                    && pn.areaCode == areaCode;
        }
        return retVal;
    }

    /**
     *
     * @return result
     */
    @Override
    public int hashCode() {
        int result = 17;
        result += 11 * result + areaCode;
        result += 7 * result + exchange;
        result += 3 * result + extension;
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
            oo.writeShort(areaCode);
            oo.writeShort(exchange);
            oo.writeShort(extension);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
            areaCode=oi.readShort();
            exchange=oi.readShort();
            extension=oi.readShort();
    }
}
