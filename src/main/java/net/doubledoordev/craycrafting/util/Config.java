/*
 * Copyright (c) 2014, DoubleDoorDevelopment
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.doubledoordev.craycrafting.util;

import net.doubledoordev.lib.DevPerks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static net.doubledoordev.craycrafting.util.Constants.MODID;

/**
 * @author Dries007
 */
public class Config
{
    private final File file;
    private       Configuration configuration;
    public boolean debug = false;
    public int     timer = 0;
    public String  timermessage = "[CrayCrafting] Recipes have been rotated!";
    public boolean listType = false;
    public Integer[] list = new Integer[0];

    public Config(File file)
    {
        this.file = file;
        configuration = new Configuration(file);

        debug = configuration.getBoolean("debug", MODID, debug, "Enable extra debug output.");
        if (configuration.getBoolean("sillyness", MODID, true, "Disable sillyness only if you want to piss off the developers XD")) MinecraftForge.EVENT_BUS.register(new DevPerks(debug));

        timer = configuration.get(MODID, "resetTimer", timer, "For extra evil, this timer rotates the crafting every X minutes. 0 for disable.").getInt();
        timermessage = configuration.get(MODID, "timermessage", timermessage, "Message to be send to all players on timer. Empty = no message").getString();

        listType = configuration.getBoolean("listType", MODID, listType, "True means that the list is a whitelist. Craycrafting only applies in the dimensions in the list.\nFalse means that the list is a blacklist. Craycrafting applies in all dimensions except the ones in the list");
        int[] templist = configuration.get(MODID, "list", new int[0], "The black/whitelist. See listType.").getIntList();
        list = new Integer[templist.length];
        for (int i = 0; i < templist.length; i++) list[i] = templist[i];

        save();
    }

    public void save()
    {
        if (configuration.hasChanged()) configuration.save();
    }
}
