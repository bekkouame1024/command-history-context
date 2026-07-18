package com.bekkouame1024.mod.commandhistorycontext.mixin.client;

import com.bekkouame1024.mod.commandhistorycontext.model.Profile;
import com.bekkouame1024.mod.commandhistorycontext.repository.HistoryRepository;
import com.bekkouame1024.mod.commandhistorycontext.repository.ProfilesRepository;
import com.bekkouame1024.mod.commandhistorycontext.service.ProfilesService;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.CommandHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ArrayListDeque;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static com.bekkouame1024.mod.commandhistorycontext.CommandHistoryContext.LOGGER;

@Mixin(CommandHistory.class)
public abstract class CommandHistoryMixin {
    @Unique
    private static int maxHistorySize = Math.toIntExact(new ProfilesService(new ProfilesRepository()).getVanillaMaxSize());

    @Final
    @Shadow
    private ArrayListDeque<String> lastCommands;

    @Shadow
    private void save() {
        
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void trimHistory(Path path, CallbackInfo ci) {
        while (lastCommands.size() > maxHistorySize) {
            lastCommands.removeFirst();
        }
        
        this.save();
    }

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 50)
    )
    private int changeHistorySize(int original) {
        return maxHistorySize;
    }

    @ModifyReturnValue(
            method = "history",
            at = @At("RETURN")
    )
    private Collection<String> replaceHistory(Collection<String> original) {
        ProfilesService profilesService = new ProfilesService(new ProfilesRepository());
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.isSingleplayer()) {
            return original;
        }

        ServerData serverData = mc.getCurrentServer();
        if (serverData == null) {
            LOGGER.warn("Server data is null");
            return original;
        }
        LOGGER.info("Current server address: {}", serverData.ip);
        
        try {
            if (!profilesService.isInAnyProfile(serverData.ip)) {
                return original;
            }

            HistoryRepository historyRepository = new HistoryRepository(profilesService.getProfileByServerAddress(serverData.ip).profileName());
            return historyRepository.getHistory();
            
        } catch (IOException e) {
            LOGGER.error("Failed to check if server is in any profile, returning original history", e);
        }

        return original;
    }

    /**
     * @author bekkouame1024
     * @reason Override the addCommand method to save command history per profile instead of globally.
     */
    @Overwrite
    public void addCommand(String string) {
        ProfilesService profilesService = new ProfilesService(new ProfilesRepository());
        Minecraft mc = Minecraft.getInstance();

        if (mc.isSingleplayer()) {
            addCommandToDefault(string);
            return;
        }

        ServerData serverData = mc.getCurrentServer();
        if (serverData == null) {
            addCommandToDefault(string);
            return;
        }
        
        try {
            Profile profile = profilesService.getProfileByServerAddress(serverData.ip);
            HistoryRepository historyRepository = new HistoryRepository(profile.profileName());
            
            historyRepository.addCommand(string, profilesService.getMaxHistorySize());
        } catch (IOException e) {
            LOGGER.error("Failed to add command to history for server {}, adding to default history instead", serverData.ip, e);
            addCommandToDefault(string);
        }
    }
    
    @Unique
    private void addCommandToDefault(String string) {
        if (!string.equals(this.lastCommands.peekLast())) {
            if (this.lastCommands.size() >= maxHistorySize) {
                this.lastCommands.removeFirst();
            }

            this.lastCommands.addLast(string);
            this.save();
        }
    }
}
