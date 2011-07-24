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
import java.util.Arrays;

import keys.QuestKeys;

public class QuestCypher implements QuestKeys {
    
    private short key_var_table[] = {0, 0, 0, 0};
    
    private final int QUEST_SIZE = 24592;
    private final int QUEST_LAST = 2496;
    
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
            System.out.println("Decrypting quest file");
            int len = decrypt_quest(short_bt);
            sb.rewind();
            sb.put(short_bt);
            FileOutputStream out = new FileOutputStream(filein + ".dec");
            out.write(byte_bt, 0, len);
            out.close();
            System.out.println("finished.");
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
            System.out.println("Updating sha1 hash");
            update_sha1(byte_bt);
            ByteBuffer bt = ByteBuffer.wrap(byte_bt);
            bt.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer sb = bt.asShortBuffer();
            short short_bt[] = new short[byte_bt.length/2];
            sb.get(short_bt);
            System.out.println("Encrypting quest file");
            encrypt_quest(short_bt);
            sb.rewind();
            sb.put(short_bt);
            FileOutputStream out = new FileOutputStream(filein + ".enc");
            out.write(byte_bt);
            out.close();
            System.out.println("Finished");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public boolean extract(String filein) {
        try {
            File fd = new File(filein);
            if(fd.length() != QUEST_SIZE * 100 + QUEST_LAST) {
            	System.err.println("Invalid file size");
            	return false;
            }
            
            String filename = new File(filein).getName();
            String directory = filename.split("\\.")[0];
            new File(directory).mkdir();
            
            FileInputStream in = new FileInputStream(fd);
            byte byte_bt[] = new byte[(int)fd.length()];
            in.read(byte_bt);
            in.close();
            for(int i = 0; i < 100; i++) {
            	ByteBuffer bt = ByteBuffer.wrap(byte_bt,i * QUEST_SIZE, QUEST_SIZE);
            	bt.order(ByteOrder.LITTLE_ENDIAN);
                ShortBuffer sb = bt.asShortBuffer();
                short short_bt[] = new short[QUEST_SIZE/2];
                sb.get(short_bt);
                System.out.println("Decrypting quest file # " +  i);
                int len = decrypt_quest(short_bt);
                if(len > 0) {
                	sb.rewind();
                	sb.put(short_bt);
                	String outname = String.format("%s/%02d_quest.bin", directory, i);
                	FileOutputStream out = new FileOutputStream(outname);
                	out.write(byte_bt, i * QUEST_SIZE, len);
                    out.close();
                } else {
                	System.out.println("The quest # " + i + " is empty");
                }
            }
            System.out.println("Writting enddata.bin");
            String outname = String.format("%s/enddata.bin", directory);
        	FileOutputStream out = new FileOutputStream(outname);
        	out.write(byte_bt, 100 * QUEST_SIZE, QUEST_LAST);
            out.close();
        } catch (Exception e) {
        	e.printStackTrace();
		}
    	return true;
    }
      
    private int decrypt_quest(short pData[]) {
    	// check if is a empty quest
    	short check[] = new short[8];
    	System.arraycopy(pData, 0, check, 0, 8);
    	if(Arrays.equals(check, new short[8])) {
    		return 0;
    	}
        calc_key(key_var_table, pData);
        for(int i = 8; i < pData.length*2; i += 2) {
            int index = (i >> 1);
            int key = get_xor_key(key_var_table, index & 0x03);
            pData[index] ^= (short)(key & 0xFFFF);
        }
        return (int)pData[4] + (int)(pData[5] << 16) + 0x20;
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
            System.out.println("Updating sha1 hash");
            update_sha1(byte_bt);
            fd.write(byte_bt);
            fd.close();
            System.out.println("Finished");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void update_sha1(byte buf[]) {
        int len = ((buf[8+3] << 24) & 0xFFFFFFFF) + ((buf[8+2] << 16) & 0xFFFFFF) + ((buf[8+1] << 8) & 0xFFFF) + ((buf[8+0] << 0) & 0xFF);
        len += quest_sha1_key.length();
        byte buffer[] = new byte[len];
        System.arraycopy(buf, 0x20, buffer, 0, len-quest_sha1_key.length());
        System.arraycopy(quest_sha1_key.getBytes(), 0, buffer, len-quest_sha1_key.length(), quest_sha1_key.length());
        try {
            MessageDigest md = MessageDigest.getInstance("sha-1");
            byte digest[] = md.digest(buffer);
            System.arraycopy(digest, 0, buf, 12, digest.length);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
