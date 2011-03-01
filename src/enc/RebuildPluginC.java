/*  MHTrans - MHP2G 537x.bin language table rebuilder
    Copyright (C) 2008-2011 Codestation

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

package enc;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import base.Encoder;
import base.HelperEnc;

/**
 * RebuildPluginC v1.0 - 537x.bin language table rebuilder
 * 
 * @author Codestation
 */
public class RebuildPluginC extends HelperEnc implements Encoder {

    @Override
    public void compile(String filepath) {
        try {
            BufferedReader files = new BufferedReader(new FileReader(filepath
                    + "/filelist.txt"));
            String string_file = files.readLine();
            // retrieve the filename and size
            String filename = string_file.split(" ")[0];
            long size = Integer.parseInt(string_file.split(" ")[1]);
            // now make a list with the string tables files
            Vector<String> filenames = new Vector<String>();
            Vector<Integer> unknown = new Vector<Integer>();
            while ((string_file = files.readLine()) != null) {
                if (string_file.equals("enddata.bin")) {
                    filenames.add(string_file);
                } else {
                    filenames.add(string_file.split(",")[1]);
                    unknown.add(Integer.parseInt(string_file.split(",")[0]));
                }
            }
            files.close();
            RandomAccessFile out = new RandomAccessFile(filename + ".out", "rw");
            writeInt(out, unknown.get(0));
            writeInt(out, (unknown.size() + 1) * 8);
            for (int i = 1; i < unknown.size(); i++) {
                writeInt(out, unknown.get(i));
                writeInt(out, 0);
            }
            writeInt(out, -1);
            writeInt(out, -1);
            int table_offset = 12;
            for (int i = 0; i < filenames.size() - 1; i++) {
                // now start to create each offset table / string table
                createStringTable(filepath, filenames.get(i), out);
                // write the end-table mark
                out.writeByte(0);
                if (i < filenames.size() - 2) {
                    long current = out.getFilePointer();
                    out.seek(table_offset);
                    // now we know the value of the next table, so write above
                    // in the main
                    // offset table
                    writeInt(out, (int) current);
                    out.seek(current);
                    table_offset += 8;
                    out.seek(current);
                }
            }
            out.writeByte(0);
            // we need to know the size of the enddata, so open it now
            System.out.println("Reading " + filenames.lastElement());
            RandomAccessFile enddata = new RandomAccessFile(filepath + "/"
                    + filenames.lastElement(), "rw");

            long enddataSize = enddata.length();

            // some checks to make sure that the file size of xxxx.bin is
            // correct
            if (out.getFilePointer() > size - enddataSize) {
                System.out.println("File too big (by "
                        + (out.getFilePointer() - (size - enddataSize))
                        + " bytes), please reduce some strings :(");
            } else if (out.getFilePointer() < size - enddataSize) {
                System.out.println("File too small (by "
                        + ((size - enddataSize) - out.getFilePointer())
                        + " bytes), filling with 0x00 (this is OK) :D");
                while (out.getFilePointer() < size - enddataSize) {
                    out.writeByte(0);
                }
            } else {
                System.out.println("Perfect size of file :O");
            }
            // now append the unknown data at the end of file
            int data;
            while ((data = enddata.read()) != -1) {
                out.write((byte) data);
            }
            enddata.close();
            out.setLength(out.getFilePointer());
            out.close();
            System.out.println("Finished!");
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 
     * @param in
     *            filename of text file
     * @param out
     *            file to write the table
     * @throws FileNotFoundException
     *             if the file is not found
     * @throws IOException
     *             if any error occur while reading/writing
     */
    private void createStringTable(String directory, String in,
            RandomAccessFile out) throws FileNotFoundException, IOException {
        System.out.println("Reading " + directory + "/" + in);
        RandomAccessFile file = new RandomAccessFile(directory + "/" + in, "r");
        checkUnicodeBOM(file); // thanks notepad :/ (*sigh*)
        Vector<String> stringTable = new Vector<String>();
        Vector<Integer> unknownTable = new Vector<Integer>();
        try {
            while (true) {
                // read all strings of file
                String str = readString(file);
                if (str == null) {
                    break;
                }
                // remove the labels and put the original data
                unknownTable.add(Integer.parseInt(str.split(",")[0]));
                str = str.substring(str.indexOf(",") + 1);
                str = str.replaceAll("<NEWLINE>", "\n");
                str = str.replaceAll("<EMPTY STRING>", "\0");
                stringTable.add(str);
            }
        } catch (EOFException e) {
        }
        file.close();
        int offset = stringTable.size() * 8;
        // now calculate the offsets using the length in bytes of the strings
        for (int i = 0; i < stringTable.size(); i++) {
            writeInt(out, unknownTable.get(i));
            writeInt(out, offset);
            if (stringTable.elementAt(i).getBytes("UTF-8").length == 1
                    && stringTable.elementAt(i).charAt(0) == '\0') {
                offset++;
            } else {
                offset += stringTable.elementAt(i).getBytes("UTF-8").length + 1;
            }
        }
        // now write the zero terminated string in the file
        for (int i = 0; i < stringTable.size(); i++) {
            String str = stringTable.elementAt(i);
            if (str.equals("\0")) {
                out.writeByte(0);
            } else {
                out.write(str.getBytes("UTF-8"));
                out.writeByte(0);
            }
        }
    }
}
