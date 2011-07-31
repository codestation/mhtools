/*  MHTools - MHP2G 53xx.bin language table rebuilder
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import base.Encoder;
import base.HelperEnc;

/**
 * RebuildPluginB v1.0 - 53xx.bin language table rebuilder
 * 
 * @author codestation
 */
public class RebuildPluginB extends HelperEnc implements Encoder {

    private int skip_bytes;
    private int seek_skip;

    public RebuildPluginB(int type) {
    	seek_skip = 0;
    	switch(type) {
    	case 7:
    		seek_skip = 32;
    	case 4:
    		skip_bytes = 4;
    		break;
    	default:
    		skip_bytes = 0;
    	}
    }

    @Override
    public void compile(String filepath) {
        try {
            BufferedReader files = new BufferedReader(new FileReader(filepath
                    + "/filelist.txt"));
            String file = files.readLine();
            // retrieve the filename and size
            String filename = file.split(" ")[0];
            // long size = Integer.parseInt(file.split(" ")[1]);
            // now make a list with the string tables files
            Vector<String> filenames = new Vector<String>();
            while ((file = files.readLine()) != null) {
                filenames.add(file);
            }
            files.close();
            copyfile(filepath + "/" + filename, filename + ".out");
            RandomAccessFile out = new RandomAccessFile(filename + ".out", "rw");
            Vector<Integer> table_offset = new Vector<Integer>();
            if(seek_skip != 0) {
            	out.skipBytes(seek_skip);
            	table_offset.add(readInt(out));
            } else {
	            int pointer;
	            while (true) {
	                pointer = readInt(out);
	                if (pointer == 0) {
	                    break;
	                }
	                table_offset.add(pointer);
	            }
            }
            for (int i = 0; i < table_offset.size(); i++) {
                patchStringTable(filepath, filenames.get(i), out, table_offset.get(i));
            }
            out.close();
            System.out.println("Finished!");
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void patchStringTable(String directory, String in,
            RandomAccessFile out, int starting_offset)
            throws FileNotFoundException, IOException {
        System.out.println("Reading " + directory + "/" + in);
        RandomAccessFile file = new RandomAccessFile(directory + "/" + in, "r");
        checkUnicodeBOM(file); // thanks notepad :/ (die notepad, die)
        Vector<String> stringTable = new Vector<String>();
        try {
            while (true) {
                // read all strings of file
                String str = readString(file);
                if (str == null) {
                    break;
                }
                // remove the labels and put the original data
                str = str.replaceAll("<NEWLINE>", "\n");
                str = str.replaceAll("<EMPTY STRING>", "\0");
                stringTable.add(str);
            }
        } catch (EOFException e) {
        }
        file.close();
        out.seek(starting_offset + seek_skip);
        out.skipBytes(20 + skip_bytes);
        int offset_table_pointer = readInt(out);

        out.seek(offset_table_pointer + seek_skip);
        int string_table_pointers = readInt(out);
        
        out.seek(string_table_pointers + seek_skip);
        int string_start = readInt(out);
        
        int total = calculateTotalSize(stringTable, 4);
        int diff = string_table_pointers - string_start - total;
        
        if (diff < 0) {
            System.err.println(in + " is too big, please remove at least "
                    + -diff + " bytes. Skipped");
            return;
        }
        out.seek(string_table_pointers + seek_skip);
        int starting_string = readInt(out);
        out.seek(starting_string + seek_skip);
        long orig_table_pointer = string_table_pointers;
        for (String str : stringTable) {
            out.write(str.getBytes("UTF-8"));
            out.writeByte(0);
            out.writeByte(0);
            while (out.getFilePointer() % 4 != 0) {
                out.writeByte(0);
            }
            int tmp = (int) out.getFilePointer();
            out.seek(string_table_pointers + seek_skip);
            writeInt(out, starting_string);
            string_table_pointers += 4;
            starting_string = tmp - seek_skip;
            out.seek(tmp);
        }
        long current_offset = out.getFilePointer() + seek_skip;
        while (current_offset < orig_table_pointer) {
            out.writeByte(0);
            current_offset++;
        }
    }

    private int calculateTotalSize(Vector<String> st, int align)
            throws UnsupportedEncodingException {
        int total = 0;
        for (String str : st) {
            int len = str.getBytes("UTF-8").length;

            if (len == 1 && str.charAt(0) == 0) {
                total++;
            } else {
                total += len + 1;
            }
            if(align != 0) {
        	     total += align - (total % 4);
        	}
        }
        return total;
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
            System.out
                    .println(ex.getMessage() + " in the specified directory.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
