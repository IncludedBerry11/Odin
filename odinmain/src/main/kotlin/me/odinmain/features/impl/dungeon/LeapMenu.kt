package me.odinmain.features.impl.dungeon

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.multiplyAlpha
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.elements.scope.hoverEffect
import com.github.stivais.ui.utils.radii
import io.github.moulberry.notenoughupdates.NEUApi
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.LeapHelper.getPlayer
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperBossChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperClearChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.worldLoad
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonClass
import me.odinmain.utils.skyblock.dungeon.DungeonPlayer
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui."
) {
    val type by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class (BetterMap)", "A-Z Name", "No Sorting"), description = "How to sort the leap menu.")
    private val colorStyle by SelectorSetting("Color Style", "Gray", arrayListOf("Gray", "Color"), description = "Which color style to use")
    private val roundedRect by BooleanSetting("Rounded Rect", true, description = "Toggles the rounded rect for the gui.")
    private val useNumberKeys by BooleanSetting("Use Number Keys", false, description = "Use keyboard keys to leap to the player you want, going from left to right, top to bottom.")
    private val topLeftKeybind by KeybindSetting("Top Left", Keyboard.KEY_1, "Used to click on the first person in the leap menu.").withDependency { useNumberKeys }
    private val topRightKeybind by KeybindSetting("Top Right", Keyboard.KEY_2, "Used to click on the second person in the leap menu.").withDependency { useNumberKeys }
    private val bottomLeftKeybind by KeybindSetting("Bottom Left", Keyboard.KEY_3, "Used to click on the third person in the leap menu.").withDependency { useNumberKeys }
    private val bottomRightKeybind by KeybindSetting("Bottom right", Keyboard.KEY_4, "Used to click on the fourth person in the leap menu.").withDependency { useNumberKeys }
    private val size by NumberSetting("Scale Factor", 1.0f, 0.5f, 2.0f, 0.1f, description = "Scale factor for the leap menu.")
    private val leapHelperToggle by BooleanSetting("Leap Helper", false, description = "Highlights the leap helper player in the leap menu.")
    private val leapHelperColor by ColorSetting("Leap Helper Color", Color.WHITE, description = "Color of the Leap Helper highlight").withDependency { leapHelperToggle }
    val delay by NumberSetting("Reset Leap Helper Delay", 30, 10.0, 120.0, 1.0, description = "Delay for clearing the leap helper highlight").withDependency { leapHelperToggle }
    private val leapAnnounce by BooleanSetting("Leap Announce", false, description = "Announces when you leap to a player.")
   // private val hoveredAnims = List(4) { EaseInOut(200L) }

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown, ResourceLocation("textures/entity/steve.png"))

    fun leapMenu() = UI {
        val sizeX = Animatable(from = 80.percent, to = 90.percent)
        val sizeY = Animatable(from = 50.percent, to = 60.percent)
        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed modMessage("Empty")
            val x = when (index) {
                0, 2 -> 16.percent
                else -> 2.percent
            }

            val y = when (index) {
                0, 1 -> 32.percent
                else -> 4.percent
            }

            val groupY = when (index) {
                0, 1 -> 0.percent
                else -> 50.percent
            }

            val groupX = when (index) {
                0, 2 -> 0.percent
                else -> 50.percent
            }
            group(constraints = constrain(groupX, groupY, 50.percent, 50.percent)) {

                val block = block(
                    constraints = constrain(x, y, sizeX, sizeY),
                    color = `gray 38`,
                    radius = 12.radii()
                ) {

                    //image(it.locationSkin.toString(), constrain(30.px, 30.px, 240.px, 240.px), 9.radii())
                    block(constrain(5.percent, 10.percent, 33.percent, 80.percent), color = Color.WHITE, radius = 9.radii())
                    column(constraints = constrain(38.percent, 40.percent)) {
                        text(it.name, size = 48.px, color = it.clazz.color)
                        divider(15.px)
                        block(constrain(0.px, 55.px, 25.percent, 1.px), color = Color.WHITE.multiplyAlpha(0.2f), radius = 9.radii())
                        text(if (it.isDead) "§cDEAD" else it.clazz.name, size = 30.px, color = Color.WHITE)
                    }
                    onClick {
                        modMessage(getQuadrant())
                        ui.main.removeElement(element)
                        true
                    }
                }
                onMouseEnterExit {
                    block.hoverEffect()
                }
            }

        }
    }

    @SubscribeEvent
    fun onDrawScreen(event: GuiEvent.DrawGuiContainerScreenEvent) {


        /*hoveredQuadrant =
        if (hoveredQuadrant != previouslyHoveredQuadrant && previouslyHoveredQuadrant != -1) {
            hoveredAnims[hoveredQuadrant - 1].start()
            hoveredAnims[previouslyHoveredQuadrant - 1].start(true)
        }
        previouslyHoveredQuadrant = hoveredQuadrant

        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            scale(1f / scaleFactor,  1f / scaleFactor)

            val displayWidth = Display.getWidth()
            val displayHeight = Display.getHeight()
            translate(displayWidth / 2f, displayHeight / 2f, 0f)
            scale(size, size, 1f)
            translate(-displayWidth / 2f, -displayHeight / 2f, 0f)
            translate(displayWidth / 2, displayHeight / 2)
            val boxWidth = 800
            val boxHeight = 300
            val x = when (index) {
                0, 2 -> -((displayWidth - (boxWidth * 2)) / 6 + boxWidth)
                else -> ((displayWidth - (boxWidth * 2)) / 6)
            }
            val y = when (index) {
                0, 1 -> -((displayHeight - (boxHeight * 2)) / 8 + boxHeight)
                else -> ((displayHeight - (boxHeight * 2)) / 8)
            }
            val color = if (colorStyle) it.clazz.color else Color.DARK_GRAY
            if (it.name == (if (DungeonUtils.inBoss) LeapHelper.leapHelperBoss else LeapHelper.leapHelperClear) && leapHelperToggle)
                roundedRectangle(x - 25, y - 25, boxWidth + 50, boxHeight + 50, leapHelperColor, if (roundedRect) 12f else 0f)

            val box = Box(x, y, boxWidth, boxHeight).expand(hoveredAnims[index].get(0f, 15f, hoveredQuadrant - 1 != index))
            dropShadow(box, 10f, 15f, if (getQuadrant() - 1 != index) ColorUtil.moduleButtonColor else Color.WHITE)
            roundedRectangle(box, color, if (roundedRect) 12f else 0f)

            drawTexturedModalRect(x + 30, y + 30, 240, 240,8f, 8f, 8, 8, 64f, 64f)

            text(it.name, x + 265f, y + 155f, if (!colorStyle) it.clazz.color else Color.DARK_GRAY, 48f)
            text(if (it.isDead) "§cDEAD" else it.clazz.name, x + 270f, y + 210f, Color.WHITE, 30f, shadow = true)
            rectangleOutline(x + 30, y + 30, 240, 240, color, 25f, 15f, 100f)
            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true*/
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return
        val guiScreen = UIScreen(leapMenu())
        event.gui = guiScreen
        if (Loader.instance().activeModList.any { it.modId == "notenoughupdates" }) NEUApi.setInventoryButtonsToDisabled()
    }

    @SubscribeEvent
    fun mouseClicked(event: GuiEvent.GuiMouseClickEvent) {
        val gui = event.gui as? GuiChest ?: return
        if (event.gui.inventorySlots !is ContainerChest || gui.inventorySlots.name != "Spirit Leap" || leapTeammates.isEmpty())  return

        val quadrant = getQuadrant()
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, gui.inventorySlots as? ContainerChest ?: return)

        event.isCanceled = true
    }

    @SubscribeEvent
    fun keyTyped(event: GuiEvent.GuiKeyPressEvent) {
        val gui = event.gui as? GuiChest ?: return
        if (
            gui.inventorySlots !is ContainerChest ||
            gui.inventorySlots.name != "Spirit Leap" ||
            !event.keyCode.equalsOneOf(topLeftKeybind.key, topRightKeybind.key, bottomLeftKeybind.key, bottomRightKeybind.key) ||
            leapTeammates.isEmpty() ||
            !useNumberKeys
        ) return
        val keyCodeNumber = when (event.keyCode) {
            topLeftKeybind.key -> 1
            topRightKeybind.key -> 2
            bottomLeftKeybind.key -> 3
            bottomRightKeybind.key -> 4
            else -> return
        }
        val playerToLeap = if (keyCodeNumber > leapTeammates.size) return else leapTeammates[keyCodeNumber - 1]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, gui.inventorySlots as ContainerChest)

        event.isCanceled = true
    }

    private fun leapTo(name: String, containerChest: ContainerChest) {
        val index = getItemIndexInContainerChest(containerChest, name, 11..16) ?: return modMessage("Cant find player $name. This shouldn't be possible!")
        modMessage("Teleporting to $name.")
        if (leapAnnounce) partyMessage("Leaping to $name.")
        mc.playerController.windowClick(containerChest.windowId, index, 2, 3, mc.thePlayer)
    }

    init {
        onMessage(Regex(".*")) {
            leapHelperClearChatEvent(it)
            leapHelperBossChatEvent(it)
        }

        onWorldLoad { worldLoad() }

        execute(100) {
            getPlayer()
        }
    }


   private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stiviaisd", DungeonClass.Healer),
        DungeonPlayer("Odtheking", DungeonClass.Archer),
        DungeonPlayer("Bonzi", DungeonClass.Mage),
        DungeonPlayer("Cezar", DungeonClass.Tank)
    )


    /**
     * Sorts the list of players based on their default quadrant and class priority.
     * The function first tries to place each player in their default quadrant. If the quadrant is already occupied,
     * the player is added to a second round list. After all players have been processed, the function fills the remaining
     * empty quadrants with the players from the second round list.
     *
     * @param players The list of players to be sorted.
     * @return An array of sorted players.
     */
    /**
     * Sorts the list of players based on their default quadrant and class priority.
     * The function first tries to place each player in their default quadrant. If the quadrant is already occupied,
     * the player is added to a second round list. After all players have been processed, the function fills the remaining
     * empty quadrants with the players from the second round list.
     *
     * @param players The list of players to be sorted.
     * @return An array of sorted players.
     */
    fun odinSorting(players: List<DungeonPlayer>): Array<DungeonPlayer> {
        val result = Array(4) { EMPTY }
        val secondRound = mutableListOf<DungeonPlayer>()

        for (player in players.sortedBy { it.clazz.priority }) {
            when {
                result[player.clazz.defaultQuadrant] == EMPTY -> result[player.clazz.defaultQuadrant] = player
                else -> secondRound.add(player)
            }
        }

        if (secondRound.isEmpty()) return result

        result.forEachIndexed { index, _ ->
            when {
                result[index] == EMPTY -> {
                    result[index] = secondRound.removeAt(0)
                    if (secondRound.isEmpty()) return result
                }
            }
        }
        return result
    }
}