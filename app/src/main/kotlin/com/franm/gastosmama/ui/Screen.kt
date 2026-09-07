package com.franm.gastosmama.ui

enum class Screen { Home, Amount, Category, Statement, History }

/** Tweakable parameters — constants, not UI, per the design handoff. */
object Config {
    const val ConfirmThresholdCents = 100_00L
    const val Recipient = "Enrique"
}
