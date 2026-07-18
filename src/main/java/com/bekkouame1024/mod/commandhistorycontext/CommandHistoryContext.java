package com.bekkouame1024.mod.commandhistorycontext;

import com.bekkouame1024.mod.commandhistorycontext.repository.ProfilesRepository;
import com.bekkouame1024.mod.commandhistorycontext.service.ProfilesService;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommandHistoryContext implements ModInitializer {
    public static final String MOD_ID = "commandhistorycontext";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        try {
            new ProfilesService(new ProfilesRepository()).copyTemplateIfMissing();
        } catch (IOException e) {
            LOGGER.error("Failed to copy template profiles.toml", e);
        }
    }
}