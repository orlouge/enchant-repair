package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    protected int enchantmentTableSeed;

    @Shadow public float experienceProgress;

    @Shadow public int experienceLevel;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "applyEnchantmentCosts", at = @At("HEAD"), cancellable = true)
    public void onEnchantmentCostApplied(ItemStack enchantedItem, int experienceLevels, CallbackInfo ci) {
        if (Config.DISABLE_ENCHANTING_XP_COST) {
            this.enchantmentTableSeed = this.random.nextInt();
            ci.cancel();
        }
    }

    @Inject(method = "getXpToDrop", at = @At("RETURN"), cancellable = true)
    public void setXpToDrop(CallbackInfoReturnable<Integer> cir) {
        if (Config.XP_LEVELS_LOST_ON_DEATH >= 0) {
            double xp = 0;
            if (Config.XP_LOST_DROPPED > 0 && Config.XP_LEVELS_LOST_ON_DEATH > 0 && cir.getReturnValue() > 0) {
                xp = xpAtLevel(this.experienceLevel) + xpDiffAtLevel(this.experienceLevel) * this.experienceProgress;
                int newLevel = Math.max(0, this.experienceLevel - Config.XP_LEVELS_LOST_ON_DEATH);
                double newProgress = this.experienceLevel - Config.XP_LEVELS_LOST_ON_DEATH >= 0 ? this.experienceProgress : 0;

                xp -= xpAtLevel(newLevel) + xpDiffAtLevel(newLevel) * newProgress;
                xp *= Config.XP_LOST_DROPPED * 0.01;
            }
            cir.setReturnValue((int) Math.floor(xp));
            cir.cancel();
        }
    }

    private static double xpAtLevel(int level) {
        if (level <= 16) return level * level + 6 * level;
        else if (level <= 31) return 2.5 * level * level - 40.5 * level + 360;
        else return 4.5 * level * level - 162.5 * level + 2220;
    }

    private static int xpDiffAtLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }
}
