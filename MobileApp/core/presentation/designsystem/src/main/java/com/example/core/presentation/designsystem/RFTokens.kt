package com.example.core.presentation.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Design tokens reutilizables prefijados con RF */
object RFColors {
  // Gradientes primarios (auth)
  val GradientStart = Color(0xFF5EECF5)
  val GradientEnd = Color(0xFFFE709B)

  // Social
  val SocialFacebook = Color(0xFF3B5998)
  val SocialTwitter = Color(0xFF1DA1F2)
  val SocialInstagram = Color(0xFFE4405F)

  // Texto auxiliares
  val Subtitle = Color(0xFF7F8C8D)
  val Link = Color(0xFFFE709B)
  val Divider = Color(0xFFECF0F1)
  val Title = Color(0xFF2C3E50)
}

object RFSpacing {
  val S4 = 4.dp
  val S8 = 8.dp
  val S15 = 15.dp
  val S16 = 16.dp
  val S18 = 18.dp
  val S20 = 20.dp
  val S22 = 22.dp
  val S25 = 25.dp
  val S28 = 28.dp
  val S30 = 30.dp
  val S35 = 35.dp
}

object RFShapes {
  val LargeCard = RoundedCornerShape(25.dp)
  val LargeButton = RoundedCornerShape(25.dp)
  val Circle = RoundedCornerShape(50)
}

object RFAnimationDurations {
  // General particle ranges de referencia (ms)
  const val ParticleMin: Int = 2200
  const val ParticleMax: Int = 4200
}

object RFButtonDefaults {
  val PrimaryHeight: Dp = 50.dp
}

