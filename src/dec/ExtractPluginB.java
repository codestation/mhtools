/*  MHP2GDEC v1.0 - MHP2G 53xx.bin language table extractor
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

package dec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Vector;

import base.Decoder;

/**
 * ExtractPluginB v1.0 - 53xx.bin language table extractor
 * 
 * @author Codestation
 */
public class ExtractPluginB extends Decoder {

	private int mhp3_skip_bytes;
	public ExtractPluginB(boolean newdec) {
		mhp3_skip_bytes = newdec ? 4 : 0;
	}

	@Override
	public void extract(String filename) {
		byte[] unknownData;
		Vector<Integer> table_offset;
		try {			
			RandomAccessFile file = new RandomAccessFile(filename, "r");
			table_offset = new Vector<Integer>();
			int pointer;
			while (true) {
				pointer = readInt(file);
				if (pointer == 0) {
					break;
				}
				table_offset.add(pointer);
			}
			String directory = filename.split("\\.")[0];
			new File(directory).mkdir();
			PrintStream filelist = new PrintStream(new FileOutputStream(new File(directory + "/filelist.txt")), true, "UTF-8");
			filelist.println(filename + " " + file.length());
			for (int j = 0; j < table_offset.size(); j++) {
				file.seek(table_offset.get(j));
				System.out.println("Creating " + directory + "/string_table_" + j + ".txt");
				PrintStream stringout = new PrintStream(new FileOutputStream(new File(directory + "/string_table_" + j + ".txt")), true, "UTF-8");
				filelist.println("string_table_" + j + ".txt");
				// int unknown0 = readInt(file);
				// int payment = readInt(file);
				// int reward = readInt(file);
				// int decrease = readInt(file);
				// int unknown_fixed = readInt(file);
				file.skipBytes(20 + mhp3_skip_bytes);
				int offset_table_pointer = readInt(file);
				// int unknown1 = readInt(file);
				// int unknown2 = readInt(file);
				// int unknown3 = readInt(file);
				// int unknown4 = readInt(file);
				// int unknown5 = readInt(file);
				// int unknown6 = readInt(file);
				// int unknown7 = readInt(file);
				file.seek(offset_table_pointer);
				int string_table_pointers = readInt(file);
				for (long i = string_table_pointers; i < offset_table_pointer; i += 4) {
					file.seek(i);
					int current_string = readInt(file);
					file.seek(current_string);
					String str = readString(file);
					if (str.length() == 1 && str.charAt(0) == 0) {
						// some offsets points to empty strings, so i put this
						// string to make
						// sure that it will created at the moment of re-pack
						stringout.println("<EMPTY STRING>");
					} else {
						str = str.substring(0, str.length() - 1);
						// need one string per line, so better replace the
						// newlines
						stringout.println(str.replaceAll("\n", "<NEWLINE>"));
					}
				}
				stringout.close();
				file.seek(offset_table_pointer + 7 * 4);
			}
			// calculate the size of the ending unknown data and make a file of
			// it
			int size = (int) (file.length() - file.getFilePointer());
			unknownData = new byte[size];
			file.read(unknownData, 0, size);
			System.out.println("Creating " + directory + "/enddata.bin");
			RandomAccessFile end = new RandomAccessFile(directory + "/enddata.bin", "rw");
			filelist.println("enddata.bin");
			end.write(unknownData, 0, size);
			end.setLength(end.getFilePointer());
			end.close();
			file.close();
			filelist.close();
			System.out.println("Copying " + filename + " to " + directory + "/" + filename + " (needed for rebuild)");
			copyfile(filename, directory + "/" + filename);
			System.out.println("Finished!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void copyfile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
