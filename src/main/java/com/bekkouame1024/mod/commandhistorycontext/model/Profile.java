package com.bekkouame1024.mod.commandhistorycontext.model;

import java.util.List;

public record Profile(String profileName, List<String> serverAddresses, int maxHistorySize) { }
