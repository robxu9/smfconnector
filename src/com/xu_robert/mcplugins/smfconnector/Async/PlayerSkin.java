package com.xu_robert.mcplugins.smfconnector.Async;

import org.bukkit.entity.Player;

import com.xu_robert.mcplugins.smfconnector.ThreadHelper;

import mmo.Core.MMOPlugin;

public class PlayerSkin extends MMOPlugin{

	private ThreadHelper th = new ThreadHelper();
	private Player p;
	private String u;

	public PlayerSkin(Player player, String url) {
		p=player;
		u=url;
	}

	public void changeSkin(final boolean noise, final boolean verbose){
		if (noise)
			th.print("Attempting to set skin of player " + p.getName(), 0);
		if (verbose)
			th.print(p,"Attempting to set your skin...");
		MMOPlugin.mmoCore.setSkin(p, u);
		if (noise)
			th.print("Set " + p.getName() + "'s skin successfully.", 0);
		if (verbose)
			th.print(p,"Set your skin successfully!");
	}

	
}