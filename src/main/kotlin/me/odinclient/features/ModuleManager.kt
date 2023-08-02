package me.odinclient.features

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.general.*
import me.odinclient.features.impl.m7.*
import me.odinclient.features.impl.qol.*
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.BaseHud
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object ModuleManager {
    val huds = mutableListOf<Pair<BaseHud, HudSetting>>()

    val modules: ArrayList<Module> = arrayListOf(
        AutoIceFill,
        AutoLeap,
        AutoMask,
        AutoReady,
        AutoSell,
        AutoShield,
        AutoUlt,
        AutoWish,
        BlessingDisplay,
        GhostBlock,
        KeyESP,
        SecretHitboxes,
        SuperBoom,
        TeammatesOutline,
        Triggerbot,
        WatcherBar,
        MapModule,
        PersonalDragon,

        ArrowTrajectory,
        Camera,
        ClickGUIModule,
        ESP,
        FPS,
        CPSDisplay,
        LockCursor,
        VanqNotifier,
        PartyCommands,
        GuildCommands,
        PrivateCommands,
        AutoEdrag,
        DioriteFucker,
        DragonBoxes,
        DragonTimer,
        LeapHelper,
        NecronDropTimer,
        DecoyDeadMessage,
        AutoSprint,
        BrokenHype,
        CookieClicker,
        GhostPick,
        GyroRange,
        KuudraAlerts,
        NoBlockAnimation,
        NoCursorReset,
        Reminders,
        TermAC,
        Ghosts,
        BPS,
        PortalFix,
        TerminalTimes,
        Waypoints,
        Server,
        DeployableTimer,
        CanClip
    )

    @SubscribeEvent
    fun activateModuleKeyBinds(event: InputEvent.KeyInputEvent) {
        if (Keyboard.getEventKeyState()) return
        val eventKey = Keyboard.getEventKey()
        if (eventKey == 0) return
        modules.filter { it.keyCode == eventKey }.forEach { it.onKeybind() }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: InputEvent.MouseInputEvent) {
        if (Mouse.getEventButtonState()) return
        val eventButton = Mouse.getEventButton()
        if (eventButton == 0) return
        modules.filter { it.keyCode + 100 == eventButton }.forEach { it.onKeybind() }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Text) {
        huds.forEach {
            if (it.second.value.isEnabled && mc.currentScreen == null) it.first.render()
        }
    }

    fun getModuleByName(name: String): Module? = modules.firstOrNull { it.name.equals(name, true) }
}
