package jp.fsoriented.cactusmetronome.lib;

/**
 * デフォルトのクリック音の波形を生成するクラス。
 */
public class DefaultClickCallback implements ClickCallback {

    /** G3の分母 */
    private static final double DENOMINATOR_G3 = 22050 / 391.99;
    /** G4の分母 */
    private static final double DENOMINATOR_G4 = 11025 / 391.99;

    /**
     * クリック音を生成する
     *
     * @param buffer    バッファ
     * @param frequency サンプリング周波数(Hz)
     * @param click     クリックパターン
     * @return 書き込んだデータ数
     */
    @Override
    public int writeClick(short[] buffer, int frequency, Click click) {
        int POS1 = (int) (click.length * 0.2);
        int POS2 = (int) (click.length * 0.35);
        for (int i = 0; i < POS2; i++) {
            if (i < POS1) {
                // 一定音量
                buffer[i] = (short) (Math.sin(i * Math.PI / DENOMINATOR_G3) * 12000);
                buffer[i] += (short) (Math.sin(i * Math.PI / DENOMINATOR_G4) * 15000);
            } else {
                // 音量減衰
                buffer[i] = (short) (Math.sin(i * Math.PI / DENOMINATOR_G3) * 12000 * ((double) (POS2 - i) / (POS2 - POS1)));
                buffer[i] += (short) (Math.sin(i * Math.PI / DENOMINATOR_G4) * 15000 * ((double) (POS2 - i) / (POS2 - POS1)));
            }
        }
        return POS2;
    }
}
