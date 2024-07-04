package plugin.memory;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.memory.command.MemoryCommand;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("memorygame").setExecutor(new MemoryCommand());
    }

    }

