package com.company;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.*;


public class Main {

    public static void main(String[] args) {
    try {

        AudioFormat audioFormat = new AudioFormat(16000f, 16, 1, true, false);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open();
        info = new DataLine.Info(TargetDataLine.class,audioFormat);
        TargetDataLine targetDataLine = (TargetDataLine)AudioSystem.getLine(info);
        targetDataLine.open();


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Thread source = new Thread(() -> {
            sourceDataLine.start();
            sourceDataLine.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
        });

        

            Thread t = new Thread(){

                @Override
                public void run() {
                    super.run();
                }
            };

            Thread target = new Thread(() -> {
                System.out.println("target start");

                targetDataLine.start();
                byte[] data = new byte[targetDataLine.getBufferSize()];
                float[] samples = new float[data.length/2];
                int readBytes;
                float rms = 0f;
                long start = 0;
                long finish = 0;
                boolean flag = false;
                while((finish - start) < 2000){
                    readBytes = targetDataLine.read(data, 0, data.length);

                    for(int i = 0, s = 0; i < readBytes;) {
                        int sample = 0;

                        sample |= data[i++] & 0xFF; // (reverse these two lines
                        sample |= data[i++] << 8;   //  if the format is big endian)

                        // normalize to range of +/-1.0f
                        samples[s++] = sample / 32768f;
                    }
                    for(float sample : samples) {

                        rms += sample * sample;
                    }
                    rms = (float)Math.sqrt(rms / samples.length);

                    System.out.println(rms);

                    if (rms > 0.15){
                        start = System.currentTimeMillis();
                        flag = true;
                    }else {
                        if(flag)
                        finish = System.currentTimeMillis();
                    }

                    if (readBytes > 0) {
                        if (start != 0){
                            byteArrayOutputStream.write(data, 0, readBytes);
                        }
                    }
                }
            });

            target.start();
            System.out.println("Say");
            target.join();
            targetDataLine.stop();
            targetDataLine.close();

            AudioInputStream is = AudioSystem.getAudioInputStream(audioFormat,new AudioInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),audioFormat,byteArrayOutputStream.size()));
            AudioSystem.write(is, AudioFileFormat.Type.WAVE,new File("D:\\Java Project\\project 5\\test.wav"));
        source.start();
        System.out.println("Play");
        source.join();
        sourceDataLine.stop();
        sourceDataLine.close();
    } catch (LineUnavailableException | InterruptedException | IOException e){
        e.printStackTrace();
    }

    }
}
