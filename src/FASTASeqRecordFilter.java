package org.jax.mgi.bio.seqfilter;

import java.io.*;
import org.apache.regexp.*;
import gnu.getopt.*;
import org.jax.mgi.bio.seqrecord.*;	

public class FASTASeqRecordFilter
{
	//Concept:
        //        IS: an object that reads FASTA sequence records, 
        //              splits them up into multiple overlapping records
        //              if the sequence is longer than a configurable length,
	// 		by using predicates, writes them to an output
	//		file, and handles logging of statistics and exceptions.
        //       HAS: a sequence record object 
	//	      an input stream object	
        //      DOES: parses arguments; reads FASTA sequence records; 
	//	        writes the sequence record(s) and 
	//	        also handles logging of statistics and exceptions
        // Implementation:
        //
	// Constructors: 1) Requires command-line arguments and 
        //                  a FASTASeqRecord object.
        //               2) Requires command-line arguments, an
        //                  explicit log path, and a FASTASeqRecord object.

        //
        // constructors
        //

	public FASTASeqRecordFilter(
	        String[] args,	       // command line arguments	 
		FASTASeqRecord sr)     // sequence record object
	{
	// Effects: creates input and output stream objects 
	// 	    creates default log file
	    try
	    {	

		//capture the log path/file from Java system properties
		String logPath = System.getProperty("LOG");		

		//create a log writer, open in append mode
		this.log = new PrintWriter(new FileWriter(
			logPath, true));

		//create a reader for stdin
		this.in = new BufferedReader(new InputStreamReader(System.in));
		
		//process command line args
		this.getArgs(args);
	
		//the sequence record object
		this.seqRec = sr;
	    }
	    catch(IOException e1)
            {
                System.err.println("IOException in " + 
		    "FASTASeqRecordFilter.construct: "
                    + e1.getMessage());
            }
	   	
	}
		
	public FASTASeqRecordFilter(
		String[] args, 	       // command line arguments	
		String name,           // explicit log path/name 	
		FASTASeqRecord sr)     // sequence record object
	{
	// Effects: creates input and output stream objects
	//	    creates log file using "name"

	    try
	    {
		// See comments in previous FASTASeqRecordFilter constructor
		this.log = new PrintWriter( new FileWriter(
			name, true)); 
		this.in = new BufferedReader(new InputStreamReader(System.in));
		this.getArgs(args);
		this.seqRec = sr;
	    }
	    catch(IOException e1)
            {
                System.err.println("IOException in FASTASeqRecordFilter.construct: "
                    + e1.getMessage());
            }
	    	

	}
	
	//
	//methods
	//

