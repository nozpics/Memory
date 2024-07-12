package plugin.memory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MemoryGameCommand implements CommandExecutor, Listener {

  private final List<Pair> pairs = new ArrayList<>();
  private final Map<UUID, Pair> lastTouched = new HashMap<>();


  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s,
      String[] strings) {
    if (commandSender instanceof Player player){
      World world = player.getWorld();

      player.setHealth(20);
      player.setFoodLevel(20);

      for (int i = 1; i <= 2; i++) {
        pairs.add(new Pair(i + "番です！"));
    }
      getSpawnLocation(player,world);
    }
    return true;
  }

  //プレイヤーがブロックを右クリックした際に発生するイベント
  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent event){
    //ブロックを右クリックしたらブロック情報を取得
    if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    Block block = event.getClickedBlock();

    if(block == null || block.getType() != Material.DIAMOND_BLOCK) return;
    //クリック先がダイヤモンドブロックなら、プレイヤー情報を取得
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    player.sendMessage("Debug: Block clicked");

    boolean pairFound = false;
    for(Pair pair : pairs){
      if(pair.containsBlock(block)){
        //タッチしたブロックの「i+番です！」が表示されるはずだが、されないのでここが原因？
        pairFound = true;
        player.sendMessage(pair.getName());
        player.sendMessage("Debug: Block is in pair");

        //過去にタッチされたブロックと今回タッチしたブロックが一致したら、ダイヤモンドブロックがAIRに変わる
        if(lastTouched.containsKey(playerId) && lastTouched.get(playerId) == pair){
          pair.removeBlocks();
          player.sendMessage("Debug: Block removed");
        }else{
          lastTouched.put(playerId, pair);
          player.sendMessage("Debug: Block registered");
        }
        break;
      }
    }
    //pairが見つからなかったらメッセージを表示
    if (!pairFound) {
      player.sendMessage("Debug: No matching pair found");
    }
  }


  private static class Pair{
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

  /**
   * エンティティの出現エリアを取得し、ブロックを2つ生成後、ペアに登録する。
   * @param player　コマンドを実行したプレイヤー
   * @param world   コマンドを実行したプレイヤーが所属するワールド
   * @return        ブロックの出現場所
   */
  public Location getSpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
    for (Pair pair : pairs) {
      for (int j = 0; j < 2; j++) {
    //現在のプレイヤー位置の周りにランダム（前後１０ブロック以内）で位置を取得
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;
    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

        Location blockLoc = new Location(world, x, y, z);
        // ブロックが既にある場合、もう一度位置を生成する
        if (blockLoc.getBlock().getType() != Material.AIR) {
          j--;
          continue;
        }
        blockLoc.getBlock().setType(Material.DIAMOND_BLOCK);
        pair.addBlock(blockLoc.getBlock());
        player.sendTitle("START!","", 10, 50, 20);
      }
    }
    return playerLocation;
  }
  }
