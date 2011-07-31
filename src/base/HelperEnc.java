/*  MHTools - MH Utilities
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

public abstract class HelperEnc extends MHUtils {
    protected String readString(RandomAccessFile file) throws IOException {
        byte[] buffer = new byte[1024];
        byte data = 0;
        boolean eol = false;
        int counter = 0;
        try {
            while (!eol) {
                switch (data = file.readByte()) {
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = file.getFilePointer();
                    if (file.readByte() != '\n') {
                        file.seek(cur);
                        eol = false;
                    }
                    break;
                default:
                    buffer[counter++] = data;
                    break;
                }
            }
        } catch (EOFException e) {
            return null;
        }
        return new String(buffer, 0, counter, "UTF-8");
    }

    /**
     * Checks if the file have the unicode BOM mark and skip it
     * (thanks notepad grr..)
     * 
     * @param file
     * @throws IOException
     *             if any error occur while reading
     */
    protected void checkUnicodeBOM(RandomAccessFile file) throws IOException {
        int a = file.readByte();
        int b = file.readByte();
        int c = file.readByte();
        if (a != -17 || b != -69 || c != -65) {
            file.seek(0);
        }
    }
}
