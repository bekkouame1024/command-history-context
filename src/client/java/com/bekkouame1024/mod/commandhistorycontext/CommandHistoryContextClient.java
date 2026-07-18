package com.bekkouame1024.mod.commandhistorycontext;

import com.bekkouame1024.mod.commandhistorycontext.accessor.ChatHudAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class CommandHistoryContextClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ((ChatHudAccessor) client.gui.getChat()).commandHistoryContext$reloadCommandHistory();
        });
    }
}