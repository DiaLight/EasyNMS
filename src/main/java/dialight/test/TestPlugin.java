package dialight.test;

import net.minecraft.Util;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CraftServer craftServer = (CraftServer) getServer();
        final DedicatedPlayerList playerList = craftServer.getHandle();
        final DedicatedServer server = playerList.getServer();
        server.getAbsoluteMaxWorldSize()
//        server.a()
        LightningStrikeTrigger trigger;
        trigger.b();
        SimpleCriterionTrigger strigger;
        strigger.addPlayerListener();
        Util utils;
    }

    @Override
    public void onDisable() {

    }

}
