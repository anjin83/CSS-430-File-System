public class SuperBlock 
{
	private final static int defaultInodeBlocks = 64;
	public int totalBlocks; // the number of disk blocks
	public int totalInodes; // the number of inodes
	public int freeList;    // the block number of the free list's head

	public SuperBlock( int diskSize ) 
	{
		// read the superblock from the disk
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread(0, superBlock );
		totalBlocks = SysLib.bytes2int( superBlock, 0 );
		totalInodes = SysLib.bytes2int( superBlock, 4 );
		freeList = SysLib.bytes2int(superBlock, (short)(8));
		
		if( totalBlocks == diskSize && totalInodes > 0 && freeList >= 2 )
			return;
		else 
		{
			//format disk
			totalBlocks = diskSize;
			format( defaultInodeBlocks );
		}

	}

	void format (int fileMax)
	{

		totalBlocks = 1000;
		totalInodes = fileMax;
		
		freeList = (fileMax / 16 + 1);	//Possibly should be plus 2
		
		// allocate inodes
		for (short i = 0; i < totalInodes; i++)
		{
			Inode node = new Inode();
			node.flag = 0;
			node.toDisk(i);
		}
		
		//create temp buffer to store blocks while reading/writing
		byte[] tempBuffer = new byte[Disk.blockSize];
		
		//create the freeList linked free blocks
		for(int i = freeList; i < totalBlocks; i++)
		{
			// get the block from disk
			SysLib.rawread(i, tempBuffer);
			SysLib.int2bytes((i+1), tempBuffer, 0);
			SysLib.rawwrite(i, tempBuffer);
		}

		// The last block has -1 as pointer as it is the last free block on the disk
		SysLib.int2bytes((-1), tempBuffer, 0);
		SysLib.rawwrite(totalBlocks - 1, tempBuffer);

		//write the formatted superBlock into the disk				
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.int2bytes(totalBlocks, superBlock, 0);
		SysLib.int2bytes(totalInodes, superBlock, 4);
		SysLib.int2bytes(freeList, superBlock, 8);
		SysLib.cwrite(0, superBlock);
		SysLib.csync();

	}
	
	void sync ()
	{
		// create a new byte array to be copied to disk
		byte[] superBlock = new byte[Disk.blockSize];

		SysLib.int2bytes(totalBlocks, superBlock, 0);
		SysLib.int2bytes(totalInodes, superBlock, 4);
		SysLib.int2bytes(freeList, superBlock, 8);

		// write the superBlock to disk
		SysLib.rawwrite(0, superBlock);

	}
	
	//finds and returns the blockID of the next free block in the freeList
	int getBlock()
	{
		int index = freeList;
		//check for free block
		if(index != -1)
		{
			byte[] tempBlock = new byte[Disk.blockSize];

			// read the next free block
			SysLib.rawread(index, tempBlock);

			// fix pointers for freeList
			freeList = SysLib.bytes2int(tempBlock, 0);						
		}
		return index;
	}
	
	//Add the given block ID back to the list of free blocks
	boolean returnBlock(short blockID)
	{
		if (blockID < totalBlocks && blockID > ((totalInodes/16)+1))
		{
			byte[] tempBuffer = new byte[Disk.blockSize]; // used to add freeList information to added block
			if(freeList == -1) // freeList is empty
			{
				SysLib.int2bytes(-1, tempBuffer, 0);
				freeList = blockID;
				SysLib.rawwrite(blockID, tempBuffer); //clear the data from the block and add -1 for next pointer
				return true;
			}
			else
			{
				SysLib.int2bytes(freeList, tempBuffer, 0);
				SysLib.rawwrite(blockID, tempBuffer);
				freeList = blockID;
				return true;
			}			
		}
		return false;
	}
}
