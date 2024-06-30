package plugin.memory.command;

import java.util.SplittableRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MemoryCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s,
      String[] strings) {
    if (commandSender instanceof Player player){
      World world = player.getWorld();

      player.setHealth(20);
      player.setFoodLevel(20);


        world.getBlockAt(getSpawnLocation(player, world)).setType(Material.DIAMOND_BLOCK);
        world.getBlockAt(getSpawnLocation(player, world)).setType(Material.DIAMOND_BLOCK);
        world.getBlockAt(getSpawnLocation(player, world)).setType(Material.DIAMOND_BLOCK);
        world.getBlockAt(getSpawnLocation(player, world)).setType(Material.DIAMOND_BLOCK);
    }
    return false;
  }

  /**
   * エンティティの出現エリアを取得する。
   * @param player　コマンドを実行したプレイヤー
   * @param world   コマンドを実行したプレイヤーが所属するワールド
   * @return        敵の出現場所
   */
  private Location getSpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20) -10 ;
    int randomZ = new SplittableRandom().nextInt(20) -10 ;
    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(world, x,y,z);
  }
}
