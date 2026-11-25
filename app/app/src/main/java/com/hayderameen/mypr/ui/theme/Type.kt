package com.hayderameen.mypr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hayderameen.mypr.R

// Variable fonts: each weight sets the font's wght axis explicitly.
private fun variable(res: Int, weight: FontWeight) =
    Font(res, weight, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val Afacad = FontFamily(
    variable(R.font.afacad_flux, FontWeight.SemiBold),
    variable(R.font.afacad_flux, FontWeight.Bold),
)
val InstrumentSans = FontFamily(
    variable(R.font.instrument_sans, FontWeight.Normal),
    variable(R.font.instrument_sans, FontWeight.SemiBold),
)
val SplineMono = FontFamily(
    variable(R.font.spline_sans_mono, FontWeight.Medium),
    variable(R.font.spline_sans_mono, FontWeight.Bold),
)

/** Design-system text tokens (names match `data-design-text-token` in the Stitch screens). */
object LmText {
    val displayLg = TextStyle(fontFamily = Afacad, fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-1.5).sp)
    val displayLgMobile = TextStyle(fontFamily = Afacad, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.75).sp)
    val headlineLg = TextStyle(fontFamily = Afacad, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp)
    val headlineLgMobile = TextStyle(fontFamily = Afacad, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp)
    val headlineMd = TextStyle(fontFamily = Afacad, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp)
    val titleLg = TextStyle(fontFamily = Afacad, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp)
    val titleMd = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp)
    val bodyLg = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val bodyMd = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val labelLg = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp)
    val labelSm = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp)
    val monoMetric = TextStyle(fontFamily = SplineMono, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-1).sp)
    val monoMetricMobile = TextStyle(fontFamily = SplineMono, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.75).sp)
    val monoSm = TextStyle(fontFamily = SplineMono, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp)
}

val AppTypography = Typography(
    displayLarge = LmText.displayLg,
    displaySmall = LmText.displayLgMobile,
    headlineLarge = LmText.headlineLg,
    headlineMedium = LmText.headlineMd,
    titleLarge = LmText.titleLg,
    titleMedium = LmText.titleMd,
    bodyLarge = LmText.bodyLg,
    bodyMedium = LmText.bodyMd,
    labelLarge = LmText.labelLg,
    labelMedium = LmText.labelSm,
    labelSmall = LmText.labelSm,
)
