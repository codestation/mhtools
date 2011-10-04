package img;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import base.MHUtils;

public class Gim extends MHUtils {
	
	public final static int RGBA5650 = 0;
	public final static int RGBA5551 = 1;
	public final static int RGBA4444 = 2;
	public final static int RGBA8888 = 3;
	public final static int INDEX_4 = 4;
	public final static int INDEX_8 = 5;
	public final static int INDEX_16 = 6;
	public final static int INDEX_32 = 7;
	public final static int GIM_UNKNOWN = 8;
	
	final static int HEADER_SIZE = 16;
	final static int WIDTH_BLOCK = 16;
	final static int HEIGHT_BLOCK = 8;
	
	int size;
	int flags[];
	
	int data_size;
	int data_flags;
	int data_type;
	int width;
	int height;
	byte imagedata[];
	
    int palette_size;
    int palette_flags;
    int palette_type;
    int palette_count;
    Vector<byte[]> palettedata;
    
    int max_colors;
    int palette_number;
    
    public int load(InputStream in) throws EOFException, IOException {
    	int color_size;
    	
        size = readInt(in);
        flags = new int[3];
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
        
        if(data_type != GIM_UNKNOWN) {
            palette_size = readInt(in);
            palette_flags = readInt(in);
            palette_type = readInt(in);
            palette_count = readInt(in);
            
			switch (palette_type) {
			case RGBA5650:
			case RGBA5551:
			case RGBA4444:
				color_size = 2;
				break;
			case RGBA8888:
				color_size = 4;
				break;
			default:
				System.err.println("error: palette type unknown");
				return -1;
			}
			
            switch (data_type) {
            case INDEX_4:
                max_colors = (int) Math.pow(2, 4);
                break;
            case INDEX_8:
                max_colors = (int) Math.pow(2, 8);
                break;
            case INDEX_16:
                max_colors = (int) Math.pow(2, 16);
                break;
            case INDEX_32:
                max_colors = (int) Math.pow(2, 32);
                break;
            }
            
            palette_number = palette_count / max_colors;
            
            palettedata = new Vector<byte[]>();
            for(int i = 0; i < palette_number; i++) {
                palettedata.add(new byte[(palette_count * color_size) / palette_number]);
                in.read(palettedata.elementAt(i));
            }
        } else {
            palette_number = 0;
        }
        return palette_number;
    }
    
    public boolean write(OutputStream out)throws IOException {    	
        if(imagedata == null) {
            return false;
        }
        
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
        
        if(data_type != GIM_UNKNOWN) {
            writeInt(out, palette_size);
            writeInt(out, palette_flags);
            writeInt(out, palette_type);
            writeInt(out, palette_count);
            for(byte[] p : palettedata) {
                out.write(p);
            }
        }
        
        return true;
    }
    
    public boolean setRGBarray(int width, int height, int rgb[], int data_type, int palette_type, int palette) {
    	int color_size;
    	int color_count;
    	
    	if(palette == 0) {        	
        	flags = new int[3];
            flags[0] = 0;
            flags[1] = 1;
            flags[2] = 1;
            data_flags = 1;
            this.width = width;
            this.height = height;
            palette_size = HEADER_SIZE;
            this.palette_type = palette_type;
            palette_flags = 2;
            this.data_type = data_type;
            palettedata = new Vector<byte[]>();
            palette_count = 0;
    	}
        switch (palette_type) {
		case RGBA5650:
		case RGBA5551:
		case RGBA4444:
			color_size = 2;
			break;
		case RGBA8888:
			color_size = 4;
			break;
		default:
			System.err.println("error: palette type unknown");
			return false;
		}
        
        switch(data_type) {
        case INDEX_4:
        	color_count = (int)Math.pow(2, 4);
        	data_size = HEADER_SIZE + (width / 2) * height;
        	break;
        case INDEX_8:
        	color_count = (int)Math.pow(2, 8);
        	data_size = HEADER_SIZE + width * height;
        	break;
        default:
        	System.err.println("Error, unknown data type");
        	return false;
        }
        palettedata.add(new byte[color_count * color_size]);
    	if(palette == 0) {
    	    imagedata = new byte[data_size - HEADER_SIZE];
    	    palette_size = HEADER_SIZE;
    	    size = (HEADER_SIZE * 2) + data_size;
    	}
    	if(fill_palette(rgb, palette) < 0) {
    		System.err.println("error, couldn't fill palette");
    		return false;
    	}
    	palette_count += color_count;
    	palette_size += color_count * color_size;
    	size += color_count * color_size;
    	return true;
    }
    
