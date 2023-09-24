package io.github.orlouge.enchantrepair.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.orlouge.enchantrepair.EnchantRepairMod;
import io.github.orlouge.enchantrepair.ModifiedGrindstoneHelper;
import io.github.orlouge.enchantrepair.ModifiedLootTables;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;

import net.minecraft.util.Identifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;

@Mod(EnchantRepairMod.MOD_ID)
public class ExampleModForge {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(
            ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, EnchantRepairMod.MOD_ID);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_LOOT_POOLS = LOOT_MODIFIERS.register("add_loot_pools", () -> AddLootPoolModifier.CODEC);

    public ExampleModForge() {
        EnchantRepairMod.init();
        MinecraftForge.EVENT_BUS.addListener(this::onGrindstoneEvent);
        LOOT_MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void onGrindstoneEvent(GrindstoneEvent.OnTakeItem event) {
        ModifiedGrindstoneHelper.modifyTopSlot(event.getTopItem(), event.getBottomItem()).ifPresent(event::setNewTopItem);
        ModifiedGrindstoneHelper.modifyBottomSlot(event.getBottomItem()).ifPresent(event::setNewBottomItem);
    }

    public static class AddLootPoolModifier extends LootModifier {
        public static final Codec<AddLootPoolModifier> CODEC = RecordCodecBuilder.create(inst -> LootModifier.codecStart(inst).and(
                    Codec.STRING.fieldOf("loot_table").forGetter(m -> m.lootTable)
                ).apply(inst, AddLootPoolModifier::new));
        private final String lootTable;

        public AddLootPoolModifier(LootCondition[] conditions, String lootTable) {
            super(conditions);
            this.lootTable = lootTable;
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            Collection<LootPool.Builder> pools = ModifiedLootTables.POOLS.get(new Identifier(this.lootTable));
            if (pools != null) {
                pools.forEach(pool -> pool.build().addGeneratedLoot(generatedLoot::add, context));
            }
            return generatedLoot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }
}