	public void getArgs(
		String[] args)   // command line arguments, see usage in Notes:
	{
	// Purpose: Set output file.
        // Returns: nothing 
        // Assumes: nothing
        // Effects: nothing
        // Throws:  IO and NumberFormat exceptions, catches them, writes their
        //           messages to log and error log, and execution
        //           halts. IOException is raised when the user does not
	//           use the correct syntax.  NumberFormatException is
	//	     raised when the user does not specify an integer argument
	//	     with the -s or -l options.
        // Notes:
	//	//
	//	//command line usage
	//	//
	//
	//	The args array will either:
	//	1) short option indicating whether to open the output
	//	    file in append or overwrite mode e.g. -o | -a
	//	2) output file path and name
	//      
	//	{-o | -a, outputPath/Filename}
	//
	//      3) short option to indicate the maximum subsequence length
	//      4) the maximum subsequence length
	//      
	//      {-s subsequenceLength}
	//
	//      5) short option to indicate the subsequence overlap length
	//      6) the subsequence overlap length
	//      
	//      {-l subsequenceOverlapLength}
	//
	//	note: java args do not contain the application name
	//	
	//	//
	//	//optstring syntax:
	//	//
	//
	//	- = return args in order regardless of if opt longOpt or arg
	//	: = return ':' if option w/o a required arg and return '?'
	//	               if option is invalid 
	//	a: = open file in append mode, argument required (filename)
	//	o: = open file in create/overwrite mode, argument required
	//      s: = maximum subsequence length
	//      l: = subsequence overlap length
	//
	//	All opts return an integer value, short opts return their
 	//        ascii integer value, longopts return their assigned integer
        //        -1 is returned when there are no more opts

	    try
	    {
		// a String containing a description of the valid args for this
		// program
		String optstring = "-:a:o:s:l:";

		// Flags for detecting the use of short options
		boolean aFlag = false;
		boolean oFlag = false;
		boolean sFlag = false;
		boolean lFlag = false;
		
		// create a Getopt object passing it:
		// 1) The prefix for all exceptions raised by Getopt
		//    (technically the program name).
		// 2) The String array passed as the command line to the 
		//    program
		// 3) A String containing description of the valid options 
		//
		Getopt g = new Getopt("Usage error", args, optstring);
		
		int c;		// the option returned from g.getopt()
		String arg;	// the arg returned from g.getOptarg()
		
		//get command line options in order, run them through the 
		//switch
		while((c = g.getopt()) != -1)
		{
		    switch(c)	 
		    {
			case 'a':

   			    //get arg (an output filename) for this option
			    arg = g.getOptarg();	

			    //open the file in append mode
			    this.seqWriter = new
				        BufferedWriter(
					new FileWriter(arg, true), 5000);

			    //set flag that an output filename was set
			    //in append mode.
			    aFlag = true;

			    break;

			case 'o':

   			    //get arg (an output filename) for this option
			    arg = g.getOptarg();	

			    //open the file in overwrite mode
			    this.seqWriter = new
				        BufferedWriter(
					new FileWriter(arg, false), 5000);

			    //set flag that an output filename was set
			    //in overwrite mode.
			    oFlag = true;

                            break;

			case 's':

   			    //get arg (an integer) for this option
			    arg = g.getOptarg();	

			    //set the maximum subsequence length
			    this.maxLength = (new Integer(arg)).intValue();

			    //set flag that a subsequence length was set
			    sFlag = true;

                            break;

			case 'l':

   			    //get arg (an integer) for this option
			    arg = g.getOptarg();	

			    //set the subsequence overlap length
			    this.overLap = (new Integer(arg)).intValue();

			    //set flag that a subsequence overlap length 
			    //was set
			    lFlag = true;

                            break;

		        case ':':
			// Missing argument for an option

			    throw new IOException("Usage:\n"
			       + usage);

		        case '?':
			// Invalid option

			    throw new IOException("Usage:\n"
			       + usage);

			default:

			    throw new IOException("Error getopt() returned: "
				+ "if 1 a non-opt"
				+ " arg element was found, its value is: "
				+ g.getOptarg());
				
		    }
		}

		// Check usage to make sure that:
		//   1. Either -o or -a options were specified.
		//   2. Both -s and -l options were specified.
		//   3. -s argument is greater than -l argument
		// Otherwise provide error message to user.
		if (((aFlag == false) && (oFlag == false))
		    || ((aFlag == true) && (oFlag == true))
		    || (sFlag == false)
		    || (lFlag == false))
		{

		    throw new IOException("Usage error: wrong number or "
		       + "combination of options used.\nUsage:\n"
		       + usage);

		}
		// Check to make sure that:
		//   1. this.maxLength > this.overLap
		else if (this.maxLength < this.overLap)
		{
		    throw new IOException("Usage error: -s argument is "
		       + "less than -l argment.\nUsage:\n"
		       + usage);
		}


	    }
	    // Raised when a non-interger argument is used for -s and -l
	    // options.
	    catch(NumberFormatException e1)
	    {

		System.err.println("Usage error: integer argument required.\n"
		    + "Usage:\n" + usage);

                System.exit(1);

	    }
	    // Raised for all other cases of improper usage.
	    catch(IOException e1)
            {

                System.err.println(e1.getMessage());

                System.exit(1);

            }

	}
	
