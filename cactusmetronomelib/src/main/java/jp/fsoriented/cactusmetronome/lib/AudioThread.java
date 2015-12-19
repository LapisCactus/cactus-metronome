package jp.fsoriented.cactusmetronome.lib;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * （内部クラス）再生スレッド.
 *
 * クリック音の波形（44.1kHz 16bit）を再生するスレッドである。
 * スレッドを終了するには、{@code notifyFinish}を呼び出す。
 */
class AudioThread extends Thread {

    private static final String LOG_TAG = "metronome";

    /** 再生するクリック音のパターン。 */
    private short[] pattern;
    /** スレッドの終了フラグ */
    private volatile boolean exit;
    /** 再生オブジェクト */
    private AudioTrack audioTrack;

    /**
     * コンストラクタ
     */
    public AudioThread() {
        super();
        exit = false;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, // 音楽再生用のオーディオストリーム
                Metronome.FREQUENCY, // サンプリングレート
                AudioFormat.CHANNEL_OUT_MONO, // モノラル
                AudioFormat.ENCODING_PCM_16BIT, // 16bit PCM
                AudioTrack.getMinBufferSize(Metronome.FREQUENCY, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),// 合計バッファサイズ
                AudioTrack.MODE_STREAM); // ストリームモード
    }

    /**
     * 起動したスレッドで行う処理
     */
    public void run() {
        //Log.i(LOG_TAG, "AudioThread start");
        audioTrack.play();
        while (!exit) {
            if (pattern != null) {
                audioTrack.write(pattern, 0, pattern.length);
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
        audioTrack.release();
        //Log.i(LOG_TAG, "AudioThread finish");
    }

    /**
     * スレッドを終了する
     */
    public void notifyFinish() {
        exit = true;
        if (audioTrack != null) {
            audioTrack.stop();
        }
    }

    /**
     * 0-1でボリュームを指定する
     *
     * @param vol ボリューム[0..1]
     */
    public void setVolume(float vol) {
        if (audioTrack != null) {
            audioTrack.setStereoVolume(vol, vol);
        }
    }

    /**
     * クリックパターンを設定する
     * @param pattern クリック音のパターン（44.1kHz 16bit）
     */
    public void setPattern(short[] pattern) {
        this.pattern = pattern;
    }

    /**
     * 最初から再生する
     */
    public void seekToStart() {
        if (audioTrack != null) {
            audioTrack.flush();
        }
    }
}
