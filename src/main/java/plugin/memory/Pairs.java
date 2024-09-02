package plugin.memory;

import java.util.ArrayList;
import java.util.List;

public class Pairs {
  private static Pairs instance;
  private final List<Pair> pairs;


  // シングルトンのため、プライベートなコンストラクタ宣言をする
  private Pairs(){
    this.pairs = new ArrayList<>();
  }

  // インスタンスはゲッターで(生成)取得する
  public static synchronized Pairs getInstance(){
    if(instance == null){
      instance =  new Pairs();
    }
    return instance;
  }

  public static synchronized void resetInstance() {
    instance = new Pairs(); // 新しいインスタンスで置き換え
  }

  // ラッパーメソッド
  public void add(Pair p){
    this.pairs.add(p);
  }

  // ラッパーメソッド
  public void remove(Pair p){
    this.pairs.remove(p);
  }

  // ラッパーメソッド
  public int size(){
    return this.pairs.size();
  }

  // ラッパーメソッド
  public Pair getPair(int i){
    return this.pairs.get(i);
  }

  // リストオブジェクト自体を取得
  public List<Pair> getPairs(){
    return new ArrayList<>(this.pairs);
  }
}