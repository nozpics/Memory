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

  public static synchronized Pairs getInstance(){
    if(instance == null){
      instance =  new Pairs();
    }
    return instance;
  }

  public static synchronized void resetInstance() {
    instance = new Pairs();
  }


  public void add(Pair p){
    this.pairs.add(p);
  }

  // リストオブジェクト自体を取得
  public List<Pair> getPairs(){
    return new ArrayList<>(this.pairs);
  }
}