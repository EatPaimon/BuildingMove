package org.eatpaimon.buildingmove;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Status {
    public Map<Player, Map<Location, Location>> map = new HashMap<>();
    public Map<Location, Location> locationMap = new HashMap<>();

    //粘贴前
    public Map<Player, List<BlockState>> cancelPlayerBlocksMap = new HashMap<>();
    //粘贴后
    public Map<Player, List<BlockState>> cancelPlayerBlocksMap1 = new HashMap<>();
}
