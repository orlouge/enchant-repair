package io.github.orlouge.enchantrepair.forge;

import io.github.orlouge.enchantrepair.EnchantRepairMod;
import io.github.orlouge.enchantrepair.ModifiedGrindstoneHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(EnchantRepairMod.MOD_ID)
public class ExampleModForge {
    public ExampleModForge() {
        EnchantRepairMod.init();
        MinecraftForge.EVENT_BUS.addListener(this::onGrindstoneEvent);
    }

    private void onGrindstoneEvent(GrindstoneEvent.OnTakeItem event) {
        ModifiedGrindstoneHelper.modifyTopSlot(event.getTopItem(), event.getBottomItem()).ifPresent(event::setNewTopItem);
        ModifiedGrindstoneHelper.modifyBottomSlot(event.getBottomItem()).ifPresent(event::setNewBottomItem);
    }
}
