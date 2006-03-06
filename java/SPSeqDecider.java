package org.jax.mgi.bio.seqfilter;

import org.jax.mgi.bio.seqrecord.*;

public abstract class  SPSeqDecider extends SeqDecider
{
	//Concept:
        //        IS: A seqDecider for deciding predeictes for SwissProt
	//	      seqRecords
	//	      Concrete subclasses of this class must
        //            define an isA method - see superclass
        //       HAS: a SwissProt interrogator object that can be used by the 
	//	      isA methoud to determine if a predicate is true
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

	// determines if a predicate is true.
	protected static SPSeqInterrogator si = new SPSeqInterrogator();
}