	public void go() 
	{
	// Purpose: Reads sequence records
	//	    For each record: 
	//	      process it by determining if sequence length is 
	//            greater than a configurable length.  If so, create
	//            overlapping subsequence records.
	//	      Write record(s) to output file, 
	//	    Logs total number of records processed and number of
	//	      records written to output file
        // Returns: nothing
        // Assumes: the constructors have initialized all readers and writers,
	//	    a sequence record object.
        // Effects: writes to log files and sequence output file
        // Throws: Nothing, but catches End of File, RESyntax and IO exceptions
	//	    and writes their messages stderr 
	//	    e.g. RESyntaxException indicates a syntax 
	//		   error in a regular expression. This would be unusual
	//		   because all RE syntax is hardcoded in the class 
	//		   source of the objects used by this method. 
	//		 EOFException, in this method, indicates EOF found
	//		   after an non-end of record symbol in a file. Please
	//		   note that normal EOF does not raise an exception, 
	//		   but returns null
	//		 IOException indicates failed or interrupted I/O 
	//		   operations e.g. the path/file does not exist  
	    
	    try
	    { 
		//debug: the record number in the file
		int testCtr = 0;
	
		//tracking of system time for this filter run
		long totalRunTimeMinutes = -1;	
		long stopTime = -1;
		long startTime = System.currentTimeMillis();
		
		//priming read
		this.seqRec.readText(this.in);
		
		//Process sequence records until end of file
		while(this.seqRec.getLine() != null)
		{

		        //process the record
			processRecord();

			//debug: increment record number and print it to screen
			testCtr ++;

			//read the next record
			this.seqRec.readText(this.in);

			//debug: increment record number and print it to screen
			testCtr ++;

		}

	        //process the last record
		processRecord();

		//debug: increment record number and print it to screen
		testCtr ++;

		//Capture the stop time of this filter
		stopTime = System.currentTimeMillis();
	
		//Figure the run time of this filter and log it
                totalRunTimeMinutes = ((stopTime - startTime) / 1000);
                log("Total runtime in seconds = ");
                log(new Long(totalRunTimeMinutes).toString());
		
		//log statistics from this filter run (see the method for info)
		logStats();

		//close all input and output streams
                this.in.close();
                this.log.close();
		this.seqWriter.close();

	    }
	    catch(EOFException e3)
            {
                    System.err.println("EOFException in " +
			"FASTASeqRecordFilter.go(): " +
		 	e3.getMessage());
            }
            catch(IOException e1)
            {
                    System.err.println("IOException in " +
                        "FASTASeqRecordFilter.go(): " +
			e1.getMessage());
            }
            catch(RESyntaxException e2)
            {
                    System.err.println("RESyntaxException in " +
			 "FASTASeqRecordFilter.go(): " +
			 e2.getMessage());
            }
 	
	}

