package org.jax.mgi.bio.seqfilter;

import org.jax.mgi.bio.seqrecord.*;

public abstract class SeqDecider
{
	//Concept:
        //        IS: an object that  applies a predicate to sequence record
	//	       objects and keeps counters of total records processed
	//	       and total records for which the predicate was true
	//	      concrete subclasses of this class must define an isA
	//		method that defines the predicate	
        //       HAS: a name, a counter for total sequence records processed, 
        //             a counter total records for which the predicate is true
        //      DOES: Determines whether the predicate is true for a sequence  
	//             record, maintains counters described in "HAS" above
        // Implementation:

	//
	//constructors 
	//

	public SeqDecider(String s)
	// Purpose: initialize "name" 
	{
		this.name = s;
	}

	//	
	//methods:
	//

	public abstract boolean isA(SeqRecord s); //override in subclasses
	// Purpose: Decides if a predicate is true for 's'. Increments
	//	      counters  
        // Returns: boolean true or false
        // Assumes: it will be implemented in all concrete subclasses
        // Effects: nothing
        // Throws: nothing
        // Notes:

	public int getAllCtr()
	// Purpose: return the counter for total records processed
	{
		return allCtr;
	}

	public int getTrueCtr()
	// Purpose: return counter for records for which the predicate is true
	{
		return trueCtr;
	}
	
	public String getName( )
        //Purpose: report this object's name
        {
                return name;
        }

	protected void incrementAllCtr()
	// Purpose: increment counter for total records processed
	{
		allCtr = allCtr + 1;
	}
	
	protected void incrementTrueCtr()
	// Purpose: increment counter for records for which the predicat is true
	{
		trueCtr = trueCtr + 1;
	}

	//	
	//instance variables:
	//
	
	// total records processed
	private int allCtr = 0;

	// number of records for which the predicate is true
	private int trueCtr = 0;
	
	// This decider's name
	protected String name;

}
