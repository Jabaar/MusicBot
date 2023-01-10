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
package com.jagrosh.jmusicbot.audio;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jmusicbot.Bot;

import net.dv8tion.jda.api.entities.Guild;

/**
 * 
 * @author Jabaar
 */
public class StayInVoiceHandler
{
    private final Bot bot;
    private final HashMap<Long, Instant> stayingSince = new HashMap<>();

    public StayInVoiceHandler(Bot bot)
    {
        this.bot = bot;
    }

    public void init()
    {
        if (bot.getConfig().getStay())
            bot.getThreadpool().scheduleWithFixedDelay(() -> check(), 0, 1, TimeUnit.SECONDS);
    }

    private void check()
    {
        Set<Long> toRemove = new HashSet<>();

        for (Map.Entry<Long, Instant> entrySet : stayingSince.entrySet())
        {
            Guild guild = bot.getJDA().getGuildById(entrySet.getKey());
            if (guild == null)
            {
                toRemove.add(entrySet.getKey());
                continue;
            }

            AudioHandler audioHandler = (AudioHandler) guild.getAudioManager().getSendingHandler();
            if (audioHandler.isMusicPlaying(bot.getJDA()))
                continue;

            if (entrySet.getValue().getEpochSecond() > Instant.now().getEpochSecond() - getStayTime(entrySet.getKey()))
                continue;

            audioHandler.stopAndClear();
            guild.getAudioManager().closeAudioConnection();
            toRemove.add(entrySet.getKey());
        }

        toRemove.forEach(id -> stayingSince.remove(id));
    }

    public void updateStaying(long guildId)
    {
        if (getStayTime(guildId) <= 0)
            return;

        if (!bot.getConfig().getStay())
            bot.closeAudioConnection(guildId);

        Guild guild = bot.getJDA().getGuildById(guildId);

        if (!bot.getPlayerManager().hasHandler(guild))
            return;

        stayingSince.put(guildId, Instant.now());
    }

    private long getStayTime(long guildId)
    {
        return bot.getSettingsManager().getSettings(guildId).getStayTime();
    }
}
