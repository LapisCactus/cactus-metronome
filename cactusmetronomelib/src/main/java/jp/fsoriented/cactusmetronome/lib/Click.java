package jp.fsoriented.cactusmetronome.lib;

/**
 * クリック音のタイミング、長さ、波形を持つクラス.
 */
public class Click {
    /**
     * クリック音をいれる時刻（単位はsample）
     */
    /*package*/ final int when;
    /**
     * クリック音の長さ（単位はsample）
     */
    /*package*/ final int length;
    /**
     * クリック音の波形を生成するコールバック
     */
    /*package*/ final ClickCallback callback;

    /**
     * このクリックの状態.
     *
     * 状態とは、次のクリックのタイミングまでの経過時間を[0..1]の割合で示したものである。
     * {@code ratio}が0のときが、クリックのタイミングである。
     */
    public volatile double ratio;

    /**
     * クリックのタイミング、クリック音の長さ、波形を生成するコールバックを指定して、{@code Click}のインスタンスを生成するコンストラクタ
     *
     * @param when クリックのタイミング
     * @param length クリック音の長さ
     * @param callback 波形を生成するコールバック
     */
    public Click(int when, int length, ClickCallback callback) {
        this.callback = callback;
        this.when = when;
        this.length = length;
    }

    /**
     * クリックのタイミング、クリック音の長さを指定して、{@code Click}のインスタンスを生成するコンストラクタ
     *
     * @param when クリックのタイミング
     * @param length クリック音の長さ
     */
    public Click(int when, int length) {
        this(when, length, new DefaultClickCallback());
    }

    /**
     * {@code Click}の配列をもとに、クリック音のパターン（波形）を生成する
     *
     * @param spec クリック音の情報。１小節分の{@code Click}の配列。
     * @param frequency 周波数。{@code Metronome.FREQUENCY}を指定する。
     * @param length クリック音の長さ（サンプル）
     * @return クリック音のパターン
     */
    /*package*/
    static short[] compile(Click[] spec, int frequency, int length) {
        if (spec == null || spec.length == 0) {
            throw new RuntimeException("Click spec should not be empty or null.");
        }
        if (length <= 0) {
            throw new RuntimeException("Pattern length should not be zero.");
        }
        // make pattern buffer
        short[] patternBuffer = new short[length];
        // make click buffer
        int maxLength = 0;
        for (Click click : spec) {
            if (maxLength < click.length) {
                maxLength = click.length;
            }
        }
        short[] buffer = new short[maxLength];
        // create a click and compose
        for (Click click : spec) {
            if (click.when > length || click.when < 0) {
                throw new RuntimeException("click.when(" + click.when + ") is out of range. [0-" + length + ")");
            }
            int size = click.callback.writeClick(buffer, frequency, click);
            for (int i = 0; i < size; i++) {
                patternBuffer[(click.when + i) % length] += buffer[i];
            }
        }
        return patternBuffer;
    }

}
