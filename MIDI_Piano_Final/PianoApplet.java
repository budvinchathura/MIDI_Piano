//Index No. 170153K
//AppletDemo.html is for running the Applet

import java.awt.Color;
import java.awt.event.*;
import java.applet.*;
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sound.midi.MidiUnavailableException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


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
					if (getBackground() == Color.green) {
						setBackground(Color.red);
					} else {
						setBackground(Color.green);
					}
					player.toggleRecording();
					return;
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case '0':
				case '-':
				case '=':
					NoteEvent ne = new BeginNote(keyToPitch(key),
							(int) System.currentTimeMillis() - player.getMachine().time);
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

				if (key == '1' || key == '2' || key == '3' || key == '4' || key == '5' || key == '6' || key == '7'
						|| key == '8' || key == '9' || key == '0' || key == '-' || key == '=') {
					NoteEvent ne = new EndNote(keyToPitch(key),
							(int) System.currentTimeMillis() - player.getMachine().time);
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

class PianoMachine implements MusicMachine {

	private boolean isRecording = false;
	private List<NoteEvent> recording, lastRecording;
	private Set<Pitch> pitchesPlaying;
	private final PianoPlayer player;
	private Midi midi;
	public int time = (int) System.currentTimeMillis();			//time variable is used to record the time intervals (delays)
																//between note events

	public PianoMachine(PianoPlayer player) {
		this.player = player;
		try {
			this.midi = new Midi();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			this.midi = null;
			e.printStackTrace();
		}
		lastRecording = new ArrayList<NoteEvent>();
		pitchesPlaying = new HashSet<Pitch>();
	}

	public void beginNote(NoteEvent event) {
		Pitch pitch = event.getPitch();

		if (pitchesPlaying.contains(pitch))
			return;
		pitchesPlaying.add(pitch);
		time = (int) System.currentTimeMillis();
		midi.beginNote(pitch.toMidiFrequency());
		if (isRecording)
			recording.add(event);
	}

	public void toggleRecording() {
		if (isRecording) {
			lastRecording = recording;
		} else {
			recording = new ArrayList<NoteEvent>();
		}
		isRecording = !isRecording;
	}

	public void requestPlayback() {
		player.playbackRecording(lastRecording);
	}

	public void endNote(NoteEvent event) {
		Pitch pitch = event.getPitch();

		pitchesPlaying.remove(pitch);
		time = (int) System.currentTimeMillis();
		midi.endNote(pitch.toMidiFrequency());
		if (isRecording)
			recording.add(event);
	}

	public void setNextInstrument() {
		Midi.customInstrument = Midi.customInstrument.next();
	}

}

class PianoPlayer {
	private final BlockingQueue<NoteEvent> queue, delayQueue;
	private final PianoMachine machine;
	private Thread processQueueThread, processDelayQueueThread;

	public PianoPlayer() {

		queue = new LinkedBlockingQueue<NoteEvent>();
		delayQueue = new LinkedBlockingQueue<NoteEvent>();
		machine = new PianoMachine(this);

		processQueueThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("ProcessQueueThread started!");
				processQueue();
			}
		});

		processDelayQueueThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("ProcessDelayQueueThread started!");
				processDelayQueue();
			}
		});
		
		//starting threads
		processQueueThread.start();
		processDelayQueueThread.start();

	}

	public void request(NoteEvent e) {
		try {
			queue.put(e);
		} catch (InterruptedException e1) {

			e1.printStackTrace();
		}
	}

	public void requestPlayback() {
		machine.requestPlayback();
	}

	public void toggleRecording() {
		machine.toggleRecording();
	}

	public void playbackRecording(List<NoteEvent> recording) {
		
		//Pressing 'P' twice does not repeat the playback twice
		//this conditions checks the delayQueue and playbacks only it is empty
		
		if (this.delayQueue.isEmpty()) {
			for (NoteEvent e : recording) {
				try {
					delayQueue.put(e);
				} catch (InterruptedException e1) {

					e1.printStackTrace();
				}
			}
		}
		return;

	}

	public void nextInstrument() {
		this.machine.setNextInstrument();

	}

	public void processQueue() {
		while (true) {
			NoteEvent e;
			try {
				e = queue.take();
				e.execute(machine);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		}
	}

	public void processDelayQueue() {
		while (true) {
			try {
				NoteEvent e = delayQueue.take();
				Midi.wait(e.getDelay());
				queue.put(e);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

	}

	public PianoMachine getMachine() {
		return this.machine;
	}

}

class BeginNote extends NoteEvent {

	public BeginNote(Pitch pitch) {
		super(pitch);
	}

	public BeginNote(Pitch pitch, int delay) {
		super(pitch, delay);
	}

	public void execute(MusicMachine m) {
		m.beginNote(this);
	}

	public BeginNote delayed(int delay) {
		return new BeginNote(pitch, delay);
	}

}

class EndNote extends NoteEvent {

	public EndNote(Pitch pitch) {
		super(pitch);
	}

	public EndNote(Pitch pitch, int delay) {
		super(pitch, delay);
	}

	public void execute(MusicMachine m) {
		m.endNote(this);
	}

	public EndNote delayed(int delay) {
		return new EndNote(pitch, delay);
	}

}

interface MusicMachine {

	public void beginNote(NoteEvent event);

	public void endNote(NoteEvent event);

}

abstract class NoteEvent {

	protected final Pitch pitch;
	protected final int delay;

	public NoteEvent(Pitch pitch) {
		this(pitch, 0);
	}

	public NoteEvent(Pitch pitch, int delay) {
		this.pitch = pitch;
		this.delay = delay;
	}

	public Pitch getPitch() {
		return this.pitch;
	}

	public int getDelay() {
		return this.delay;
	}

	abstract public NoteEvent delayed(int delay);

	abstract public void execute(MusicMachine m);

}

/**
 * Pitch represents the frequency of a musical note. Standard music notation
 * represents pitches by letters: A, B, C, ..., G. Pitches can be sharp or flat,
 * or whole octaves up or down from these primitive generators. For example: new
 * Pitch('C') makes middle C. new Pitch('C').transpose(1) makes C-sharp. new
 * Pitch('E').transpose(-1) makes E-flat. new Pitch('C').transpose(OCTAVE) makes
 * high C. new Pitch('C').transpose(-OCTAVE) makes low C.
 */
class Pitch {
	private final int value;
	// Rep invariant: true.
	// Abstraction function AF(value):
	// AF(0),...,AF(12) map to middle C, C-sharp, D, ..., A, A-sharp, B.
	// AF(i+12n) maps to n octaves above middle AF(i)
	// AF(i-12n) maps to n octaves below middle AF(i)

	private static final int[] scale = { /* A */ 9,
			/* B */ 11,
			/* C */ 0,
			/* D */ 2,
			/* E */ 4,
			/* F */ 5,
			/* G */ 7, };

	// middle C in the Pitch data type, used to
	// map pitches to Midi frequency numbers.
	private final static Pitch C = new Pitch('C');

	public Pitch(int value) {
		this.value = value;
	}

	/**
	 * Make a Pitch.
	 * 
	 * @requires c in {'A',...,'G'}
	 * @returns Pitch named c in the middle octave of the piano keyboard. For
	 *          example, new Pitch('C') constructs middle C
	 */
	public Pitch(char c) {
		try {
			value = scale[c - 'A'];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(c + " must be in the range A-G");
		}
	}

	/**
	 * Number of pitches in an octave.
	 */
	public static final int OCTAVE = 12;

	/**
	 * @return pitch made by transposing this pitch by semitonesUp semitones. For
	 *         example, middle C transposed by 12 semitones is high C; E transposed
	 *         by -1 semitones is E flat.
	 */
	public Pitch transpose(int semitonesUp) {
		return new Pitch(value + semitonesUp);
	}

	/**
	 * @return number of semitones between this and that; i.e., n such that
	 *         that.transpose(n).equals(this).
	 */
	public int difference(Pitch that) {
		return this.value - that.value;
	}

	/**
	 * @return true iff this pitch is lower than that pitch
	 */
	public boolean lessThan(Pitch that) {
		return this.difference(that) < 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		Pitch that = (Pitch) obj;
		return this.value == that.value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	public int toMidiFrequency() {
		return difference(C) + 60;
	}

	/**
	 * @return this pitch in abc music notation (see
	 *         http://www.walshaw.plus.com/abc/examples/)
	 */
	@Override
	public String toString() {
		String suffix = "";
		int v = value;

		while (v < 0) {
			suffix += ",";
			v += 12;
		}

		while (v >= 12) {
			suffix += "'";
			v -= 12;
		}

		return valToString[v] + suffix;
	}

	private static final String[] valToString = { "C", "^C", "D", "^D", "E", "F", "^F", "G", "G^", "A", "^A", "B" };

}

/**
 * Instrument represents a musical instrument.
 * 
 * These instruments are the 128 standard General MIDI Level 1 instruments. See
 * http://www.midi.org/about-midi/gm/gm1sound.shtml.
 */
enum Instrument {
	// Order is important in this enumeration,
	// because an instrument's position must
	// corresponds to its MIDI program number.
	PIANO,
    BRIGHT_PIANO,
    ELECTRIC_GRAND,
    HONKY_TONK_PIANO,
    ELECTRIC_PIANO_1,
    ELECTRIC_PIANO_2,
    HARPSICHORD,
    CLAVINET,

    CELESTA,
    GLOCKENSPIEL,
    MUSIC_BOX,
    VIBRAPHONE,
    MARIMBA,
    XYLOPHONE,
    TUBULAR_BELL,
    DULCIMER,

    HAMMOND_ORGAN,
    PERC_ORGAN,
    ROCK_ORGAN,
    CHURCH_ORGAN,
    REED_ORGAN,
    ACCORDION,
    HARMONICA,
    TANGO_ACCORDION,

    NYLON_STR_GUITAR,
    STEEL_STRING_GUITAR,
    JAZZ_ELECTRIC_GTR,
    CLEAN_GUITAR,
    MUTED_GUITAR,
    OVERDRIVE_GUITAR,
    DISTORTION_GUITAR,
    GUITAR_HARMONICS,

    ACOUSTIC_BASS,
    FINGERED_BASS,
    PICKED_BASS,
    FRETLESS_BASS,
    SLAP_BASS_1,
    SLAP_BASS_2,
    SYN_BASS_1,
    SYN_BASS_2,

    VIOLIN,
    VIOLA,
    CELLO,
    CONTRABASS,
    TREMOLO_STRINGS,
    PIZZICATO_STRINGS,
    ORCHESTRAL_HARP,
    TIMPANI,

    ENSEMBLE_STRINGS,
    SLOW_STRINGS,
    SYNTH_STRINGS_1,
    SYNTH_STRINGS_2,
    CHOIR_AAHS,
    VOICE_OOHS,
    SYN_CHOIR,
    ORCHESTRA_HIT,

    TRUMPET,
    TROMBONE,
    TUBA,
    MUTED_TRUMPET,
    FRENCH_HORN,
    BRASS_ENSEMBLE,
    SYN_BRASS_1,
    SYN_BRASS_2,

    SOPRANO_SAX,
    ALTO_SAX,
    TENOR_SAX,
    BARITONE_SAX,
    OBOE,
    ENGLISH_HORN,
    BASSOON,
    CLARINET,

    PICCOLO,
    FLUTE,
    RECORDER,
    PAN_FLUTE,
    BOTTLE_BLOW,
    SHAKUHACHI,
    WHISTLE,
    OCARINA,

    SYN_SQUARE_WAVE,
    SYN_SAW_WAVE,
    SYN_CALLIOPE,
    SYN_CHIFF,
    SYN_CHARANG,
    SYN_VOICE,
    SYN_FIFTHS_SAW,
    SYN_BRASS_AND_LEAD,

    FANTASIA,
    WARM_PAD,
    POLYSYNTH,
    SPACE_VOX,
    BOWED_GLASS,
    METAL_PAD,
    HALO_PAD,
    SWEEP_PAD,

    ICE_RAIN,
    SOUNDTRACK,
    CRYSTAL,
    ATMOSPHERE,
    BRIGHTNESS,
    GOBLINS,
    ECHO_DROPS,
    SCI_FI,

    SITAR,
    BANJO,
    SHAMISEN,
    KOTO,
    KALIMBA,
    BAG_PIPE,
    FIDDLE,
    SHANAI,

    TINKLE_BELL,
    AGOGO,
    STEEL_DRUMS,
    WOODBLOCK,
    TAIKO_DRUM,
    MELODIC_TOM,
    SYN_DRUM,
    REVERSE_CYMBAL,

    GUITAR_FRET_NOISE,
    BREATH_NOISE,
    SEASHORE,
    BIRD,
    TELEPHONE,
    HELICOPTER,
    APPLAUSE,
    GUNSHOT;

	/**
	 * @return the next instrument in the standard ordering (or the first in the
	 *         ordering if this is the last)
	 */
	public Instrument next() {
		for (Instrument i : Instrument.values()) {
			if (i.ordinal() == (ordinal() + 1) % 128)
				return i;
		}
		return this;
	}
}

/**
 * Midi represents a MIDI synthesis device.
 */
class Midi {
	private Synthesizer synthesizer;

	public final static Instrument DEFAULT_INSTRUMENT = Instrument.PIANO;
	public static Instrument customInstrument = Instrument.PIANO;

	// active MIDI channels, assigned to instruments
	private final Map<Instrument, MidiChannel> channels = new HashMap<Instrument, MidiChannel>();

	// next available channel number (unassigned to an instrument yet)
	private int nextChannel = 0;

	// volume -- a percentage?
	private static final int VELOCITY = 100;

	private void checkRep() {
		assert synthesizer != null;
		assert channels != null;
		assert nextChannel >= 0;
	}

	/**
	 * Make a Midi.
	 * 
	 * @throws MidiUnavailableException if MIDI is not available
	 */
	public Midi() throws MidiUnavailableException {
		synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
		synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
		checkRep();
	}

	/**
	 * Play a note on the Midi scale for a duration in milliseconds using a
	 * specified instrument.
	 * 
	 * @requires 0 <= note < 256, duration >= 0, instr != null
	 */
	public void play(int note, int duration, Instrument instr) {
		MidiChannel channel = getChannel(instr);
		synchronized (channel) {
			channel.noteOn(note, VELOCITY);
		}
		wait(duration);
		synchronized (channel) {
			channel.noteOff(note);
		}
	}

	/**
	 * Start playing a note on the Midi scale using a specified instrument.
	 * 
	 * @requires 0 <= note < 256, instr != null
	 */
	public void beginNote(int note, Instrument instr) {
		MidiChannel channel = getChannel(instr);
		synchronized (channel) {
			channel.noteOn(note, VELOCITY);
		}
	}

	public void beginNote(int note) {
		beginNote(note, customInstrument);
	}

	/**
	 * Stop playing a note on the Midi scale using a specified instrument.
	 * 
	 * @requires 0 <= note < 256, instr != null
	 */
	public void endNote(int note, Instrument instr) {
		MidiChannel channel = getChannel(instr);
		synchronized (channel) {
			channel.noteOff(note, VELOCITY);
		}
	}

	public void endNote(int note) {
		endNote(note, customInstrument);
	}

	/**
	 * Wait for a duration in milliseconds.
	 * 
	 * @requires duration >= 0
	 */
	public static void wait(int duration) {
		long now = System.currentTimeMillis();
		long end = now + duration;
		while (now < end) {
			try {
				Thread.sleep((int) (end - now));
			} catch (InterruptedException e) {
			}
			now = System.currentTimeMillis();
		}
	}

	private MidiChannel getChannel(Instrument instr) {
		synchronized (channels) {
			// check whether this instrument already has a channel
			MidiChannel channel = channels.get(instr);
			if (channel != null)
				return channel;

			channel = allocateChannel();
			patchInstrumentIntoChannel(channel, instr);
			channels.put(instr, channel);
			checkRep();
			return channel;
		}
	}

	private MidiChannel allocateChannel() {
		MidiChannel[] channels = synthesizer.getChannels();
		if (nextChannel >= channels.length)
			throw new RuntimeException("tried to use too many instruments: limited to " + channels.length);
		MidiChannel channel = channels[nextChannel];
		// quick hack by DNJ to allow more instruments to be used
		nextChannel = (nextChannel + 1) % channels.length;
		return channel;
	}

	private void patchInstrumentIntoChannel(MidiChannel channel, Instrument instr) {
		channel.programChange(0, instr.ordinal());
	}

	/**
	 * Discover all the instruments in your MIDI synthesizer and print them to
	 * standard output, along with their bank and program codes.
	 */
	public static void main(String[] args) throws MidiUnavailableException {
		Midi m = new Midi();
		for (javax.sound.midi.Instrument i : m.synthesizer.getLoadedInstruments()) {
			System.out.println(i.getName() + " " + i.getPatch().getBank() + " " + i.getPatch().getProgram());
		}
	}
}
