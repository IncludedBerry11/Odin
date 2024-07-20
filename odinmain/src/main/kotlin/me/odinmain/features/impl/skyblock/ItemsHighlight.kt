package me.odinmain.features.impl.skyblock

import com.github.stivais.ui.color.Color
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.HighlightRenderer.highlightModeDefault
import me.odinmain.utils.render.HighlightRenderer.highlightModeList
import me.odinmain.utils.skyblock.*
import net.minecraft.entity.item.EntityItem

object ItemsHighlight : Module(
    name = "Item Highlight",
    description = "Outlines dropped item entities.",
) {
    private val renderThrough by BooleanSetting("Through Walls", true)
    private val mode by SelectorSetting("Mode", highlightModeDefault, highlightModeList)
    private val thickness by NumberSetting("Line Width", 0.5f, 0.2f, 1f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes")
    private val colorStyle by SelectorSetting("Color Style", "Rarity", arrayListOf("Rarity", "Distance"), description = "Which color style to use")

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled || !LocationUtils.inSkyblock) emptyList()
            else mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>().map {
                HighlightRenderer.HighlightEntity(it, getEntityOutlineColor(it), thickness, !renderThrough, 1f)
            }
        }
    }

    private fun getEntityOutlineColor(entity: EntityItem): Color {
        return when {
            colorStyle == 0 -> getRarity(entity.entityItem.lore)?.color ?: Color.WHITE
            entity.ticksExisted <= 11 -> Color.MINECRAFT_YELLOW
            entity.getDistanceToEntity(mc.thePlayer) <= 3.5 -> Color.MINECRAFT_GREEN
            else -> Color.RED
        }
    }
}