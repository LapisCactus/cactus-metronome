/**
 * メトロノームのような定期的なクリック音を再生するためのパッケージ.
 *
 * <h2>使い方</h2>
 * {@link jp.fsoriented.cactusmetronome.lib.Click}クラスが１つの音を現しており、いつどのような音を鳴らすか表現します。
 * {@link jp.fsoriented.cactusmetronome.lib.Metronome}の{@code setPattern}メソッドによって、{@code Click}クラスの配列（またはリスト）を設定し、{@code start}メソッドで再生を開始します。
 * 再生はサブスレッド上で非同期に行われます。
 * 再生を停止するには、{@code Metronome}の{@code finish}メソッドを呼び出します。
 */
package jp.fsoriented.cactusmetronome.lib;
