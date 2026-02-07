package com.oralvis.oralvisclient.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 8dp grid system for calm medical spacing.
 */
object OralVisDimensions {
    val GridUnit: Dp = 8.dp
    val Half = GridUnit / 2
    val One = GridUnit
    val Two = GridUnit * 2
    val Three = GridUnit * 3
    val Four = GridUnit * 4
    val Five = GridUnit * 5
    val Six = GridUnit * 6
    val Eight = GridUnit * 8

    val CardCornerRadius: Dp = 20.dp
    val ButtonCornerRadius: Dp = 20.dp
    val ChipCornerRadius: Dp = 16.dp
    val BottomNavCornerRadius: Dp = 28.dp

    // Action cards: compact, flatter corners, prominent icons
    val ActionCardCornerRadius: Dp = 8.dp
    val ActionCardHeight: Dp = 76.dp
    val ActionCardShadow: Dp = 2.dp
    val ActionCardIconSize: Dp = 56.dp
    val ActionCardLabelSpacing: Dp = 6.dp

    // Screenshot-exact: patient list pill cards
    val PatientPillCornerRadius: Dp = 24.dp
    val PatientPillShadow: Dp = 2.dp
    val PatientAvatarSize: Dp = 44.dp
    val PatientPillPaddingH: Dp = 12.dp
    val PatientPillPaddingV: Dp = 10.dp
    val PatientListSpacing: Dp = 10.dp

    // Screenshot-exact: FAB
    val FabCornerRadius: Dp = 28.dp
    val FabShadow: Dp = 4.dp
    val FabPaddingH: Dp = 20.dp
    val FabPaddingV: Dp = 12.dp
}
