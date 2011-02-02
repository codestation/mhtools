/*  MHP2GENC v1.0 - PAK rebuilder
 Copyright (C) 2008-2010 codestation

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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import base.Encoder;
import base.EndianFixer;

/**
 * RebuildPluginE v1.0
 * 
 * @author Codestation
 */
public class RebuildPluginE extends EndianFixer implements Encoder {
    
    @Override
    public void compile(String filepath) {
        try {
            File dir = new File(filepath);
            if(!dir.isDirectory()) {
                System.err.println("Isn't a directory: " + filepath);
                return;
            }
            File files[] = dir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            Vector<File> clean_files = new Vector<File>();
            for(File file : files) {
                if(!file.isDirectory())
                    clean_files.add(file);
            }
            if(clean_files.size() > 0) {
                RandomAccessFile out = new RandomAccessFile(dir.getName() + ".pak","rw");
                int table_size = 4 + clean_files.size() * 8;                
                if(table_size % 16 > 0) {
                    table_size += 16 - (table_size % 16);
                    System.out.println("Table size (padded): " + table_size + " bytes");
                }
                writeInt(out, clean_files.size());
                long table_start = out.getFilePointer();
                byte pad[] = new byte[table_size - 4];
                out.write(pad);
                long data_start = out.getFilePointer();
                for(File file : clean_files) {
                    if(file.getName().endsWith(".tmh")) {
                        File tmhdir = new File(dir + "/" + file.getName().replaceAll(".tmh$", ""));
                        if(tmhdir.isDirectory()) {
                            file.delete();
                            Encoder enc = new RebuildPluginD();
                            System.out.println("Processing directory " + tmhdir.getName());
                            enc.compile(dir + "/" + tmhdir.getName());
                            new File(file.getName()).renameTo(new File(dir + "/" + file.getName()));
                        }
                    }
                    System.out.println("Processing " + file.getName());
                    out.seek(table_start);
                    if(file.length() == 0) {
                        writeInt(out, 0);
                        writeInt(out, 0);
                    } else {
                        writeInt(out, (int)data_start);
                        writeInt(out, (int)file.length());
                    }
                    table_start = out.getFilePointer();
                    if(file.length() > 0) {
                        FileInputStream in = new FileInputStream(file);
                        byte buffer[] = new byte[(int) file.length()];
                        in.read(buffer);
                        out.seek(data_start);
                        out.write(buffer);
                        long curr_pos = out.getFilePointer();
                        data_start = curr_pos;
                        if(data_start % 16 > 0)
                            data_start += 16 - (data_start % 16);
                        while(curr_pos < data_start) {
                            out.writeByte(0);
                            curr_pos++;
                        }
                    }
                }
                out.setLength(data_start);
                out.close();
            } else {
                System.err.println("Empty directory\n");                
            }               
            System.out.println("Finished!");
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
