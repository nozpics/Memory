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
import plugin.memory.Pair;
import plugin.memory.Pairs;

public class MemoryGameCommand implements CommandExecutor, Listener {

  // private final List<Pair> pairs = new ArrayList<>();
  private final Map<UUID, Pair> lastTouched = new HashMap<>();


  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s,
      String[] strings) {
    if (commandSender instanceof Player player) {
      World world = player.getWorld();

      player.setHealth(20);
      player.setFoodLevel(20);

      Pairs pairs = Pairs.getInstance();
      for (int i = 1; i <= 2; i++) {
        pairs.add(new Pair(i + "番です！"));
        player.sendMessage("Debug: Pair " + pairs.size() + " created.");
      }
      this.getSpawnLocation(player, world);

      player.sendMessage("Debug: Spawned Block Count" +pairs.size());
    }
    return true;
  }

  //プレイヤーがブロックを右クリックした際に発生するイベント
  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent event) {
    Pairs pairs = Pairs.getInstance(); //Pairsの要素数を再取得
    Player player = event.getPlayer();
    player.sendMessage("Debug: Event Block Count " + pairs.size());

    //ブロックを右クリックしたらブロック情報を取得
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
      return;
    Block block = event.getClickedBlock();

    if (block == null || block.getType() != Material.DIAMOND_BLOCK)
      return;
    //クリック先がダイヤモンドブロックなら、プレイヤー情報を取得
    UUID playerId = player.getUniqueId();
    player.sendMessage("Debug: Block clicked");

    boolean pairFound = false;
    player.sendMessage("Debug: Block Count " +pairs.size());
   //block.setType(Material.AIR);


   //for(Pair pair : this.pairs){
    for(Pair pair : pairs.getPairs()){
      if(pair.containsBlock(block)){
        pairFound = true;
        player.sendMessage(pair.getName());
        player.sendMessage("Debug: Block is in pair");

        //過去にタッチされたブロックと今回タッチしたブロックが一致したら、ダイヤモンドブロックがAIRに変わる
        if(this.lastTouched.containsKey(playerId) && this.lastTouched.get(playerId) == pair){
          pair.removeBlocks();
          player.sendMessage("Debug: Block removed");
        }else{
          this.lastTouched.put(playerId, pair);
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

    /**
     * エンティティの出現エリアを取得し、ブロックを2つ生成後、ペアに登録する。
     * @param player　コマンドを実行したプレイヤー
     * @param world   コマンドを実行したプレイヤーが所属するワールド
     * @return ブロックの出現場所
     */
    public Location getSpawnLocation(Player player, World world){
      Pairs pairs = Pairs.getInstance();
      Location playerLocation = player.getLocation();
      //for (Pair pair : this.pairs) {
      for (Pair pair : pairs.getPairs()) {
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
          player.sendTitle("START!", "", 10, 50, 20);
        }
      }
      player.sendMessage("Debug: Spawn Block Count " + pairs.size());
      return playerLocation;
    }
  }
