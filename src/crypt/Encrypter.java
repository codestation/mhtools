/*  MHP2GDEC v1.0 - MHP2G data.bin/xxxx.bin encrypter
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

package crypt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Encrypter extends DecryptTable {

    private byte[] encrypt_table;

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
            int file_number = extractNumber(in);
            long file_len = filein.length();
            long table_len = (getOffset(file_number + 1) << 11) - (getOffset(file_number) << 11);
            if(file_len < table_len) {
                System.out.println(in + " filesize is less than the stored table by " +
                        (table_len - file_len) + " bytes, fill the file with 0x00 at the end");
                System.exit(1);
            }else if(file_len > table_len) {
                System.err.println(in + " filesize is greater than the stored table by " + 
                        (file_len - table_len) + " bytes, aborting");
                System.exit(1);
            }
            initSeed(getOffset(file_number));
            byte[] buffer = new byte[4];
            System.out.println("Encrypting " + in);
            while (filein.read(buffer) >= 0) {
                long gamma = get_table_value(buffer, 0);
                long beta = getBeta();
                long alpha = beta ^ gamma;
                set_table_value(buffer, 0, alpha);
                buffer[0] = encrypt_table[buffer[0] & 0xFF];
                buffer[1] = encrypt_table[buffer[1] & 0xFF];
                buffer[2] = encrypt_table[buffer[2] & 0xFF];
                buffer[3] = encrypt_table[buffer[3] & 0xFF];
                fileout.write(buffer);
            }
            filein.close();
            fileout.close();
            System.out.println("Finished!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
