package minerealmsteam.replacecommand;


 import java.util.List;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;

 public class PlayerInputCommandListener implements Listener
 {
   @EventHandler
  public void onInput(PlayerCommandPreprocessEvent e)
   {
     Player player = e.getPlayer();
     if (player.isOp() && e.getMessage().equalsIgnoreCase("/rc reload"))
     {
      CommandReplace.getInstance().reloadConfig();
       CommandReplace.setConfig(CommandReplace.getInstance().getConfig());
       player.sendMessage("[Replacecommand] §areload!");
       e.setCancelled(true);
     } else if (CommandReplace.config.get(e.getMessage()) != null)
     {
       ConfigurationSection cs = CommandReplace.config.getConfigurationSection(e.getMessage());
       e.setCancelled(true);
       String runType = cs.getString("Run");
       List<String> list = cs.getStringList("List");
       if (runType.equalsIgnoreCase("player")) //以玩家身份执行命令
      {
         for (String s : list) {
           player.chat(s.replace("%player%", player.getName()));
         }
       } else if (runType.equalsIgnoreCase("console")) //以控制台身份执行命令
       {
           for (String s : list) //命令
           {
           Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getServer().getConsoleSender(), s.replace("%player%", player.getName()).replace("/", ""));
         }
      } else if (runType.equalsIgnoreCase("op"))//以管理身份执行命令
      {
             boolean isOp = player.isOp();//让玩家代理成OP
/         try {
           player.setOp(true);
           for (String s : list) {
             player.chat(s.replace("%player%", player.getName()));
           }
         } catch (Exception e1) {
           e1.printStackTrace();
         } finally {
           player.setOp(isOp);
             }
        }
     }
   }
}
