/* Filter out graphical classes

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.util.NamedObj;

import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// FilterOutGraphicalClasses
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter out graphical classes.

<p>This is very useful for running applets with out requiring files
like diva.jar to be downloaded.  It is also used by the nightly build to
run tests when there is no graphical display present.

@author  Edward A. Lee, Christopher Hylands
@version $Id$
*/

public class FilterOutGraphicalClasses implements MoMLFilter {
        
    /** If the attributeValue is "ptolemy.vergil.icon.ValueIcon",
     *  or "ptolemy.vergile.basic.NodeControllerFactory"
     *  then return "ptolemy.kernel.util.Attribute"; if the attributeValue
     *  is "ptolemy.vergil.icon.AttributeValueIcon" or
     *  "ptolemy.vergil.icon.BoxedValueIcon" then return null, which
     *  will cause the MoMLParser to skip the rest of the element;
     *  otherwise return the original value of the attributeValue.
     *  
     *  @param container  The container for this attribute, ignored
     *  in this method.
     *  @param attributeName The name of the attribute, ignored
     *  in this method.
     *  @param attributeValue The value of the attribute.
     *  @return the filtered attributeValue.
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue) {

        // If the nightly build is failing with messages like:
        // " X connection to foo:0 broken (explicit kill or server shutdown)."
        // Try uncommenting the next lines to see what is being
        // expanding before the error:
        //System.out.println("filterAttributeValue: " + container + "\t"
        //        +  attributeName + "\t" + attributeValue);

        if (attributeValue == null) {
            return null;
        } else if (_graphicalClasses.containsKey(attributeValue)) {
            return (String) _graphicalClasses.get(attributeValue);
        }
        return attributeValue;
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map of actor names a HashMap of graphical classes to their
     *  non-graphical counterparts, usually either
     *  ptolemy.kernel.util.Attribute or null.
     */
    private static HashMap _graphicalClasses;

    static {
	_graphicalClasses = new HashMap();
	// Alphabetical by key class
	_graphicalClasses.put("ptolemy.vergil.basic.NodeControllerFactory",
			      "ptolemy.kernel.util.Attribute");
	_graphicalClasses.put("ptolemy.vergil.icon.AttributeValueIcon",
			      null);
	_graphicalClasses.put("ptolemy.vergil.icon.BoxedValueIcon",
			      null);
	_graphicalClasses.put("ptolemy.vergil.icon.ValueIcon",
			      "ptolemy.kernel.util.Attribute");
    }
}

