/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package piano;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sound.midi.MidiUnavailableException;

import midi.Midi;
import music.MusicMachine;
import music.NoteEvent;
import music.Pitch;
/**
 *
 * @author 170153k
 */
public class PianoMachine implements MusicMachine{
    
    private boolean isRecording = false;
    private List<NoteEvent> recording, lastRecording;
    private Set<Pitch> pitchesPlaying;
    private final PianoPlayer player;
    private Midi midi;
    public int time=(int)System.currentTimeMillis();

    public PianoMachine(PianoPlayer player) {
        this.player = player;
        try {
			this.midi = new Midi();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			this.midi=null;
			e.printStackTrace();
		}
        lastRecording = new ArrayList<NoteEvent>();
        pitchesPlaying = new HashSet<Pitch>();
    }
    
    public void beginNote(NoteEvent event){
        Pitch pitch = event.getPitch();
        
        if (pitchesPlaying.contains(pitch)) return;
        pitchesPlaying.add(pitch);
        time=(int)System.currentTimeMillis();
        midi.beginNote(pitch.toMidiFrequency());
        if (isRecording) recording.add(event);
        
    }
    
    public void toggleRecording(){
        if (isRecording){
            lastRecording = recording;
        }
        else{
            recording = new ArrayList<NoteEvent>();
        }
        isRecording = !isRecording;
    }
    
    public void requestPlayback(){    	
        player.playbackRecording(lastRecording);
    }

    public void endNote(NoteEvent event) {
    	Pitch pitch = event.getPitch();
        
        pitchesPlaying.remove(pitch);
        time=(int)System.currentTimeMillis();
        midi.endNote(pitch.toMidiFrequency());
        if (isRecording) recording.add(event);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setNextInstrument() {
    	Midi.customInstrument=Midi.customInstrument.next();
    }
    
    
}
