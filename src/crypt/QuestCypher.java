/*  MHTrans - MH quest decrypter/encrypter
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import keys.QuestKeys;

public class QuestCypher implements QuestKeys {
    
    private short key_var_table[] = {0, 0, 0, 0};
    
    public boolean decrypt(String filein) {
        try {
            File fd = new File(filein);
            FileInputStream in = new FileInputStream(fd);
            byte byte_bt[] = new byte[(int)fd.length()];
            in.read(byte_bt);
            in.close();
            ByteBuffer bt = ByteBuffer.wrap(byte_bt);
            bt.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer sb = bt.asShortBuffer();
            short short_bt[] = new short[byte_bt.length/2];
            sb.get(short_bt);
            decrypt_quest(short_bt);
            sb.rewind();
            sb.put(short_bt);
            FileOutputStream out = new FileOutputStream(filein + ".dec");
            out.write(byte_bt);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public boolean encrypt(String filein) {
        try {
            File fd = new File(filein);
            FileInputStream in = new FileInputStream(fd);
            byte byte_bt[] = new byte[(int)fd.length()];
            in.read(byte_bt);
            in.close();
            update_sha1(byte_bt);
            ByteBuffer bt = ByteBuffer.wrap(byte_bt);
            bt.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer sb = bt.asShortBuffer();
            short short_bt[] = new short[byte_bt.length/2];
            sb.get(short_bt);
            encrypt_quest(short_bt);
            sb.rewind();
            sb.put(short_bt);
            FileOutputStream out = new FileOutputStream(filein + ".enc");
            out.write(byte_bt);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
      
    private void decrypt_quest(short pData[]) {   
        calc_key(key_var_table, pData);        
        for(int i = 8; i < pData.length*2; i += 2) {
            int index = (i >> 1);
            int key = get_xor_key(key_var_table, index & 0x03);
            pData[index] ^= (short)(key & 0xFFFF);
        }        
    }
    
    private void encrypt_quest(short pData[]) {
        decrypt_quest(pData);
    }
    
    private void calc_key(short pTable[], short pData[]) {
        for(int i = 0; i < 4; i++) {
            pTable[i] = pData[i];            
            if (pTable[i] == 0){
                pTable[i] = (short)(seed_0[i] & 0xFFFF);
            }
        }
    } 
    int i = 0;
    private int get_xor_key(short pTable[], int pos) {
        int key = (int) ((long)(pTable[pos] & 0xFFFF) * (long)(seed_0[pos] & 0xFFFF) % (seed_1[pos] & 0xFFFF));
        pTable[pos] = (short)(key & 0xFFFF);
        return key;
    }
    
    public void update_sha1(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            update_sha1(byte_bt);
            fd.write(byte_bt);
            fd.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void update_sha1(byte buf[]) {
        int len = ((buf[8+3] << 24) & 0xFFFFFFFF) + ((buf[8+2] << 16) & 0xFFFFFF) + ((buf[8+1] << 8) & 0xFFFF) + ((buf[8+0] << 0) & 0xFF);
        len += 0x10;
        byte buffer[] = new byte[len];
        System.arraycopy(buf, 0x20, buffer, 0, len-0x10);
        System.arraycopy(quest_sha1_key.getBytes(), 0, buffer, len-0x10, 0x10);
        try {
            MessageDigest md = MessageDigest.getInstance("sha-1");
            byte digest[] = md.digest(buffer);
            System.arraycopy(digest, 0, buf, 12, 0x10);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
