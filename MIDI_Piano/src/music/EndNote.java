package music;

public class EndNote extends NoteEvent{
	
	public EndNote(Pitch pitch) {
		super(pitch);
	}
    
    public EndNote(Pitch pitch, int delay) {
		super(pitch,delay);
	}
    

	public void execute(MusicMachine m){
        m.endNote(this);
    }
    
    public EndNote delayed (int delay){
        return new EndNote(pitch, delay);
    }

	

}
