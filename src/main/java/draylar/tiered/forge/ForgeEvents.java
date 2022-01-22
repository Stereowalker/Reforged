package draylar.tiered.forge;

import draylar.tiered.network.AttributeSyncer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import static draylar.tiered.Tiered.HANDLER;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE, modid = "tiered")
public class ForgeEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent par0){
        if(par0.getPlayer().level.isClientSide) return;
        HANDLER.sendTo(new AttributeSyncer(), ((ServerPlayer)par0.getPlayer()).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }
}
