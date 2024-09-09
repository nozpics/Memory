package plugin.memory;

import java.util.ArrayList;
import java.util.List;

//シングルトンクラス
public class Pairs {
  private static Pairs instance;
  private final List<Pair> pairs;


  // シングルトンのため、プライベートなコンストラクタ宣言をする
  private Pairs(){
    this.pairs = new ArrayList<>();
  }

  //インスタンスを取得
  public static synchronized Pairs getInstance(){
    if(instance == null){
      instance =  new Pairs();
    }
    return instance;
  }

  //インスタンスをリセット
  public static synchronized void resetInstance() {
    instance = new Pairs();
  }

  //リストにPairオブジェクトを追加
  public void add(Pair p){
    this.pairs.add(p);
  }

  // リストのコピーを返す
  public List<Pair> getPairs(){
    return new ArrayList<>(this.pairs);
  }
}