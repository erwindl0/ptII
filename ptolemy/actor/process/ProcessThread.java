/* Thread class for process oriented domains.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// ProcessThread
/**
Thread class acting as a process for process oriented domains.
<P>
In process oriented domains, each actor acts as a separate process and 
its execution is not centrally controlled by the director. Each process
runs concurrently with other processes and is responsible for calling
its execution methods.
<P>
This class provides the mechanism to implement the above. 
An instance of this class can be created by passing an actor as an 
argument to the constructor. This class runs as a separate thread on
being started and calls the execution methods on the actor, repeatedly.
In specific, it calls the prefire(), fire() and postfire() methods
of the actor. Before termination, this calls the wrapup() method of 
the actor.
<P>
If an actor returns false in its prefire() or postfire() methods, the
actor is never fired again and the thread or process would terminate 
after calling wrapup() on the actor.
<P>
The initialize() method of the actor is not called from this class. It 
should be called before starting this thread. 
<P>
In process oriented domains, the director needs to keep a count of the 
number of active processes in the system. This is used for detection of
deadlocks, termination, and possibly some other reasons. For this two
methods _increaseActiveCount() and _decreaseActiveCount() are defined 
in the ProcessDirector. _increaseActiveCount() is called on the director
from the constructor of this class and the _decreaseActiveCount() method
is called at the end of the run() method, i.e. before the thread terminates.
<P>

@author Mudit Goel, Neil Smyth
@version $Id$
*/
public class ProcessThread extends PtolemyThread {

    /** Construct a thread to be used for the execution of the
     *  iteration methods of the actor. This increases the count of active
     *  actors in the director.
     *  @param actor The actor that needs to be executed.
     *  @param director The director responsible for the execution of this
     *  actor.
     */
    public ProcessThread(Actor actor, ProcessDirector director) {
        super();
	_actor = actor;
        _director = director;
        _manager = ((CompositeActor)((NamedObj)actor).getContainer()).getManager();

	//This method is called here and not in the run() method as the 
	//count should be incremented before any thread is started 
	//or made active. This is because the second started thread might
	//block on a read or write to this process and increment the block
	//count even before this thread has incremented the active count.
	//This results in false deadlocks.
	_director._increaseActiveCount();
    }

    /** Construct a thread to be used for the execution of the
     *  iteration methods of the actor.
     *  @param actor The actor that needs to be executed.
     *  @param director The director responsible for the execution of this
     *  actor.
     *  @param name The name of the thread.
     *  @deprecated Please use the first constructor alone.
     */
    public ProcessThread(Actor actor, ProcessDirector director, String name) {
        super(name);
	_actor = actor;
        _director = director;
        _manager = ((CompositeActor)((NamedObj)actor).getContainer()).getManager();
        _director.increaseActiveCount();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor being executed by this thread
     *  @return The actor being executed by this thread.
     */
    public Actor getActor() {
	return _actor;
    }

    /** Initialize the actor, iterate it through the execution cycle
     *  till it terminates. At the end of the termination, calls wrapup 
     *  on the actor.
     */
    public void run() {
	try {
            boolean iterate = true;
	    while (iterate) {
                iterate = false;
                // container is checked for null to detect the
                // deletion of the actor from the topology.
                if ( ((Entity)_actor).getContainer() != null ) {
                    if (_actor.prefire()){
			_actor.fire();
			iterate =  _actor.postfire();
		    }
		}
            }
        } catch (TerminateProcessException t) {
            // Process was terminated.
        } catch (IllegalActionException e) {
            _manager.fireExecutionError(e);
        } finally {
            try {
                _actor.wrapup();
            } catch (IllegalActionException e) {
                _manager.fireExecutionError(e);
            }
            _director._decreaseActiveCount();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Actor _actor;
    private ProcessDirector _director;
    private Manager _manager;
}


















































































