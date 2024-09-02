package plugin.memory;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Pair {
  //nameメンバ変数のgetter
  @Getter
  private final String name;
  private final List<Block> blocks = new ArrayList<>();
  private final List<Entity> entities = new ArrayList<>();

  public Pair(String name){
    this.name = name;
  }

  public void addBlock(Block block) {
    blocks.add(block);
  }
  public void addEntity(Entity entity){
    entities.add(entity);
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
   public boolean containsEntity(Entity entity){
     return entities.contains(entity);
   }
   public void removeEntities(){
    for(Entity entity : entities){
      entity.remove();
    }
   }
}
