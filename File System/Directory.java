import java.util.Stack;

public class Directory {
   private final static int MAX_CHARS = 30; // max characters of each file name
   private final static int INT_SIZE = 4;
   private final static int SHORT_SIZE = 2;
   private final static int DIRECTORY_ENTRY_SIZE = MAX_CHARS * SHORT_SIZE;

   // Directory entries
   private int fsize[];        // each element stores a different file name size.
   private char fnames[][];    // each element stores a different file name.
   private Stack<Short> availableInumbers;

   public Directory( int maxInumber ) { // directory constructor
      fsize = new int[maxInumber];     // maxInumber = max files
      for ( short i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][MAX_CHARS];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = 1;        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
      availableInumbers = new Stack<Short>();
      for (short i = 1; i < maxInumber; i++) {
		availableInumbers.push(i);
	  }
   }

   public void bytes2directory( byte data[] ) {
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]
      for (short fileNum = 0; fileNum < fsize.length; fileNum++) {
      	int sizeOffset = fileNum * DIRECTORY_ENTRY_SIZE;
      	int nameOffset = sizeOffset + INT_SIZE;
      	fsize[fileNum] = SysLib.bytes2int(data, sizeOffset);
      	if (fsize[fileNum] == 0) {
      		availableInumbers.push(fileNum);
      	}
      	else for (int charNum = 0; charNum < fsize[fileNum]; charNum++) {
      		fnames[fileNum][charNum] = (char)(SysLib.bytes2short(data, nameOffset + charNum * SHORT_SIZE));
      	}
      }
   }

   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
      byte[] data = new byte[fsize.length * (SHORT_SIZE + MAX_CHARS)];
      for (int fileNum = 0; fileNum < fsize.length; fileNum++) {
      	int sizeOffset = fileNum * DIRECTORY_ENTRY_SIZE;
      	int nameOffset = sizeOffset + INT_SIZE;
      	SysLib.int2bytes(fsize[fileNum], data, sizeOffset);
      	for (int charNum = 0; charNum < fsize[fileNum]; charNum++) {
      		SysLib.short2bytes((short)(fnames[fileNum][charNum]), data, nameOffset + charNum * SHORT_SIZE);
      	}
      }
      return data;
   }

   public short ialloc( String filename ) {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
      if (availableInumbers.isEmpty()) {
      	return -1;
      }
      else {
      	short inumber = availableInumbers.pop();
      	fsize[inumber] = filename.length();
      	for (int i = 0; i < fsize[inumber]; i++) {
      		fnames[inumber][i] = filename.charAt(i);
      	}
	Inode inode = new Inode(inumber);
	inode.length = 0;
	inode.flag = 1;
	inode.toDisk(inumber);
      	return inumber;
      }
   }

   // Returns true if it deleted the file, false if it didn't exist.
   public boolean ifree( short iNumber ) {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
      boolean deleted = (fsize[iNumber] != 0);
      if (deleted) {
      	fsize[iNumber] = 0;
      	availableInumbers.push(iNumber);
      }
      return deleted;
   }

   public short namei( String filename ) {
      // returns the inumber corresponding to this filename
      for (short i = 0; i < fnames.length; i++) {
      	if (fsize[i] != 0) {
      		boolean match = true;
      		for (int j = 0; j < filename.length(); j++) {
      			if (filename.charAt(j) != fnames[i][j]) {
      				match = false;
      				break;
      			}
      		}
      		if (match) {
      			return i;
      		}
      	}
      }
      return -1;
   }
}