    public int[]getRGBarray(int palette) {
        int pos = 0;
    	int color;
    	int offset;
    	int counter;
    	int modifier;
    	int bitmapbuffer[];
    	int palettedata_int[] = null;
    	short palettedata_short[] = null;
    	IntBuffer intbuf;
    	ShortBuffer shortbuf;
    	
    	bitmapbuffer = new int[width * height];
    	ByteBuffer bytearr = ByteBuffer.wrap(palettedata.elementAt(palette));
    	
    	switch(data_type) {
    	case INDEX_4:
    	    modifier = 2;
    	    break;
    	default:
    	    modifier = 1;
    	    break;
    	}
    	
    	switch(palette_type) {
        case RGBA5650:
        case RGBA5551:
        case RGBA4444:
            palettedata_short = new short[palettedata.elementAt(palette).length / 2];
            shortbuf = bytearr.asShortBuffer();
            shortbuf.get(palettedata_short);
            break;
        case RGBA8888:
            palettedata_int = new int[palettedata.elementAt(palette).length / 4];
            intbuf = bytearr.asIntBuffer();
            intbuf.get(palettedata_int);
            break;
        default:
            System.err.println("Error, unknown data type");
            return null;
        }
    	
        counter = 0;
        for (int sy = 0; sy < height / HEIGHT_BLOCK; sy++) {
            for (int sx = 0; sx < width / (WIDTH_BLOCK * modifier); sx++) {
                for (int y = 0; y < HEIGHT_BLOCK; y++) {
                    for (int x = 0; x < (WIDTH_BLOCK * modifier); x++) {
                    	offset = (((sy * HEIGHT_BLOCK) + y) * width + ((sx * (WIDTH_BLOCK * modifier)) + x));
                        pos = imagedata[counter] & 0xFF;
                    	switch(data_type) {
                    	case INDEX_4:
                            if((x % 2) == 0) {
                                pos = pos & 0xF;
                            } else {
                                pos = pos >> 4;
                                counter++;
                            }
                            break;
                    	case INDEX_8:
                    	    counter++;
                            break;                            
                    	}
                    	switch(palette_type) {
                        case RGBA5650:
                        case RGBA5551:
                        case RGBA4444:
                            if(counter == 15)
                                counter = 15;
                            color = color5551to8888(palettedata_short[pos]);
                            break;
                    	case RGBA8888:
                            color = changeEndianess(palettedata_int[pos]);
                            break;
                    	default:
                    	    color = 0;
                    	}
                    	bitmapbuffer[offset] = color;
                    }
                }
            }
        }
        return bitmapbuffer;
    }
    
