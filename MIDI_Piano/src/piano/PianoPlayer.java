/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package piano;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import music.NoteEvent;

/**
 *
 * @author 170153k
 */
public class PianoPlayer {
	private final BlockingQueue<NoteEvent> queue, delayQueue;
	private final PianoMachine machine;
	private Thread processQueueThread, processDelayQueueThread;

	public PianoPlayer() {

		queue = new LinkedBlockingQueue<NoteEvent>();
		delayQueue = new LinkedBlockingQueue<NoteEvent>();
		machine = new PianoMachine(this);		

		
		processQueueThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("ProcessQueue started!");
				processQueue();
			}
		});

		processDelayQueueThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("ProcessDelayQueue started!");
				processDelayQueue();
			}
		});
		
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
				midi.Midi.wait(e.getDelay());
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
