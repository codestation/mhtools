/*  MHTrans - MH decrypter utilities
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

public abstract class DecryptUtils {
    
    protected byte decrypt_table[];

    private long lower_offset;
    private long upper_offset;
    
    private long seed_key_a;
    private long seed_key_b;
    
    private long mod_a;
    private long mod_b;
    
    abstract protected byte[] getDecryptTable();
    abstract protected long getSeedKeyA();
    abstract protected long getSeedKeyB();
    abstract protected long getModA();
    abstract protected long getModB();
    
    public DecryptUtils() {
        decrypt_table = getDecryptTable();
        seed_key_a = getSeedKeyA();
        seed_key_b = getSeedKeyB();
        mod_a = getModA();
        mod_b = getModB();
    }
    
    protected void initSeed(long seed) {
        lower_offset = seed & 0xFFFF;
        upper_offset = seed >> 0x10 & 0xFFFF;
        if (lower_offset == 0) {
            lower_offset = seed_key_a;
        }
        if (upper_offset == 0) {
            upper_offset = seed_key_b;
        }
    }

    protected long getBeta() {
        lower_offset = (lower_offset * seed_key_a) % mod_a;
        upper_offset = (upper_offset * seed_key_b) % mod_b;
        return lower_offset + (upper_offset << 0x10);
    }

    protected void set_table_value(byte table[], int pos, long value) {
        table[pos] = (byte) value;
        table[pos + 1] = (byte) (value >> 8);
        table[pos + 2] = (byte) (value >> 16);
        table[pos + 3] = (byte) (value >> 24);
    }

    protected long get_table_value(byte table[], int pos) {
        return (table[pos] & 0xFF) + ((long) (table[pos + 1] & 0xFF) << 8)
                + ((long) (table[pos + 2] & 0xFF) << 16)
                + ((long) (table[pos + 3] & 0xFF) << 24);
    }
    
    protected void get_table_value(byte table[], byte buffer[]) {
        buffer[0] = table[buffer[0] & 0xFF];
        buffer[1] = table[buffer[1] & 0xFF];
        buffer[2] = table[buffer[2] & 0xFF];
        buffer[3] = table[buffer[3] & 0xFF];
    }
    
    protected void set_table_data(byte table[], int i) {
        table[i] = decrypt_table[table[i] & 0xFF];
        table[i + 1] = decrypt_table[table[i + 1] & 0xFF];
        table[i + 2] = decrypt_table[table[i + 2] & 0xFF];
        table[i + 3] = decrypt_table[table[i + 3] & 0xFF];
    }
}
