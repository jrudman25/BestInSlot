package com.bestinslot;

import com.bestinslot.client.ClientEvents;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(BestInSlot.MODID)
public class BestInSlot {
    public static final String MODID = "bestinslot";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BestInSlot(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("BestInSlot loading");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEvents.registerModBusEvents(modEventBus);
        }
    }
}
