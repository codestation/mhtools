/*  MHTrans - MH savedata decrypter/encrypter
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

package crypt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import keys.SavedataKeys;

public class SavedataCypher extends DecryptUtils implements SavedataKeys {

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

    public boolean decrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            decrypt_buffer(byte_bt);
            fd.write(byte_bt);
            fd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    private void decrypt_buffer(byte buffer[]) {
        int len = buffer.length - 4;
        byte seed[] = new byte[4];
        System.arraycopy(buffer, len, seed, 0, 4);        
        get_table_value(decrypt_table, seed);
        get_table_value(decrypt_table, seed);          
        long alpha = get_table_value(seed, 0);
        initSeed(alpha);
        for (int i = 0; i < len; i += 4) {
            set_table_data(buffer, i);
            alpha = get_table_value(buffer, i);
            long beta = getBeta();
            long gamma = alpha ^ beta;
            set_table_value(buffer, i, gamma);
            set_table_data(buffer, i);
        }
    }
}
