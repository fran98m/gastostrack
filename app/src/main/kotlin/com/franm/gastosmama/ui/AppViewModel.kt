package com.franm.gastosmama.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.franm.gastosmama.data.AppDatabase
import com.franm.gastosmama.data.CategoryTile
import com.franm.gastosmama.data.Expense
import com.franm.gastosmama.data.ExpenseRepository
import com.franm.gastosmama.data.Statement
import com.franm.gastosmama.data.icons.SEED_CATEGORY_ICONS
import com.franm.gastosmama.util.amountTextToCents
import com.franm.gastosmama.util.applyKey
import com.franm.gastosmama.util.centsToPlainString
import com.franm.gastosmama.util.expensesToCsv
import com.franm.gastosmama.util.formatCents
import com.franm.gastosmama.util.formatDayMonth
import com.franm.gastosmama.util.shareStatement
import com.franm.gastosmama.util.statementFileNameFor
import com.franm.gastosmama.util.writeCsvToCache
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val screen: Screen = Screen.Home,
    val amountText: String = "",
    val editId: String? = null,
    val selectedCategory: String? = null,
    val otherMode: Boolean = false,
    val otherText: String = "",
    val confirmOpen: Boolean = false,
    val shareOpen: Boolean = false,
    val toast: String? = null,
    val openHistoryId: String? = null,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExpenseRepository(AppDatabase.get(application).dao())

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val stopSharing = SharingStarted.WhileSubscribed(5_000)

    val unclaimed: StateFlow<List<Expense>> =
        repo.unclaimedExpenses().stateIn(viewModelScope, stopSharing, emptyList())

    val statements: StateFlow<List<Statement>> =
        repo.statements().stateIn(viewModelScope, stopSharing, emptyList())

    val claimedByStatement: StateFlow<Map<String, List<Expense>>> =
        repo.claimedByStatement().stateIn(viewModelScope, stopSharing, emptyMap())

    private val usage: StateFlow<Map<String, Int>> =
        repo.usageCounts().stateIn(viewModelScope, stopSharing, emptyMap())

    private val customLabelHistory: StateFlow<List<String>> =
        repo.customLabelHistory().stateIn(viewModelScope, stopSharing, emptyList())

    /** Category rows for the Categoría screen, most-used first. */
    val categoryTiles: StateFlow<List<CategoryTile>> =
        usage.combine(customLabelHistory) { u, _ -> repo.categoryTiles(u) }
            .stateIn(viewModelScope, stopSharing, repo.categoryTiles(emptyMap()))

    /** "Otro…" suggestion chips: past custom labels, filtered live by the typed text. */
    val otherSuggestions: StateFlow<List<String>> =
        customLabelHistory.combine(ui) { labels, s ->
            val q = s.otherText.trim().lowercase()
            labels.filter { q.isEmpty() || it.lowercase().contains(q) }.take(6)
        }.stateIn(viewModelScope, stopSharing, emptyList())

    private var toastJob: Job? = null
    private fun toast(msg: String) {
        toastJob?.cancel()
        _ui.update { it.copy(toast = msg) }
        toastJob = viewModelScope.launch { delay(2600); _ui.update { it.copy(toast = null) } }
    }

    // ---- Navigation ----

    fun goHome() = _ui.update { UiState() }

    fun goAdd() = _ui.update {
        it.copy(screen = Screen.Amount, amountText = "", editId = null, selectedCategory = null, otherMode = false, otherText = "")
    }

    fun goHistory() = _ui.update { it.copy(screen = Screen.History) }

    fun goStatement() {
        if (unclaimed.value.isNotEmpty()) _ui.update { it.copy(screen = Screen.Statement) }
    }

    fun backToAmount() = _ui.update { it.copy(screen = Screen.Amount, confirmOpen = false) }
    fun backToCategory() = _ui.update { it.copy(screen = Screen.Category) }

    // ---- Amount screen ----

    fun press(key: String) = _ui.update { it.copy(amountText = applyKey(it.amountText, key)) }

    fun amountCents(): Long = amountTextToCents(_ui.value.amountText)

    fun next() {
        val cents = amountCents()
        if (cents == 0L) return
        _ui.update {
            if (cents > Config.ConfirmThresholdCents) it.copy(confirmOpen = true)
            else it.copy(screen = Screen.Category)
        }
    }

    fun confirmYes() = _ui.update { it.copy(confirmOpen = false, screen = Screen.Category) }
    fun confirmNo() = _ui.update { it.copy(confirmOpen = false, amountText = "") }

    // ---- Category screen ----

    fun selectCategory(label: String) = _ui.update { it.copy(selectedCategory = label, otherMode = false) }
    fun openOther() = _ui.update { it.copy(otherMode = true, selectedCategory = null) }
    fun closeOther() = _ui.update { it.copy(otherMode = false) }
    fun setOtherText(text: String) = _ui.update { it.copy(otherText = text) }

    fun edit(expense: Expense) {
        val isSeed = SEED_CATEGORY_ICONS.any { it.first == expense.label }
        _ui.update {
            it.copy(
                screen = Screen.Amount,
                amountText = centsToPlainString(expense.amountCents),
                editId = expense.id,
                selectedCategory = if (isSeed) expense.label else null,
                otherMode = !isSeed,
                otherText = if (isSeed) "" else expense.label,
            )
        }
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repo.deleteExpense(expense)
        toast("Borrado: ${expense.label} $ ${formatCents(expense.amountCents)}")
    }

    fun save() = viewModelScope.launch {
        val s = _ui.value
        val label = (if (s.otherMode) s.otherText else s.selectedCategory)?.trim()
        if (label.isNullOrEmpty()) return@launch
        val cents = amountCents()
        val editId = s.editId
        val existing = editId?.let { id -> unclaimed.value.find { it.id == id } }
        if (existing != null) {
            repo.updateExpense(existing, label, cents)
            toast("Corregido: $label $ ${formatCents(cents)}")
        } else {
            repo.addExpense(UUID.randomUUID().toString(), label, cents, System.currentTimeMillis())
            toast("Anotado: $label $ ${formatCents(cents)}")
        }
        _ui.update { UiState() }
    }

    // ---- Statement / share ----

    fun openShare() = _ui.update { it.copy(shareOpen = true) }
    fun closeShare() = _ui.update { it.copy(shareOpen = false) }

    fun statementFileName(): String {
        val expenses = unclaimed.value
        val oldest = expenses.minOfOrNull { it.ts } ?: System.currentTimeMillis()
        return statementFileNameFor(oldest, System.currentTimeMillis())
    }

    fun statementMessage(): String {
        val expenses = unclaimed.value
        val oldest = expenses.minOfOrNull { it.ts } ?: System.currentTimeMillis()
        val total = expenses.sumOf { it.amountCents }
        return "Hola te paso los gastos de mi mamá de ${formatDayMonth(oldest)} a " +
            "${formatDayMonth(System.currentTimeMillis())}. \nTotal: $${formatCents(total)}. Muchas gracias"
    }

    /** Builds the CSV, launches the WhatsApp share, and marks every unclaimed expense claimed. */
    fun sendViaWhatsApp(context: Context) = viewModelScope.launch {
        val expenses = unclaimed.value
        if (expenses.isEmpty()) return@launch
        val total = expenses.sumOf { it.amountCents }
        val message = statementMessage()
        val csv = expensesToCsv(expenses, total)
        val file = writeCsvToCache(context, statementFileName(), csv)
        shareStatement(context, message, file)

        repo.sendStatement(UUID.randomUUID().toString(), expenses, System.currentTimeMillis())
        _ui.update { UiState() }
        toast("Enviado a ${Config.Recipient}. ${expenses.size} gastos cobrados.")
    }

    // ---- History ----

    fun toggleHistory(id: String) = _ui.update { it.copy(openHistoryId = if (it.openHistoryId == id) null else id) }
}
