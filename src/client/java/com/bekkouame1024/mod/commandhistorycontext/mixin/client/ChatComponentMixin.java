package com.bekkouame1024.mod.commandhistorycontext.mixin.client;

import com.bekkouame1024.mod.commandhistorycontext.accessor.ChatHudAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.ArrayListDeque;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatComponent.class)
public class ChatComponentMixin implements ChatHudAccessor {

    @Final
    @Shadow
    private ArrayListDeque<String> recentChat;

    @Shadow
    @Final
    Minecraft minecraft;

    public void commandHistoryContext$reloadCommandHistory() {
        this.recentChat.clear();
        this.recentChat.addAll(this.minecraft.commandHistory().history());
    }
}
