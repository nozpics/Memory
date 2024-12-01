# はじめに

こちらは、Javaのspigot-pluginを使用して開発した、「マインクラフト」というゲーム内で遊ぶことができる**神経衰弱ゲーム**です。(Macでのみ動作確認済）



# デモ動画

https://github.com/user-attachments/assets/deb88474-ec09-403f-bc76-69dbafd3274c

# 特徴

「memorygame」は、下記「遊び方」に記載してあるコマンドを入力することで遊ぶことができます。
また、難易度により出現対象が変わります。出現対象は以下のとおりです：

* **easy** : ダイヤモンドブロック

* **normal**: 動き回る羊

* **hard**  : 攻撃してくるゾンビ
  
# 遊び方

ゲームを開始するには、以下のコマンドを使用します：

```
/memorygame <difficulty>
```

`<difficulty>`: ゲームの難易度を指定します。`easy`, `normal`, `hard` のいずれかを入力してください。

例:
```
/memorygame easy
```




ゲームが開始されたら<ins>***右クリック***</ins>で対象にタッチしていきます。チャット内にペア番号が表示され、同じペアをタッチすることができたらスコアが加算されます。

制限時間：20秒

# データベース

データベースに接続後、コマンド引数に「list」を指定すると過去のスコアを表示することができます。

### ***データベースに接続する方法***

1.自身のローカル環境でMySQLに接続する

2.データベースを作成する。

```
CREATE DATABASE memory_score;
USE memory_score;
```  
3.テーブルを作成する。

```
CREATE TABLE memory_score(
      id int auto_increment,
      player_name varchar(100),
      score int,
      difficulty varchar(30),
      registered_at datetime,
      primary key(id)
);
```
   
4.mybatis-config.xmlにローカル環境に合わせたurl,username,passwordを設定する。

# 対応バージョン

* Minecraft ver: 1.20.4
* Spigot ver: 1.20.4


# 使用技術

* Java
* Mysql
* MyBatis

# おわりに
このプロジェクトを通じて、Javaプログラミング、Spigot APIの活用、データベース連携等の基礎を学ぶことができました。何よりも、常に楽しく作成できたことで自信にも繋がりました。
今後もこのプロジェクトで得た知識と経験を活かし、より複雑で大規模なシステム開発にも挑戦していく所存です。ここまでご覧いただき、ありがとうございました。