        private void processRecord()
        {
            //Purpose: Process a single FASTA sequence record by:
            // 1. determing whether it is longer than threshold length.
            //      - If so, then:
            //          1. Break up sequence into subsequences that:
            //                1. are at least 1 MB
            //                2. overlap by 5kb
            //          2. Construct new FASTASeqRecords:
            //                1. Assign subsequence
            //                2. Label each subsequence with:
            //                     1. a new seqID of the format:
            //                        <seqID>.<start coor>.<end coor>
            //                     2. the original description
            //          3. Write each new FASTASeqRecords to BufferedWriter.
            //      - If not, then:
            //          1. Write the sequence record to BufferedWriter.
            // Returns: nothing
            // Assumes: the constructors have initialized all readers and
            //          writers, a sequence record object.
            // Effects: writes to log files and sequence output file
            // Throws: Nothing, but catches End of File, RESyntax and
            //         IO exceptions and writes their messages to stderr
            //         e.g. RESyntaxException indicates a syntax
            //             error in a regular expression. This would be unusual
            //             because all RE syntax is hardcoded in the class
            //             source of the objects used by this method.
            //           EOFException, in this method, indicates EOF found
            //             after an non-end of record symbol in a file. Please
            //             note that normal EOF does not raise an exception,
            //             but returns null
            //           IOException indicates failed or interrupted I/O
            //             operations e.g. the path/file does not exist
            // Notes: Sequences are broken up into overlapping subsequences
            //        that look like the following.  They remind me of how
            //        shingles are placed on a roof.
            //
            //          |<-maxLength->|<-overLap->|
            // subseq#1 |-------------|-----------|
            //                        |<-maxLength->|<-overLap->|
            // subseq#2               |-------------|-----------|---
            //                                      |<-maxLength->|<-overLap->|
            // subseq#3                             |-------------|-----------|
            //           ... and so on ....
            //
            //        The first sequence has a length = maxLength + overLap.
            //        The following sequences have a
            //             length = maxLength + 2(overlap).
            //        The last sequence has whatever is left (at most
            //             maxLength).
            //

	    try
	    { 

		// The FASTASeqRecord object that is re-used for each
		// subsequence.
		FASTASeqRecord fastasr = new FASTASeqRecord();

		// set the sequence length
		int seqLength = this.seqRec.getSeqLength();

		// initialize variables
		String baseSeqID = "";  // prefix of subsequence seqIDs
		String seqID = "";
		String description = "";

		// set baseSeqID
		for (int j = 0; j < this.seqRec.getSeqIds().size(); j++)
		{
		    baseSeqID = this.seqRec.getSeqIds().elementAt(j).
                                   toString();
		}

		// For sequences that need to be broken up into subsequences
		if (seqLength > maxLength)
		{

		    // calculate the number of subsequences
		    // (i.e. of length = maxLength)
		    int div = seqLength / maxLength;

		    // calculate whether the last sequence will not have
		    // maxLength number of sequence characters
		    int modulus = seqLength % maxLength;

		    // Initialize some variables
		    int loopLimit = 0;
		    int start = 0;
		    Integer startint = new Integer(start);
		    int end = 0;
		    Integer endint = new Integer(end);

		    // get the sequence of the original record that
		    // will be split up
		    String completeSeq = this.seqRec.getSequence();

		    // set the number of iterations required to reformat
		    // the sequence.
		    if (modulus > 0)
		    {
			loopLimit = div + 1;
		    }
		    else
		    {
			loopLimit = div;
		    }
		    
		    // break up the sequences by calculating the
		    // coordinates for the sequence that will be
		    // appended to the entry.
		    for (int i = 0; i < loopLimit; i++)
		    {

			// start coordinate
			start = i*maxLength;

			// end coordinate
			end = (i+1)*maxLength + overLap;

			// case for last sequence record
			if (end > seqLength)
			{
			    end = seqLength;
			}

			// construct the seqID for the subsequence
			// of the format "seqID"."start"."end"
			seqID = baseSeqID + "." + startint.toString(start+1) 
                           + "." + endint.toString(end);

			// construct the description for the subsequence
			// of the format "description" ("start"-"end")
			description = this.seqRec.getDescription() + " (" 
                           + startint.toString(start+1) + "-" 
                           + endint.toString(end) + ")";

			// set the attributes of the current subsequence
			fastasr.setThyself(seqID,description,
                           completeSeq.substring(start,end));

			//write out current subsequence record
			this.seqWriter.write(fastasr.getText());

		    }
		    
		}
		else
		{
		    //write out record
		    this.seqWriter.write(this.seqRec.getText());
		}
		
	    }
            catch(IOException e1)
            {
                    System.err.println("IOException in "
                        + "FASTASeqRecordFilter.processRecord(): "
			+ e1.getMessage());
            }

	}

	private void logStats()
	{
	//
	//Purpose: Logs total number of records processed and number of records
	//         processed for each output file

		this.log.println("Count statistics for ");
		this.log.println('\n');

	}
	
	private void log(String s)
	{
	//	
	//Purpose: Writes 's' to the log 
	//
		this.log.println(s);
	}

	//
	//instance variables
	//

	//the sequence record object that will read and parse all 
	//the records in the input stream
	private FASTASeqRecord seqRec;

	//general logging includes statistics, exceptions, etc
	private PrintWriter log;
	
	//the file input stream reader
	private BufferedReader in; 

	//the file output stream writer
	private BufferedWriter seqWriter;

        //the maximum subsequence length
        private int maxLength;

        //the subsequence overlap length
        private int overLap;

	// Usage description
	private String usage = "cat inputSeqFile | java -DLOG=logFile " 
	    + "-[o|a] outputFile -s subsequenceLength "
	    + "-l overlapLength\n     where subsequenceLength > overlapLength";


}
