package plugin.memory;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Pair {

  @Getter
  private final String name;
  private final List<Block> blocks = new ArrayList<>();
  private final List<Entity> entities = new ArrayList<>();

  public Pair(String name){
    this.name = name;
  }

  //ブロックリストへの追加
  public void addBlock(Block block) {
    blocks.add(block);
  }

  //エンティティリストへの追加
  public void addEntity(Entity entity){
    entities.add(entity);
  }

  //ブロックがリストに含まれているかの確認
  public boolean containsBlock(Block block){
    return blocks.contains(block);
  }

  //リストに含まれているブロックを削除する
  public void removeBlocks(){
    for(Block block : blocks){
      block.setType(Material.AIR);
    }
  }

  //エンティティがリストに含まれているかの確認
  public boolean containsEntity(Entity entity){
     return entities.contains(entity);
   }

   //リストに含まれているエンティティを削除する
   public void removeEntities(){
    for(Entity entity : entities){
      entity.remove();
    }
   }
}