    public int fill_palette(int rgb[], int palette) {
        int pos;
        int color;
        int offset;
        int counter;
        int modifier;
        int color_count;
        int index_offset;
        int palettedata_int[] = null;
        short palettedata_short[] = null;
        
        switch(data_type) {
        case INDEX_4:
            modifier = 2;
            color_count = (int)Math.pow(2, 4);
            break;
        default:
            color_count = (int)Math.pow(2, 8);
            modifier = 1;
            break;
        }

        switch(palette_type) {
        case RGBA5650:
        case RGBA5551:
        case RGBA4444:
            palettedata_short = new short[color_count];
            break;
        case RGBA8888:
            palettedata_int = new int[color_count];
            break;
        default:
            System.err.println("Error, unknown data type");
            return 0;
        }
        pos = 0;
        counter = 0;
        index_offset = 0;
        for (int sy = 0; sy < height / HEIGHT_BLOCK; sy++) {
            for (int sx = 0; sx < width / (WIDTH_BLOCK * modifier); sx++) {
                for (int y = 0; y < HEIGHT_BLOCK; y++) {
                    for (int x = 0; x < (WIDTH_BLOCK * modifier); x++) {
                        offset = (((sy * HEIGHT_BLOCK) + y) * width + ((sx * (WIDTH_BLOCK * modifier)) + x));                                         
                        color = rgb[offset];
                        switch(palette_type) {
                        case RGBA8888:
                            color = swapColor(color);
                            if(counter > 0) {
                                for(pos = 0; pos < counter; pos++) {
                                    if(palettedata_int[pos] == color) {
                                        break;
                                    }
                                }
                                if(pos >= counter) {
                                    pos = -1;
                                }
                            } else {
                                pos = 0;
                                palettedata_int[counter++] = color;
                            }
                            if(pos < 0) {
                                pos = counter;
                                palettedata_int[counter++] = color;
                            }
                            break;
                        case RGBA5650:
                        case RGBA5551:
                        case RGBA4444:
                            //FIXME whole loop
                            short color16 = color8888to5551(color);
                            if(counter > 0) {
                                for(pos = 0; pos < counter; pos++) {
                                    if(palettedata_short[pos] == color16) {
                                        break;
                                    }
                                }
                                if(pos >= counter) {
                                    pos = -1;
                                }
                            } else {
                                pos = 0;
                                palettedata_short[counter++] = color16;
                            }
                            if(pos < 0) {
                                pos = counter;
                                palettedata_short[counter++] = color16;
                            }                      
                            break;
                        }
                        
                        switch(data_type) {
                        case INDEX_4:
                            if((x % 2) == 0) {
                                imagedata[index_offset] |= (byte) (pos & 0xF);
                            } else {
                                imagedata[index_offset] |= (byte) (pos << 4);
                                index_offset++;
                            }
                            break;
                        case INDEX_8:
                            imagedata[index_offset++] = (byte) (pos & 0xFF);
                            break;
                        }
                    }
                }
            }
        }
        ByteBuffer bytebuf = ByteBuffer.wrap(palettedata.elementAt(palette));
        switch(palette_type) {
        case RGBA8888:
            IntBuffer intbuf = bytebuf.asIntBuffer();
            intbuf.put(palettedata_int);
            break;
        case RGBA5650:
        case RGBA5551:
        case RGBA4444:
            ShortBuffer shortbuf = bytebuf.asShortBuffer();
            shortbuf.put(palettedata_short);
            break;
        }
        return counter;
    }
        
    private int color5551to8888(short color) {
        color = (short) ((int)((color & 0xFF) << 8) | (color >> 8) & 0xFF);
        int R = color & 0x1F;
        int G = (color >> 5) & 0x1F;
        int B = (color >> 10) & 0x1F;
        int A = (color >> 15) & 0x1;
        R = (R << 3) | (R >> 2);
        G = (G << 3) | (G >> 2);
        B = (B << 3) | (B >> 2);
        A = A == 0 ? 0 : 255;
        return ((A << 24) + (R << 16) + (G << 8) + B);
    }
    
    private short color8888to5551(int color) {
        short A = (short) ((color >> 24) & 0xFF);
        short R = (short) ((color >> 16) & 0xFF);
        short G = (short) ((color >> 8) & 0xFF);
        short B = (short) (color & 0xFF);
        A = (short) (A == 0 ? 0 : 1);
        R = (short) (R >> 3);
        G = (short) (G >> 3);
        B = (short) (B >> 3);
        color = ((A << 15) + (B << 10) + (G << 5) + R);
        return (short) ((int)((color & 0xFF) << 8) | (color >> 8) & 0xFF);
    }
    
    private int changeEndianess(int color) {
        int A = color & 0xFF;
        int R = (color >> 24) & 0xFF;
        int G = (color >> 16) & 0xFF;
        int B = (color >> 8) & 0xFF;
        return ((A << 24) + (R << 16) + (G << 8) + B);
    }
    
    private int swapColor(int color) {
        int A = (color >> 24) & 0xFF;
        int R = (color >> 16) & 0xFF;
        int G = (color >> 8) & 0xFF;
        int B = color & 0xFF;
        return ((R << 24) + (G << 16) + (B << 8) + A);
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
        return (data_type != GIM_UNKNOWN) || (data_type == INDEX_4 && palette_count <= 16) ||
        (data_type == INDEX_8 && palette_count <= 256);
    }
}
