package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DrownedEntity.class)
public class DrownedEntityMixin extends ZombieEntity {
    public DrownedEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public DrownedEntityMixin(World world) {
        super(world);
    }

    @Inject(method = "initEquipment", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/mob/DrownedEntity;equipStack(Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)V"))
    public void equipLoyalty(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        if (Config.DROWNED_SPAWNS_WITH_LOYALTY && random.nextInt(5) == 0) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.set(Map.of(Enchantments.SWEEPING, random.nextInt(3) + 1), book);
            this.equipStack(EquipmentSlot.OFFHAND, book);
            this.updateDropChances(EquipmentSlot.OFFHAND);
        }
    }
}
