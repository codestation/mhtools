/*  MHP2GDEC v1.0 - MHP2G xxxx.bin language table rebuilder
    Copyright (C) 2008 Codestation

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

public abstract class Encoder {

	public abstract void compile(String filelist);

	/**
	 * The "readUTF8" function of java expects a different format of the string
	 * so i have to make a custom one.
	 * 
	 * @param file
	 * @return string extracted from file
	 * @throws IOException
	 *             if any error occur while reading
	 */
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
	 * Checks if the file have the unicode BOM mark and skip it (thanks notepad
	 * grr..)
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

	/**
	 * The "writeInt" function of java writes in BigEndian mode but we need
	 * LittleEndian so i made a custom function for that
	 * 
	 * @param file
	 * @throws IOException
	 *             if any error occur while writing
	 */
	protected void writeInt(RandomAccessFile file, int value) throws IOException {
		int ch1 = (byte) (value >>> 24);
		int ch2 = (byte) (value >>> 16);
		int ch3 = (byte) (value >>> 8);
		int ch4 = (byte) value;
		file.write(ch4);
		file.write(ch3);
		file.write(ch2);
		file.write(ch1);
	}

	/**
	 * The "readInt" function of java reads in BigEndian mode but we need
	 * LittleEndian so i made a custom function for that
	 * 
	 * @param file
	 * @return 8 byte integer in LittleEndian mode
	 * @throws IOException
	 *             if any error occur while reading
	 */
	protected int readInt(RandomAccessFile file) throws IOException, EOFException {
		int ch1 = file.read();
		int ch2 = file.read();
		int ch3 = file.read();
		int ch4 = file.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) {
			throw new EOFException();
		}
		return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
	}
}
