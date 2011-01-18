package img;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import base.EndianFixer;

public class Gim extends EndianFixer {
    
    public static final int RGBA8888 = 3;
    public static final int RGBA5551 = 1;
    public static final int GIM_TYPE_PIXELS = 4;
    public static final int GIM_TYPE_NOPALETTE = 8;
    public static final int GIM_TYPE_PALETTE = 5;
    public static final int HEADER_SIZE = 16;
    public static final int BPP32_BYTE = 4;
    public static final int BPP16_BYTE = 2;
    public static final int BPP32 = 32;
    public static final int BPP16 = 16;
    
    private int size;
    private int flags[] = new int[3];
    private int data_size;
    private int data_flags;
    private int data_type;
    private int width;
    private int height;
    private byte imagedata[];
    private int palette_size;
    private int palette_flags;
    private int palette_type;
    private int palette_count;
    private byte palettedata[];
    private int palettedata_count = 0;
    private boolean loaded = false;
    
    public void load(InputStream in) throws EOFException, IOException {
        size = readInt(in);
        flags[0] = readInt(in);
        flags[1] = readInt(in);
        flags[2] = readInt(in);
        data_size = readInt(in);
        data_flags = readInt(in);
        data_type = readInt(in);
        width = readShort(in);
        height = readShort(in);
        imagedata = new byte[data_size - 16];
        in.read(imagedata);
        if(data_type != GIM_TYPE_NOPALETTE) {
            palette_size = readInt(in);
            palette_flags = readInt(in);
            palette_type = readInt(in);
            palette_count = readInt(in);
            int palette_datasize = palette_count * (palette_type == RGBA8888 ? 4 : 2);
            palettedata = new byte[palette_datasize];
            in.read(palettedata);
        }
        loaded = true;
    }
    
    public boolean setRGBarray(int width, int height, int rgb[], int data_type, int palette_type) {
        flags[0] = 0;
        flags[1] = 1;
        flags[2] = 1;
        data_size = HEADER_SIZE;
        data_flags = 1;
        this.width = width;
        this.height = height;
        palette_size = HEADER_SIZE;
        this.palette_type = palette_type;
        palette_flags = 2;
        this.data_type = data_type;
        if(data_type == GIM_TYPE_PIXELS) {
            int palettesize = 16 * (palette_type == RGBA8888 ? 4 : 2);
            palettedata = new byte[palettesize];
            data_size += (width / 2) * height;
            imagedata = new byte[data_size - HEADER_SIZE];
            palette_count = fill_to_palette(rgb);
            palette_count = 16;
            palette_size += palettesize;
            size = data_size + palette_size + HEADER_SIZE;            
        } else if(data_type == GIM_TYPE_PALETTE) {
            int palettesize = 256 * (palette_type == RGBA8888 ? 4 : 2);
            palettedata = new byte[palettesize];
            data_size += width * height;
            imagedata = new byte[data_size - HEADER_SIZE];
            palette_count = fill_to_palette(rgb);
            palette_count = 256;
            palette_size += palettesize;
            size = data_size + palette_size + HEADER_SIZE;
        } else {
            loaded = false;
            return false;
        }        
        loaded = true;
        return true;
    }
    
