package jp.fsoriented.cactusmetronome.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import jp.fsoriented.cactusmetronome.lib.Click;
import jp.fsoriented.cactusmetronome.lib.DefaultHighClickCallback;
import jp.fsoriented.cactusmetronome.lib.Metronome;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Metronome mMetronome = new Metronome();

    // 再生ボタンの処理
    public void onStartClick(View view) {
        // すでに再生していたら、一度止める
        mMetronome.finish();

        // 入力されたテンポを取得する
        EditText editText = (EditText)findViewById(R.id.editText);
        int tempo = Integer.parseInt(editText.getText().toString());

        // 再生するクリック音のリストを作成する
        ArrayList<Click> list = new ArrayList<Click>();
        int samples = BpmUtil.getSampleLength(tempo);
        int beatsPerMeasure = 4;
        NoteEnum note = NoteEnum.BASIC_4;
        for (int i=0; i<beatsPerMeasure ; i++) {
            note.addNewClicks(list, samples, i);
        }

        // 再生する
        mMetronome.start();
        mMetronome.setPattern(list, samples * beatsPerMeasure);
    }

    public void onStopClick(View view) {
        mMetronome.finish();
    }

    private static class BpmUtil {
        public static int getSampleLength(double bpm) {
            // 1beatあたりの長さ（sample）
            return (int)(60 * Metronome.FREQUENCY / bpm);
        }
    }

    /**
     * 譜割りを表すクラス。
     */
    private static enum NoteEnum {
        // basic notes
        BASIC_4(new double[]{0}, 1.0/8),
        BASIC_8(new double[]{0, 0.5}, 1.0/8);

        // 4分音符の長さを0..1としたときに、クリックがどこにあるかを表す
        // Clickを作るもとになる

        /** いつ発音するか */
        private final double[] beats;
        /** 音の長さ */
        private final double length;

        /** コンストラクタ */
        private NoteEnum(double[] beats, double length) {
            this.beats = beats;
            this.length = length;
        }

        /**
         * 指定されたリストに、この{@code NoteEnum}が表す音を追加する。
         *
         * @param destination Clickの書き込み先
         * @param lengthOfQuarter 4分音符の長さ（サンプル）
         * @param index 何個目の4分音符か。0はじまり。
         */
        public void addNewClicks(ArrayList<Click> destination, int lengthOfQuarter, int index) {
            for (int i=0; i<beats.length; i++) {
                double beat = beats[i];
                int when = (int)(beat * lengthOfQuarter) + lengthOfQuarter * index;
                int len = (int)(length * lengthOfQuarter);
                Click c;
                if (index == 0 && i == 0) {
                    c = new Click(when, len, new DefaultHighClickCallback());
                } else {
                    c = new Click(when, len);
                }
                destination.add(c);
            }
        }
    }
}
