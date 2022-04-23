/*
 * Copyright (C) 2017, 2018, 2022 Adrian Siekierka, ACGaming
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package mod.acgaming.inworldbuoyancy.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mod.acgaming.inworldbuoyancy.InWorldBuoyancy;

@Config(modid = InWorldBuoyancy.MODID, name = "InWorldBuoyancy")
public class IWBConfig
{
    @Config.Name("Omit item in hand")
    @Config.Comment({"Exempts the item in the player's hand.", "Default = false"})
    public static boolean omitItemInHand = false;

    @Config.Name("Float both ways")
    @Config.Comment({"Makes non-floating items drop.", "Default = false"})
    public static boolean floatBothWays = false;

    @Config.RequiresWorldRestart
    @Config.Name("Cannot displace liquid")
    @Config.Comment({"Makes it impossible to easily displace liquids with blocks.", "Default = true"})
    public static boolean cannotDisplaceLiquid = true;

    @Config.Name("In-world buoyancy")
    @Config.Comment({"Inventory is buoyant. Hah.", "Default = true"})
    public static boolean inWorldBuoyancy = true;

    @Config.RequiresMcRestart
    @Config.Name("Custom buoyancy list")
    @Config.Comment({"Custom list of items to float. If empty, default handler is used.", "Use registry names (with :) or ore dictionary IDs (without :)."})
    public static String[] customBuoyancyList = {};

    @Config.RequiresMcRestart
    @Config.Name("Custom transform list")
    @Config.Comment({"Custom list of items to transform. If empty, default handler is used.", "Use registry names (with :).", "Use --> as a separator."})
    public static String[] customTransformList = {"minecraft:book-->minecraft:paper"};

    @Config.Name("Debug mode")
    @Config.Comment({"Should the debug mode of the IWB logger be enabled?"})
    public static boolean debug = false;

    @Mod.EventBusSubscriber(modid = InWorldBuoyancy.MODID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(InWorldBuoyancy.MODID))
            {
                ConfigManager.sync(InWorldBuoyancy.MODID, Config.Type.INSTANCE);
            }
        }
    }
}