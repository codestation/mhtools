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

import base.MHUtils;

import jpcsp.crypto.CryptoEngine;
import keys.GameKeys;

public class KirkCypher extends MHUtils implements GameKeys {
	
    public void decrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            System.out.println("Decrypting savedata (KIRK engine): " + byte_bt.length + " bytes");
            System.out.println("Gamekey: " + getHex(gamekey));
            byte hash[] = new byte[0x10];
            byte out[] = new CryptoEngine().DecryptSavedata(byte_bt, byte_bt.length, gamekey, 0, hash);
            fd.write(out);
			fd.setLength(out.length);
            fd.close();
            System.out.println("Finished (" + out.length + " bytes)");
            System.out.println("Hash: " + getHex(hash));
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
            System.out.println("Encrypting savedata (KIRK engine): " + byte_bt.length + " bytes");
            System.out.println("Gamekey: " + getHex(gamekey));
            CryptoEngine ce = new CryptoEngine();
            byte out[] = ce.EncryptSavedata(byte_bt, byte_bt.length, gamekey, 0);
            fd.write(out);
			fd.setLength(out.length);
            fd.close();
            System.out.println("Finished (" + out.length + " bytes)");
            byte hash[] = ce.UpdateSavedataHashes(out, out.length, 0);
            RandomAccessFile hashfd = new RandomAccessFile("hash.bin", "rw");
            hashfd.write(hash);
            hashfd.close();
            System.out.println("Hash saved to hash.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
