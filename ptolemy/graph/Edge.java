/* A weighted or unweighted edge for a directed or undirected graph.

 Copyright (c) 2001-2002 The University of Maryland
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.graph;

//////////////////////////////////////////////////////////////////////////
//// Edge
/**
A weighted or unweighted edge for a directed or undirected graph.
Edges are immutable, and should
normally created only by classes of
graphs that contain them. The connectivity of edges is specified by
"source" nodes and "sink" nodes.  A directed edge is directed "from"
its source node "to" its sink node.  For an undirected edge, the
source node is simply the first node that was specified when the edge
was created, and the sink node is the second node.  This convention
allows undirected edges to later be converted in a consistent manner
to directed edges, if desired.

<p>On creation of an edge, an arbitrary object can be associated with the edge
as the weight of the edge.  We say that an edge is <em>unweighted</em> if it
does not have an assigned weight. It is an error to attempt to access the
weight of an unweighted edge.

<p>Self-loop edges (edges whose source and sink nodes are identical)
are allowed.

@author Shuvra S. Bhattacharyya
@version $Id$
@see ptolemy.graph.Node
*/
public final class Edge {

    /** Construct an unweighted edge with a specified source node and sink node.
     *  @param source The source node.
     *  @param sink The sink node.
     */
    public Edge(Node source, Node sink) {
        _source = source;
        _sink = sink;
        _weight = null;
    }

    /** Construct an edge with a specified source node, sink node, and
     *  edge weight.
     *  @param source The source node.
     *  @param sink The sink node.
     *  @param weight The edge weight.
     *  @exception IllegalArgumentException If the specified weight is
     *  <code>null</code>.
     */
    public Edge(Node source, Node sink, Object weight) {
        this(source, sink);
        if (weight == null) {
            throw new IllegalArgumentException("Attempt to assign a null "
                    + "weight to an edge.\nThe source node: " + source
                    + ".\nThe sink node: " + sink + ".\n");
        } else {
            _weight = weight;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this is a weighted edge.
     *  @return True if this is a weighted edge.
     */
    public boolean hasWeight() {
        return _weight != null;
    }

    /** Return true if this is a self-loop edge.
     *  @return True if this is a self-loop edge.
     */
    public boolean isSelfLoop() {
        return _source == _sink;
    }

    /** Return the sink node of the edge.
     *  @return The sink node.
     */
    public Node sink() {
        return _sink;
    }

    /** Return the source node of the edge.
     *  @return The source node.
     */
    public Node source() {
        return _source;
    }

    /** Return a string representation of the edge. The string
     *  representation is of the form
     *
     *  <p> <code>(source, sink, weight)</code>,
     *
     *  <p> where <code>source</code>, <code>sink</code>, and
     *  <code>weight</code>
     *
     *  are string representations
     *  of the source node, sink node, and edge weight, respectively.
     *  If the edge is unweighted or the <code>showWeight</code> argument is
     *  false, then the string representation is
     *  is simply
     *
     *  <p> <code>(source, sink)</code>.
     *
     *  @param showWeight True to include a string representation of the edge
     *  weight in the string representation of the edge.
     *  @return A string representation of the edge.
     */
    public String toString(boolean showWeight) {
        String result = new String("(" + _source + ", " + _sink);
        if (showWeight && hasWeight()) {
            result += ", " + _weight;
        }
        result += ")";
        return result;
    }

    /** Return a string representation of the edge. Include information
     *  about the edge weight.
     *  @see #toString(boolean)
     *  @return A string representation of the edge.
     */
    public String toString() {
        return toString(true);
    }

    /** Return the weight of the edge.
     *  @return The edge weight.
     *  @exception IllegalStateException If this is an unweighted node.
     */
    public Object weight() {
        if (!hasWeight()) {
            throw new IllegalStateException("Attempt to access the weight "
                    + "of an unweighted edge. The edge: " + this + ".\n");
        } else {
            return _weight;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    // The sink node of the edge.
    private Node _sink;

    // The source node of the edge.
    private Node _source;

    // The weight of the edge
    private Object _weight;
}

