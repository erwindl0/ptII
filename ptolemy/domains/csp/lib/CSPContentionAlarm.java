/* CSPContentionAlarm

 Copyright (c) 1998-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;


//////////////////////////////////////////////////////////////////////////
//// CSPContentionAlarm
/**
   FIXME: add description!!

@author John S. Davis II
@version $Id$

*/

public class CSPContentionAlarm extends CSPActor {

    /**
     */
    public CSPContentionAlarm(CompositeActor cont, String name) 
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         
         _input = new IOPort(this, "input", true, false);
         _output = new IOPort(this, "output", false, true);
    }
         
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public void fire() throws IllegalActionException {
        
        while(true) {
            // State 1
	    System.out.println("\t\t\t\tSTATE 1: " +getName());
            _input.get(0);
            
            // State 2
	    System.out.println("\t\t\t\tSTATE 2: " +getName());
            waitForDeadlock();
            
            // State 3
	    System.out.println("\t\t\t\tSTATE 3: " +getName());
            _output.send(0, new Token());
        }
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    private IOPort _input;
    private IOPort _output;
}
