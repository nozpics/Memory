package plugin.memory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
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
import org.bukkit.scheduler.BukkitRunnable;
import plugin.memory.Main;
import plugin.memory.Pair;
import plugin.memory.Pairs;
import plugin.memory.data.PlayerScore;

public class MemoryGameCommand implements CommandExecutor, Listener {
  private Main main;
  private final Map<UUID, Pair> lastTouched = new HashMap<>();
  private int score;
  private BossBar bossBar;

  public MemoryGameCommand(Main main){
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s,
      String[] strings) {
    if (commandSender instanceof Player player) {
      World world = player.getWorld();

      player.setHealth(20);
      player.setFoodLevel(20);


      Pairs pairs = Pairs.getInstance();
      for (int i = 1; i <= 5; i++) {
        pairs.add(new Pair(i + "番！"));
      }
      this.getSpawnLocation(player, world);

        // BossBarの作成
        bossBar = Bukkit.createBossBar("制限時間", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
      //制限時間の設定
      new BukkitRunnable() {
        int time = 20;

        @Override
        public void run() {
          if(time > 0){
            double progress = time / 20.0;
            bossBar.setProgress(progress);

            // 残り3秒でカウントダウン
            if (time <= 3) {
              String title = ChatColor.WHITE + String.valueOf(time);
              player.sendTitle(title, "", 0, 20, 0);
            }

            time--;
          } else {
            bossBar.setVisible(false);
            cancel();
            // 制限時間が経過した際に実行する処理
            player.sendTitle("ゲーム終了！", "最終スコアは" + score + "点", 10, 50, 20);
            // 残っているダイヤブロックを消す処理
            pairs.getPairs().forEach(Pair::removeBlocks);
          }
        }
      }.runTaskTimer(main, 0,20L);

    }
    return true;
  }

  //プレイヤーがブロックを右クリックした際に発生するイベント
  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND)
      return;

    Pairs pairs = Pairs.getInstance(); //Pairsの要素数を再取得
    Player player = event.getPlayer();

    //ブロックを右クリックしたらブロック情報を取得

    Block block = event.getClickedBlock();

    if (block == null || block.getType() != Material.DIAMOND_BLOCK)
      return;
    //クリック先がダイヤモンドブロックなら、プレイヤー情報を取得
    UUID playerId = player.getUniqueId();
    for(Pair pair : pairs.getPairs()){
      if(pair.containsBlock(block)){
        player.sendMessage(pair.getName());
        //過去にタッチされたブロックと今回タッチしたブロックが一致したら、ダイヤモンドブロックがAIRに変わる
        if(this.lastTouched.containsKey(playerId) && this.lastTouched.get(playerId) == pair){
          pair.removeBlocks();
          score +=10;
          player.sendMessage("10点！　現在のスコアは" + score + "点");
        }else{
          this.lastTouched.put(playerId, pair);
        }
        break;
      }
    }
  }


    /**
     * エンティティの出現エリアを取得し、ブロックを2つ生成後、ペアに登録する。
     * @param player　コマンドを実行したプレイヤー
     * @param world   コマンドを実行したプレイヤーが所属するワールド
     * @return
     */
    public Location getSpawnLocation(Player player, World world){
      Pairs pairs = Pairs.getInstance();
      Location playerLocation = player.getLocation();
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
      return playerLocation;
    }
  }
