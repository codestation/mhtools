/*  MHTools - MH data.bin/xxxx.bin decrypter
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

package crypt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import keys.DataKeys;

import base.MHUtils;

public class Encrypter extends DecryptUtils implements DataKeys {

    private byte[] encrypt_table;
    
    @Override
    protected byte[] getDecryptTable() {
        return decrypter_table;
    }
    
    @Override
    protected long getSeedKeyA() {
        return seed_a;
    }

    @Override
    protected long getSeedKeyB() {
        return seed_b;
    }

    @Override
    protected long getModA() {
        return mod_a;
    }

    @Override
    protected long getModB() {
        return mod_b;
    }

    public Encrypter() {
        encrypt_table = new byte[256];
        for (int i = 0; i < 256; i++) {
            encrypt_table[decrypt_table[i] & 0xFF] = (byte) i;
        }
    }

    public void encrypt(String in, String out) {
        try {
            RandomAccessFile filein = new RandomAccessFile(in, "r");
            RandomAccessFile fileout = new RandomAccessFile(out, "rw");
            int file_number = MHUtils.extractNumber(in);
            long file_len = filein.length();
            long table_len = (MHUtils.getOffset(file_number + 1) << 11) - (MHUtils.getOffset(file_number) << 11);
            int filler = (int) (table_len - file_len);
            if(file_len < table_len) {
                System.out.println("Adding " + filler  + " bytes of filler at the end");
            }else if(file_len > table_len) {
                System.err.println(in + " filesize is greater than the stored table by " + 
                        (file_len - table_len) + " bytes, aborting");
                System.exit(1);
            }
            initSeed(MHUtils.getOffset(file_number));
            byte[] buffer = new byte[4];
            System.out.println("Encrypting " + in);
            while (filein.read(buffer) >= 0) {
                long gamma = get_table_value(buffer, 0);
                long beta = getBeta();
                long alpha = beta ^ gamma;
                set_table_value(buffer, 0, alpha);
                get_table_value(encrypt_table, buffer);
                fileout.write(buffer);
            }
            filein.close();
            if(filler > 0) {
            	byte filler_arr[] = new byte[filler];
            	fileout.write(filler_arr);
            }
            fileout.close();
            System.out.println("Finished!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
