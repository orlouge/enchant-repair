package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin extends HostileEntity {
    @Shadow @Final private static TrackedData<Boolean> CHARGED;

    protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "dropEquipment", at = @At("HEAD"))
    public void dropBlastProtIfCharged(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (!Config.CHARGED_CREEPER_DROPS_BLAST_PROT) return;
        if (this.dataTracker.get(CHARGED) && this.getRandom().nextFloat() < 0.5 + 0.2 * lootingMultiplier) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.set(Map.of(
                    Enchantments.BLAST_PROTECTION,
                    Math.min(4, this.getRandom().nextInt(4 + lootingMultiplier) + 1)
            ), book);
            this.dropStack(book);
        }
    }
}
