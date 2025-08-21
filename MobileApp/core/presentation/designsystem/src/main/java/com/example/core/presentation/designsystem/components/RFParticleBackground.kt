package com.example.core.presentation.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class RFParticle(
  val id: Int,
  val initialX: Float,
  val speed: Float,
  val size: Float,
  val alpha: Float,
  val color: Color
)

@Composable
fun RFParticleBackground(
  modifier: Modifier = Modifier,
  particleCount: Int = 50,
  showBubbles: Boolean = true,
  bubbles: List<RFBubbleSpec> = defaultBubbles(),
  content: @Composable BoxScope.() -> Unit = {}
) {
  var size by remember { mutableStateOf(IntSize.Zero) }
  Box(modifier = modifier.fillMaxSize().onGloballyPositioned { size = it.size }) {
    if (size.width > 0 && size.height > 0) {
      RFParticleSystem(
        screenWidth = size.width.toFloat(),
        screenHeight = size.height.toFloat(),
        count = particleCount
      )
    }
    if (showBubbles) {
      RFBubbles(bubbles = bubbles, modifier = Modifier.fillMaxSize())
    }
    content()
  }
}

@Composable
private fun RFParticleSystem(screenWidth: Float, screenHeight: Float, count: Int) {
  val particles = remember(screenWidth, screenHeight, count) {
    (1..count).map { id ->
      val r = Random.nextFloat()
      val sizePx = when {
        r < 0.07f -> Random.nextFloat() * 28f + 22f
        r < 0.32f -> Random.nextFloat() * 14f + 10f
        else -> Random.nextFloat() * 8f + 4f
      }
      val speedBase = when {
        sizePx > 30f -> Random.nextFloat() * 3000f + 3600f
        sizePx > 18f -> Random.nextFloat() * 2600f + 3000f
        else -> Random.nextFloat() * 2200f + 2600f
      }
      RFParticle(
        id = id,
        initialX = Random.nextFloat() * screenWidth,
        speed = speedBase,
        size = sizePx,
        alpha = if (sizePx > 30f) Random.nextFloat() * 0.30f + 0.30f else Random.nextFloat() * 0.35f + 0.15f,
        color = listOf(
          Color.White.copy(alpha = 0.95f),
          Color.White.copy(alpha = 0.70f),
          Color.White.copy(alpha = 0.50f),
          Color(0xCCFFFFFF)
        ).random()
      )
    }
  }
  particles.forEach { p -> RFAnimatedParticle(p, screenHeight) }
}

@Composable
private fun RFAnimatedParticle(particle: RFParticle, screenHeight: Float) {
  val density = LocalDensity.current
  val infinite = rememberInfiniteTransition(label = "rf_particle_${'$'}{particle.id}")
  val y by infinite.animateFloat(
    initialValue = screenHeight + 140f,
    targetValue = -220f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = particle.speed.toInt(), easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ), label = "rf_py_${'$'}{particle.id}"
  )
  val x by infinite.animateFloat(
    initialValue = particle.initialX - 70f,
    targetValue = particle.initialX + 70f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = (particle.speed * 0.7f).toInt(), easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ), label = "rf_px_${'$'}{particle.id}"
  )
  val scale by infinite.animateFloat(
    initialValue = 0.65f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = (particle.speed * 0.45f).toInt(), easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ), label = "rf_ps_${'$'}{particle.id}"
  )
  val rotation by infinite.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = (particle.speed * 1.4f).toInt(), easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ), label = "rf_pr_${'$'}{particle.id}"
  )
  Box(
    modifier = Modifier
      .offset(
        x = with(density) { x.toDp() },
        y = with(density) { y.toDp() }
      )
      .size(with(density) { (particle.size * scale).toDp() })
      .graphicsLayer { rotationZ = rotation; alpha = particle.alpha }
      .background(particle.color, shape = CircleShape)
  )
}

// Burbujas decorativas

data class RFBubbleSpec(
  val xFraction: Float,
  val yFraction: Float,
  val radiusDp: Float,
  val alpha: Float
)

private fun defaultBubbles(): List<RFBubbleSpec> = listOf(
  RFBubbleSpec(0.12f, 0.25f, 70f, 0.20f),
  RFBubbleSpec(0.85f, 0.30f, 110f, 0.18f),
  RFBubbleSpec(0.12f, 0.75f, 55f, 0.17f),
  RFBubbleSpec(0.92f, 0.70f, 42f, 0.15f),
  RFBubbleSpec(0.30f, 0.90f, 90f, 0.14f)
)

@Composable
fun RFBubbles(
  bubbles: List<RFBubbleSpec>,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    bubbles.forEach { b ->
      val radius = b.radiusDp.dp.toPx()
      val center = Offset(size.width * b.xFraction, size.height * b.yFraction)
      drawCircle(
        color = Color.White.copy(alpha = b.alpha),
        radius = radius,
        center = center
      )
      drawCircle(
        color = Color.White.copy(alpha = b.alpha * 0.55f),
        radius = radius,
        center = center,
        style = Stroke(width = radius * 0.05f)
      )
    }
  }
}
