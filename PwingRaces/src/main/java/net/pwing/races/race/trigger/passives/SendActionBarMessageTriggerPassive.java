package net.pwing.races.race.trigger.passives;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.pwing.races.api.race.trigger.RaceTriggerPassive;
import net.pwing.races.util.MessageUtil;
import org.bukkit.entity.Player;

public class SendActionBarMessageTriggerPassive extends RaceTriggerPassive {

    public SendActionBarMessageTriggerPassive(String name) {
        super(name);
    }

    @Override
    public void runTriggerPassive(Player player, String[] trigger) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < trigger.length; i++) {
            builder.append(trigger[i]).append(" ");
        }

        String message = builder.toString();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageUtil.getPlaceholderMessage(player, message)));
    }
}
