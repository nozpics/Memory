# memorygame

「memorygame」は、Javaを使用して開発されたマインクラフト用の神経衰弱ゲームです。(Macでのみ動作確認済）


# 特徴・参考動画

「memorygame」は、コマンド引数に「easy」「normal」「hard」のいずれかを指定することでゲームの難易度を設定できます。各難易度に対応する出現対象と参考動画は以下の通りです。

* **easy** : ダイヤモンドブロック


https://github.com/user-attachments/assets/7670edc2-02ac-4a1e-8a32-e4a3239571ed



* **normal**: 動き回る羊

https://github.com/user-attachments/assets/8d05c10c-4c89-472f-aa50-632885668d8e


* **hard**  : 攻撃してくるゾンビ


https://github.com/user-attachments/assets/8f001c09-7076-40ca-b975-bb38566c279a



# 遊び方

ゲームを開始するには、以下のコマンドを使用します。

```
/memorygame difficulty
```

`difficulty`: ゲームの難易度を指定します。`easy`, `normal`, `hard` のいずれかを選択してください。

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
```
```
USE memory_score;
```  
3.テーブルを作成する。

```
CREATE TABLE memory_score(id int auto_increment, player_name varchar(100), score int, difficulty varchar(30), registered_at datetime, primary key(id));
```
   
4.mybatis-config.xmlにローカル環境に合わせたurl,username,passwordを設定する。

# 対応バージョン

* Minecraft ver: 1.20.4
* Spigot ver: 1.20.4


# 使用技術

* Java
* Mysql
* MyBatis
