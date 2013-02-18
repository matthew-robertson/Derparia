package net.dimensia.src;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

public class CallableLoadChunk implements Callable<Chunk>
{
	private String basepath;
	private String worldName;
	private int x;
	
	public CallableLoadChunk(int x, String basepath, String worldName)
	{
		this.x = x;
		this.basepath = basepath;
		this.worldName = worldName;
	}
	
	public Chunk call() throws Exception
	{
		String fileName = basepath + "/" + x + ".wdat";
		ObjectInputStream ois = new ObjectInputStream(new DataInputStream(new GZIPInputStream(new FileInputStream(fileName)))); //Open an input stream
		Chunk chunk = (Chunk)ois.readObject(); //Load the object
		System.out.println("Chunk Loaded From File Path : " + fileName);
		ois.close();
		return chunk;
	}
	
	public String getBasePath()
	{
		return basepath;
	}
}
