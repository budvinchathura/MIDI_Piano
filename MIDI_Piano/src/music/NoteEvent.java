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
public abstract class NoteEvent {

    protected final Pitch pitch;
    protected final int delay;

    public NoteEvent(Pitch pitch) {
        this(pitch,0);
    }
    public NoteEvent(Pitch pitch, int delay) {
        this.pitch = pitch;
        this.delay = delay;
    }
    
    public Pitch getPitch(){
        return this.pitch;
    }
    
    public int getDelay() {
    	return this.delay;
    }
    
    abstract public NoteEvent delayed(int delay);
    abstract public void execute (MusicMachine m);
      
    
}
