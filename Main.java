import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/**
 *	Main class for MultiRecorder
 */
public class Main {

	// Record duration in seconds
	private static final int RECORD_TIME = 6;

	/**
	 * Main entry point
	 */
	public static void main(String[] args) {

		List<LineNamePair> inputs = new ArrayList<>();

		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		try {
			// Collect all recording lines
			for (Mixer.Info info : mixerInfos) {
				Mixer m = AudioSystem.getMixer(info);
				Line.Info[] lineInfos = m.getSourceLineInfo();
				lineInfos = m.getTargetLineInfo();
				for (Line.Info lineInfo : lineInfos) {
					Line line = m.getLine(lineInfo);
					inputs.add(new LineNamePair(line, info.getName()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		long time = System.currentTimeMillis();
		List<Recorder> records = new ArrayList<>();
		for (int i = 0; i < inputs.size(); i++) {
			try {
				Recorder r;
				// Start record from all lines in separate threads
				Thread t = new Thread(
						r = new Recorder((TargetDataLine) inputs.get(i).line, i, time, inputs.get(i).name));
				records.add(r);
				t.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Wait 'RECORD_TIME' amount before closing lines
		try {
			Thread.sleep(RECORD_TIME * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Close all lines
		for (Recorder r : records) {
			r.stop();
		}
	}

}

/**
 *	Data holder for lines
 */
class LineNamePair {
	Line line;
	String name;

	public LineNamePair(Line line, String name) {
		this.line = line;
		this.name = name;
	}
}

/**
 *	Recorder class
 */
class Recorder implements Runnable {

	int index;
	TargetDataLine line;
	long time;
	String name;

	public Recorder(TargetDataLine line, int i, long time, String name) {
		this.line = line;
		this.index = i;
		this.time = time;
		this.name = name;
	}

	@Override
	public void run() {
		File wavFile = new File(time + "_" + index + "_" + name + ".wav");

		AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
		try {
			line.open(format);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		line.start();
		AudioInputStream ais = new AudioInputStream(line);
		try {
			// Record till line closes
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		line.close();
	}

	public void stop() {
		line.stop();
		line.close();
	}
}
