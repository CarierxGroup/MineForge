 package cn.minerealms.replacecommand;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
   public final class ReplaceCommand extends JavaPlugin 
   {
   public static FileConfiguration config;
   
   public void onEnable() 
   {
     instance = this;
     saveDefaultConfig();
     config = getConfig();
     Bukkit.getPluginManager().registerEvents(new PlayerInputCommandListener(), (Plugin)this);
   }
   private static JavaPlugin instance;
   public static JavaPlugin getInstance() {
	   return instance; 
	   }
 
   
   public static void setConfig(FileConfiguration conf) {
	   config = conf; 
	   }
 }
