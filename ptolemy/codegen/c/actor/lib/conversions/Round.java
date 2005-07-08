/* A helper class for actor.lib.conversions.Round

 @Copyright (c) 2005 The Regents of the University of California.
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
/*
 * Created on Apr 23, 2005
 *
 */
package ptolemy.codegen.c.actor.lib.conversions;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Man-Kit Leung
 * @version $Id$
 */
public class Round extends CCodeGeneratorHelper {

    /**
     * Constructor method for the Round helper
     * @param actor the associated actor
     */
    public Round(ptolemy.actor.lib.conversions.Round actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method reads in <code>codeBlock1</code> from Round.c 
     * and puts into the given stream buffer
     * @param stream the given buffer to append the code to
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        ptolemy.actor.lib.conversions.Round actor = (ptolemy.actor.lib.conversions.Round) getComponent();

        CodeStream tmpStream = new CodeStream(this);
        String function = actor.function.getExpression();
        String code = (function.equals("ceil")) ? "ceilBlock"
                : ((function.equals("floor")) ? "floorBlock" : ((function
                        .equals("round")) ? "roundBlock" : "truncateBlock"));

        tmpStream.appendCodeBlock(code);

        stream.append(processCode(tmpStream.toString()));
    }

    /** Get the files needed by the code generated for the
     *  Round actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the Round actor.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        files.add("\"stdlib.h\"");
        files.add("\"limits.h\"");
        files.add("\"math.h\"");

        return files;
    }
}
