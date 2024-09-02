package plugin.memory.command;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.memory.PlayerScoreData;
import plugin.memory.Main;
import plugin.memory.Pair;
import plugin.memory.Pairs;
import plugin.memory.mapper.data.PlayerScore;

/**
 * 神経衰弱ゲームを起動するコマンドです。制限時間内に出現するエンティティのペアを揃えてスコアを獲得します。
 * 結果はプレイヤー名、難易度、点数、日時などで保存されます。
 */
public class MemoryGameCommand extends BaseCommand implements Listener {

  public static final String EASY = "easy";
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String NONE = "none";
  public static final String List ="list";

  private final Map<UUID, Pair> lastTouched = new HashMap<>();
  private final Map<String, Integer> playerScores = new HashMap<>();
  private Main main;
  private PlayerScoreData playerScoreData = new PlayerScoreData();
  private BossBar bossBar;

  public MemoryGameCommand(Main main){
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
    // 最初の引数が「list」だったらスコアを一覧表示して処理を終了する。
    if (args.length == 1 && List.equals(args[0])) {
      sendPlayerScoreList(player);
      return false;
    }

    String difficulty = getDifficulty(player, args);
    if(difficulty.equals(NONE)){
      player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS,1.0f,1.0f);
      return false;
    }

    initGame(player,difficulty);

    //制限時間の設定
    String finalDifficulty = difficulty;
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
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE,0.5f,1.0f);
          }

          time--;
        } else {
          bossBar.setVisible(false);
          cancel();
          // 制限時間が経過した際に実行する処理
          player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_5,1.0f,1.0f);
          int finalScore = playerScores.get(player.getName());
          player.sendTitle("ゲーム終了！", player.getName() + " : " + finalScore + "点 (" + finalDifficulty + ")", 10, 50, 20);


          // 残っているダイヤブロック、エンティティを消す処理
          Pairs pairs = Pairs.getInstance();
          pairs.getPairs().forEach(Pair::removeBlocks);
          pairs.getPairs().forEach(Pair::removeEntities);

          playerScoreData.insert(new PlayerScore(player.getName(),finalScore,finalDifficulty));

        }
      }
    }.runTaskTimer(main, 0,20L);
    return true;
  }


  @Override
  public boolean onExecuteNPCCommand(CommandSender commandSender,Command command, String s,
      String[] strings) {
    return false;
  }

  /**
   * ゲームを開始する処理。
   * @param player　コマンドを実行したプレイヤー
   */

  private void initGame(Player player, String difficulty) {
    Pairs.resetInstance();
    resetBossBar();
    Pairs pairs = Pairs.getInstance();
    for (int i = 1; i <= 5; i++) {
      pairs.add(new Pair(i + "番！"));
    }
    playerScores.put(player.getName(), 0);
    setUpBossBar(player);
    player.sendTitle("START!", "", 10, 50, 20);
    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1,1.0f,1.0f);
    getSpawnLocation(player, difficulty);
  }

  /**
   * コマンド入力時に制限時間を初期化する。
   */
  private void resetBossBar() {
    if (bossBar != null) {
      bossBar.removeAll();
    }
  }

  /**
   * 　制限時間を表示するバーの設定
   * @param player コマンドを実行したプレイヤー
   */
  private void setUpBossBar(Player player) {
    bossBar = Bukkit.createBossBar("制限時間", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
    bossBar.addPlayer(player);
    bossBar.setVisible(true);
  }


  /**
   * 現在登録されているスコアの一覧をメッセージに送る。
   *
   * @param player　プレイヤー
   */
  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreData.selectList();
    for(PlayerScore playerScore : playerScoreList){
      player.sendMessage(
          playerScore.getId() + "  |  "
              + playerScore.getPlayerName() + "  |  "
              + playerScore.getScore() + "  |  "
              + playerScore.getDifficulty() + "  |  "
              + playerScore.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
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
    String playerName = player.getName();
    for(Pair pair : pairs.getPairs()){
      if(pair.containsBlock(block)){
        player.sendMessage(pair.getName());
        //過去にタッチされたブロックと今回タッチしたブロックが一致したら、ダイヤモンドブロックがAIRに変わる
        if(this.lastTouched.containsKey(playerId) && this.lastTouched.get(playerId) == pair){
          pair.removeBlocks();
          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1.0f,1.0f);
          int newScore = playerScores.getOrDefault(playerName, 0) + 10;
          playerScores.put(playerName, newScore);
          player.sendMessage("10点！　現在のスコアは" + newScore + "点");
        }else{
          this.lastTouched.put(playerId, pair);
          player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,1.0f,1.0f);
        }
        break;
      }
    }
  }
