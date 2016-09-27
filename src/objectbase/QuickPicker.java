package objectbase;

import java.util.Arrays;
import org.jware.objectbase.util.Utilities;

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
 * File: QuickPicker.java Created On: Sep 30, 2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 *
 */
public class QuickPicker {

    public static void main(String[] args) {

        int[] numbers = new int[6];

        for (int n = 0; n < 100; n++) {
            for (int i = 0; i < 5; i++) {
                numbers[i] = Utilities.getRandomBetween(1, 59);
            }
            numbers[5] = Utilities.getRandomBetween(1, 42);

            Arrays.sort(numbers);
            for (int i = 0; i < 6; i++) {
                System.out.print(numbers[i] + " ");
            }
            System.out.println();
        }
    }

}


/*
 51 35 56 13 50 13
 54 47 46 27 27 10

 18 8 4 17 1 20
 56 43 1 45 30 35
 35 50 25 57 37 17
 */
