package org.eatpaimon.buildingmove;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Status {
    public Map<Player, Map<Location, Location>> map = new HashMap<>();
    public Map<Location, Location> locationMap = new HashMap<>();

    public Map<Player, List<Block>> cancelPlayerBlocksMap = new HashMap<>();
    public Map<Player, List<Block>> cancelPlayerBlocksMap1 = new HashMap<>();
}
