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
    
    @Override
    public void extract(String filename) {
        String directory = filename.split("\\.")[0];
        new File(directory).mkdir();
        try {
            RandomAccessFile file = new RandomAccessFile(filename,"rw");
            int flag0 = readInt(file);
            if(flag0 == 0x8) {
                int init0_offset = readInt(file);
                int init0_size = readInt(file);
                
                int pmo0_offset = readInt(file);
                int pmo0_size = readInt(file);
                int tmh0_offset = readInt(file);
                int tmh0_size = readInt(file);
                int data0_offset = readInt(file);    
                int data0_size = readInt(file);
                
                @SuppressWarnings("unused")
                int pad0_offset = readInt(file);
                @SuppressWarnings("unused")
                int pad0_size = readInt(file);
                
                int pmo1_offset = readInt(file);
                int pmo1_size = readInt(file);
                int tmh1_offset = readInt(file);
                int tmh1_size = readInt(file);
                int data1_offset = readInt(file);
                int data1_size = readInt(file);
                
                file.seek(init0_offset);
                save_file(directory + "/000_unk0.dat", file, init0_size);
                
                file.seek(pmo0_offset);
                save_file(directory + "/001_data0.pmo", file, pmo0_size);
                file.seek(tmh0_offset);
                save_file(directory + "/002_data0.tmh", file, tmh0_size);
                Decoder dec = new ExtractPluginD();
                dec.extract(directory + "/002_data0.tmh");
                file.seek(data0_offset);
                save_file(directory + "/003_data0.bin", file, data0_size);
                
                file.seek(pmo1_offset);
                save_file(directory + "/004_data1.pmo", file, pmo1_size);
                file.seek(tmh1_offset);
                save_file(directory + "/005_data1.tmh", file, tmh1_size);
                dec.extract(directory + "/005_data1.tmh");
                file.seek(data1_offset);
                save_file(directory + "/006_data1.bin", file, data1_size);
            }
            if(flag0 == 0x4) {
                int init0_offset = readInt(file);
                int init0_size = readInt(file);                
                int pmo0_offset = readInt(file);
                int pmo0_size = readInt(file);
                int tmh0_offset = readInt(file);
                int tmh0_size = readInt(file);
                
                file.seek(init0_offset);
                save_file(directory + "/000_unk0.dat", file, init0_size);
                
                file.seek(pmo0_offset);
                save_file(directory + "/001_data0.pmo", file, pmo0_size);
                file.seek(tmh0_offset);
                save_file(directory + "/002_data0.tmh", file, tmh0_size);
                Decoder dec = new ExtractPluginD();
                dec.extract(directory + "/002_data0.tmh");
            }
            if(flag0 == 0x3) {
                int pmo0_offset = readInt(file);
                int pmo0_size = readInt(file);            
                int unk0_offset = readInt(file);                
                int unk0_size = readInt(file);                
                int tmh0_offset = readInt(file);                
                int tmh0_size = readInt(file);
                
                file.seek(pmo0_offset);
                save_file(directory + "/000_data0.pmo", file, pmo0_size);
                file.seek(unk0_offset);
                save_file(directory + "/001_unk0.dat", file, unk0_size);
                file.seek(tmh0_offset);
                save_file(directory + "/002_data0.tmh", file, tmh0_size);
                Decoder dec = new ExtractPluginD();
                dec.extract(directory + "/002_data0.tmh");
            }
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void save_file(String filename, RandomAccessFile in, int size) throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(filename);
        byte buffer[] = new byte[size];
        in.read(buffer);
        out.write(buffer);
        out.close();
    }
}
