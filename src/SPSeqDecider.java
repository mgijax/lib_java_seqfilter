package org.jax.mgi.bio.seqfilter;

import org.jax.mgi.bio.seqrecord.*;

public abstract class  SPSeqDecider extends SeqDecider
{
	//Concept:
        //        IS: see superclass
        //       HAS: a SwissProt interrogator object to determine if a 
	//	       predicate is true
        //      DOES: see superclass
        // Implementation:
	
	//
	//constructors
	//

	public SPSeqDecider(String s)
	// Purpose: initialize "name" in superclass
	{
		super(s);
	}
	
	//
	//instance variables:
	//

	protected static SPSeqInterrogator si = new SPSeqInterrogator();
}
