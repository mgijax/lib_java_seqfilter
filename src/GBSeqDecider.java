package org.jax.mgi.bio.seqfilter;

import org.jax.mgi.bio.seqrecord.*;

public abstract class  GBSeqDecider extends SeqDecider
{
	//Concept:
        //        IS: see superclass
        //       HAS: a Genbank interrogator object to determine if a 
	//	       predicate is true  
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
	
	protected static GBSeqInterrogator si = new GBSeqInterrogator();
}
