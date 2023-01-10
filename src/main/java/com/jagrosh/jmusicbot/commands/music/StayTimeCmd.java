/*
 * Copyright 2022 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;

/**
 * 
 * @author Jabaar
 */
public class StayTimeCmd extends MusicCommand
{
    public StayTimeCmd(Bot bot)
    {
        super(bot);
        this.name = "staytime";
        this.help = "sets the time to stay in the channel after the queue is empty";
        this.arguments = "<seconds>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (!bot.getConfig().getStay())
        {
            event.replyError("The configuration must have stayinchannel enab-led to use this command");
        }
        else if (event.getArgs().isEmpty())
        {
            event.reply("Stay time is currently set to `" + s.getStayTime() + "` seconds");
        }
        else
        {
            long maxTimeSeconds = Long.MAX_VALUE/ 1000;
            long stayTime;

            try
            {
                stayTime = Long.valueOf(event.getArgs());
            }
            catch (NumberFormatException e)
            {
                stayTime = - 1;
            }

            if (stayTime < 0 || stayTime > maxTimeSeconds)
                event.replyError("Stay time must be a valid integer greater than or equal to 0");
            else
            {
                s.setStayTime(stayTime);

                if (stayTime == 0)
                    event.replySuccess("Stay time is now `indefinite` on *" + event.getGuild().getName() + "*");
                else
                    event.replySuccess("Stay time set to `" + event.getArgs() + "` seconds on *"
                            + event.getGuild().getName() + "*");
            }
        }
    }
}