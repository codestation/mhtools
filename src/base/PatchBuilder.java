package base;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import crypt.DecryptTable;

public class PatchBuilder extends DecryptTable {
    public void create(String[] args) {
        List<String> list = new ArrayList<String>(Arrays.asList(args));
        list.remove(0);
        String outfile = list.get(list.size()-1);
        list.remove(list.size()-1);
        try {
            RandomAccessFile out = new RandomAccessFile(outfile, "rw");
            int table_size = (list.size() + 1) * 4 * 2;
            System.out.println("Table size: " + table_size + " bytes");
            if(table_size % 16 > 0) {
                table_size += 16 - (table_size % 16);
                System.out.println("Table size (padded): " + table_size + " bytes");
            }
            out.setLength(table_size);
            out.seek(0);
            long current = 0;
            writeInt(out, list.size());
            for (String file : list) {
                int offset = getOffset(extractNumber(file)) << 11;
                writeInt(out, offset);
                InputStream in = new FileInputStream(file);
                int len = (int)new File(file).length();
                writeInt(out, (int)len);
                System.out.println(file + ", offset: " + offset  + ", size: " + len);
                current = out.getFilePointer();
                byte buffer[] = new byte[1024];
                out.seek(out.length());
                while((len = in.read(buffer)) > 0)
                    out.write(buffer, 0, len);
                in.close();
                out.seek(current);                
            }
            writeInt(out, 0);
            out.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * The "writeInt" function of java writes in BigEndian mode but we need
     * LittleEndian so i made a custom function for that
     * 
     * @param file
     * @throws IOException
     *             if any error occur while writing
     */
    private void writeInt(RandomAccessFile file, int value)
            throws IOException {
        int ch1 = (byte) (value >>> 24);
        int ch2 = (byte) (value >>> 16);
        int ch3 = (byte) (value >>> 8);
        int ch4 = (byte) value;
        file.write(ch4);
        file.write(ch3);
        file.write(ch2);
        file.write(ch1);
    }
}
