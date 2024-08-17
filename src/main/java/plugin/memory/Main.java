package plugin.memory;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.memory.command.MemoryGameCommand;

public final class Main extends JavaPlugin implements Listener {
    private MemoryGameCommand memoryGameCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        memoryGameCommand = new MemoryGameCommand(this);
        Bukkit.getPluginManager().registerEvents(memoryGameCommand, this);
        getCommand("memorygame").setExecutor(memoryGameCommand);
    }
    }

