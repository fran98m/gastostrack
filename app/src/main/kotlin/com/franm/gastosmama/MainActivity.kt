package com.franm.gastosmama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.franm.gastosmama.ui.GastosMamaApp

/** Single-activity, single-screen app — see design_handoff_gastos_mama/README.md. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GastosMamaApp() }
    }
}
