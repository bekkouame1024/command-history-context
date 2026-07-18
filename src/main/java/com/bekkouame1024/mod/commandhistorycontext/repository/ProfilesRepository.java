package com.bekkouame1024.mod.commandhistorycontext.repository;

import net.fabricmc.loader.api.FabricLoader;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.bekkouame1024.mod.commandhistorycontext.CommandHistoryContext.MOD_ID;

public class ProfilesRepository {
    private final Path filePath;
    
    public ProfilesRepository() {
        this.filePath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve("profiles.toml");
    }
    
    public TomlParseResult getParseResult() throws IOException {
        if (!Files.exists(this.filePath)) {
            Files.createDirectories(this.filePath.getParent());
            Files.createFile(this.filePath);
        }
        
        return Toml.parse(this.filePath);
    }

    public void copyTemplateIfMissing() throws IOException {
        if (Files.exists(this.filePath)) {
            return;
        }

        Files.createDirectories(this.filePath.getParent());

        try (InputStream in = ProfilesRepository.class.getClassLoader().getResourceAsStream("templates/profiles.toml")) {
            if (in == null) {
                throw new IOException("Template profiles.toml not found in resources.");
            }

            Files.copy(in, this.filePath);
        }
    }
}
