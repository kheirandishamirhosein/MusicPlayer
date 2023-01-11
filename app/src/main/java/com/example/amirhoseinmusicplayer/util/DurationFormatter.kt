package com.example.amirhoseinmusicplayer.util

import java.util.*

object DurationFormatter {

  fun formatDuration(duration: Int): String {
    var seconds: Int = (duration / 1000)
    val minutes: Int = seconds / 60
    seconds %= 60
    return String.format(Locale.ENGLISH, "%02d", minutes) +
        ":" +
        String.format(Locale.ENGLISH, "%02d", seconds)
  }
}