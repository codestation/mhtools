/*  MHP2GDEC v1.0 - MHP2G 537x.bin language table extractor
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Vector;

import base.Decoder;

/**
 * MHP2GDEC v1.0 - 537x.bin language table extractor
 * 
 * @author Codestation
 */
public class ExtractPluginC extends Decoder {

    @Override
    public void extract(String filename) {
        Vector<Integer> offset_tables = new Vector<Integer>();
        Vector<Integer> unknown_values = new Vector<Integer>();
        try {
            RandomAccessFile file = new RandomAccessFile(filename, "r");
            while (true) {
                int unknown = readInt(file);
                int offset = readInt(file);
                if (unknown == -1 && offset == -1) {
                    break;
                }
                unknown_values.add(unknown);
                offset_tables.add(offset);

            }
            filename = new File(filename).getName();
            String directory = filename.split("\\.")[0];
            new File(directory).mkdir();
            // create the list of string tables used in the rebuild
            PrintStream filelist = new PrintStream(new FileOutputStream(
                    new File(directory + "/filelist.txt")), true, "UTF-8");
            // save the name and size of the file
            filelist.println(filename + " " + file.length());
            int string_table_end = 0;
            ;
            for (int j = 0; j < offset_tables.size(); j++) {
                file.seek(offset_tables.get(j));
                System.out.println("Creating " + directory + "/string_table_"
                        + j + ".txt");
                PrintStream stringout = new PrintStream(new FileOutputStream(
                        new File(directory + "/string_table_" + j + ".txt")),
                        true, "UTF-8");
                filelist.println(unknown_values.get(j) + ",string_table_" + j
                        + ".txt");
                int offset_table_start = (int) file.getFilePointer();
                int string_table_start = 0;
                boolean first = false;
                while (true) {
                    int unknown = readInt(file);
                    int offset = readInt(file);
                    if (!first) {
                        first = true;
                        string_table_start = offset + offset_table_start;
                    }
                    int actual_offset = (int) file.getFilePointer();
                    file.seek(offset + offset_table_start);
                    String str = readString(file);
                    string_table_end = (int) file.getFilePointer();
                    file.seek(actual_offset);
                    if (str.length() == 1 && str.charAt(0) == 0) {
                        // some offsets points to empty strings, so i put this
                        // string to make
                        // sure that it will created at the moment of re-pack
                        stringout.println(unknown + ",<EMPTY STRING>");
                    } else {
                        str = str.substring(0, str.length() - 1);
                        // need one string per line, so better replace the
                        // newlines
                        stringout.println(unknown + ","
                                + str.replaceAll("\n", "<NEWLINE>"));
                    }
                    if (file.getFilePointer() >= string_table_start) {
                        break;
                    }
                }
                stringout.close();
            }
            file.seek(string_table_end);
            // calculate the size of the ending unknown data and
            // make a file of it
            int size = (int) (file.length() - file.getFilePointer());
            byte[] unknownData = new byte[size];
            file.read(unknownData, 0, size);
            file.close();
            System.out.println("Creating " + directory + "/enddata.bin");
            RandomAccessFile end = new RandomAccessFile(directory
                    + "/enddata.bin", "rw");
            filelist.println("enddata.bin");
            filelist.close();
            end.write(unknownData, 0, size);
            end.setLength(end.getFilePointer());
            end.close();
            System.out.println("Finished!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
