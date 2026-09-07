package com.franm.gastosmama.data.icons

import com.franm.gastosmama.R

/**
 * Category → icon map. This is the one file to touch when adding a new
 * category icon:
 *
 *   1. Export the icon from Claude Design (or grab one from lucide.dev) as
 *      an SVG.
 *   2. Run: python3 tools/svg_to_vector.py <path-or-url> <name>
 *      This writes app/src/main/res/drawable/ic_cat_<name>.xml — a plain
 *      single-color VectorDrawable, tinted at draw time (see
 *      CategoryIcon.kt), so any stroke-style SVG works without editing XML
 *      by hand.
 *   3. Add one line to SEED_CATEGORY_ICONS below.
 *
 * Order here is the fallback order on a fresh install — see
 * ExpenseRepository.categoryTiles, which re-sorts by real usage count
 * descending once there's history (README: "Seed categories get +1 so the
 * initial order holds until real use overtakes it").
 */
val SEED_CATEGORY_ICONS: List<Pair<String, Int>> = listOf(
    "Frutera" to R.drawable.ic_cat_apple,
    "Pan" to R.drawable.ic_cat_croissant,
    "Supermaxi" to R.drawable.ic_cat_shopping_cart,
    "AlgoMarket" to R.drawable.ic_cat_store,
    "Rappi" to R.drawable.ic_cat_bike,
    "Química" to R.drawable.ic_cat_shirt,
    "Peluquería" to R.drawable.ic_cat_scissors,
    "Médico" to R.drawable.ic_cat_stethoscope,
    "Podólogo" to R.drawable.ic_cat_footprints,
)

const val OTHER_LABEL = "Otro…"
val OTHER_ICON: Int = R.drawable.ic_cat_smile

/** Any label not in [SEED_CATEGORY_ICONS] (a custom "Otro…" entry) gets [OTHER_ICON]. */
fun iconForLabel(label: String): Int =
    SEED_CATEGORY_ICONS.firstOrNull { it.first == label }?.second ?: OTHER_ICON
