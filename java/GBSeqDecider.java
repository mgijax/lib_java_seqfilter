package org.jax.mgi.bio.seqfilter;

import org.jax.mgi.bio.seqrecord.*;

public abstract class  GBSeqDecider extends SeqDecider
{
	//Concept:
        //        IS: A SeqDecider for deciding predicates for GenBank
	//	      seqRecords
	//	      Concrete subclasses of this class must
	//	      define an isA method - see superclass 
        //       HAS: a Genbank interrogator object that can be used by the isA
	//	      method to determine if a predicate is true
        //      DOES: see superclass
        // Implementation:

	//
	//constructors
	//

	public GBSeqDecider(String s)
	{
		// Purpose: initialize "name" in superclass
		super(s);
	}
	
	//
	//instance variables:
	//

	// determines if a predicate is true. 	
	protected static GBSeqInterrogator si = new GBSeqInterrogator();
}
