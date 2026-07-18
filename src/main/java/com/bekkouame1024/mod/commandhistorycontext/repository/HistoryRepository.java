package com.bekkouame1024.mod.commandhistorycontext.repository;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ArrayListDeque;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.bekkouame1024.mod.commandhistorycontext.CommandHistoryContext.LOGGER;
import static com.bekkouame1024.mod.commandhistorycontext.CommandHistoryContext.MOD_ID;

public class HistoryRepository {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,32}$");
    private final ArrayListDeque<String> commandHistories = new ArrayListDeque<>(50);
    
    private final Path filePath;
    
    public HistoryRepository(String profileName) throws IllegalArgumentException {
        if (!NAME_PATTERN.matcher(profileName).matches()) {
            throw new IllegalArgumentException("Invalid profile name: " + profileName);
        }
        
        this.filePath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve("histories").resolve(profileName + ".txt");
        
        if (!Files.exists(this.filePath)) {
            try {
                Files.createDirectories(this.filePath.getParent());
                Files.createFile(this.filePath);
            } catch (IOException e) {
                LOGGER.error("Failed to create history file for profile: {}", profileName, e);
                return;
            }
        }
        
        try (BufferedReader bufferedReader = Files.newBufferedReader(this.filePath, StandardCharsets.UTF_8)) {
            this.commandHistories.addAll(bufferedReader.lines().toList());
        } catch (Exception exception) {
            LOGGER.error("Failed to read {}, command history will be missing", this.filePath.getFileName(), exception);
        }
    }

    public void addCommand(String string, int maxHistorySize) {
        if (!string.equals(this.commandHistories.peekLast())) {
            if (this.commandHistories.size() >= maxHistorySize) {
                this.commandHistories.removeFirst();
            }

            this.commandHistories.addLast(string);
            this.save();
        }

    }

    private void save() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.filePath, StandardCharsets.UTF_8)) {
            for(String string : this.commandHistories) {
                bufferedWriter.write(string);
                bufferedWriter.newLine();
            }
        } catch (IOException iOException) {
            LOGGER.error("Failed to write {}, command history will be missing", this.filePath.getFileName(), iOException);
        }
    }

    public Collection<String> getHistory() {
        return this.commandHistories;
    }
}
