package org.eatpaimon.buildingmove;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.eatpaimon.buildingmove.commands.Command;

public final class BuildingMove extends JavaPlugin {
    private final Status status = new Status();

    @Override
    public void onEnable() {

        Bukkit.getPluginCommand("buildingmove").setExecutor(new Command(status));
        Bukkit.getLogger().info("[建筑移动]插件已加载 作者--吃一口pai蒙");
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
