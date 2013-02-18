package net.dimensia.src;

/**
 * MetaDataHelper is a utility class to help deal with blocks of sizes greater than 1x1. Currently supporting blocks upto size 3x3.
 * 
 * <br><br>
 * 
 * It exposes only one method {@link #getMetaDataArray(int, int)}, taking arguments of width, and height. This returns a constant 
 * meta data array of size widthxheight. Using this metadata, it's possible to make easy calculations on which part of a block 
 * has been hit, and how to deal with it appropriately. Metadata array values are of type int.
 * 
 * <br><br>
 * 
 * An example of a metadata array, for size 2x3, is: <br>
 * {{ 1, 2 }, <br>
 * { 3, 4 }, <br>
 * { 5, 6 }} <br>
 * 
 * 
 * @author      Alec Sobeck
 * @author      Matthew Robertson
 * @version     1.0
 * @since       1.0
 */
public class MetaDataHelper 
{	
	/**
	 * Returns a constant metadata array of size w by h.
	 * @param w the width of the metadata (block width)
	 * @param h the height of the metadata (block height)
	 * @return a metadata array of specified width and height
	 */
	public static int[][] getMetaDataArray(int w, int h)
	{
		return getMetaDataByType(getTypeBySize(w, h));
	}
	
	private static EnumBlockSize getTypeBySize(int w, int h)
	{
		return (w == 2 && h == 1) ? EnumBlockSize.TWOBYONE : (w == 3 && h == 1) ? EnumBlockSize.THREEBYONE : (w == 1 && h == 2) ? EnumBlockSize.ONEBYTWO : (w == 2 && h == 2) ? EnumBlockSize.TWOBYTWO : (w == 3 && h == 2) ? EnumBlockSize.THREEBYTWO : (w == 1 && h == 3) ? EnumBlockSize.ONEBYTHREE : (w == 2 && h == 3) ? EnumBlockSize.TWOBYTHREE : (w == 3 && h == 3) ? EnumBlockSize.THREEBYTHREE : EnumBlockSize.ONEBYONE;
	}
		
	private static int[][] getMetaDataByType(EnumBlockSize size)
	{
		return (size == EnumBlockSize.TWOBYONE) ? twoByOne : (size == EnumBlockSize.THREEBYONE) ? threeByOne : (size == EnumBlockSize.ONEBYTWO) ? oneByTwo : (size == EnumBlockSize.TWOBYTWO) ? twoByTwo : (size == EnumBlockSize.THREEBYTWO) ? threeByTwo : (size == EnumBlockSize.ONEBYTHREE) ? oneByThree : (size == EnumBlockSize.TWOBYTHREE) ? twoByThree : (size == EnumBlockSize.THREEBYTHREE) ? threeByThree : oneByOne;		
	}
	
	private static final int[][] oneByOne = 
	{ 
		{ 1 }
	};
	private static final int[][] twoByOne = 
	{
		{ 1 },
		{ 2 }
	};
	private static final int[][] threeByOne = 
	{
		{ 1 },
		{ 2 },
		{ 3 }
	};
	private static final int[][] oneByTwo = 
	{
		{ 1, 2 }
	};
	private static final int[][] twoByTwo = 		
	{ 
		{ 1, 2 },
		{ 3, 4 } 
	};
	private static final int[][] threeByTwo = 	
	{ 
		{ 1, 2 },
		{ 3, 4 },
		{ 5, 6 } 
	};
	private static final int[][] oneByThree = 	
	{ 
		{ 1, 2, 3 }
	};
	private static final int[][] twoByThree = 	
	{
		{ 1, 2, 3 },
		{ 4, 5, 6 }
	};
	private static final int[][] threeByThree = 
	{ 
		{ 1, 2, 3 },
		{ 4, 5, 6 },
		{ 7, 8, 9 } 
	};		     
}
