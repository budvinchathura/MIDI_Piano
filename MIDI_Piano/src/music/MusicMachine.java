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
public interface MusicMachine {
    
    public void beginNote (NoteEvent event);
    public void endNote(NoteEvent event);
    
}
