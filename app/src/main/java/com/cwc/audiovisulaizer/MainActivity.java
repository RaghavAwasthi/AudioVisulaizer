package com.cwc.audiovisulaizer;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionManager permissionManager = new PermissionManager(this, this);
        permissionManager.checkAndAskPermissions(Manifest.permission.RECORD_AUDIO);


    }

    public class input {
        private static final String TAG = "Aufnahme";
        private AudioRecord recorder = null;
        private boolean isRecording = false;
        private int SAMPLERATE = 8000;
        private int CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        private int bufferSize = AudioRecord.getMinBufferSize(SAMPLERATE, CHANNELS,
                AUDIO_FORMAT);
        private Thread recordingThread = null;

        public void startRecording() {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLERATE,
                    CHANNELS, AUDIO_FORMAT, bufferSize);

            recorder.startRecording();
            isRecording = true;

            recordingThread = new Thread(new Runnable() {
                public void run() {
                    writeAudioData();
                }

            });
            recordingThread.start();

        }

        public void stopRecording() {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }

        private void writeAudioData() {

            byte data[] = new byte[bufferSize];

            while (isRecording) {

                recorder.read(data, 0, bufferSize);


            }


        }

    }

    public class FFT {

        int n, m;

        // Lookup tables. Only need to recompute when size of FFT changes.
        double[] cos;
        double[] sin;

        public FFT(int n) {
            this.n = n;
            this.m = (int) (Math.log(n) / Math.log(2));

            // Make sure n is a power of 2
            if (n != (1 << m))
                throw new RuntimeException("FFT length must be power of 2");

            // precompute tables
            cos = new double[n / 2];
            sin = new double[n / 2];

            for (int i = 0; i < n / 2; i++) {

                cos[i] = Math.cos(-2 * Math.PI * i / n);
                sin[i] = Math.sin(-2 * Math.PI * i / n);
            }

        }

        public void fft(double[] x, double[] y) {
            int i, j, k, n1, n2, a;
            double c, s, t1, t2;

            // Bit-reverse
            j = 0;
            n2 = n / 2;
            for (i = 1; i < n - 1; i++) {
                n1 = n2;
                while (j >= n1) {
                    j = j - n1;
                    n1 = n1 / 2;
                }
                j = j + n1;

                if (i < j) {
                    t1 = x[i];
                    x[i] = x[j];
                    x[j] = t1;
                    t1 = y[i];
                    y[i] = y[j];
                    y[j] = t1;
                }
            }

            // FFT
            n1 = 0;
            n2 = 1;

            for (i = 0; i < m; i++) {
                n1 = n2;
                n2 = n2 + n2;
                a = 0;

                for (j = 0; j < n1; j++) {
                    c = cos[a];
                    s = sin[a];
                    a += 1 << (m - i - 1);

                    for (k = j; k < n; k = k + n2) {
                        t1 = c * x[k + n1] - s * y[k + n1];

                        t2 = s * x[k + n1] + c * y[k + n1];
                        x[k + n1] = x[k] - t1;
                        y[k + n1] = y[k] - t2;
                        x[k] = x[k] + t1;
                        y[k] = y[k] + t2;
                    }
                }
            }
        }
    }
}