package net.teamio.smfconnector;

import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SMFConnectorPlayerListener extends PlayerListener{

	/*
	 * read from configuration to see if debug is on; if it is, print out statements verbosely
	 */
	
	public SMFConnector plugin;
	 
	public SMFConnectorPlayerListener(SMFConnector instance) {
	    plugin = instance;
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event){
		setCape(event);
		setSkin(event);
		// async call method to reset skin and cape
		// async call with event.getPlayer();
	}
	
	public void onPlayerTeleport(PlayerTeleportEvent event){
		setCape(event);
		setSkin(event);
	}
	
	public void onPlayerJoin(PlayerJoinEvent event){
		setCape(event);
		setSkin(event);
		setTitle(event);
	}
	
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
		setCape(event);
		setSkin(event);
		setTitle(event);
	}
	
	private void setCape(PlayerEvent event){
		noisy(1,event,"cape");
		if (plugin.schedCapeChange(event.getPlayer()))
			noisy(2,event,"cape");
		else
			noisy(3,event,"cape");
	}
	
	private void setSkin(PlayerEvent event){
		noisy(1,event,"skin");
		if (plugin.schedSkinChange(event.getPlayer()))
			noisy(2,event,"skin");
		else
			noisy(3,event,"skin");
	}
	
	private void setTitle(PlayerEvent event){
		noisy(1,event,"title");
		if (plugin.schedTitleChange(event.getPlayer()))
			noisy(2,event,"title");
		else
			noisy(3,event,"title");
	}
	
	private void noisy(int i, PlayerEvent event, String name){
		if (i==1){
			if (plugin.noise)
				plugin.th.print("Caught event " + event.getEventName()
						+ " for player " + event.getPlayer() + ", setting " + name + ":",0);
			if (plugin.verbose)
				plugin.th.print(event.getPlayer(),
						"Caught event " + event.getEventName() + ", setting " + name + ":",
						0);
		}
		else if (i==2){
			if(plugin.noise)
				plugin.th.print("Successfully set " + name + " for " + event.getPlayer()+".",0);
			if(plugin.verbose)
				plugin.th.print(event.getPlayer(), "Set " + name + " successfully.");
		}
		else if (i==3){
			if(plugin.noise)
				plugin.th.print("None, Invalid, or No Perms " + name + " for " + event.getPlayer()+".",1);
			if(plugin.verbose)
				plugin.th.print(event.getPlayer(), "None, Invalid, or No Perms " + name + ".");
		}
	}
}
