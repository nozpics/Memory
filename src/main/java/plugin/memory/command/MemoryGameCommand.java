package plugin.memory.command;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

  private static final int AREA_RADIUS = 10;
  private static final int AREA_DIAMETER = AREA_RADIUS * 2;
  private final Map<UUID, Pair> lastTouched = new HashMap<>();
  private final Map<String, Integer> playerScores = new HashMap<>();
  private final Main main;
  private final PlayerScoreData playerScoreData = new PlayerScoreData();
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

    startGameCountdown(player,difficulty);
    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender commandSender,Command command, String label,
      String[] args) {
    return false;
  }



  /**
   * 現在登録されているスコアの一覧をメッセージに送る。
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

  /**
   * 難易度をコマンド引数から取得。
   * @param player　コマンドを実行したプレイヤー
   * @param args　コマンド引数
   * @return NONE
   */
  private String getDifficulty(Player player, String[] args) {
    if(args.length ==1 && (EASY.equals(args[0]) || NORMAL.equals(args[0]) || HARD.equals(
        args[0]))){
      return args[0];
    }
    player.sendMessage(ChatColor.RED + "実行できません。コマンド引数の１つ目に[easy,normal,hard]いずれかの難易度指定が必要です。");
    return NONE;
  }

  /**
   * ゲーム開始前のカウントダウン処理。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param difficulty　難易度
   */
  private void startGameCountdown(Player player, String difficulty) {
    new BukkitRunnable() {
      int countdown = 3;

      @Override
      public void run() {
        if (countdown > 0) {
          player.sendTitle(ChatColor.WHITE + String.valueOf(countdown), "", 0, 20, 0);
          player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
          countdown--;
        } else {
          initGame(player, difficulty);
          finalCountDown(player, difficulty);
          cancel();
        }
      }
    }.runTaskTimer(main, 0, 20L);
  }

  /**
   * ゲームを開始する処理。
   * 制限時間の表示、エンティティの生成。
   * @param player　コマンドを実行したプレイヤー
   * @param difficulty 難易度
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
   * 難易度に合わせてブロックやエンティティのペアを生成する。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param difficulty 難易度
   */
  public void getSpawnLocation(Player player, String difficulty){
    Pairs pairs = Pairs.getInstance();
    Location playerLocation = player.getLocation();

    for (Pair pair : pairs.getPairs()) {
      for (int j = 0; j < 2; j++) {
        Location memoryLoc = getMemoryLoc(player.getWorld(), playerLocation);
        if (memoryLoc.getBlock().getType() != Material.AIR) {
          j--;
          continue;
        }
        switch (difficulty) {
          case EASY:
            handleEasyDifficulty(memoryLoc, pair);
            break;
          case NORMAL:
            handleNormalDifficulty(memoryLoc, pair, player);
            break;
          case HARD:
            handleHardDifficulty(memoryLoc, pair, player);
            break;
          default:
            throw new IllegalArgumentException("予期せぬ難易度の値: " + difficulty);
        }
      }
    }
  }

  /**
   * ブロックとエンティティの出現エリアを取得する
   * @param world　プレイヤーが所属するワールド
   * @param playerLocation　コマンドを実行したプレイヤーの現在地
   * @return memoryLoc 出現エリア
   */

  private static Location getMemoryLoc(World world, Location playerLocation) {
    int randomX = new SplittableRandom().nextInt(AREA_DIAMETER) - AREA_RADIUS;
    int randomZ = new SplittableRandom().nextInt(AREA_DIAMETER) - AREA_RADIUS;
    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(world, x, y, z);
  }

  /**
   * 難易度easyの処理。ダイヤモンドブロックのペアを生成する。
   *
   * @param memoryLoc　ブロックの出現エリア
   * @param pair　ペア要素
   */
  private void handleEasyDifficulty(Location memoryLoc, Pair pair) {
    memoryLoc.getBlock().setType(Material.DIAMOND_BLOCK);
    pair.addBlock(memoryLoc.getBlock());
  }

  /**
   * 難易度normalの処理。白い羊のペアを生成する。
   *
   * @param memoryLoc　エンティティの出現エリア
   * @param pair　ペア要素
   * @param player　コマンドを実行したプレイヤー
   */
  private void handleNormalDifficulty(Location memoryLoc, Pair pair, Player player) {
    Sheep sheep = (Sheep) player.getWorld().spawnEntity(memoryLoc, EntityType.SHEEP);
    sheep.setColor(DyeColor.WHITE);
    pair.addEntity(sheep);
  }

  /**
   * 難易度hardの処理。プレイヤーの状態を整え、ゾンビのペアを生成する。
   *
   * @param memoryLoc　エンティティの出現エリア
   * @param pair　ペア要素
   * @param player　コマンドを実行したプレイヤー
   */
  private void handleHardDifficulty(Location memoryLoc, Pair pair, Player player) {
    initPlayerStatus(player);
    Zombie zombie = (Zombie) player.getWorld().spawnEntity(memoryLoc, EntityType.ZOMBIE);
    pair.addEntity(zombie);
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
   * 制限時間20秒を設定後、BossBarにて視認できるようにする。
   * ゲーム終了３秒前からカウントダウンし、ゲーム終了する。
   * @param player　コマンドを実行したプレイヤー
   * @param difficulty　難易度
   */
  private void finalCountDown(Player player, String difficulty) {
    new BukkitRunnable() {
      int time = 20;

      @Override
      public void run() {
        if(time > 0){
          double progress = time / 20.0;
          bossBar.setProgress(progress);

          if (time <= 3) {
            String title = ChatColor.WHITE + String.valueOf(time);
            player.sendTitle(title, "", 0, 20, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE,0.5f,1.0f);
          }
          time--;
        } else {
          endGame(player, difficulty);
          cancel();
        }
      }
    }.runTaskTimer(main, 0,20L);
  }

  /**
   * ゲーム終了時に動作するもの。
   * ゲーム終了をサウンドとともに画面表示し、残っているエンティティを削除後、最終スコアを登録する。
   * @param player　コマンドを実行したプレイヤー
   * @param difficulty  難易度
   */
  private void endGame(Player player, String difficulty) {
    bossBar.setVisible(false);

    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_5,1.0f,1.0f);
    int finalScore = playerScores.get(player.getName());
    player.sendTitle("ゲーム終了！", player.getName() + " : " + finalScore + "点 (" + difficulty
        + ")", 10, 50, 20);

    Pairs pairs = Pairs.getInstance();
    pairs.getPairs().forEach(Pair::removeBlocks);
    pairs.getPairs().forEach(Pair::removeEntities);

    playerScoreData.insert(new PlayerScore(player.getName(),finalScore, difficulty));
  }



  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
      return;
    }

    Player player = event.getPlayer();
    Block block = event.getClickedBlock();

    if (block == null || block.getType() != Material.DIAMOND_BLOCK) {
      return;
    }

    UUID playerId = player.getUniqueId();
    String playerName = player.getName();
    Pairs pairs = Pairs.getInstance();

    for(Pair pair : pairs.getPairs()){
      if(pair.containsBlock(block)){
        removePairs(pair, player, playerId, playerName);
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
  Pairs pairs = Pairs.getInstance(); //

  for(Pair pair : pairs.getPairs()){
    if(pair.containsEntity(clickedEntity)){
      removePairs(pair, player, playerId, playerName);
    }
  }
}

  /**
   * 　右クリックしたペアが揃えば削除し、スコアを加算します。
   *
   * @param pair　ペア要素
   * @param player　コマンドを実行したプレイヤー
   * @param playerId　プレイヤーの情報
   * @param playerName　プレイヤーの名前
   */
  private void removePairs(Pair pair, Player player, UUID playerId, String playerName) {
    player.sendMessage(pair.getName());
    if(this.lastTouched.containsKey(playerId) && this.lastTouched.get(playerId) == pair){
      pair.removeBlocks();
      pair.removeEntities();
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1.0f,1.0f);
      addScore(player, playerName);
    }else{
      this.lastTouched.put(playerId, pair);
      player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,1.0f,1.0f);
    }
  }

  /**
   * スコアの加算を行います。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param playerName　プレイヤーの名前
   */
  private void addScore(Player player, String playerName) {
    int newScore = playerScores.getOrDefault(playerName, 0) + 10;
    playerScores.put(playerName, newScore);
    player.sendMessage("10点！　現在のスコアは" + newScore + "点");
  }
}

