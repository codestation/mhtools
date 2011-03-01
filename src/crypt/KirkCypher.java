/*  MHTrans - KIRK savedata decrypter/encrypter
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

import jpcsp.crypto.CryptoEngine;
import keys.GameKeys;

public class KirkCypher implements GameKeys {
    
    public void decrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            System.out.print("Decrypting savedata (KIRK engine)");
            byte out[] = new CryptoEngine().DecryptSavedata(byte_bt, byte_bt.length, gamekey, 1);
            fd.write(out);
            fd.close();
            System.out.println("Finished");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void encrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            System.out.println("Encrypting savedata (KIRK engine)");
            byte out[] = new CryptoEngine().EncryptSavedata(byte_bt, byte_bt.length, gamekey, 0);
            fd.write(out);
            fd.close();
            System.out.println("Finished");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
