/**
 * Author: dnj
 * Date: August 29, 2008
 * 6.005 Elements of Software Construction
 * (c) 2008, MIT and Daniel Jackson
 */
package piano;

import java.awt.Color;
import java.awt.event.*;

import java.applet.*;
import music.*;

/**
 * A skeletal applet that shows how to bind methods to key events
 */
public class PianoApplet extends Applet {

	public void init() {

		final PianoPlayer player = new PianoPlayer();
		setBackground(Color.green);

		// this is a standard pattern for associating method calls with GUI events
		// the call to the constructor of KeyAdapter creates an object of an
		// anonymous subclass of KeyAdapter, whose keyPressed method is called
		// when a key is pressed in the GUI

		addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				char key = (char) e.getKeyCode();
//                System.out.println("Pressed");

				switch (key) {

				case 'I':
					player.nextInstrument();
					return;
				case 'P':
					player.requestPlayback();
					return;
				case 'R':
					if (getBackground()==Color.green) {
						setBackground(Color.red);
					}
					else {
						setBackground(Color.green);
					}
					player.toggleRecording();
					return;
				case '1': case '2': case '3': case '4': 
				case '5': case '6': case '7': case '8': 
				case '9': case '0': case '-': case '=':
					NoteEvent ne = new BeginNote(keyToPitch(key), (int) System.currentTimeMillis() - player.getMachine().time);
					player.request(ne);
					return;
				}				
			}

			public void keyReleased(KeyEvent e) {
				char key = (char) e.getKeyCode();
//                System.out.println("Released");

				if (key == 'I' || key == 'P' || key == 'R') {
                	player.getMachine().time = (int) System.currentTimeMillis();
					return;
				}
				
				if (key == '1' || key == '2' || key == '3'||
						key == '4' || key == '5' || key == '6' ||
						key == '7' || key == '8' || key == '9' ||
						key == '0' || key == '-' || key == '=') {
					NoteEvent ne = new EndNote(keyToPitch(key), (int) System.currentTimeMillis() - player.getMachine().time);
					player.request(ne);
					return;
				}
				return;				
			}

		});

	}

	public static Pitch keyToPitch(char key) {
		switch (key) {

		case '1':
			return new Pitch('C');
		case '2':
			return new Pitch('C').transpose(1);
		case '3':
			return new Pitch('D');
		case '4':
			return new Pitch('D').transpose(1);
		case '5':
			return new Pitch('E');
		case '6':
			return new Pitch('F');
		case '7':
			return new Pitch('F').transpose(1);
		case '8':
			return new Pitch('G');
		case '9':
			return new Pitch('G').transpose(1);
		case '0':
			return new Pitch('A');
		case '-':
			return new Pitch('A').transpose(1);
		case '=':
			return new Pitch('B');
		}
		return new Pitch('C');

	}

}
