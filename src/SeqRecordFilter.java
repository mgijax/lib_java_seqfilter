package org.jax.mgi.bio.seqfilter;

import java.io.*;
import org.apache.regexp.*;
import gnu.getopt.*;
import org.jax.mgi.bio.seqrecord.*;	
import BufferedLargeFileWriter;

public class SeqRecordFilter
{
	// Concept:
        //        IS: an object that reads sequence records, filters them
	// 		by using predicates, writes them to the 
	//		appropriate files when a predicate returns true,
	//		and handles logging of statistics and exceptions
        //       HAS: decider objects that encapsulate predicates and  
	//		corresponding file writer objects 
	//	      a sequence record object 
	//	      a file reader object	
        //      DOES: parses arguments; reads sequence records;  
	//	      writes sequence records to files corresponding to 
	//		predicates
	//	      handles logging of statistics and exceptions
        // Implementation:

	// constructors
	
	public SeqRecordFilter(
		SeqDecider[] deciders, // All possible predicates for this filt-
				       // er to determine the organism type of 
				       // a sequence record.
	        String[] args,	       // command line arguments	 
		String logName,	       // abs log name 
		SeqRecord sr)	       // sequence record object
	{
	    // Purpose: creates an file reader object 
	    //	    creates file writer objects for each predicate 
	    //	    creates a file writer object for 'logName'
	    try
	    {	
		// create an array for the predicates
		this.seqDeciders = new SeqDecider[deciders.length];
		
		// create an array for writers which correspond (by index) 
		// to predicates in the seqDeciders array by index
		this.seqWriters = new BufferedLargeFileWriter[deciders.length];

		// create a log writer, open in append mode
		this.log = new PrintWriter(new FileWriter(
			logName, true));

		// create a reader for stdin
		this.in = new BufferedReader(new InputStreamReader(System.in));
	
		// the sequence record object
                this.seqRec = sr;
	
		// process command line args
		this.getArgs(deciders, args);
	    }
	    catch(IOException e1)
            {
                System.err.println("IOException in SeqRecordFilter.construct: "
                    + e1.getMessage());
            }
	   	
	}
		
	public SeqRecordFilter(
		SeqDecider[] deciders, // All possible predicates for this filt-
				       // er to determine the organism type of
				       // a sequence record
		String[] args, 	       // command line arguments	
		SeqRecord sr)	       // sequence record object
	{
		// Purpose: creates an file reader object
		//          creates file writer objects for each predicate 
		//          creates a default log file writer object 
		
		// call first constructor with Configurable abs log name
		this(deciders, args, System.getProperty("LOG"), sr);
	}
	
	//
	// methods
	//

