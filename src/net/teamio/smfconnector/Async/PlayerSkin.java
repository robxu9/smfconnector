package net.teamio.smfconnector.Async;

import net.teamio.ThreadHelper;

import org.bukkit.entity.Player;


import mmo.Core.MMOPlugin;

public class PlayerSkin extends MMOPlugin{

	private final ThreadHelper th = new ThreadHelper("SMFCon");
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
