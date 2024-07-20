package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.TerminalOpenedEvent
import me.odinmain.events.impl.TerminalSolvedEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGui
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalTimes : Module(
    name = "Terminal Times",
    description = "Records the time taken to complete terminals in floor 7."
) {
    private val sendOnlyPB by SelectorSetting("Send Message", "Always", arrayListOf("Always", "Only PB"), true, description = "Send a message when a terminal is completed")
    private val reset by ActionSetting("Reset pbs") {
        repeat(6) { i -> termPBs.set(i, 999.0) }
        modMessage("§6Terminal PBs §fhave been reset.")
    }

    private val termPBs = PersonalBest("Terminals", 6)
    private var startTimer = 0L
    private var type = TerminalTypes.NONE

    @SubscribeEvent
    fun onTerminalOpen(event: TerminalOpenedEvent) {
        if (event.type == type || mc.currentScreen is TermSimGui) return
        type = event.type
        startTimer = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onTerminalClose(event: TerminalSolvedEvent) {
        if (type == TerminalTypes.NONE || mc.currentScreen is TermSimGui) return
        val time = (System.currentTimeMillis() - startTimer) / 1000.0
        termPBs.time(event.type.ordinal, time, "s§7!", "§a${event.type.guiName} §7solved in §6", addPBString = true, addOldPBString = true, sendOnlyPB = sendOnlyPB == 1)
        type = TerminalTypes.NONE
    }
}