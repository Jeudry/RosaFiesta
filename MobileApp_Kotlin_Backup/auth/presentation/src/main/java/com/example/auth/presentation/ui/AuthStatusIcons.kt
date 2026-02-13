package com.example.auth.presentation.ui

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val RFSignalIcon: ImageVector by lazy {
  materialIcon(name = "RF.Signal") {
    materialPath {
      moveTo(2.0f, 17.0f)
      horizontalLineTo(6.0f)
      verticalLineTo(24.0f)
      horizontalLineTo(2.0f)
      close()
      moveTo(7.0f, 14.0f)
      horizontalLineTo(11.0f)
      verticalLineTo(24.0f)
      horizontalLineTo(7.0f)
      close()
      moveTo(12.0f, 11.0f)
      horizontalLineTo(16.0f)
      verticalLineTo(24.0f)
      horizontalLineTo(12.0f)
      close()
      moveTo(17.0f, 8.0f)
      horizontalLineTo(21.0f)
      verticalLineTo(24.0f)
      horizontalLineTo(17.0f)
      close()
    }
  }
}

val RFWifiIcon: ImageVector by lazy {
  materialIcon(name = "RF.Wifi") {
    materialPath {
      moveTo(1.0f, 9.0f)
      lineToRelative(2.0f, 2.0f)
      curveToRelative(4.97f, -4.97f, 13.03f, -4.97f, 18.0f, 0.0f)
      lineToRelative(2.0f, -2.0f)
      curveTo(17.93f, 3.93f, 6.07f, 3.93f, 1.0f, 9.0f)
      close()
      moveTo(9.0f, 17.0f)
      lineToRelative(3.0f, 3.0f)
      lineToRelative(3.0f, -3.0f)
      curveToRelative(-1.65f, -1.66f, -4.34f, -1.66f, -6.0f, 0.0f)
      close()
      moveTo(5.0f, 13.0f)
      lineToRelative(2.0f, 2.0f)
      curveToRelative(2.76f, -2.76f, 7.24f, -2.76f, 10.0f, 0.0f)
      lineToRelative(2.0f, -2.0f)
      curveTo(15.14f, 9.14f, 8.87f, 9.14f, 5.0f, 13.0f)
      close()
    }
  }
}

val RFBatteryIcon: ImageVector by lazy {
  materialIcon(name = "RF.Battery") {
    materialPath {
      moveTo(15.67f, 4.0f)
      horizontalLineTo(14.0f)
      verticalLineTo(2.0f)
      horizontalLineTo(10.0f)
      verticalLineTo(4.0f)
      horizontalLineTo(8.33f)
      curveTo(7.6f, 4.0f, 7.0f, 4.6f, 7.0f, 5.33f)
      verticalLineTo(20.67f)
      curveTo(7.0f, 21.4f, 7.6f, 22.0f, 8.33f, 22.0f)
      horizontalLineTo(15.67f)
      curveTo(16.4f, 22.0f, 17.0f, 21.4f, 17.0f, 20.67f)
      verticalLineTo(5.33f)
      curveTo(17.0f, 4.6f, 16.4f, 4.0f, 15.67f, 4.0f)
      close()
      moveTo(15.0f, 20.0f)
      horizontalLineTo(9.0f)
      verticalLineTo(6.0f)
      horizontalLineTo(15.0f)
      verticalLineTo(20.0f)
      close()
      moveTo(9.0f, 18.0f)
      horizontalLineTo(15.0f)
      verticalLineTo(16.0f)
      horizontalLineTo(9.0f)
      close()
      moveTo(9.0f, 14.0f)
      horizontalLineTo(15.0f)
      verticalLineTo(12.0f)
      horizontalLineTo(9.0f)
      close()
    }
  }
}
