# Tests for the Director class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#


######################################################################
####
#
test ProcessThread-2.1 {Constructor tests} {
    set manager [java::new ptolemy.actor.Manager manager]
    set e0 [java::new ptolemy.actor.CompositeActor]
    set d1 [java::new ptolemy.actor.ProcessDirector director]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d1
    set a1 [java::new ptolemy.actor.test.TestProcessActor $e0 A1]
    set a2 [java::new ptolemy.actor.test.TestProcessActor $e0 A2]


    set p1 [java::new ptolemy.actor.ProcessThread $a1 $d1]
    $p1 setName P1
    set p2 [java::new ptolemy.actor.ProcessThread $a2 $d1 P2]
    list [$p1 getName] [$p2 getName] 
} {P1 P2}


######################################################################
####
#
test ProcessThread-5.1 {Test action methods} {
    set manager [java::new ptolemy.actor.Manager manager]
    set e0 [java::new ptolemy.actor.CompositeActor]
    set d1 [java::new ptolemy.actor.ProcessDirector director]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d1
    set a1 [java::new ptolemy.actor.test.TestProcessActor $e0 A1]
    $a1 clear
    $manager run
    $a1 getRecord
    
} {.E0.A1.initialize
.E0.A1.prefire
.E0.A1.fire
.E0.A1.postfire
.E0.A1.wrapup
}






