package com.bekkouame1024.mod.commandhistorycontext.service;

import com.bekkouame1024.mod.commandhistorycontext.model.Profile;
import com.bekkouame1024.mod.commandhistorycontext.repository.ProfilesRepository;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bekkouame1024.mod.commandhistorycontext.CommandHistoryContext.LOGGER;

public class ProfilesService {
    private final ProfilesRepository profilesRepository;
    
    public ProfilesService(ProfilesRepository profilesRepository) {
        this.profilesRepository = profilesRepository;
    }
    
    private List<Profile> getProfiles() throws IOException {
        List<Profile> profiles = new ArrayList<>();
        TomlParseResult result = this.profilesRepository.getParseResult();
        
        for (String key : result.keySet()) {
            Object value = result.get(key);

            if (value instanceof TomlTable table) {
                TomlArray array = table.getArray("serverAddresses");
                if (array == null) {
                    continue;
                }
                
                List<String> serverAddresses = array.toList()
                        .stream()
                        .map(Object::toString)
                        .toList();
                
                Long maxHistorySize = table.getLong("maxHistorySize");
                if (maxHistorySize == null) {
                    maxHistorySize = 50L;
                }
                
                profiles.add(new Profile(key, serverAddresses, Math.toIntExact(maxHistorySize)));
            }
        }
        
        return profiles;
    }
    
    public List<String> getProfileNames() throws IOException {
        List<Profile> profiles = this.getProfiles();
        return profiles.stream()
                .map(Profile::profileName)
                .toList();
    }
    
    public Profile getProfileByName(String name) throws IOException {
        List<Profile> profiles = this.getProfiles();
        return profiles.stream()
                .filter(profile -> profile.profileName().equals(name))
                .findFirst()
                .orElse(null);
    }
    
    public Profile getProfileByServerAddress(String serverAddress) throws IOException {
        List<Profile> profiles = this.getProfiles();
        
        for (Profile profile : profiles) {
            if (profile.serverAddresses().contains(serverAddress)) {
                return profile;
            }
        }
        
        return null;
    }
    
    public boolean isInAnyProfile(String serverAddress) throws IOException {
        List<Profile> profiles = this.getProfiles();
        
        for (Profile profile : profiles) {
            if (profile.serverAddresses().contains(serverAddress)) {
                return true;
            }
        }
        
        return false;
    }
    
    public int getVanillaMaxSize() {
        try {
            TomlParseResult result = this.profilesRepository.getParseResult();
            
            Long maxSize = result.getLong("vanillaMaxSize");
            if (maxSize == null) {
                return 50;
            }

            return Math.toIntExact(maxSize);
            
        } catch (Exception e) {
            LOGGER.error("Failed to read vanillaMaxSize from profiles.toml, defaulting to 50", e);
            return 50;
        }
    }
    
    public int getMaxHistorySize() {
        try {
            TomlParseResult result = this.profilesRepository.getParseResult();
            
            Long maxSize = result.getLong("maxHistorySize");
            if (maxSize == null) {
                return 50;
            }

            return Math.toIntExact(maxSize);
            
        } catch (Exception e) {
            LOGGER.error("Failed to read maxHistorySize from profiles.toml, defaulting to 50", e);
            return 50;
        }
    }

    public void copyTemplateIfMissing() throws IOException {
        this.profilesRepository.copyTemplateIfMissing();
    }
}
