package plugin.memory;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Pair {
  private final String name;
  private final List<Block> blocks = new ArrayList<>();

  public Pair(String name){
    this.name = name;
  }

  public void addBlock(Block block) {
    blocks.add(block);
  }

  //nameメンバ変数のgetter
  public String getName(){
    return this.name;
  }

  // 格納したブロックリストから取り出す
  public Block getBlock(int i){
    int idx = Math.min(blocks.size() - 1, i);
    return this.blocks.get(idx);
  }
  public boolean containsBlock(Block block){

    return blocks.contains(block);
  }
  public void removeBlocks(){
    for(Block block : blocks){
      block.setType(Material.AIR);
    }
  }
}
