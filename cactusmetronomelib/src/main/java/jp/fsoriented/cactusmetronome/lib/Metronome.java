package jp.fsoriented.cactusmetronome.lib;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * メトロノームクラス.
 *
 * クリックパターンと、再生する時間（サンプル）を与えると、音を鳴らす。また、Clickリストのratioを常時更新する。
 */
public class Metronome {

    private static final String TAG = "metronome";

    /**
     * 再生スレッド
     */
    private AudioThread mAudioThread;
    /**
     * 計測スレッド
     */
    private MeasureThread mMeasureThread;
    /**
     * クリックパターン
     */
    private Click[] mClicks;
    /**
     * パターンの長さ（サンプル数）
     */
    private int mPatternLength;

    /**
     * サンプリング周波数. 対応しているのは44.1kHzのみ。
     */
    public static final int FREQUENCY = 44100;

    /**
     * メトロノームの再生を開始する.
     *
     * 事前に{@code }setPattern}を呼び出して、即座に再生が始まる。そうでなければ、{@code setPattern}で再生が始まる。
     */
    public void start() {
        if (mAudioThread == null) {
            mAudioThread = new AudioThread();
            mMeasureThread = new MeasureThread();
            if (mClicks != null) {
                short[] pattern = Click.compile(mClicks, FREQUENCY, mPatternLength);
                mAudioThread.setPattern(pattern);
                mMeasureThread.reset(mClicks, mPatternLength);
            }
            mAudioThread.start();
            mMeasureThread.start();
        }
    }

    /**
     * メトロノームの再生を終了する。
     */
    public void finish() {
        if (mAudioThread != null) {
            mAudioThread.notifyFinish();
            mAudioThread = null;
            mMeasureThread.notifyFinish();
            mMeasureThread = null;
        }
        mClicks = null;
        mPatternLength = 0;
    }

    /**
     * メトロノームのクリックパターンを設定する.
     *
     * 再生中であれば、即座にパターンが再生される。まだ再生前であれば、{@code start}が呼ばれるまで設定を保持する。
     *
     * @param clickList クリックパターン
     * @param length クリック音の長さ（サンプル）
     */
    public void setPattern(Click[] clickList, int length) {
        if (clickList == null) {
            mClicks = null;
            mPatternLength = 0;
            if (mAudioThread != null) {
                mAudioThread.setPattern(null);
            }
            return;
        }
        mClicks = Arrays.copyOf(clickList, clickList.length);
        mPatternLength = length;
        if (mAudioThread != null) {
            short[] pattern = Click.compile(clickList, FREQUENCY, length);
            mAudioThread.setPattern(pattern);
            mMeasureThread.reset(mClicks, length);
        }
    }

    /**
     * メトロノームのクリックパターンを設定する.
     *
     * 再生中であれば、即座にパターン再生される。まだ再生前であれば、{@code start}が呼ばれるまで設定を保持する。
     *
     * @param clickList クリックパターン
     * @param length クリック音の長さ（サンプル）
     */
    public void setPattern(ArrayList<Click> clickList, int length) {
        if (clickList == null) {
            mClicks = null;
            mPatternLength = 0;
            if (mAudioThread != null) {
                mAudioThread.setPattern(null);
            }
            return;
        }
        mClicks = clickList.toArray(new Click[clickList.size()]);
        mPatternLength = length;
        if (mAudioThread != null) {
            short[] pattern = Click.compile(mClicks, FREQUENCY, length);
            mAudioThread.setPattern(pattern);
            mMeasureThread.reset(mClicks, length);
        }
    }

    /**
     * 指定されたファイルに、クリックパターン（波形、44.1kHz 16bit PCM）を保存する。
     *
     * @param file 保存先のファイルパス。
     * @param clickList クリックパターン
     * @param samplesPerPattern クリックパターン１回分の長さ（サンプル数）
     * @param repeats クリックパターンを繰り返す回数. 例えばクリックパターンが１小節で、32小節分のクリック音を保存する場合は、32を指定する。
     */
    public void saveAsWavFile(File file, ArrayList<Click> clickList, int samplesPerPattern, int repeats) {
        // 指定されたファイルに、クリック音のファイルを保存する。
        short[] pattern = Click.compile(clickList.toArray(new Click[clickList.size()]), FREQUENCY, samplesPerPattern);
        ByteBuffer patternBuf = ByteBuffer.allocate(2 * pattern.length).order(ByteOrder.LITTLE_ENDIAN);
        for (short sample : pattern) {
            patternBuf.putShort(sample);
        }
        byte[] patternData = patternBuf.array();
        // 保存
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            WaveHeader header = new WaveHeader(patternData.length * repeats);
            bos.write(header.getRiffHeader());
            bos.write(header.getWaveHeader());
            for (int i=0; i<repeats; i++) {
                bos.write(patternData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 16bitモノラルWAVファイルのヘッダを生成する
     */
    private static class WaveHeader {
        /** データサイズ */
        private int mSize;

        /**
         * コンストラクタ
         * @param size 波形データのサイズ
         */
        public WaveHeader(int size) {
            mSize = size;
        }

        /**
         * RIFFヘッダを生成する
         * @return RIFFヘッダ
         */
        public byte[] getRiffHeader() {
            ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            buf.put((byte)'R').put((byte)'I').put((byte)'F').put((byte)'F');
            buf.putInt(mSize + 36);
            return buf.array();
        }

        /**
         * Waveファイルのヘッダを生成する
         * @return ヘッダ
         */
        public byte[] getWaveHeader() {
            ByteBuffer buf = ByteBuffer.allocate(36).order(ByteOrder.LITTLE_ENDIAN);
            buf.put((byte)'W').put((byte)'A').put((byte)'V').put((byte)'E');
            buf.put((byte)'f').put((byte)'m').put((byte)'t').put((byte)' ');
            buf.putInt(16);
            buf.putShort((short) 1);
            buf.putShort((short) 1);
            buf.putInt(FREQUENCY);
            buf.putInt(FREQUENCY * 2);
            buf.putShort((short)2);
            buf.putShort((short)16);
            buf.put((byte)'d').put((byte)'a').put((byte)'t').put((byte)'a');
            buf.putInt(mSize);
            return buf.array();
        }
    }

}
