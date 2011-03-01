/*  MHTrans - MH Utilities
    Copyright (C) 2011 Codestation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package base;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class HelperDec extends MHUtils {
    /**
     * The "readUTF8" function of java expects a different format of the
     * string so i have to make a custom one
     * 
     * @param file
     * @return string extracted from file
     * @throws IOException
     *             if any error occur while reading
     */
    protected String readString(RandomAccessFile file) throws IOException {
        byte[] buffer = new byte[1024];
        byte data = 0;
        int counter = 0;
        try {
            do {
                data = file.readByte();
                buffer[counter++] = data;
            } while (data != 0);
            // checks if the string is a edited one
            while (file.readByte() == 0) {
                ;
            }
            file.seek(file.getFilePointer() - 1);
        } catch (EOFException e) {
            return null;
        }
        return new String(buffer, 0, counter, "UTF-8");
    }
}
