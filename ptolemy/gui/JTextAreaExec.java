/* A JTextArea that takes a list of commands and runs them.

 Copyright (c) 2001 The Regents of the University of California.
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

package ptolemy.gui;

//import util.testsuite.PrintThreads;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/** Execute commands in a subprocess and display them in a JTextArea.

<p>Loosely based on Example1.java from
http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html

@author Christopher Hylands
@version $Id$
 */
public class JTextAreaExec extends JPanel {

    /** Create the JTextArea, progress bar, status text field and
     *  optionally Start and Cancel buttons.
     *
     *  @param name A String containing the name to label the JTextArea
     *  with.
     *  @param showButtons True if the Start and Cancel buttons should be
     *  made visible.
     */
    public JTextAreaExec(String name, boolean showButtons) {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	_jTextArea = new JTextArea("", 20, 100);
	_jTextArea.setEditable(false);
	JScrollPane scrollPane = new JScrollPane(_jTextArea);
	add(scrollPane);

        setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black),
                      name));

	_progressBar = new JProgressBar();

        _startButton = new JButton("Start");
        _startButton.addActionListener(_startListener);
	_enableStartButton();

        _cancelButton = new JButton("Cancel");
        _cancelButton.addActionListener(_interruptListener);
        _cancelButton.setEnabled(false);

	Border spaceBelow = BorderFactory.createEmptyBorder(0, 0, 5, 0);

	if (showButtons) {
	    JComponent buttonBox = new JPanel();
	    buttonBox.add(_startButton);
	    buttonBox.add(_cancelButton);

	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    add(buttonBox);
	    buttonBox.setBorder(spaceBelow);
	    _statusBar = new JLabel("Click Start to begin", JLabel.CENTER);
	} else {
	    _statusBar = new JLabel("", JLabel.CENTER);
	}

        add(_progressBar);
        add(_statusBar);
        _statusBar.setAlignmentX(CENTER_ALIGNMENT);

        Border progressBarBorder = _progressBar.getBorder();
        _progressBar.setBorder(BorderFactory.createCompoundBorder(
                                        spaceBelow,
                                        progressBarBorder));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the text message to the JTextArea and include a trailing
     *  newline.
     */
    public void appendJTextArea(final String text) {
        Runnable doAppendJTextArea = new Runnable() {
            public void run() {
		// Oddly, we can just use '\n' here,
		// we do not need to call
		// System.getProperties("line.separator")
		_jTextArea.append(text + '\n');
            }
        };
        SwingUtilities.invokeLater(doAppendJTextArea);
    }

    /** Cancel any running commands. */
    public void cancel() {
                _cancelButton.doClick();
    }

    /** Main method used for testing. */
    public static void main(String [] args) {
        JFrame jFrame = new JFrame("JTextAreaExec Example");
        WindowListener windowListener = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
        };
        jFrame.addWindowListener(windowListener);

	List execCommands = new LinkedList();
	execCommands.add("date");
	execCommands.add("sleep 5");
	execCommands.add("date");

        final JTextAreaExec exec =
	    new JTextAreaExec("JTextAreaExec Tester", true);
	exec.setCommands(execCommands);
        jFrame.getContentPane().add(exec);
        jFrame.pack();
        jFrame.show();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
		//We can't do this until now
                exec.getStartButton().requestFocus();
		exec.start();
            }
        });

    }

    /** Return the Start button.
     *  This method is used to get the Start button so we can
     *  set the focus to it.
     */
    public JButton getStartButton() {

        return _startButton;
    }

    /** Set the list of commands. */
    public void setCommands(List commands) {
        _commands = commands;
	_enableStartButton();
    }

    /** Start running the commands. */
    public void start() {
	_startButton.doClick();
    }

    /** Update thet status area with the text message.*/
    public void updateStatusBar(final String text) {
        Runnable doUpdateStatusBar = new Runnable() {
            public void run() {
		_statusBar.setText(text);
            }
        };
        SwingUtilities.invokeLater(doUpdateStatusBar);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Enable the Start button if there are any commands in the list.
    private void _enableStartButton() {
	if (_commands != null && _commands.size() > 0) {
	    _startButton.setEnabled(true);
	} else {
	    _startButton.setEnabled(false);
	}
    }

    // Execute the commands in the list.  Update the JTextArea with
    // the command being run and the output.  Update the progress bar
    // and the status bar.
    private Object _executeCommands() {
        try {
            Runtime runtime = Runtime.getRuntime();
	    try {

                if (_process != null) _process.destroy();
		_progressBar.setMaximum(_commands.size());
		int commandCount = 0;
                Iterator commands = _commands.iterator();
                while(commands.hasNext()) {
		    _updateProgressBar(++commandCount);
		    if (Thread.interrupted()) {
			throw new InterruptedException();
		    }

		    final String command = (String)commands.next();
		    appendJTextArea("Executing: " + command);

		    // Print only the first 50 chars of the command
		    if (command.length() < 50) {
			_statusBar.setText("Executing: " + command);
		    } else {
			_statusBar.setText("Executing: "
					   + command.substring(0,50)
					   + " . . .");
		    }

                    _process = runtime.exec(command);

		    try {
			InputStream processStream = _process.getErrorStream();
			String line;
			BufferedReader reader =
			    new BufferedReader(new
				InputStreamReader(processStream));
			while((line = reader.readLine()) != null) {
			    appendJTextArea(line);
			}
			reader.close();

			processStream = _process.getInputStream();
			reader =
			    new BufferedReader(new
				InputStreamReader(processStream));
			while((line = reader.readLine()) != null) {
			    appendJTextArea(line);
			}
			reader.close();
		    } catch (IOException io) {
			appendJTextArea("IOException: " + io);
		    }
		    try {
			int processReturnCode = _process.waitFor();
			synchronized(this) {
			    _process = null;
                            //_execses.remove(this);
			}
			if (processReturnCode != 0) break;
		    } catch (InterruptedException interrupted) {
			appendJTextArea("InterruptedException: "
					+ interrupted);
			throw interrupted;
	    	    }
                }
		appendJTextArea("All Done.");
	    } catch (final IOException io) {
		appendJTextArea("IOException: " + io);
            }
        }
        catch (InterruptedException e) {
	    _process.destroy();
            _updateProgressBar(0);
            return "Interrupted";  // SwingWorker.get() returns this
        }
        return "All Done";         // or this
    }

    // This action listener, called by the Cancel button, interrupts
    // the _worker thread which is running this._executeCommands().
    // Note that the _executeCommands() method handles
    // InterruptedExceptions cleanly.
    private ActionListener _interruptListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            _cancelButton.setEnabled(false);
	    appendJTextArea("Cancel button was pressed");
            _worker.interrupt();
	    _process.destroy();
	    _enableStartButton();
        }
    };

    // This action listener, called by the Start button, effectively
    // forks the thread that does the work.
    private ActionListener _startListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            _startButton.setEnabled(false);
            _cancelButton.setEnabled(true);
            _statusBar.setText("Working...");

            /* Invoking start() on the SwingWorker causes a new Thread
             * to be created that will call construct(), and then
             * finished().  Note that finished() is called even if
             * the _worker is interrupted because we catch the
             * InterruptedException in _executeCommands().
             */
            _worker = new SwingWorker() {
                public Object construct() {
                    return _executeCommands();
                }
                public void finished() {
		    _enableStartButton();
                    _cancelButton.setEnabled(false);
                    _statusBar.setText(get().toString());
                }
            };
            _worker.start();
        }
    };


    // When the _worker needs to update the GUI we do so by queuing a
    // Runnable for the event dispatching thread with
    // SwingUtilities.invokeLater().  In this case we're just changing
    // the value of the progress bar.
    private void _updateProgressBar(final int i) {
        Runnable doSetProgressBarValue = new Runnable() {
            public void run() {
		//_jTextArea.append(new Integer(i).toString());
                _progressBar.setValue(i);
            }
        };
        SwingUtilities.invokeLater(doSetProgressBarValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Cancel Button.
    private JButton _cancelButton;

    // The list of command to be executed.  Each entry in the list is
    // a String.  It might be better to have each element of the list
    // be an String [] so that the shell can interpret each word in
    // the command.
    private List _commands = null;

    // JTextArea to write the command and the output of the command.
    private JTextArea _jTextArea;

    // The Process that we are running.
    private Process _process;


    // Progress bar where the length of the bar is the total number
    // of commands being run.
    private JProgressBar _progressBar;

    // Label at the bottom that provides feedback as to what is happening.
    private JLabel _statusBar;

    // The Start Button.
    private JButton _startButton;

    // SwingWorker that actually does the work.
    private SwingWorker _worker;
}
