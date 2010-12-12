/*  MHP2GDEC v1.0 - MHP2G 0016/0017/475x.bin language table extractor
    Copyright (C) 2008-2010 Codestation

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import base.Decoder;

/**
 * ExtractPluginA v1.0 - 0016//0017/475x.bin language table extractor
 * 
 * @author Codestation
 */
public class ExtractPluginA extends Decoder {

    @Override
    public void extract(String filename) {
        int tables_count;
        byte[] paddingData;
        int[] table_offset;
        int offset;
        try {
            RandomAccessFile file = new RandomAccessFile(filename, "r");
            // reading of number of main tables
            tables_count = readInt(file);
            // skipping 4 bytes of unknown data
            file.skipBytes(4);
            table_offset = new int[tables_count];
            // read the location of each table
            for (int i = 0; i < tables_count; i++) {
                table_offset[i] = readInt(file);
            }
            String directory = filename.split("\\.")[0];
            new File(directory).mkdir();
            // create the list of string tables used in the rebuild
            PrintStream filelist = new PrintStream(new FileOutputStream(
                    new File(directory + "/filelist.txt")), true, "UTF-8");
            // save the name and size of the file
            filelist.println(filename + " " + file.length());
            for (int j = 0; j < tables_count; j++) {
                if (table_offset[j] == -1) {
                    // System.out.println("Creating " + directory +
                    // "/string_table_" + j + ".txt (empty)");
                    System.out.println("Can't create " + directory
                            + "/string_table_" + j
                            + ".txt (null table), skipping.");
                    File f = new File(directory + "/string_table_" + j + ".txt");
                    f.delete();
                    // f.createNewFile();
                    filelist.println("string_table_" + j + ".txt");
                    continue;
                }
                file.seek(table_offset[j]);
                System.out.println("Creating " + directory + "/string_table_"
                        + j + ".txt");
                PrintStream stringout = new PrintStream(new FileOutputStream(
                        new File(directory + "/string_table_" + j + ".txt")),
                        true, "UTF-8");
                filelist.println("string_table_" + j + ".txt");
                int offsetCounter = 0;
                // just skip the offset section (not needed)
                while (true) {
                    offset = readInt(file);
                    if (offset == -1) {
                        break;
                    }
                    offsetCounter++;
                }
                int stringCounter = 0;
                while (stringCounter < offsetCounter) {
                    String str = readString(file);
                    if (str.length() == 1 && str.charAt(0) == 0) {
                        // some offsets points to empty strings, so i put this
                        // string to make
                        // sure that it will created at the moment of repack
                        stringout.println("<EMPTY STRING>");
                    } else {
                        str = str.substring(0, str.length() - 1);
                        // need one string per line, so better replace the
                        // newlines
                        stringout.println(str.replaceAll("\n", "<NEWLINE>"));
                    }
                    stringCounter++;
                }
                // skip the end-byte mark
                file.skipBytes(1);
                stringout.close();
            }
            file.seek(file.getFilePointer() - 1);
            // calculate the size of the ending padding data and make
            // a file of it
            int size = (int) (file.length() - file.getFilePointer());
            paddingData = new byte[size];
            file.read(paddingData, 0, size);
            System.out.println("Creating " + directory + "/enddata.bin");
            RandomAccessFile end = new RandomAccessFile(directory
                    + "/enddata.bin", "rw");
            filelist.println("enddata.bin");
            end.write(paddingData, 0, size);
            end.setLength(end.getFilePointer());
            end.close();
            file.close();
            filelist.close();
            System.out.println("Finished!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
