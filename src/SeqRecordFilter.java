package org.jax.mgi.bio.seqfilter;

import java.io.*;
import java.util.*;
import org.apache.regexp.*;
import gnu.getopt.*;
import org.jax.mgi.bio.seqrecord.*;	
import org.jax.mgi.shr.unix.*;

public class SeqRecordFilter
{
	// Concept:
        //        IS: an object that reads sequence records, filters them
	// 		by using Decider objects, writes them to the 
	//		appropriate files when a Decider object returns true,
	//		and handles logging of statistics and exceptions
        //       HAS: Decider objects that encapsulate predicates  
	//	      a FileWriter object for each Decider object
	//	      a sequence record object 
	//	      a FileReader object	
        //      DOES: parses arguments; reads sequence records;  
	//	      writes sequence records to files 
	//	      handles logging of statistics and exceptions
        // Implementation:

	// constructors
	
	public SeqRecordFilter(
		SeqDecider[] deciders, // All possible Decider objects for this
				       // filter 
	        String[] args,	       // command line arguments (see getArgs()
				       // method header) 
		String logName,	       // full path name of this filter's log
		SeqRecord sr)	       // sequence record object
	{
	    // Purpose: creates a file reader object 
	    //	    creates file writer objects for each Decider in 'args'
	    //	    creates a file writer object for 'logName'
	    // Effects: catches IOException,
	    // 		writes exception msg to stderr then exits	
	    try
	    {	
		// create an array to hold subset of 'deciders' based on 
		// 'args'; create same size as 'deciders' 
		this.decidersForThisFilterRun = new SeqDecider[deciders.length];
		// create a log writer, open in append mode
		this.log = new BufferedWriter(new FileWriter(
			logName, true));

		// create a reader for stdin
		this.in = new BufferedReader(new InputStreamReader(System.in));

		//DEBUG
		//this.in = new BufferedReader(new FileReader(
		//	"Place abs path to input file here"));

		// the sequence record object
                this.seqRec = sr;
		
		// do the decider to outputLocation mapping
		this.getArgs(deciders, args);
	    }
	    catch(IOException e1)
            {
                System.err.println("IOException in SeqRecordFilter.construct: "
                    + e1.getMessage());
		System.exit(1);
            }
	   	
	}
		
	public SeqRecordFilter(
		SeqDecider[] deciders, // All possible Decider objects for this
				       // filter 
		String[] args, 	       // command line arguments (see getArgs()
				       // method header)	
		SeqRecord sr)	       // sequence record object
	{
	// Purpose: creates a file reader object
	//          creates file writer objects for each Decider in 'args'
	//          creates a default log file writer object 
	// Assumes: There is a System property called 'LOG'

		// call first constructor with Configurable abs log name
		this(deciders, args, System.getProperty("LOG"), sr); 
	}
	
	//
	// methods
	//

