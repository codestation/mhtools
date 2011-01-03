package base;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

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
            install_tables(out, list);
            out.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void install_tables(RandomAccessFile out, List<String> list) {
        System.out.print("Trying to add data_install tables...");
        try {
            BufferedReader in = new BufferedReader(new FileReader("data_install.txt"));
            out.seek(out.length());
            
            String line;
            Vector<Integer> patch_files = new Vector<Integer>();
            Vector<String> install_files = new Vector<String>();
            Vector<String> offsets = new Vector<String>();
            
            while((line = in.readLine()) != null) {
                if(!line.startsWith("#")) {
                    String []tokens = line.split(",");
                    patch_files.add(Integer.parseInt(tokens[0]));
                    install_files.add(tokens[1]);
                    offsets.add(tokens[2]);
                }
            }
            Vector<String> install_uniq = new Vector<String>(new LinkedHashSet<String>(install_files));
            while(install_uniq.remove("NONE"));
            writeInt(out, install_uniq.size());
            int install_count = 0;
            String match = install_files.firstElement();
            out.write(match.getBytes());
            writeInt(out, install_count);
            for(String file : install_files) {
                if(!file.equals("NONE") && !match.equals(file)) {
                    out.write(file.getBytes());
                    match = file;
                    writeInt(out, install_count);
                }
                install_count++;
            }
            for(String trans_file : list) {
                int index = patch_files.indexOf(extractNumber(trans_file));
                int offset = -1;
                if(index != -1) {
                    // UGH!!, 11 years and the Integer.parseInt bug isn't fixed
                    // This is the last time that i use Java in a project of mine
                    // http://bugs.sun.com/view_bug.do?bug_id=4215269
                    offset = (int)Long.parseLong(offsets.get(index),16);
                } else {
                    System.out.println("Install info not found for " + trans_file);
                }
                writeInt(out, offset);
            }            
            long filelength = out.length();
            if(filelength % 16 > 0) {
                filelength += 16 - (filelength % 16);
                out.setLength(filelength);
            }
            System.out.println("OK");
        } catch (FileNotFoundException e) {
            System.out.println("\ndata_install.txt not found");
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
