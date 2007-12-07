/*
 @Copyright (c) 2005-2007 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.codegen.c.actor.lib.colt;

import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.colt.ColtPoisson.
 *
 * NOTE: Currently, random numbers produced by c code
 * generated by this class differ from those produced
 * by the ColtPoisson random generator.
 *
 * @see ptolemy.actor.lib.ColtPoisson
 * @author Teale Fristoe
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red
 * @Pt.AcceptedRating
 *
 */
public class ColtPoisson extends ColtRandomSource {
    /**
     * Constructor method for the ColtPoisson helper.
     * @param actor the associated actor
     */
    public ColtPoisson(ptolemy.actor.lib.colt.ColtPoisson actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate shared code.
     * Read from ColtPoisson.c, replace macros with their values and
     * return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set getSharedCode() throws IllegalActionException {
        // LinkedHashSet gives order to the insertion. The order of code block
        // is important here because binomialBlock uses code from the other
        // shared code blocks.
        Set sharedCode = new LinkedHashSet();
        sharedCode.addAll(super.getSharedCode());

        // poissonBlock is from the ColtRandomSource parent class.
        sharedCode.add(_generateBlockCode("poissonBlock"));
        return sharedCode;
    }

    /**
     * Get the files needed by the code generated for the
     * ColtPoisson actor.
     * @return A set of Strings that are names of the files
     *  needed by the code generated for the ColtPoisson actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for producing a new random number.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return The code that produces a new random number.
     */
    protected String _generateRandomNumber() throws IllegalActionException {
        return _generateBlockCode("poissonDistributionBlock");
    }
}
