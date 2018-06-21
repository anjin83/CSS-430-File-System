public class Inode 
{
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers
   
   private final static int INT_SIZE = 4;
   private final static int SHORT_SIZE = 2;
   private final static int FLAG_START = 0;
   private final static int LENGTH_START = FLAG_START + SHORT_SIZE;
   private final static int COUNT_START = LENGTH_START + INT_SIZE;
   private final static int DIRECT_START = COUNT_START + SHORT_SIZE;
   private final static int INDIRECT_START = DIRECT_START + (SHORT_SIZE * directSize);
   private static final int DISK_OFFSET = 1;
   private static final int BLOCK_SIZE = 512;
   
   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

	Inode( ) 
	{                                     // a default constructor
		length = 0;
		count = 0;
		flag = 1;
		for ( int i = 0; i < directSize; i++ )
		direct[i] = -1;
		indirect = -1;
	}

	Inode( short iNumber ) 
	{                       // retrieve inode from disk
		byte[] buffer = new byte[BLOCK_SIZE];
		int blockNumber = DISK_OFFSET + iNumber / (BLOCK_SIZE / iNodeSize);
		int blockOffset = (iNumber % (BLOCK_SIZE / iNodeSize))*32;
		SysLib.rawread(blockNumber, buffer);
		length = SysLib.bytes2int(buffer, blockOffset + LENGTH_START);
		count = SysLib.bytes2short(buffer, blockOffset + COUNT_START);
		flag = SysLib.bytes2short(buffer, blockOffset + FLAG_START);
		for (int i = 0; i < directSize; i++) 
		{
			direct[i] = SysLib.bytes2short(buffer, blockOffset + DIRECT_START + i * SHORT_SIZE);
		}
		indirect = SysLib.bytes2short(buffer, blockOffset + INDIRECT_START);
	}

	int toDisk( short iNumber ) 
	{       // save to disk as the i-th inode
		byte[] buffer = new byte[BLOCK_SIZE];
		int blockNumber = DISK_OFFSET + iNumber / (BLOCK_SIZE / iNodeSize);
		int blockOffset = (iNumber % (BLOCK_SIZE / iNodeSize))*32;
		SysLib.rawread(blockNumber, buffer);
		

		SysLib.int2bytes(length, buffer, blockOffset + LENGTH_START);
		SysLib.short2bytes(count, buffer, blockOffset + COUNT_START);
		SysLib.short2bytes(flag, buffer, blockOffset + FLAG_START);

		for (int i = 0; i < directSize; i++) 
		{
			SysLib.short2bytes(direct[i], buffer, blockOffset + DIRECT_START + i * SHORT_SIZE);
		}

		SysLib.short2bytes(indirect, buffer, blockOffset + INDIRECT_START);
		SysLib.rawwrite(blockNumber, buffer);

		//delete
		return 0;
	}
}
