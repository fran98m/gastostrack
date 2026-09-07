package com.franm.gastosmama.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franm.gastosmama.ui.screens.AmountScreen
import com.franm.gastosmama.ui.screens.CategoryScreen
import com.franm.gastosmama.ui.screens.HistoryScreen
import com.franm.gastosmama.ui.screens.HomeScreen
import com.franm.gastosmama.ui.screens.StatementScreen
import com.franm.gastosmama.ui.theme.BoxWithToast
import com.franm.gastosmama.ui.theme.Modernist

@Composable
fun GastosMamaApp(viewModel: AppViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsState()
    val unclaimed by viewModel.unclaimed.collectAsState()
    val statements by viewModel.statements.collectAsState()
    val claimedByStatement by viewModel.claimedByStatement.collectAsState()
    val categoryTiles by viewModel.categoryTiles.collectAsState()
    val otherSuggestions by viewModel.otherSuggestions.collectAsState()
    val context = LocalContext.current

    // Android 15+ enforces edge-to-edge regardless of theme attrs, so the
    // design's fixed paddings (52dp header top, etc.) need the system bars
    // reserved explicitly to land where the design assumed they would.
    Box(Modifier.fillMaxSize().background(Modernist.Ground).windowInsetsPadding(WindowInsets.systemBars)) {
        BoxWithToast(message = ui.toast) {
            Box(Modifier.fillMaxSize()) {
            when (ui.screen) {
                Screen.Home -> HomeScreen(
                    unclaimed = unclaimed,
                    onGoHistory = viewModel::goHistory,
                    onGoAdd = viewModel::goAdd,
                    onGoStatement = viewModel::goStatement,
                    onEdit = viewModel::edit,
                    onDelete = viewModel::deleteExpense,
                )

                Screen.Amount -> AmountScreen(
                    amountText = ui.amountText,
                    isEditing = ui.editId != null,
                    confirmOpen = ui.confirmOpen,
                    thresholdCents = Config.ConfirmThresholdCents,
                    onBack = viewModel::goHome,
                    onKey = viewModel::press,
                    onNext = viewModel::next,
                    onConfirmYes = viewModel::confirmYes,
                    onConfirmNo = viewModel::confirmNo,
                )

                Screen.Category -> {
                    val saveEnabled = if (ui.otherMode) ui.otherText.isNotBlank() else ui.selectedCategory != null
                    CategoryScreen(
                        amountCents = viewModel.amountCents(),
                        tiles = categoryTiles,
                        selectedLabel = ui.selectedCategory,
                        otherMode = ui.otherMode,
                        otherText = ui.otherText,
                        suggestions = otherSuggestions,
                        saveLabel = if (ui.editId != null) "Guardar cambio" else "Guardar",
                        saveEnabled = saveEnabled,
                        onBack = viewModel::backToAmount,
                        onBackFromOther = viewModel::closeOther,
                        onSelect = viewModel::selectCategory,
                        onOpenOther = viewModel::openOther,
                        onOtherTextChange = viewModel::setOtherText,
                        onPickSuggestion = viewModel::setOtherText,
                        onSave = viewModel::save,
                    )
                }

                Screen.Statement -> StatementScreen(
                    expenses = unclaimed,
                    message = viewModel.statementMessage(),
                    fileName = viewModel.statementFileName(),
                    shareOpen = ui.shareOpen,
                    onBack = viewModel::goHome,
                    onOpenShare = viewModel::openShare,
                    onCloseShare = viewModel::closeShare,
                    onSendWhatsApp = { viewModel.sendViaWhatsApp(context) },
                )

                Screen.History -> HistoryScreen(
                    statements = statements,
                    claimedByStatement = claimedByStatement,
                    openId = ui.openHistoryId,
                    onBack = viewModel::goHome,
                    onToggle = viewModel::toggleHistory,
                )
            }
            }
        }
    }
}
