package client.io;

import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import items.Item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import client.render.Render;

import spells.Spell;
import utils.Texture;
import blocks.Block;

public class TextureLoader 
{    
	/** The colour model including alpha for the GL image */
    private ColorModel glAlphaColorModel;  
    /** The colour model for the GL image */
    private ColorModel glColorModel; 
    /** Scratch buffer for texture ID's */
    private IntBuffer textureIDBuffer = BufferUtils.createIntBuffer(1); 

    /**
     * Create a new texture loader based on the game panel
     */
    public TextureLoader() 
    {
        glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8,8,8,8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8,8,8,0}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
    }

    /**
     * Create a new texture ID
     */    
    private int createTextureID() 
    {
    	glGenTextures(textureIDBuffer);
    	return textureIDBuffer.get(0);
    }

    /**
     * Load a texture into OpenGL from a image reference on disk.
     *
     * @param resourceName The location of the resource to load
     * @param target The GL target to load the texture against
     * @param dstPixelFormat The pixel format of the screen
     * @param minFilter The minimising filter
     * @param magFilter The magnification filter
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public Texture getTexture(String resourceName)
    {
	
        int srcPixelFormat;
        
        // create the texture ID for this texture
        int textureID = createTextureID();
        Texture texture = new Texture(textureID);

        // bind this texture
        glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        BufferedImage bufferedImage = loadImage(resourceName);
        
        if (bufferedImage.getColorModel().hasAlpha()) 
        {
            srcPixelFormat = GL_RGBA;
        }
        else 
        {
            srcPixelFormat = GL_RGB;
        }

        // convert that image into a byte buffer of texture data
        ByteBuffer textureBuffer = convertImageData(bufferedImage,texture);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
       
        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        glTexParameteri (GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        glTexParameteri (GL11.GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        glTexParameteri (GL11.GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        
        // produce a texture from the byte buffer
        glTexImage2D(GL11.GL_TEXTURE_2D, 
        		0, 
        		srcPixelFormat, 
        		get2Fold(bufferedImage.getWidth()), 
        		get2Fold(bufferedImage.getHeight()), 
        		0, 
        		srcPixelFormat, 
        		GL_UNSIGNED_BYTE, 
        		textureBuffer);

        return texture;
    }
       
    public Texture[] getIcons()
    {
    	int[][] coordBySlot = 
			{ 
				{ 2 * 16, 8 * 16, 32, 32 }, //Helm
				{ 2 * 16, 10 * 16, 32, 32 }, //Body
				{ 0 * 16, 10 * 16, 32, 32 }, //Belt
				{ 2 * 16, 9 * 16, 32, 32 }, //Pants
				{ 2 * 16, 14 * 16, 32, 32 }, //Boots
				{ 0 * 16, 8 * 16, 32, 32 }, //Gloves
				{ 0 * 16, 14 * 16, 32, 32 }, //Ring
				{ 0 * 16, 5 * 16, 16, 16 }, //Quiver
				{ 1 * 16, 4 * 16, 16, 16 } //Gem Socket
			};
    	Texture[] textures = new Texture[coordBySlot.length];
    	
    	int srcPixelFormat;
    	int textureID;
    	BufferedImage iconsSheet = loadImage("Resources/icons.png");
    	
    	 for(int i = 0; i < textures.length; i++) //Terrain Textures
         {  	
 	        textureID = createTextureID();
 	        textures[i] = new Texture(textureID);
 	        glBindTexture(GL11.GL_TEXTURE_2D, textureID);
            BufferedImage img = iconsSheet.getSubimage(coordBySlot[i][0], coordBySlot[i][1], coordBySlot[i][2], coordBySlot[i][3]);
 	        if (img.getColorModel().hasAlpha()) 
 	        {
 	            srcPixelFormat = GL_RGBA;
 	        } 
 	        else
 	        {
 	            srcPixelFormat = GL_RGB;
 	        }
 	        
 	        ByteBuffer textureBuffer = convertImageData(img, textures[i]);
 	        
	        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	    	glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
 	        glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, get2Fold(img.getWidth()), get2Fold(img.getHeight()), 0, 
 	        		srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);		        
         } 	
 	
    	return textures;
    }
    
    /**
     * Loads textures for all items and Blocks
     */
    public Texture[] getItemTextureArray() 
    {
    	BufferedImage bufferStore[] = new BufferedImage[64000];
        Texture textureStore[] = new Texture[64000];
        
    	int srcPixelFormat;
    	int textureID;
    	BufferedImage blockSheet = loadImage("Resources/terrain_earth.png");
        Block[] list = Block.blocksList;
        Item[] list1 = Item.itemsList;
        BufferedImage itemSheet = loadImage("Resources/items.png");
        Spell[] list2 = Spell.spellList;
        
        for(int i = 0; i < list.length; i++) //Terrain Textures
        {
        	if(list[i] == null)
        	{
        		continue;
        	}

	        textureID = createTextureID();
	        textureStore[i] = new Texture(textureID);
	        glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	        
	        int w = 0;
	        int h = 0;
	        int x = 1;
	        int y = 1;
	        
	        //TODO: Associate specific blocks w/ specific sheets - broadly speaking; fix block texture issues in the inventory
	        //
	        
	        if(list[i].iconOverriden)
	        {
		        if(list[i].associatedTextureSheet == Render.TEXTURE_SHEET_ITEMS)
		        {
		        	w = (int)list[i].textureWidth;
			        h = (int)list[i].textureHeight;
			        x = (int) (list[i].iconOverrideX * 32);
			        y = (int) (list[i].iconOverrideY * 32);
			        bufferStore[i] = itemSheet.getSubimage(x, y, w, h);
		        	
		        }
		        else //This should be an else if.
		        {
			        w = (int)list[i].textureWidth;
			        h = (int)list[i].textureHeight;
			        x = (int) (list[i].iconOverrideX * 16);
			        y = (int) (list[i].iconOverrideY * 16);
			        bufferStore[i] = blockSheet.getSubimage(x, y, w, h);
		        }
	        }
	        else
	        {
		        w = (int)list[i].textureWidth;
		        h = (int)list[i].textureHeight;
		        x = (int) (list[i].iconX * 16);
		        y = (int) (list[i].iconY * 16);
		        bufferStore[i] = blockSheet.getSubimage(x, y, w, h);
	        }
	        
	        
	        
	        if (bufferStore[i].getColorModel().hasAlpha()) 
	        {
	            srcPixelFormat = GL_RGBA;
	        } 
	        else
	        {
	            srcPixelFormat = GL_RGB;
	        }
	        
	        ByteBuffer textureBuffer = convertImageData(bufferStore[i], textureStore[i]);
	        
	        glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,  GL11.GL_LINEAR);
	        glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	        glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, get2Fold(bufferStore[i].getWidth()), get2Fold(bufferStore[i].getHeight()), 0, 
	        		srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);		        
        } 	
	
        
	        
        for(int i = 0; i < list1.length; i++) //Item Textures
        {
        	if(list1[i] == null)
        	{
        		continue;
        	}
        	
	        textureID = createTextureID();
	        textureStore[i] = new Texture(textureID);
	        glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	        
	        int w = 32;
	        int h = 32;
	        int x = (int) (list1[i].iconX * 32);
	        int y = (int) (list1[i].iconY * 32);
	        
	        bufferStore[i] = itemSheet.getSubimage(x, y, w, h);
	        
	        if (bufferStore[i].getColorModel().hasAlpha()) 
	        {
	            srcPixelFormat = GL_RGBA;
	        } 
	        else
	        {
	            srcPixelFormat = GL_RGB;
	        }
	
	        ByteBuffer textureBuffer = convertImageData(bufferStore[i], textureStore[i]);
	
	        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);//generates a texture that never changes based on view distance
	    	glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	        glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, get2Fold(bufferStore[i].getWidth()), get2Fold(bufferStore[i].getHeight()), 0, 
	        		srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);
        } 	
        

        
        for(int i = 0; i < list2.length; i++) //Spell Textures
        {
        	if(list2[i] == null)
        	{
        		continue;
        	}
        	
	        textureID = createTextureID();
	        textureStore[i] = new Texture(textureID);
	        glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	        
	        int w = 32;
	        int h = 32;
	        int x = (int) (list2[i].iconX * 32);
	        int y = (int) (list2[i].iconY * 32);
	        
	        bufferStore[i] = itemSheet.getSubimage(x, y, w, h);
	        
	        if (bufferStore[i].getColorModel().hasAlpha()) 
	        {
	            srcPixelFormat = GL_RGBA;
	        } 
	        else
	        {
	            srcPixelFormat = GL_RGB;
	        }
	
	        ByteBuffer textureBuffer = convertImageData(bufferStore[i], textureStore[i]);
	
	        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);//generates a texture that never changes based on view distance
	    	glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	        glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, get2Fold(bufferStore[i].getWidth()), get2Fold(bufferStore[i].getHeight()), 0, 
	        		srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);
        } 	
        
        
        return textureStore;
    }
    
    /**
     * returns an array of the moon textures
     */
    public Texture[] getMoonTextureArray() 
    {
    	BufferedImage bufferStore[] = new BufferedImage[32];
        Texture textureStore[] = new Texture[32];       
    	int srcPixelFormat;
    	int textureID;
    	String resourceName = "Resources/moon_sprites.png";
    	BufferedImage bufferedImage = loadImage(resourceName);             
	        
        for(int i = 0; i < 5; i++) //Item Textures
        {	        	
	        textureID = createTextureID();
	        textureStore[i] = new Texture(textureID);
	        glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	        
	        int w = 64;
	        int h = 64;
	        int x = i * 64;
	        int y = 0;
	        
	        bufferStore[i] = bufferedImage.getSubimage(x, y, w, h);
	        
	        if (bufferStore[i].getColorModel().hasAlpha()) 
	        {
	            srcPixelFormat = GL_RGBA;
	        } 
	        else
	        {
	            srcPixelFormat = GL_RGB;
	        }
	
	        ByteBuffer textureBuffer = convertImageData(bufferStore[i], textureStore[i]);
	
	        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);//generates a texture that never changes based on view distance
	    	glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	        glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, get2Fold(bufferStore[i].getWidth()), get2Fold(bufferStore[i].getHeight()), 0, 
	        		srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);
        } 	
        
        return textureStore;
    }
    
    /**
     * Gets individual textures for all the items and blocks 
     */
    public Texture[] getTextureArray(String location, int subWidth, int subHeight)
    {
    	Vector<Texture> vector = new Vector<Texture>();
        try
    	{
        	int srcPixelFormat;
        	int textureID;
        	BufferedImage bufferedImage = loadImage(location);
	            
	        for(int i = 0; ; i++) //Item Textures
	        {	        	
	        	if((i+1) * subWidth > bufferedImage.getWidth())
	        	{
	        		break;
	        	}
	        	
	        	Texture tex;
		        textureID = createTextureID();
		        tex = new Texture(textureID);
		        glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		        
		        int w = subWidth;
		        int h = subHeight;
		        int x = i * subWidth;
		        int y = 0;
		        BufferedImage buf = bufferedImage.getSubimage(x, y, w, h);
		        
		        if (buf.getColorModel().hasAlpha()) 
		        {
		            srcPixelFormat = GL_RGBA;
		        } 
		        else
		        {
		            srcPixelFormat = GL_RGB;
		        }
		
		        ByteBuffer textureBuffer = convertImageData(buf, tex);
		
		        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);//generates a texture that never changes based on view distance
		    	glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		        glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, get2Fold(buf.getWidth()), get2Fold(buf.getHeight()), 0, 
		        		srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);
		        
		        vector.add(tex);
	        } 	
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new RuntimeException("Critical Failure Loading Moon Textures");
    	}
        
        Texture[] temp = new Texture[vector.size()];
        vector.copyInto(temp);        
        
        return temp;
    }
    
    /**
     * Returns the closest (larger) power of 2
     */
    private int get2Fold(int fold)
    {
        int ret = 2;
        while (ret < fold) 
        {
            ret *= 2;
        }
        return ret;
    }

    /**
     * Convert the buffered image to a texture
     *
     * @param bufferedImage The image to convert to a texture
     * @param texture The texture to store the data into
     * @return A buffer containing the data
     */
    @SuppressWarnings("rawtypes")
	private ByteBuffer convertImageData(BufferedImage bufferedImage,Texture texture) 
    {
        ByteBuffer imageBuffer;
        WritableRaster raster;
        BufferedImage texImage;

        int texWidth = 2;
        int texHeight = 2;

        // find the closest power of 2 for the width and height of the produced texture 
        while (texWidth < bufferedImage.getWidth()) 
        {
            texWidth *= 2;
        }
        while (texHeight < bufferedImage.getHeight()) 
        {
            texHeight *= 2;
        }
        
        // create a raster that can be used by OpenGL as a source for a texture 
        if (bufferedImage.getColorModel().hasAlpha()) 
        {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
            texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable());
        }
        else 
        {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,3,null);
            texImage = new BufferedImage(glColorModel,raster,false,new Hashtable());
        }

        // copy the source image into the produced image
        Graphics g = texImage.getGraphics();
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0,texWidth,texHeight);
        g.drawImage(bufferedImage,0,0,null);

        // build a byte buffer from the temporary image that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();

        return imageBuffer;
    }

    /**
     * Load a given resource as a buffered image
     *
     * @param ref The location of the resource to load
     * @return The loaded buffered image
     * @throws IOException Indicates a failure to find a resource
     */
    private BufferedImage loadImage(String ref)
    {
    	try
    	{
	        URL url = TextureLoader.class.getClassLoader().getResource(ref);
	
	        if (url == null) 
	        {
	            throw new IOException("Cannot find: " + ref);
	        }
	
	        // due to an issue with ImageIO and mixed signed code we are now using good oldfashioned ImageIcon to load
	        // images and the paint it on top of a new BufferedImage
	        Image img = new ImageIcon(url).getImage();
	        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB/*It's crucial this is _ARGB for alpha*/);
	        Graphics g = bufferedImage.getGraphics();
	        g.drawImage(img, 0, 0, null);
	        g.dispose();
	
	        return bufferedImage;
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	return null;
    }
}