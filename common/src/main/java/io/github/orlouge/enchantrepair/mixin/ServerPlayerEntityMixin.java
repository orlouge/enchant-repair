package io.github.orlouge.enchantrepair.mixin;

import com.mojang.authlib.GameProfile;
import io.github.orlouge.enchantrepair.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow public abstract void addExperienceLevels(int levels);

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "copyFrom", at = @At(value = "HEAD"))
    public void preserveExperienceOnCopy(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (Config.XP_LEVELS_LOST_ON_DEATH >= 0) {
            this.experienceLevel = oldPlayer.experienceLevel;
            this.experienceProgress = oldPlayer.experienceProgress;
            if (Config.XP_LEVELS_LOST_ON_DEATH > 0) this.addExperienceLevels(-Config.XP_LEVELS_LOST_ON_DEATH);
        }
    }
}
