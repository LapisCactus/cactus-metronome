package jp.fsoriented.cactusmetronome.lib;

import android.util.Log;

/**
 * （内部クラス）計測スレッド.
 *
 * 起点からの時間を計測し、クリック音のパターンのどの位置を再生中かを更新する。
 * スレッドを終了するには、{@code notifyFinish}を呼び出す。
 */
class MeasureThread extends Thread {

    private static final String TAG = "metronome";

    /** スレッドの終了フラグ */
    private volatile boolean exit;

    /**
     * パターンの長さ（サンプル）
     */
    private int patternLength;
    /**
     * クリックパターン
     */
    private Click[] clicks;
    /**
     * 開始時点の時刻
     */
    private long start;

    /**
     * コンストラクタ
     */
    public MeasureThread() {
    }

    /**
     * 起動したスレッドで行う処理
     */
    public void run() {
        // Log.i(TAG, "MeasureThread start");
        while (!exit) {
            // 2ms待つ
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
            }
            // ratio 計算
            calcRatio();
        }
        // Log.i(TAG, "MeasureThread finish");
    }

    /**
     * {@code Click.ratio}を更新する。
     */
    private void calcRatio() {
        if (clicks == null) {
            return;
        }
        // Ratioは、patternLengthのうち、再生中の位置を表す。0または1が、Clickの鳴動点（when）である。
        // 計算式：((現在時刻)-(基準時刻)-when)%patternLength)/patternLength
        long now = System.nanoTime();
        for (Click click : clicks) {
            long pos = (long)((now - start) * Metronome.FREQUENCY / 1000 / 1000 / 1000);// samples from start
            click.ratio = ((patternLength + pos - click.when) % patternLength) / (double)patternLength;
        }
    }


    /**
     * スレッドを終了する
     */
    public void notifyFinish() {
        exit = true;
    }

    /**
     * 起点の時刻を設定し、クリックパターンを設定する。
     *
     * @param clickList クリックパターン
     * @param samplesOfPattern クリックパターンの長さ(サンプル数)
     */
    public void reset(Click[] clickList, int samplesOfPattern) {
        start = System.nanoTime();
        clicks = clickList;
        patternLength = samplesOfPattern;
        calcRatio();
        Log.d(TAG, "Measure: reset w/ " + clickList);
    }


}