@EventHandler
public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
  if (event.getHand() != EquipmentSlot.HAND) {
    return;
  }
  Player player = event.getPlayer();
  Entity clickedEntity = event.getRightClicked();
  UUID playerId = player.getUniqueId();
  String playerName = player.getName();
  Pairs pairs = Pairs.getInstance(); //Pairsの要素数を再取得

  for(Pair pair : pairs.getPairs()){
    if(pair.containsEntity(clickedEntity)){
      player.sendMessage(pair.getName());
      //過去にタッチされたエンティティと今回タッチしたエンティティが一致したら、エンティティが消える
      if(this.lastTouched.containsKey(playerId) && this.lastTouched.get(playerId) == pair){
        pair.removeEntities();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1.0f,1.0f);
        int newScore = playerScores.getOrDefault(playerName, 0) + 10;
        playerScores.put(playerName, newScore);
        player.sendMessage("10点！　現在のスコアは" + newScore + "点");
      }else{
        this.lastTouched.put(playerId, pair);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,1.0f,1.0f);
      }
      break;
    }
  }
}

  /**
   * 難易度をコマンド引数から取得。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param strings　コマンド引数
   * @return　難易度
   */
  private String getDifficulty(Player player, String[] strings) {
    if(strings.length ==1 && (EASY.equals(strings[0]) || NORMAL.equals(strings[0]) || HARD.equals(
        strings[0]))){
      return strings[0];
    }
    player.sendMessage(ChatColor.RED + "実行できません。コマンド引数の１つ目に[easy,normal,hard]いずれかの難易度指定が必要です。");
    return NONE;
  }

  /**
   * ゲーム難易度HARDを始める前にプレイヤーの状態を設定する。
   * 体力と空腹度を最大にして、装備はネザライト一式になる。
   *
   * @param player　コマンドを実行したプレイヤー
   */
  private void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);

    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
    inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
  }

    /**
     * エンティティの出現エリアを取得し、ブロックを2つ生成後、ペアに登録する。
     * @param player　コマンドを実行したプレイヤー
     * @param difficulty 難易度
     * @return
     */
    public Location getSpawnLocation(Player player, String difficulty){
      Pairs pairs = Pairs.getInstance();
      Location playerLocation = player.getLocation();

      switch(difficulty){
        case EASY:
      for (Pair pair : pairs.getPairs()) {
        for (int j = 0; j < 2; j++) {
          //現在のプレイヤー位置の周りにランダム（前後１０ブロック以内）で位置を取得
          Location memoryLoc = getMemoryLoc(player.getWorld(), playerLocation);
          // ブロックが既にある場合、もう一度位置を生成する
          if (memoryLoc.getBlock().getType() != Material.AIR) {
            j--;
            continue;
          }
          memoryLoc.getBlock().setType(Material.DIAMOND_BLOCK);
          pair.addBlock(memoryLoc.getBlock());
        }
      }
      break;

        case NORMAL:
          // NORMALモードは白い羊をスポーンさせる
          for (Pair pair : pairs.getPairs()) {
            for (int j = 0; j < 2; j++) {
              Location memoryLoc = getMemoryLoc(player.getWorld(), playerLocation);
              Sheep sheep = (Sheep) player.getWorld().spawnEntity(memoryLoc, EntityType.SHEEP);
              sheep.setColor(DyeColor.WHITE); // 白い羊を設定
              pair.addEntity(sheep);
            }
          }
          break;

        case HARD:
          initPlayerStatus(player);
          for (Pair pair : pairs.getPairs()) {
            for (int j = 0; j < 2; j++) {
              Location memoryLoc = getMemoryLoc(player.getWorld(), playerLocation);
              Zombie zombie = (Zombie) player.getWorld().spawnEntity(memoryLoc, EntityType.ZOMBIE);
              pair.addEntity(zombie);
            }
          }
          break;
      }
      return playerLocation;
    }

  /**
   * ブロックとエンティティの出現エリアを取得する
   * @param world　プレイヤーが所属するワールド
   * @param playerLocation　コマンドを実行したプレイヤーの現在地
   * @return memoryLoc 出現エリア
   */

  private static Location getMemoryLoc(World world, Location playerLocation) {
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;
    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    Location memoryLoc = new Location(world, x, y, z);
    return memoryLoc;
  }
}
