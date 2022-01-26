# FUSE

![image015](https://user-images.githubusercontent.com/18564097/151006842-ff7eb66a-519c-4f8b-9459-31724d41b0ed.jpg)

![image040](https://user-images.githubusercontent.com/18564097/151007019-d3dbb7e4-0879-4d8f-a074-1d5b3fee83b2.jpg)

![image053](https://user-images.githubusercontent.com/18564097/151007087-69afb44d-b2d6-4faa-a1f9-afbb5b5199cb.jpg)

## 概要

Furu-lab Unified Simulation Environmentの略で、  
マルチエージェントシミュレーションを簡潔なソースコードにより実現することを目的としたjavaのライブラリです。  
日本大学生産工学部数理情報工学科古市研究室において研究向けに開発されました。  

## できること
エージェントによる様々な行動を記述でき、  
ライブラリとして地図データ(OpenStreetMap)の読み込みや  
CaSPA(Cascaded Sub-goals Production Algorithm)と呼ばれるタスクを  
連鎖的に自動生成して目標タスクをこなすタスクプランニング手法も用意されています。

## インストール(FUSE, K7System2)
  　
インストール方法は次の通りです。  
  　  
1.  eclipseのインストール  
eclipseのjava版をダウンロードします。どのeclipseでもかまいません。  
おすすめは日本語化対応しているpleadesです。  

2.  FUSE K7System2のインポート  
"パッケージエクスプローラー"で右クリックを押して、"インポート(import)"を選択します。  
eclipseにgithubからチェックアウトしたfuse及びK7System2をインポートします。  
各フォルダはそのままeclipseのプロジェクト形式になっているので、簡単に読み込めます。  

3.  ビルドパスの確認  
"パッケージエクスプローラー"の"FUSE"上で右クリックし、"プロパティ"を選択します。  
"Javaのビルド・パス"を選択し、"ライブラリー"及び"プロジェクト"という項目で×のパスを修正します。  
内容は以下の通りです。  
- Fuse　　　gluegen-rt.jar, jog-all.jar, jruby.jar, K7System2  
- K7System2　gluegen-rt.jar, jog-all.jar  

4.  ライブラリのダウンロード  
gluegen-rt.jar、jog-all.jar( Java OpenGL(JOGL) )の最新版は  
以下のサイトにあるjogamp-all-platforms.7zをダウンロードしてください。  
　  
http://jogamp.org/wiki/index.php/Downloading_and_installing_JOGL  
　  
ruby.jarは以下のサイトにあります。1.7.*  
と記載されているバージョンの最新版をダウンロードしてください。  
なお、1.7.6で動作することは確認されています。  
　  
http://jruby.org/files/downloads/  

5.  文字化けしている場合  
"パッケージエクスプローラー"の"Fuse"上で右クリックし、プロパティを選択します。  
"リソース"を選択し、"テキスト・ファイルのエンコード"という項目の"その他"を"UTF-8"あるいは"MS932"に変更してください。  
逆の場合も同様です。K7Systemでも同様です。

## 使用方法
### FUSE
SDKの形態をとっているため、単独動作はしません。  
サンプルコードにより確認したい場合は  
FUSEのsamplesにサンプルコードが記述されていますので、  
実行するとFUSEがどのように動作しているのか確認することができます。  
実行方法は次のようにしてください。  

1.  main関数が記述されたファイル（以下）を開きます。  
samples　→　FuseSample.java
  　　　
2.  開いたら上部にある"実行ボタン(緑色の丸に白い三角のアイコン)"を押して実行します。  

3.  ダイアログが開いた場合は"OK"を選択して次に進みます。  

4.  成功するとwindowが表示されます。  
  
5.  表示されたら、右側の操作パネルに表示されている再生ボタンを押してシミュレーションを実行します。
  
### K7System
samplesにあるファイル一つ一つが実行可能ファイルになっているので、  
K7Systemに搭載されているグラフィック処理エンジンの確認ができます。  
実行方法は上記実行方法と同じです。  
詳細は以下を参照してください。  
　  
https://github.com/gitkobaya/K7System#readme

## 作成者
日本大学生産工学研究科数理工学専攻古市研究室に所属していた倉本博士及び志甫氏がメインで作成しています。  
このgitを管理しているのは最後にライブラリの管理を任された同研究室に所属していた小林博士です。  

## 研究成果一覧
FUSEを利用した研究成果は以下となります。  

倉本健介, "マルチエージェントシミュレーション方式による組織構造を有する人間行動モデル構築法及びその応用に関する研究," 日本大学生産工学部博士論文, 2016.  

Kensuke Kuramoto, Masakazu Furuichi, Kazuo Kakuda, "Efficient Load-balancing Scheme for Multi-agent Simulation Systems" CMES(Computer Modeling in Engineering & Sciences) Vol.106, No.3, 169-185, 2015.  

倉本 健介, 古市 昌一, "マルチエージェントシミュレーションのためのフレームワーク FUSE の開発とその応用," 日本デジタルゲーム学会2014年度年次大会, 大和町, 2015.  

竹多政裕, 瀧嶋悠, 倉本健介, 古市昌一, "マルチエージェントシミュレーション方式による歴史研究支援システムへの動的環境モデルの実装法と評価", 第77回全国大会講演論文集 2015(1), 641-642, 2015.  

田中和幸, 古市昌一, "小型無人航空機を用いた大規模災害時における避難誘導のMASによる有効性検証," 情報科学技術フォーラム講演論文集 14(4), 473-474, 2015.  

Kuramoto, K and Furuichi, M. "FUSE: A MULTI-AGENT SIMULATION ENVIRONMENT," Proc. of the 2013 Winter Simulation Conference, Washington D.C., USA, pp. 3982-3983, 2013

Kuramoto, K. and Furuichi, M. "A Design and Preliminary Evaluation of Hierarchical Organizational Behavior Modeling Architecture," Proc. of JSST2012 International Conference on Simulation Technology, Kobe, Japan, pp. RS1–7, 2012.

etc...  
