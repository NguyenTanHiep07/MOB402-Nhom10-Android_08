package com.mob10.deliveryapp.ui.theme

import androidx.compose.ui.graphics.Color


// GoDrop delivery palette: energetic green for movement and trust, with
// orange reserved for attention states. The palette is intentionally its own
// identity instead of copying another delivery brand.
val UthPrimary = Color(0xFF0AA873)
val UthPrimaryDark = Color(0xFF087A55)
val UthPrimaryContainer = Color(0xFFDFF6ED)
val UthBackground = Color(0xFFF3F7F5)
val UthSurface = Color(0xFFFFFFFF)
val UthSurfaceContainerLow = Color(0xFFF8FBF9)
val UthSurfaceContainerHighest = Color(0xFFE4ECE8)
val UthSecondary = Color(0xFFFF7A1A)
val UthSecondaryContainer = Color(0xFFFFF0E5)
val UthOnSurface = Color(0xFF10251D)
val UthOnSurfaceVariant = Color(0xFF60736B)
val UthOutline = Color(0xFF9BAAA4)
val UthOutlineVariant = Color(0xFFDCE5E1)
val UthSuccess = Color(0xFF11875D)
val UthSuccessContainer = Color(0xFFDDF7EC)
val UthWarning = Color(0xFFF59E0B)
val UthWarningContainer = Color(0xFFFFF3D6)
val UthError = Color(0xFFD92D20)
val UthErrorContainer = Color(0xFFFFE7E5)
val UthInfo = Color(0xFF2563EB)
val UthInfoContainer = Color(0xFFE8F0FF)

// ── Gradient helpers (light) ──────────────────────────────────────────
// Used by DashboardHeroCard and GoDropHeader. Screens should prefer the
// Brush-based composable helpers in the theme package instead of these raw
// colours so that dark-mode variants are resolved automatically.
val UthPrimaryGradientStart = Color(0xFF0CC07E)   // lighter green
val UthPrimaryGradientEnd   = Color(0xFF089465)   // deeper green

// Elevated surface — a very faint green tint that lifts cards off the
// flat white background without relying on drop shadows alone.
val UthSurfaceElevated = Color(0xFFF6FBF9)
