package com.bestinslot.client;

import com.bestinslot.BestInSlot;
import com.bestinslot.client.gui.BestInSlotScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("null")
@EventBusSubscriber(modid = BestInSlot.MODID, value = Dist.CLIENT)
public class ClientEvents {

    public static final KeyMapping OPEN_SCREEN_KEY = new KeyMapping(
            "key.bestinslot.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.bestinslot"
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_SCREEN_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new BestInSlotScreen());
            }
        }
    }

    public static void registerModBusEvents(IEventBus modEventBus) {
        modEventBus.addListener(ClientEvents::registerKeyMappings);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SCREEN_KEY);
    }
}
