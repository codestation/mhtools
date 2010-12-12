/*  MHP2GDEC v1.0 - MHP2G xxxx.bin language table extractor
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

public abstract class Decoder {

	public abstract void extract(String filename);

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

	/**
	 * Some hex-edited files have some extra zeros at the end of the strings so
	 * its better to skip them
	 * 
	 * @param file
	 * @throws IOException
	 *             if any error occur while reading
	 */
	protected void advanceNext(RandomAccessFile file) throws IOException {
		while (file.readByte() == 0) {
			;
		}
		file.seek(file.getFilePointer() - 1);
	}

	/**
	 * The "readUTF8" function of java expects a different format of the string
	 * so i have to make a custom one
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
			advanceNext(file);
		} catch (EOFException e) {
			return null;
		}
		return new String(buffer, 0, counter, "UTF-8");
	}
}