	public void getArgs(
		SeqDecider[] sd, // All possible Deciders for this filter
		String[] args)   // command line arguments, see usage in Notes:
	{
	// Purpose: Maps Deciders from "sd" to output files or directories 
	//           in "args"
        // Returns: nothing 
        // Assumes: nothing
        // Effects: catches IOException, InterruptedException 
	//	    write exception message to stderr
	//	    and exits. See the go() method for description of Exceptions
        // Throws:  nothing
        // Notes:
	//	//
	//	// command line usage
	//	//
	//
	//	The args array consists of triples of information
	//	1) an decider name indicating which Decider in 'sd' to map
	//         the output file in 3)
	//		e.g. --mouse or  --rat
	//	2) option indicating output location is a file and to open that
	//	    file in append or overwrite mode i.e. -o | -a 
	//	    OR
	//	   option indicating output location is a directory i.e. -d
	//	3) full path to output file or directory to be mapped to 
	//         the Decider in 1)
	//	
	//	e.g.
	//	--mouse -o absOutputFileName
	//	--mouseBAC -d absOutputDir
	//	
	//	//
	//	// optstring syntax:
	//	//
	//
	//	- = return args in order regardless of if opt longOpt or arg
	//	: = return ':' if option w/o a required arg and return '?' 	
	//	               if option is invalid 
	//	a: = required argument is a file - open it in append mode
	//	o: = required argument is a file - open file in overwrite mode
	//	d: = required argument is a directory
	//
	//	All opts return an integer value, short opts return their
 	//        ascii integer value, longopts return their assigned integer
        //        -1 is returned when there are no more opts

	    try
	    {
		// a String containing a description of the valid (short) optns 
		String optstring = "-:a:o:d:";
		
		/* long options are defined by an array of "LongOpt" objects.
		 the LongOpt constructor takes four params
                 1) a String representing the option name
                 2) a integer specifying what args the option takes (no arg-
                    ument in this case)
                 3) a StringBuffer flag object (null in this case)
                 4) an integer whose value is returned when getOpt() is called
                      it is this integer representation of the option name
                      used in the case statement of the switch below
		    Since we are doing the same thing for all the options
		      we assign the int 2 to each LongOpt.
		*/
		LongOpt[] longopts = {
    		    new LongOpt("mouse", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("rat", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("rodent", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("human", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("genbank", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("sprot", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("htg", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("stsmouse", LongOpt.NO_ARGUMENT, null, 2),
		    new LongOpt("mouseBAC", LongOpt.NO_ARGUMENT, null, 2)};

		/* create a Getopt object passing it:
		   1) The name to display as the program name when logging
		   2) The args
		   3) description of valid options 
		   4) description of valid long options
		*/
		Getopt g = new Getopt("Usage Error", args, optstring, longopts);
		
		// true = have a Decider  object and need to get  
		// 	corresponding output file; this assures that 
		// 	decider/outputLocation pairs are obtained
		boolean haveDecider = false;

		int c;		// the option returned from g.getopt()
		String arg;	// the arg returned from g.getOptarg()

		//get command line options in order, run them through the switch
		while((c = g.getopt()) != -1)
		{
		    switch(c)	 
		    {
			// case for all the longopts
			case 2:
			    if(haveDecider == true)
			    {
				System.err.println("ERROR:Don't have a writer " 
					 + "for last Decider!!");
			    }
			    else
			    {
			    	// loop through all the possible Deciders
				for(int i = 0; i < sd.length; i++)
			    	{
		 			// if the 'sd's  name == the 
					// command line long option, place
					// that Decider object in 'deciders
		    			// ForThisFilterRun' array
				    if(sd[i].getName().equals(
					longopts[g.getLongind()].getName() ))
			       	    {
					this.decidersForThisFilterRun[
					    this.deciderCtr] = sd[i];
				    }
			        }
			        // we have a Decider
				haveDecider = true;
			        break;
			    } 

			case 'a':
			    if(haveDecider == false)
			    {
				System.err.println("ERROR in getArgs(): " +
                                        "Don't have a Decider!!");
			    }  
			    else
			    {
				// get arg (an output filename) for this option
				arg = g.getOptarg();	
				
				// open the file in append mode and place it in
				// the seqOutput Vector parallel to its decider
				// in the decidersForThisFilterRun array
				this.seqOutput.add(new
                                	BufferedLargeFileWriter(arg, true));
				// we are now expecting a Decider
				haveDecider = false; 	
				
				// increment the Decider object count
				this.deciderCtr++;
			    }
			    break;

			case 'o':
			    if(haveDecider == false)
                            {
                                System.err.println("ERROR in getArgs(): " +
					"Don't have a decider!!");
                            }
                            else
                            {
                                // get arg (an output filename) for this option
				arg = g.getOptarg();

				// open the file in overwrite mode and place it 
                                // in the seqOutput Vector  
                                this.seqOutput.add(new 
					BufferedLargeFileWriter(arg, false));
                                
				// we are now expecting a Decider
				haveDecider = false;    
			
				// increment the Decider object count
                                this.deciderCtr++;           
                            }
                            break;
			case 'd':
			    if(haveDecider == false)
                            {
                                System.err.println("ERROR in getArgs(): " +
                                        "Don't have a decider!!");
                            }
			    else
			    {
				// get arg (and output directory for this option
                                arg = g.getOptarg();

				// Create a File object, add to seqOutput Vector
				this.seqOutput.add(new File(arg));

				// we are now expecting a Decider
                                haveDecider = false;

                                // increment the Decider object count
                                this.deciderCtr++;
			    }
			    break;
			case ':':
				throw new IOException(
					"Missing argument for option");

			case '?':
				throw new IOException("Invalid option");

			default:
			    System.err.println("Error getopt() returned: " + 
				c + "if 1 a non-opt" +
				" arg element was found, its value is: " +
				g.getOptarg());
			     
				
		    }
		}
	    }
	    catch(IOException e1)
            {
                System.err.println("IOException in SeqRecordFilter.getArgs(): " 
                    + e1.getMessage());
		System.exit(1);
            }
	    catch(InterruptedException e2)
	    {
		System.err.println(
		    "InterruptedException in SeqRecordFilter.getArgs(): "
                    + e2.getMessage());	
		System.exit(1);
	    }
	}
	
	public void go() 
	{
	// Purpose: Reads sequence records 
	//	    For each record read: 
	//	      if a Decider is true write the record using the
	//	      corresponding writer 
	//	    Logs total number of records processed and number of
	//	      records written by each writer
        // Returns: nothing
        // Assumes: the constructors have initialized all readers and writers,
	//	    a sequence record object, and created Decider and
	//	    file writer arrays
        // Effects: writes to log files and sequence output files
	//	    reads sequence records from stdin to EOF
        // 	    catches RESyntax and IO exceptions and writes their
	//	    messages stderr 
	//	    e.g. RESyntaxException indicates a syntax 
	//		   error in a regular expression. This would be unusual
	//		   and should only occur during development.
	//		 IOException indicates failed or interrupted I/O 
	//		   operations e.g. the path/file does not exist  
	//		 InterruptedException indicates another thread has 
	//		  interrupted this thread
	// Throws: nothing
	    try
	    { 
		// track time elapsed for this filter run
		long totalRunTimeMinutes = 0;	
		long stopTime = 0;
		long startTime = System.currentTimeMillis();
		
		// priming read of a sequence record
		this.seqRec.readText(this.in);

		// seqRec.getLine() returns the last line that it read when
		// reading itself. If it returns null the last line of the last
		// sequence record has been read. 
		while(this.seqRec.getLine() != null)
		{
			processRecord();
			// read the next record
			this.seqRec.readText(this.in);
		}
		
		// Capture the stop time of this filter
		stopTime = System.currentTimeMillis();

		// Figure the run time of this filter and log it
                totalRunTimeMinutes = ((stopTime - startTime) / 1000);
                this.logGeneral("Total runtime in seconds: " +
			new Long(totalRunTimeMinutes).toString() + "\n");
		
		// log statistics from this filter run (see the method for info)
		logStats();

		// close all open readers and writers
                this.in.close();
                this.log.close();
                
		// if seqOutput contains a writer (not a dir),  close it
		for(int i = 0; i < this.deciderCtr; i++)
		{
			if(this.seqOutput.get(i) instanceof 
					BufferedLargeFileWriter)
			{
				((BufferedLargeFileWriter)(
					this.seqOutput.get(i))).close();
			}
		}

	    }
            catch(IOException e1)
            {
                    System.err.println("IOException in SeqRecordFilter.go(): " +
			e1.getMessage());
		    System.exit(1);
            }
            catch(RESyntaxException e2)
            {
                    System.err.println("RESyntaxException SeqRecordFilter.go():"
			 + e2.getMessage());
		    System.exit(1);
            }
	    catch(InterruptedException e4)
	    { 
		System.err.println("InterruptedException SeqRecordFilter.go():"
                	+ e4.getMessage());
		System.exit(1);
	    }
 	
	}

	private void processRecord() 
	{
	// Purpose: 
        // Returns: 
        // Assumes: the constructors have initialized all readers and writers,
	//	    for deciders whose outputLocation is a file
        //          a sequence record object, and created Decider and
        //          output arrays
        // Effects: writes files to the filesystem
        //          catches Interrupted and IO exceptions and writes their
        //          messages stderr
        //               IOException indicates failed or interrupted I/O
        //                 operations e.g. the path/file does not exist
        //               InterruptedException indicates another thread has
        //                interrupted this thread

	    try
	    {		
	        // loop through the Deciders for this filter run
            	for(int i = 0; i < this.deciderCtr; i++)
            	{
                    // if Decider returns true for seqRec
                    if(this.decidersForThisFilterRun[i].isA(
                            this.seqRec) == true)
                    {
			// if seqOutput[i] is type File then we write 
			// the seqRec text to its own file
			if(this.seqOutput.get(i) instanceof File)
			{
			    // write text to a new file named seqIdVersion in	
			    // the output directory for the decider 
			    this.singleSeqWriter = new 
			        BufferedLargeFileWriter(
			   	    ((File)(this.seqOutput.get(i))).getPath() + 
				    File.separator +
				    this.seqRec.getVersion(), false);
			    this.singleSeqWriter.write(this.seqRec.getText());	
			    this.singleSeqWriter.close();
			}
			// then it is a writer, simply write
			else
			{
			    ((BufferedLargeFileWriter)this.seqOutput.get(i)).
                                write(this.seqRec.getText());
			}
		    }
		}
	    }
	    catch(InterruptedException e2)
            {
                System.err.println(
                    "InterruptedException in SeqRecordFilter.processRecord(): "
                    + e2.getMessage());
                System.exit(1);
            }
	    catch (IOException e3)
	    {
		System.err.println(
		    "InterruptedException in SeqRecordFilter.processRecord(): "
		    + e3.getMessage());
		System.exit(1);
	    }
	}

	private void logStats() throws IOException
        {
	//
	// Purpose: Logs total number of records processed and number of records
	//         processed for each output file
	// Throws: IOException

		// loop through the Deciders
		for (int i = 0; i < this.deciderCtr; i++)
		{
			this.log.write("Count statistics for " + 
				this.decidersForThisFilterRun[i].getName() + 
				"\n");
			this.log.write("    Total records: ");
			this.log.write(
				this.decidersForThisFilterRun[i].getAllCtr() + 
				"\n");
			this.log.write(
				"    Records for this filter: ");
			this.log.write(
				this.decidersForThisFilterRun[i].getTrueCtr() +
				 "\n");
			this.log.write("\n");
		}
	}
	
	private void logGeneral(String s) throws IOException
	{
	//	
	// Purpose: Writes 's' to the log 
	// Throws: IOException

		this.log.write(s + "\n");
	}

	//
	// instance variables
	//

	// the sequence record object that will read and parse records from
	// the input stream until EOF
	private SeqRecord seqRec;

	// the number of Deciders for this filter run. We use this because
	// decidersForThisFilterRun.length gives us total length of array
	// not number of array elements filled.
	private int deciderCtr = 0;		 
					
	// log writer object 
	private BufferedWriter log;
	
	// the input file reader object
	private BufferedReader in; 

	// Decider objects for deciders requested on the command line  
	private SeqDecider[] decidersForThisFilterRun;

	// Vector holds filewriters or directories which correspond, by index, 
	// to 'decidersForThisFilterRun' 
	private Vector seqOutput = new Vector();

	// writer for those deciders which require single record/file output
	private BufferedLargeFileWriter singleSeqWriter;

}
