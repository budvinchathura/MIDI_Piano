/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

/**
 *
 * @author 170153k
 */
public class BeginNote extends NoteEvent{


    public BeginNote(Pitch pitch) {
		super(pitch);
	}
    
    public BeginNote(Pitch pitch, int delay) {
		super(pitch,delay);
	}
    

	public void execute(MusicMachine m){
        m.beginNote(this);
    }
    
    public BeginNote delayed (int delay){
        return new BeginNote(pitch, delay);
    }

    
}
