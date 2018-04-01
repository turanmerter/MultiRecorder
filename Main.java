import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main (String[] args){

        List<Line> inputs = new ArrayList<>();
        List<String> names = new ArrayList<>();

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        try {
            for (Mixer.Info info: mixerInfos){
                Mixer m = AudioSystem.getMixer(info);
                Line.Info[] lineInfos = m.getSourceLineInfo();
                for (Line.Info lineInfo:lineInfos){
                    System.out.println (info.getName()+"---"+lineInfo);
                    Line line = m.getLine(lineInfo);
                    System.out.println("\t-----"+line);
                }
                lineInfos = m.getTargetLineInfo();
                for (Line.Info lineInfo:lineInfos){
                    System.out.println (info.getName() + " " + m+" +++ "+lineInfo);
                    Line line = m.getLine(lineInfo);
                    inputs.add(line);
                    names.add(info.getName());
                    System.out.println("\t++++++++++++++"+line);

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
                Thread t = new Thread(r = new Recorder((TargetDataLine) inputs.get(i), i, time, names.get(i)));
                records.add(r);
                t.start();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.println("sleeping");
        Thread.sleep(6000);
            System.out.println("woke up");
        } catch(Exception e) {
            e.printStackTrace();
        }

        for (Recorder r : records) {
            r.stop();
        }
    }

}

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

        System.out.println("started");
        File wavFile = new File(time + "_" + index + "_" + name + ".wav");

        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        try {
            line.open(format);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("started1");
        line.start();
        AudioInputStream ais = new AudioInputStream(line);
        try {
            System.out.println("started2");
            int br = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
            System.out.println("started3 " + br);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        line.close();

        System.out.println("started4");
    }

    public void stop() {
        line.stop();
        line.close();
        System.out.println("Finished");
    }
}