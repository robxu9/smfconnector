package net.teamio.smfconnector.Async;

import net.teamio.smfconnector.ThreadHelper;

import org.bukkit.entity.Player;

import mmo.Core.MMOPlugin;


public class PlayerCape extends MMOPlugin{

	private ThreadHelper th = new ThreadHelper();
	private Player p;
	private String u;

	public PlayerCape(Player player, String url) {
		p=player;
		u=url;
	}

	public void changeCape(final boolean noise, final boolean verbose){
		if (noise)
			th.print("Attempting to set cape of player " + p.getName(), 0);
		if (verbose)
			th.print(p,"Attempting to set your cape...");
		MMOPlugin.mmoCore.setCloak(p, u);
		if (noise)
			th.print("Set " + p.getName() + "'s cape successfully.", 0);
		if (verbose)
			th.print(p,"Set your cape successfully!");
	}

}
