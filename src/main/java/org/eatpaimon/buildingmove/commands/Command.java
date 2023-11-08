package org.eatpaimon.buildingmove.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
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
    Map<List<Block>, Integer> rotateTimeMap = new HashMap<>();
    List<Material> materialList = new ArrayList<>();
    List<BlockData> blockDataList = new ArrayList<>();
    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if (strings.length == 0){
            commandSender.sendMessage("<建筑移动> 作者--吃一口pai蒙");
            commandSender.sendMessage("/bm pos1 -- 设置第一个坐标点");
            commandSender.sendMessage("/bm pos2 -- 设置第二个坐标点");
            commandSender.sendMessage("/bm cut -- 剪切两个坐标点内的方块");
            commandSender.sendMessage("/bm rotate -- 顺时针旋转剪贴板的方块");
            commandSender.sendMessage("/bm paste -- 于 pos1 ——> pos2 的方向粘贴剪切的方块（此时原位置剪切的方块消失）");
            commandSender.sendMessage("/bm cancel -- 撤销操作");
            commandSender.sendMessage("§4警告：剪切位置与粘贴位置禁止重合，如涉及旋转请仔细计算，如重合会导致结构损坏");
            return true;
        }
        if (strings.length == 1 && strings[0].equals("pos1")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4控制台不能执行此命令");
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
            return true;
        }
        if (strings.length == 1 && strings[0].equals("pos2")){
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4控制台不能执行此命令");
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
                    commandSender.sendMessage("§4您没有权限");
                }
            }
            return true;
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
                        rotateTimeMap.put(blocks, 0);
                        status.cancelPlayerBlocksMap.remove(player);
                        status.cancelPlayerBlocksMap1.remove(player);
                        blocks1.clear();

                    } else {
                        commandSender.sendMessage("§4未选定区域");
                    }
                }else {
                    commandSender.sendMessage("§4您没有权限");
                }
            }else {
                commandSender.sendMessage("§4控制台不能执行此命令");
            }
            return true;
        }
        if (strings.length == 1 && strings[0].equals("paste")) {
            if (commandSender instanceof Player) {
                if (commandSender.hasPermission("bm.paste")) {
                    Player player = ((Player) commandSender).getPlayer();
                    if (playerBlocksMap.containsKey(player)) {
                        Location location0 = pos1Map.get(player);
                        if (player.getWorld() == location0.getWorld()) {
                            int centerX = ((pos1Map.get(player).getBlockX() + pos2Map.get(player).getBlockX()) / 2);
                            int centerZ = ((pos1Map.get(player).getBlockZ() + pos2Map.get(player).getBlockZ()) / 2);

                            List<BlockState> blockStateList = new ArrayList<>();
                            List<BlockState> blockStateList1 = new ArrayList<>();
                            for (Block block : blocks) {
                                blockStateList.add(block.getState());
                            }

                            status.cancelPlayerBlocksMap.put(player, blockStateList);

                            if (rotateTimeMap.get(playerBlocksMap.get(player)) == 0) {
                                Location newLocation = player.getLocation().clone();
                                int i = 0;
                                for (Block block : playerBlocksMap.get(player)) {
                                    int deltaX = block.getX() - pos1Map.get(player).getBlockX();
                                    int deltaY = block.getY() - pos1Map.get(player).getBlockY();
                                    int deltaZ = block.getZ() - pos1Map.get(player).getBlockZ();

                                    Location location = newLocation.clone().add(deltaX, deltaY, deltaZ);
                                    blocks1.add(location.getBlock());

                                    blockStateList1.add(location.getBlock().getState());

                                    materialList.add(block.getState().getType());
                                    blockDataList.add(block.getState().getBlockData());
                                    block.getLocation().getBlock().setType(Material.AIR);

                                    location.getBlock().setType(materialList.get(i));
                                    location.getBlock().setBlockData(blockDataList.get(i));

                                    i = i + 1;
                                }
                            } else if (rotateTimeMap.get(playerBlocksMap.get(player)) == 1) {
                                rotateAndTranslate(centerX, centerZ, 90, player, playerBlocksMap.get(player), blockStateList1);
                            } else if (rotateTimeMap.get(playerBlocksMap.get(player)) == 2) {
                                rotateAndTranslate(centerX, centerZ, 180, player, playerBlocksMap.get(player), blockStateList1);
                            } else if (rotateTimeMap.get(playerBlocksMap.get(player)) == 3) {
                                rotateAndTranslate(centerX, centerZ, 270, player, playerBlocksMap.get(player), blockStateList1);
                            }
                            status.cancelPlayerBlocksMap1.put(player, blockStateList1);
                            commandSender.sendMessage("§a已粘贴");
                            status.locationMap.remove(pos1Map.get(player));
                            playerBlocksMap.remove(player);
                            pos1Map.remove(player);
                            pos2Map.remove(player);
                            status.map.remove(player);
                            rotateTimeMap.remove(blocks);
                            materialList.clear();
                            blockDataList.clear();
                        } else {
                            commandSender.sendMessage("§4跨世界无法使用");
                        }
                    } else {
                        commandSender.sendMessage("§4您的剪贴板是空的！");
                    }
                } else {
                    commandSender.sendMessage("§4您没有权限");
                }
            } else {
                commandSender.sendMessage("§4控制台不能执行此命令");
            }
            return true;
        }
        if (strings.length == 1 && strings[0].equals("cancel")){
            if (commandSender instanceof Player){
                if (commandSender.hasPermission("bm.cancel")){
                    Player player = ((Player) commandSender).getPlayer();
                    if (status.cancelPlayerBlocksMap.containsKey(player) &&
                    status.cancelPlayerBlocksMap1.containsKey(player)){

                        int i = 0;
                        for (BlockState block1 : status.cancelPlayerBlocksMap1.get(player)){
                            block1.getLocation().getBlock().setType(block1.getType());
                            block1.getLocation().getBlock().setBlockData(block1.getBlockData());

                            BlockState block = status.cancelPlayerBlocksMap.get(player).get(i);
                            block.getLocation().getBlock().setType(block.getType());
                            block.getLocation().getBlock().setBlockData(block.getBlockData());

                            i = i + 1;
                        }
                        status.cancelPlayerBlocksMap1.remove(player);
                        status.cancelPlayerBlocksMap.remove(player);
                        commandSender.sendMessage("§e已撤销操作！");
                    } else {
                        commandSender.sendMessage("§4您没有要撤销的操作！");
                    }
                }else {
                    commandSender.sendMessage("§4您没有权限");
                }
            }else {
                commandSender.sendMessage("§4控制台不能执行此命令");
            }
            return true;
        }
        if (strings.length == 1 && strings[0].equals("rotate")) {
            if (commandSender instanceof Player) {
                if (commandSender.hasPermission("bm.rotate")) {
                    Player player = ((Player) commandSender).getPlayer();
                    if (rotateTimeMap.containsKey(playerBlocksMap.get(player))) {
                        if (rotateTimeMap.get(playerBlocksMap.get(player)) < 3) {
                            int i = rotateTimeMap.get(playerBlocksMap.get(player));
                            i = i + 1;
                            rotateTimeMap.put(playerBlocksMap.get(player), i);
                        } else if (rotateTimeMap.get(playerBlocksMap.get(player)) >= 3) {
                            int i = 0;
                            rotateTimeMap.put(playerBlocksMap.get(player), i);
                        }
                        commandSender.sendMessage("§a已顺时针旋转90°！");
                    } else {
                        commandSender.sendMessage("§4您的剪贴板是空的！");
                    }
                } else {
                    commandSender.sendMessage("§4您没有权限");
                }
            } else {
                commandSender.sendMessage("§4控制台不能执行此命令");
            }
            return true;
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
    public void rotateAndTranslate(int centerX, int centerZ, int angle, Player player, List<Block> blocks, List<BlockState> blockStateList1) {
        Location newLocation = player.getLocation().clone();
        int i = 0;
        for (Block block : blocks) {
            int cornerX = block.getX();
            int cornerY = block.getY();
            int cornerZ = block.getZ();

            int relativeX = cornerX - centerX;
            int relativeZ = cornerZ - centerZ;

            double radian = Math.toRadians(angle);
            int newCornerX = (int) Math.round(relativeX * Math.cos(radian) - relativeZ * Math.sin(radian));
            int newCornerZ = (int) Math.round(relativeX * Math.sin(radian) + relativeZ * Math.cos(radian));

            int translateX = newCornerX + centerX;
            int translateZ = newCornerZ + centerZ;

            int deltaX0 = translateX - pos1Map.get(player).getBlockX();
            int deltaY0 = cornerY - pos1Map.get(player).getBlockY();
            int deltaZ0 = translateZ - pos1Map.get(player).getBlockZ();

            Location location = newLocation.clone().add(deltaX0, deltaY0, deltaZ0);
            blocks1.add(location.getBlock());

            blockStateList1.add(location.getBlock().getState());

            materialList.add(block.getState().getType());
            blockDataList.add(block.getState().getBlockData());
            block.getLocation().getBlock().setType(Material.AIR);

            location.getBlock().setType(materialList.get(i));
            location.getBlock().setBlockData(blockDataList.get(i));

            rotateBlock(location.getBlock(), angle);

            i = i + 1;
        }
    }
    private void rotateBlock(Block block, double angle) {
        if (block.getBlockData() instanceof Directional) {
            Directional directional = (Directional) block.getBlockData();
            int times = 0;
            if (angle == 90) {
                times = 1;
            } else if (angle == 180) {
                times = 2;
            } else if (angle == 270) {
                times = 3;
            }
            for (int i = 0; i < times; i++) {
                directional.setFacing(rotateClockwise(directional.getFacing()));
                block.setBlockData(directional);
                block.getState().update();
                // 重新获取更新后的directional
                directional = (Directional) block.getBlockData();
            }
        }
    }


    private BlockFace rotateClockwise(BlockFace currentDirection) {
        switch (currentDirection) {
            case NORTH:
                return BlockFace.EAST;
            case SOUTH:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.SOUTH;
            case WEST:
                return BlockFace.NORTH;
            default:
                throw new IllegalArgumentException("Invalid direction " + currentDirection);
        }
    }


}