    public int[]getRGBarray() {
        if(!loaded)
            return null;
        int dsx = width;
        int dsy = height;
        int mod = (data_type == GIM_TYPE_PALETTE ? BPP16 : BPP32);
        int colorsize = palette_type == RGBA8888 ? BPP32_BYTE : BPP16_BYTE;
        dsx /= mod;
        dsy /= 8;
        int counter = 0;
        int bitmapbuffer[] = new int[width * height]; 
        boolean flip = false;
        for (int sy = 0; sy < dsy; sy++) {
            for (int sx = 0; sx < dsx; sx++) {
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < mod; x++) {
                        int pos = (int)(imagedata[counter] & 0xFF);
                        if(data_type == GIM_TYPE_PIXELS) {
                            if(flip)
                                pos = pos >> 4;
                            else
                                pos = pos & 0xF;
                        }
                        int off = (((sy * 8) + y) * width + ((sx * mod) + x));
                        int rgb = get_color(pos * colorsize);
                        bitmapbuffer[off] = rgb;
                        if(data_type == GIM_TYPE_PIXELS) {
                            if(flip) {
                                counter++;
                                flip = false;
                            } else {
                                flip = true;
                            }
                        } else {
                            counter++;
                        }
                    }
                }
            }
        }
        return bitmapbuffer;
    }
    
    public boolean write(OutputStream out)throws IOException {
        if(!loaded)
            return false;
        writeInt(out, size);
        writeInt(out, flags[0]);
        writeInt(out, flags[1]);
        writeInt(out, flags[2]);
        writeInt(out, data_size);
        writeInt(out, data_flags);
        writeInt(out, data_type);
        writeShort(out, width);
        writeShort(out, height);
        out.write(imagedata);
        if(data_type != GIM_TYPE_NOPALETTE) {
            writeInt(out, palette_size);
            writeInt(out, palette_flags);
            writeInt(out, palette_type);
            writeInt(out, palette_count);
            out.write(palettedata);
        }
        return true;
    }
    
    private int fill_to_palette(int rgb[]) {
        int mod = (data_type == GIM_TYPE_PALETTE ? BPP16 : BPP32);
        int dsx = width / mod;
        int dsy = height / 8;
        boolean flip = false;
        int counter = 0;
        palettedata_count = 0;
        for (int sy = 0; sy < dsy; sy++) {
            for (int sx = 0; sx < dsx; sx++) {
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < mod; x++) {
                        int off = (((sy * 8) + y) * width + ((sx * mod) + x));
                        if(data_type == GIM_TYPE_PALETTE && palettedata_count > 256 ||
                                data_type == GIM_TYPE_PIXELS && palettedata_count > 16) {
                            return 0;
                        }
                        int color = rgb[off];
                        int index = set_unique_color(color);
                        if(data_type == GIM_TYPE_PIXELS) {
                            if(flip) {
                                imagedata[counter] |= (byte) (index << 4);
                                counter++;
                                flip = false;
                            } else {
                                imagedata[counter] = (byte) (index & 0xF);
                                flip = true;
                            }
                        } else {
                            imagedata[counter] = (byte) index;
                            counter++;
                        }
                    }
                }
            }
        }
        return palette_count;
    }
    
    private void set_color(int color, int offset) {
        int A = (color >> 24) & 0xFF;
        int R = (color >> 16) & 0xFF;
        int G = (color >> 8) & 0xFF;
        int B = color & 0xFF;
        if(palette_type == RGBA8888) {
            palettedata[offset + 2] = (byte) B;
            palettedata[offset + 1] = (byte) G;
            palettedata[offset + 0] = (byte) R;
            palettedata[offset + 3] = (byte) A;
        } else {
            A = A == 0 ? 0 : 1;
            R = R >> 3;
            G = G >> 3;
            B = B >> 3;
            color = R;
            color |= G << 5;
            color |= B << 10;
            color |= A << 15;
            palettedata[offset+1] = (byte) (color >> 8 & 0xFF);
            palettedata[offset+0] = (byte) (color & 0xFF);
        }
    }
    
    private int set_unique_color(int color) {
        int i = 0;
        while(i < palettedata_count) {
            if(color == get_color(i * (palette_type == RGBA8888 ? 4 : 2))) {
                return i;
            }
            i++;
        }
        set_color(color, i * (palette_type == RGBA8888 ? 4 : 2));
        palettedata_count = i + 1;
        return i;
    }
    
    private int get_color(int offset) {
        int A, R, G, B;
        if(palette_type == RGBA8888) {
            B = (int)palettedata[offset + 2] & 0xFF;
            G = (int)palettedata[offset + 1] & 0xFF;
            R = (int)palettedata[offset + 0] & 0xFF;
            A = (int)palettedata[offset + 3] & 0xFF;
        } else {
            int color = (((int)(palettedata[offset+1]) << 8) & 0xFF00);
            color |=((int)(palettedata[offset]) & 0xFF);
            R = color & 0x1F;
            G = (color >> 5) & 0x1F;
            B = (color >> 10) & 0x1F;
            A = (color >> 15) & 0x1;
            R = (R << 3) | (R >> 2);
            G = (G << 3) | (G >> 2);
            B = (B << 3) | (B >> 2);
            A = A == 0 ? 0 : 255;
        }
        int res = ((A << 24) + (R << 16) + (G << 8) + B);
        return res;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getDataType() {
        return data_type;
    }
    
    public int getPaletteType() {
        return palette_type;
    }
    
    public int getPaletteCount() {
        return palette_count;
    }
    
    public boolean isSupported() {
        return (data_type != GIM_TYPE_NOPALETTE) || (data_type == GIM_TYPE_PIXELS && palette_count <=16) ||
        (data_type == GIM_TYPE_PALETTE && palette_count <= 256);
    }
}
