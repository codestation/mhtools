/*  MHP2GDEC v1.0 - MH TMH image extractor
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

package dec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import base.Decoder;
import base.EndianFixer;


/**
 * ExtractPluginD v1.0
 * 
 * @author Codestation
 */
public class ExtractPluginE extends EndianFixer implements Decoder {
    
    byte pmo[] = {0x70, 0x6d, 0x6f, 0x00};
    byte tmh[] = {0x2E, 0x54, 0x4D, 0x48};
    
    @Override
    public void extract(String filename) {
        String directory = filename.split("\\.")[0];
        new File(directory).mkdir();
        try {
            RandomAccessFile file = new RandomAccessFile(filename,"r");
            int count = readInt(file);
            long current = 4;
            for(int i = 0; i < count; i++) {
                file.seek(current);
                int file_offset = readInt(file);
                int file_size = readInt(file);
                current = file.getFilePointer();
                if(file_offset == 0) {
                    String empty = String.format("%s/%03d_empty.bin", directory, i);
                    System.out.println("Creating " + empty);
                   new File(empty).createNewFile();
                   continue;
                }
                file.seek(file_offset);
                byte buffer[] = new byte[file_size];
                file.read(buffer);
                String fileout = String.format(directory + "/%03d", i);
                if(equals(buffer, pmo, 4)) {
                    fileout += "_data.pmo";
                } else if(equals(buffer, tmh, 4)) {
                    fileout += "_image.tmh";
                } else {
                    fileout += "_data.bin";
                }
                System.out.println("Extracting " + fileout);
                FileOutputStream out = new FileOutputStream(fileout);
                out.write(buffer);
                out.close();
                if(fileout.endsWith(".tmh")) {
                    new ExtractPluginD().extract(fileout);
                }                
            }
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean equals(byte a[], byte b[], int size) {
        int i = 0;
        while(i < size) {
            if(a[i] != b[i])
                return false;
            i++;
        }
        return true;
    }
}
