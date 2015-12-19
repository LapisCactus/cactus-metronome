package jp.fsoriented.cactusmetronome.lib;

/**
 * クリック音の波形を生成するコールバックのためのインターフェース.
 *
 * このインターフェースの実装は、指定されたバッファにクリック音の波形を書き込まなければならない。
 * クリックの数だけ繰り返し呼ばれる。
 */
public interface ClickCallback {
    /**
     * 指定されたバッファに、クリック音を書き込む.
     *
     * @param buffer バッファ
     * @param frequency サンプリング周波数(Hz)
     * @param click クリックパターン
     * @return 書き込んだデータ数
     */
    public int writeClick(short[] buffer, int frequency, Click click);
}
