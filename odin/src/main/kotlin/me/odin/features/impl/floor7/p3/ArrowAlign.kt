package me.odin.features.impl.floor7.p3

import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.addVec
import me.odinmain.utils.distanceSquaredTo
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Items
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ArrowAlign : Module(
    name = "Arrow Align",
    description = "Shows a solution for the Arrow Align device.",
    category = Category.FLOOR7
) {
    private val standPosition = Vec3(0.0, 120.0, 77.0)
    private val frameGridCorner = Vec3(-2.0, 120.0, 75.0)

    private val recentClickTimestamps = mutableMapOf<Int, Long>()
    private val clicksRemaining = mutableMapOf<Int, Int>()
    private var currentFrameRotations: List<Int>? = null
    private var targetSolution: List<Int>? = null

    init {
        execute(200) {
            clicksRemaining.clear()
            if ((mc.thePlayer?.distanceSquaredTo(standPosition) ?: return@execute) > 225) {
                currentFrameRotations = null
                targetSolution = null
                return@execute
            }

            currentFrameRotations = getFrames()

            possibleSolutions.find { solution ->
                solution.indices.all { i ->
                    val currentRotation = currentFrameRotations?.get(i) ?: return@all false
                    (solution[i] == -1 || currentRotation == -1) && solution[i] == currentRotation
                }
            }?.let { foundSolution ->
                targetSolution = foundSolution
                foundSolution.forEachIndexed { i, targetRotation ->
                    val currentRotation = currentFrameRotations?.get(i) ?: return@forEachIndexed
                    val clicksNeeded = calculateClicksNeeded(currentRotation, targetRotation)
                    if (clicksNeeded > 0) clicksRemaining[i] = clicksNeeded
                }
            }
        }
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.RightClickEvent) {
        val targetFrame = mc.objectMouseOver?.entityHit as? EntityItemFrame ?: return

        val frameIndex = ((targetFrame.posY - frameGridCorner.yCoord) + (targetFrame.posZ - frameGridCorner.zCoord) * 5).toInt()

        if (targetFrame.posX != frameGridCorner.xCoord || currentFrameRotations?.get(frameIndex) == -1 || frameIndex !in 0..24) return

        if (!clicksRemaining.containsKey(frameIndex) && mc.thePlayer.isSneaking) return

        recentClickTimestamps[frameIndex] = System.currentTimeMillis()
        currentFrameRotations = currentFrameRotations?.toMutableList()?.apply { this[frameIndex] = (this[frameIndex] + 1) % 8 }

        currentFrameRotations?.let {
            val target = targetSolution ?: return
            val remainingClicks = calculateClicksNeeded(it[frameIndex], target[frameIndex])
            if (remainingClicks == 0) clicksRemaining.remove(frameIndex)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (clicksRemaining.isEmpty()) return
        clicksRemaining.forEach { (index, clickNeeded) ->
            val framePosition = getFramePositionFromIndex(index)
            val color = when {
                clickNeeded == 0 -> return@forEach
                clickNeeded < 3 -> Color(85, 255, 85)
                clickNeeded < 5 -> Color(255, 170, 0)
                else -> Color(170, 0, 0)
            }
            Renderer.drawStringInWorld(clickNeeded.toString(), framePosition.addVec(y = 0.6, z = 0.5), color, scale = 0.5f)
        }
    }

    private fun getFrames(): List<Int> {
        val itemFrames = mc.theWorld.loadedEntityList
            .filterIsInstance<EntityItemFrame>()
            .filter { it.displayedItem?.item == Items.arrow }
        if (itemFrames.isEmpty()) return List(25) { -1 }

        val positionToRotationMap = itemFrames.associate { Vec3(it.posX, it.posY, it.posZ).toString() to it.rotation }

        return (0..24).map { index ->
            if (recentClickTimestamps[index]?.let { System.currentTimeMillis() - it < 1000 } == true && currentFrameRotations != null)
                currentFrameRotations?.get(index) ?: -1
            else positionToRotationMap[getFramePositionFromIndex(index).toString()] ?: -1
        }
    }

    private fun calculateClicksNeeded(currentRotation: Int, targetRotation: Int): Int {
        return (8 - currentRotation + targetRotation) % 8
    }

    private fun getFramePositionFromIndex(index: Int): Vec3 {
        return frameGridCorner.addVec(0, index % 5, index / 5)
    }

    private val possibleSolutions = listOf(
        listOf(7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, -1, -1, -1, 7, 1),
        listOf(-1, -1, 7, 7, 5, -1, 7, 1, -1, 5, -1, -1, -1, -1, -1, -1, 7, 5, -1, 1, -1, -1, 7, 7, 1),
        listOf(7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, -1, 7, 5, -1, -1, -1, -1, 5, -1, -1, -1, 3, 3),
        listOf(5, 3, 3, 3, -1, 5, -1, -1, -1, -1, 7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, -1),
        listOf(5, 3, 3, 3, 3, 5, -1, -1, -1, 1, 7, 7, -1, -1, 1, -1, -1, -1, -1, 1, -1, 7, 7, 7, 1),
        listOf(7, 7, 7, 7, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, -1, 7, 7, 7, 1),
        listOf(-1, -1, -1, -1, -1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1),
        listOf(-1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, 7, 7, 7, 7, 1, -1, -1, -1, -1, -1),
        listOf(-1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 7, 1, 7, 1, 3, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1)
    )
}
