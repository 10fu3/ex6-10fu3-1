## Java 自由制作課題レポート

ベースになった古いバージョンのNginjはこちら, https://github.com/10fu3/NginJ

## 制作物について
- 名前: NewNginj (ウェブサーバーのnginxのJavaVerという意)
- 種別: Webアプリケーション作成用のライブラリ

## 前回提出のバージョンが解決した問題

前期Javaの授業で提出した課題はWebアプリケーション作成におけるクライアント1万台問題(以後,C10K問題と略す) の解決を目指したライブラリ(Nginj)を制作し, それを使ったWeb掲示板を制作した.

通常JavaでWebアプリケーションを作成する際, ほとんどの場合において, リクエストのたびにスレッドを生成する手法がとられてきた.

この手法は限られた数のリクエストを捌く場合においては有用で,Javaでは非常に簡単に記述することが可能である.

Javaのスレッドが生成される際, 内部ではOSのスレッド作成APIを使用している.

故にOSがスレッドを切り替える際のコンテキストスイッチの発生によるタイムロスや, スレッドを切り替えるために切り替え前の処理の状態を保存しておくためのメモリを多く消費するという問題があり, C10K時にはその問題が顕在化する.

C10K問題を解決するため, 近年登場したプログラミング言語では次のアプローチがとられている.
  - I/Oにかかる時間を待機せず, OSにI/O処理が完了したことをアプリケーション側に通知させるAPIを使用する (JavaScript)
  - 軽量のスレッドをランタイム内で定義し, アプリケーションは軽量スレッドを利用させ, ランタイムはネイティブスレッドとI/Oの通知機能を組み合わせてうまく運用する(golang)

Javaには前者のAPIが用意されているが, 開発者にとっては検索してもそれほど情報が得られず, 近年登場したプログラミング言語の機能より扱いづらいものであった.

そこでNginjはWebアプリケーション作成において必須ともいえるルーティングを登録用メソッドにラムダ式で処理を委譲するという形で開発者にC10K問題を解決するためのインターフェイスを提供した.

## 前回提出したバージョンの問題点
I/Oバウンドの問題は解決したが, CPUバウンドの問題を解決できなかったことが挙げられる.

具体的には, ルーティングに登録したラムダ式の処理が負荷のかかるものだった場合, ほかのリクエストを処理できなくなるという問題点があった.

例えば次のようなルーティングを登録した場合,リクエストをさばいている最中に処理が一度停止するのは自明である.

```java
HttpServer.get("/wait",(ctx)->{
    try {
        Thread.sleep(9500); //ここで9.5秒も処理が止まる
        ctx.res.setBody(UUID.randomUUID().toString());
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
});
```
しかし, ほかのリクエストが同時に来ていた場合,
呼び出し元のHttpServer.javaでは
```java
 public void start(int port){
    try {
        server = AsynchronousServerSocketChannel.open();
        server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        server.bind(new InetSocketAddress(port));

        while(true) {
            Future<AsynchronousSocketChannel> acceptFuture = server.accept();
            try {
                handleRequest(acceptFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        } catch (IOException e) {
    e.printStackTrace();
    }
}
```
のwhile文の無限ループによってリクエストを処理しているため, 処理がすべて止まってしまうという問題があった.

そこで最初に次のような実装をした

0. シングルトンなConcurrentLinkedQueue(スレッドからの取り出しで競合しないキュー)を定義
1. スレッドをCPUのコア数分立ち上げておく.
2. スレッドの処理内容は 無限ループ状態にしておき, キューに中身があればすぐに取り出すようにしておく
3. リクエストが来たら, キューにAsynchronousSocketChannelを格納
4. スレッドたちがキューの中身を拾ってそのあとの処理につなげる

この実装をしたところ, 当初は仮説通りに時間のかかる処理でも問題なく動作した.
しかし, アイドリング時のCPUの使用率が100%付近に張り付くようになってしまった.

スレッドを立ち上げる個数を半分に減らしたところ, CPUの使用率は50%に低下したため, スレッドの実装に問題があると考えた.

仮説として, スレッド内部での無限ループが原因ではないかと考えた.

そこで, ExecutorServiceという決まった個数のスレッドを立ち上げておき, そのスレッドを処理が終了しても使いまわすというJavaのAPIを利用した.
ここに時間がかかるかもしれない移譲された処理を実行することで, 無限ループを止めることなく実行することができ, アイドリング時もCPUの使用率も通常時の **20%** と抑えて実装することができた.

## まとめ
JavaのノンブロッキングAPIとExecutorServiceを使うことにより, I/Oバウンドな処理をノンブロッキングAPIが, CPUバウンドな処理をExecutorServiceが, それぞれ補うことで, 大量のリクエストをさばきつつ, ある程度の重たい処理も捌くためのライブラリを作ることができた.

## 満たした条件の説明
- コメント、空白行などを除く行数が100行以上 
  
  コードを参照

  
- 表明 (Assert) を利用
  
  HttpServer.java の処理移譲を登録する部分に使用

  HttpServer.java の 67行目から90行目を参照

  
- デザインパターンを使用

  HttpServer.java の処理移譲を登録する部分に "シングルトンオブジェクト" を使用


- マルチスレッドを使用
  初期の実装, 先述のスレッドプールでも使用
  
  
- Lambda式を使用
  ルーティング処理を開発者に移譲するためにラムダ式を実行時に登録するようなインターフェイスを提供
  
