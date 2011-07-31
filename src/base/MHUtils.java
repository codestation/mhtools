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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public abstract class MHUtils {
 
    public static int readInt(InputStream file) throws IOException, EOFException {
        int ch1 = file.read();
        int ch2 = file.read();
        int ch3 = file.read();
        int ch4 = file.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }
    
    public static int readInt(RandomAccessFile file) throws IOException, EOFException {
        int ch1 = file.read();
        int ch2 = file.read();
        int ch3 = file.read();
        int ch4 = file.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }
    
    public static int readShort(InputStream file) throws IOException, EOFException {
        int ch1 = file.read();
        int ch2 = file.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (ch2 << 8) + (ch1 << 0);
    }
    
    public static void writeShort(OutputStream file, int value) throws IOException {
        int ch1 = (byte) (value >>> 8);
        int ch2 = (byte) value;
        file.write(ch2);
        file.write(ch1);
    }
    
    /**
     * The "writeInt" function of java writes in BigEndian mode but we need
     * LittleEndian so i made a custom function for that
     * 
     * @param file
     * @throws IOException
     *             if any error occur while writing
     */
    public static void writeInt(OutputStream file, int value) throws IOException {
        int ch1 = (byte) (value >>> 24);
        int ch2 = (byte) (value >>> 16);
        int ch3 = (byte) (value >>> 8);
        int ch4 = (byte) value;
        file.write(ch4);
        file.write(ch3);
        file.write(ch2);
        file.write(ch1);
    }
    
    public static void writeInt(RandomAccessFile file, int value) throws IOException {
        int ch1 = (byte) (value >>> 24);
        int ch2 = (byte) (value >>> 16);
        int ch3 = (byte) (value >>> 8);
        int ch4 = (byte) value;
        file.write(ch4);
        file.write(ch3);
        file.write(ch2);
        file.write(ch1);
    }
    
    public static int extractNumber(String filename) {
        return Integer.parseInt(filename.substring(filename.indexOf(".") - 4,
                filename.indexOf(".")));
    }
    

    public static int getOffset(int value) throws EOFException,
            FileNotFoundException, IOException {
        int res = -1;
        if (value == 0) {
            res = 0;
        } else {
            RandomAccessFile table = new RandomAccessFile("index.bin", "r");
            table.seek(value * 4 - 4);
            res = readInt(table);
            table.close();
        }
        return res;
    }
    
	static final String HEXES = "0123456789ABCDEF";

	public static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(' ');
		}
		return hex.toString();
	}
}
