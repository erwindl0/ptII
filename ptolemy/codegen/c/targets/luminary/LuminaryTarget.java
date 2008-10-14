/*thus far I am not sure if thiis works. I have not been able to get code to generate for this target from any of the 
 * existing examples. I also realized that many of the examples cannot generate code for iRobot*/ 
/* Common code for the Luminary

 @Copyright (c) 2007 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.targets.luminary;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;

/**
 Common code for the Luminary.

 @author Shanna-Shaye Forbes
 @version $Id: ArduinoTarget.java 47617 2007-12-18 19:59:51Z cxh $
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */
public class LuminaryTarget extends CCodeGeneratorHelper {

    /**
     * Construct a helper for the Luminary target.
     * @param actor The associated actor.
     */
    public LuminaryTarget(ptolemy.actor.TypedCompositeActor actor) {
        super(actor);
    }
}