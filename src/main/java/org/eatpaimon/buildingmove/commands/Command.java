package org.eatpaimon.buildingmove.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.eatpaimon.buildingmove.Status;

import java.util.*;

public class Command implements CommandExecutor {
    private Status status;
    public Command(Status status){
        this.status = status;
    }
    Map<Player, Location> pos1Map = new HashMap<>();
    Map<Player, Location> pos2Map = new HashMap<>();
    List<Block> blocks = new ArrayList<>();
    List<Block> blocks1 = new ArrayList<>();
    Map<Player, List<Block>> playerBlocksMap = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if (strings.length == 0){
            commandSender.sendMessage("<建筑移动> 作者--吃一口pai蒙");
            commandSender.sendMessage("/bm pos1 -- 设置第一个坐标点");
            commandSender.sendMessage("/bm pos2 -- 设置第二个坐标点");
            commandSender.sendMessage("/bm cut -- 剪切两个坐标点内的方块");
            commandSender.sendMessage("/bm paste -- 于 pos1 ——> pos2 的方向粘贴剪切的方块（此时原位置剪切的方块消失）");
            commandSender.sendMessage("/bm cancel -- 撤销操作");
        }
        if (strings.length == 1 && strings[0].equals("pos1")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("控制台不能执行此命令");
            }else {
                if (commandSender.hasPermission("bm.pos")) {
                    Player player = ((Player) commandSender).getPlayer();
                    Location pos1 = player.getLocation();
                    pos1Map.put(player, pos1);
                    commandSender.sendMessage("已选点1： §a" + pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ());
                    if (pos2Map.containsKey(player)) {
                        Location pos2 = pos2Map.get(player);
                        status.locationMap.put(pos1, pos2);
                        status.map.put(player, status.locationMap);
                    }
                }
                else {
                    commandSender.sendMessage("您没有权限");
                }
            }
        }
        if (strings.length == 1 && strings[0].equals("pos2")){
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("控制台不能执行此命令");
            }else {
                if (commandSender.hasPermission("bm.pos")) {
                    Player player = ((Player) commandSender).getPlayer();
                    Location pos2 = player.getLocation();
                    pos2Map.put(player, pos2);
                    commandSender.sendMessage("已选点2： §e" + pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ());
                    if (pos1Map.containsKey(player)) {
                        Location pos1 = pos1Map.get(player);
                        status.locationMap.put(pos1, pos2);
                        status.map.put(player, status.locationMap);
                    }
                }else {
                    commandSender.sendMessage("您没有权限");
                }
            }
        }
        if (strings.length == 1 && strings[0].equals("cut")){
            if (commandSender instanceof Player) {
                if (commandSender.hasPermission("bm.cut")) {
                    Player player = ((Player) commandSender).getPlayer();
                    if (status.map.containsKey(player)) {
                        Map<Location, Location> pos = status.map.get(player);
                        Set<Location> pos1Set = pos.keySet();
                        commandSender.sendMessage("§a已剪切");
                        for (Location pos1 : pos1Set) {
                            Location pos2 = pos.get(pos1);
                            blocks = getBlocksInRegion(pos1, pos2);
                            playerBlocksMap.put(player, blocks);
                        }
                        status.cancelPlayerBlocksMap.remove(player);
                        status.cancelPlayerBlocksMap1.remove(player);
                        blocks1.clear();

                    } else {
                        commandSender.sendMessage("§4未选定区域");
                    }
                }else {
                    commandSender.sendMessage("您没有权限");
                }
            }else {
                commandSender.sendMessage("控制台不能执行此命令");
            }
        }
        if (strings.length == 1 && strings[0].equals("paste"))
            if (commandSender instanceof Player) {
                if (commandSender.hasPermission("bm.paste")) {
                    Player player = ((Player) commandSender).getPlayer();
                    if (playerBlocksMap.containsKey(player)) {
                        Location location0 = pos1Map.get(player);
                        if (player.getWorld() == location0.getWorld()) {
                            Location newLocation = player.getLocation().clone();
                            for (Block block : playerBlocksMap.get(player)) {
                                int deltaX = block.getX() - pos1Map.get(player).getBlockX();
                                int deltaY = block.getY() - pos1Map.get(player).getBlockY();
                                int deltaZ = block.getZ() - pos1Map.get(player).getBlockZ();

                                Location location = newLocation.clone().add(deltaX, deltaY, deltaZ);
                                location.getBlock().setType(block.getType());
                                location.getBlock().setBlockData(block.getBlockData());
                                block.getLocation().getBlock().setType(Material.AIR);
                                blocks1.add(location.getBlock());
                            }
                            status.cancelPlayerBlocksMap.put(player, blocks);
                            status.cancelPlayerBlocksMap1.put(player, blocks1);
                            commandSender.sendMessage("§a已粘贴");
                            status.locationMap.remove(pos1Map.get(player));
                            playerBlocksMap.remove(player);
                            pos1Map.remove(player);
                            pos2Map.remove(player);
                            status.map.remove(player);
                        }
                    }else {
                        commandSender.sendMessage("跨世界无法使用");
                    }
                }else {
                    commandSender.sendMessage("您没有权限");
                }
            }else {
                commandSender.sendMessage("控制台不能执行此命令");
            }
        if (strings.length == 1 && strings[0].equals("cancel")){
            if (commandSender instanceof Player){
                if (commandSender.hasPermission("bm.cancel")){
                    Player player = ((Player) commandSender).getPlayer();
                    if (status.cancelPlayerBlocksMap.containsKey(player) &&
                    status.cancelPlayerBlocksMap1.containsKey(player)){
                        int i = 0;
                        for (Block block1 : status.cancelPlayerBlocksMap1.get(player)){
                            Block block = status.cancelPlayerBlocksMap.get(player).get(i);
                            block.setType(block1.getType());
                            block.setBlockData(block1.getBlockData());
                            block1.setType(Material.AIR);
                            i = i + 1;
                        }
                        commandSender.sendMessage("§e已撤销操作！");
                    } else {
                        commandSender.sendMessage("§4您没有要撤销的操作！");
                    }
                }else {
                    commandSender.sendMessage("您没有权限");
                }
            }else {
                commandSender.sendMessage("控制台不能执行此命令");
            }
        }
        return false;
    }
    private List<Block> getBlocksInRegion(Location pos1, Location pos2) {
        List<Block> blocks = new ArrayList<>();
        World world = pos1.getWorld();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

}
