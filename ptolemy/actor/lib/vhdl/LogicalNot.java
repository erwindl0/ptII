/** An actor that outputs the fixpoint value of the concatenation of
 the input bits.

 Copyright (c) 1998-2006 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.vhdl;

import java.math.BigInteger;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPoint;
import ptolemy.math.Precision;


//////////////////////////////////////////////////////////////////////////
//// LogicalNot

/**
 Produce an output token on each firing with a FixPoint value that is
 equal to the sum of all the inputs at the plus port minus the inputs at the
 minus port. 

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class LogicalNot extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LogicalNot(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        A = new TypedIOPort(this, "A", true, false);
        A.setTypeEquals(BaseType.FIX);
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens to be inverted.  This is a multiport of fix point
     *  type
     */
    public TypedIOPort A;

        
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Output the fixpoint value of the sum of the input bits. 
     *  If there is no inputs, then produce null.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        BigInteger intResult = null;
        int bitsInResult = 0;
        
        if (A.hasToken(0)) {
            FixPoint valueA = ((FixToken) A.get(0)).fixValue();
            bitsInResult = valueA.getPrecision().getNumberOfBits();
            BigInteger bigIntA = valueA.getUnscaledValue();
            intResult = bigIntA.not();     
        }

        if(intResult != null )
        {
            Precision precision = new Precision(1, bitsInResult, 0);
            FixToken result = new FixToken(intResult.doubleValue(), precision);
            output.send(0, result);
        }
    }
}