	public void getArgs(
		SeqDecider[] sd, // All possible predicates for this filter
		String[] args)   // command line arguments, see usage in Notes:
	{
	// Purpose: Maps predicates from "sd" to output file names in "args"
	//	    See Notes below
        // Returns: nothing 
        // Assumes: nothing
        // Effects: nothing
        // Throws:  nothing, but catches IO exceptions, writes their
        //           messages to log and error log, and execution
        //           halts. See the method "go()" for a description of 
	//	     I/O exceptions
        // Notes:
	//	//
	//	// command line usage
	//	//
	//
	//	The args array consists of triples of information
	//	1) an organism name indicating which predicate in 'sd' to map
	//         the output file in 3)
	//		e.g. --mouse or  --rat
	//	2) option indicating whether to open the output
	//	    file in append or overwrite mode e.g. -o | -a
	//	3) abs output file to be mapped to the predicate
	//	   in 1)
	//	
	//	e.g.
	//	--mouse -o absOutputFileName
	//	
	//	//
	//	// optstring syntax:
	//	//
	//
	//	- = return args in order regardless of if opt longOpt or arg
	//	: = return ':' if option w/o a required arg and return '?' 	
	//	               if option is invalid 
	//	a: = open file in append mode, argument required (filename)
	//	o: = open file in create/overwrite mode, argument required
	//
	//	All opts return an integer value, short opts return their
 	//        ascii integer value, longopts return their assigned integer
        //        -1 is returned when there are no more opts

	    try
	    {
		// a String containing a description of the valid (short) optns 
		String optstring = "-:a:o:";
		
		// long options are defined by an array of "LongOpt" objects. 
		LongOpt[] longopts = new LongOpt[8];

		// the LongOpt constructor takes four params
		// 1) a String representing the option name
		// 2) a integer specifying what args the option takes (no arg-
		//	ument in this case)
		// 3) a StringBuffer flag object (null in this case)
		// 4) an integer whose value is returned when getOpt() is called
		// 	it is this integer representation of the flag that we
		//	will use in the case statement of the switch below
		longopts[0] = new LongOpt (
			"mouse", LongOpt.NO_ARGUMENT, null, 2);
		longopts[1] = new LongOpt (
			"rat", LongOpt.NO_ARGUMENT, null, 3);
		longopts[2] = new LongOpt (
			"rodent", LongOpt.NO_ARGUMENT, null, 4);
		longopts[3] = new LongOpt (
			"human", LongOpt.NO_ARGUMENT, null, 5);
		longopts[4] = new LongOpt (
			"genbank", LongOpt.NO_ARGUMENT, null, 6);
		longopts[5] = new LongOpt (
			"sprot", LongOpt.NO_ARGUMENT, null, 7);
		longopts[6] = new LongOpt (
                	"gbhtg", LongOpt.NO_ARGUMENT, null, 8);
		longopts[7] = new LongOpt (
                        "gbstsmouse", LongOpt.NO_ARGUMENT, null, 9);
		
		// create a Getopt object passing it:
		// 1) The name to display as the program name when logging
		// 2) The String array passed as the command line to the program
		// 3) A String containing description of the valid options 
		// 4) An array of LongOpt objects describing valid long options
		//
		Getopt g = new Getopt("who knows", args, optstring, longopts);
		
		// all longopts (which represent predicates) have corresponding 
		// (short) opts (which represent output files). 
		// true = have a predicate  object and need to get its 
		// corresponding output file
		boolean haveDecider = false;

		int c;		// the option returned from g.getopt()
		String arg;	// the arg returned from g.getOptarg()

		//get command line options in order, run them through the switch
		while((c = g.getopt()) != -1)
		{
		    switch(c)	 
		    {
			// cases for all the longopts which represent predicates
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			    if(haveDecider == true)
			    {
				System.err.println("ERROR:Don't have a writer " 
					 + "for last predicate!!");
			    }
			    else
			    {
			    	// loop through all the possible predicates
				for(int i = 0; i < sd.length; i++)
			    	{
		 			// if the 'sd's  name == the 
					// command line long option, place
					// that predicate in the decider
		    			// array.
				    if(sd[i].getName().equals(
					longopts[g.getLongind()].getName() ))
			       	    {
					this.seqDeciders[this.predicateCtr] = 
						sd[i];
				    }
			        }
			        // we have a predicate
				haveDecider = true;
			        break;
			    } 

			case 'a':
			    if(haveDecider == false)
			    {
				System.err.println("ERROR in getArgs(): " +
                                        "Don't have a predicate!!");
			    }  
			    else
			    {
				// get arg (an output filename) for this option
				arg = g.getOptarg();	
				
				// open the file in append mode and place it in
				// the seqWriters array parallel to the predic-
				// array
				this.seqWriters[this.predicateCtr] = new
                                	BufferedLargeFileWriter(arg, true);

				// we are now expecting a predicate
				haveDecider = false; 	
				
				// increment the predicate object count
				this.predicateCtr++;
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
                                // in the seqWriters array  
                                this.seqWriters[this.predicateCtr] = new 
					BufferedLargeFileWriter(arg, false);
                                
				// we are now expecting a predicate
				haveDecider = false;    
			
				// increment the predicate object count
                                this.predicateCtr++;           
                            }
                            break;

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
                System.err.println("IOException in SeqRecordFilter.go(): " 
                    + e1.getMessage());
            }
	    catch(InterruptedException e2)
	    {
		System.err.println("InterruptedException in SeqRecordFilter.go(): "
                    + e2.getMessage());	
	    }
	}
	
	public void go() 
	{
	// Purpose: Reads sequence records
	//	    For each record read: 
	//	      if a predicate is true write the record using the pred-
	//	      icates corresponding writer, 
	//	    Logs total number of records processed and number of
	//	      records written by each writer
        // Returns: nothing
        // Assumes: the constructors have initialized all readers and writers,
	//	    a sequence record object, and created predicate and
	//	    (corresponding) writer arrays
        // Effects: writes to log files and sequence output files
        // Throws: Nothing, but catches End of File, RESyntax and IO exceptions
	//	    and writes their messages stderr 
	//	    e.g. RESyntaxException indicates a syntax 
	//		   error in a regular expression. This would be unusual
	//		   because all RE syntax is hardcoded in the class 
	//		   source of the objects used by this method. 
	//		 EOFException, in this method, indicates that 'seqRec'
	// 		   found end of file (null) and it did not coincide
	//		   with an end of record symbol. Please note that  
	//		   normal EOF does not raise an exception, but
	//		   returns null
	//		 IOException indicates failed or interrupted I/O 
	//		   operations e.g. the path/file does not exist  
	    try
	    { 
		// tracking time elapsed for this filter run
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
			// loop through the predicates for this filter run 	
			for(int i = 0; i < this.predicateCtr; i++)
			{
				//if this predicate returns true for this seqRec
				// write it with this predicate's corresponding 
				// writer
				if(this.seqDeciders[i].isA(this.seqRec) ==true)
				{
					this.seqWriters[i].write(
						this.seqRec.getText());
				}
			}
			// read the next record
			this.seqRec.readText(this.in);
		}

		// Capture the stop time of this filter
		stopTime = System.currentTimeMillis();
	
		// Figure the run time of this filter and log it
                totalRunTimeMinutes = ((stopTime - startTime) / 1000);
                log("Total runtime in seconds = ");
                log(new Long(totalRunTimeMinutes).toString());
		
		// log statistics from this filter run (see the method for info)
		logStats();

		// close all readers and writers
                this.in.close();
                this.log.close();
                
		for(int i = 0; i < this.predicateCtr; i++)
                {
                        this.seqWriters[i].close();
                }

	    }
	    catch(EOFException e3)
            {
                    System.err.println("EOFException in SeqRecordFilter.go(): " 
		 	+ e3.getMessage());
            }
            catch(IOException e1)
            {
                    System.err.println("IOException in SeqRecordFilter.go(): " +
			e1.getMessage());
            }
            catch(RESyntaxException e2)
            {
                    System.err.println("RESyntaxException SeqRecordFilter.go():"
			 + e2.getMessage());
            }
	    catch(InterruptedException e4)
	    { 
		  System.err.println("InterruptedException SeqRecordFilter.go():"
                         + e4.getMessage());
	    }
 	
	}

	private void logStats()
	{
	//
	// Purpose: Logs total number of records processed and number of records
	//         processed for each output file

		// loop through the predicates
		for (int i = 0; i < this.predicateCtr; i++)
		{
			this.log.println("Count statistics for " + 
				this.seqDeciders[i].getName());
			this.log.println("    Total records:");
			this.log.println(this.seqDeciders[i].getAllCtr());
			this.log.println("    Records for this filter");
			this.log.println(this.seqDeciders[i].getTrueCtr());
			this.log.println('\n');
		}
	}
	
	private void log(String s)
	{
	//	
	// Purpose: Writes 's' to the log 
	//
		this.log.println(s);
	}

	//
	// instance variables
	//

	// the sequence record object that will read and parse *all* 
	// the records in the input stream
	private SeqRecord seqRec;

	// the number of predicates for this filter e.g.
	// the number of organisms we are filtering for
	private int predicateCtr = 0;		 
					
	// log writer object 
	private PrintWriter log;
	
	// the input file reader object
	private BufferedReader in; 

	// predicate objects for organisms requested on the command line  
	// which correspond, by index, to 'this.seqWriters'
	private SeqDecider[] seqDeciders;

	// file writers which correspond, by index, to 'this.seqDeciders' 
	private BufferedLargeFileWriter[] seqWriters;


}
