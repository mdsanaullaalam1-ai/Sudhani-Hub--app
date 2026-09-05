package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Official Sudhanihub Brand Logo Component
 *
 * Faithfully reproduces the official brand identity from the uploaded asset:
 * - Deep navy blue background (#070E4E)
 * - Clean white shopping cart with round wheels and angled handle
 * - Two golden-yellow (#F5A623) horizontal rounded product bars in the basket
 * - Golden-yellow badge with white '+' icon on the top right
 * - Bold italic 'Sudhani' (Golden-Yellow) + 'hub' (White) brand signature
 */
@Composable
fun SudhanihubLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showText: Boolean = true,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null
) {
    val cornerRadius = size * 0.22f

    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(SudhaniNavyPrimary)
            .then(clickModifier)
            .testTag("sudhanihub_official_logo"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = size * 0.08f, vertical = size * 0.08f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cart and Plus Badge Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (showText) 0.72f else 1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSudhaniCartWithBadge(
                        cartColor = Color.White,
                        goldColor = SudhaniGoldPrimary
                    )
                }
            }

            // 'Sudhanihub' Text at the bottom of the badge
            if (showText && size >= 36.dp) {
                val fontSize = (size.value * 0.17f).sp
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = SudhaniGoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append("Sudhani")
                        }
                        withStyle(
                            SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append("hub")
                        }
                    },
                    fontSize = fontSize,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = size * 0.02f)
                )
            }
        }
    }
}

/**
 * Draws the white shopping cart wireframe, the two gold inside bars, and the gold plus badge.
 */
private fun DrawScope.drawSudhaniCartWithBadge(
    cartColor: Color,
    goldColor: Color
) {
    val w = size.width
    val h = size.height

    val strokeWidth = (w * 0.065f).coerceAtLeast(1.5f)

    // 1. Shopping Cart Frame (Handle -> Front Lip -> Base Floor -> Back Upright)
    val cartPath = Path().apply {
        // Handle angled from top-left
        moveTo(w * 0.14f, h * 0.18f)
        lineTo(w * 0.28f, h * 0.22f)
        // Curves downward into basket front
        cubicTo(
            w * 0.33f, h * 0.23f,
            w * 0.35f, h * 0.32f,
            w * 0.38f, h * 0.45f
        )
        // Basket front wall down to bottom
        lineTo(w * 0.42f, h * 0.76f)
        // Basket bottom floor
        cubicTo(
            w * 0.43f, h * 0.82f,
            w * 0.48f, h * 0.84f,
            w * 0.55f, h * 0.84f
        )
        lineTo(w * 0.76f, h * 0.84f)
        // Curves up to back lip
        cubicTo(
            w * 0.82f, h * 0.84f,
            w * 0.85f, h * 0.78f,
            w * 0.88f, h * 0.65f
        )
        lineTo(w * 0.92f, h * 0.46f)
    }

    drawPath(
        path = cartPath,
        color = cartColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 2. Cart Undercarriage Loop connecting wheel base
    val chassisPath = Path().apply {
        moveTo(w * 0.39f, h * 0.83f)
        cubicTo(
            w * 0.36f, h * 0.90f,
            w * 0.42f, h * 0.94f,
            w * 0.48f, h * 0.94f
        )
        lineTo(w * 0.76f, h * 0.94f)
        cubicTo(
            w * 0.82f, h * 0.94f,
            w * 0.86f, h * 0.89f,
            w * 0.84f, h * 0.83f
        )
    }
    drawPath(
        path = chassisPath,
        color = cartColor,
        style = Stroke(
            width = strokeWidth * 0.9f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 3. Wheels (two white ring circles)
    val wheelRadius = w * 0.08f
    val wheelStroke = strokeWidth * 0.75f
    val leftWheelCenter = Offset(w * 0.44f, h * 1.05f)
    val rightWheelCenter = Offset(w * 0.77f, h * 1.05f)

    drawCircle(
        color = cartColor,
        radius = wheelRadius,
        center = leftWheelCenter,
        style = Stroke(width = wheelStroke)
    )
    drawCircle(
        color = cartColor,
        radius = wheelRadius,
        center = rightWheelCenter,
        style = Stroke(width = wheelStroke)
    )

    // 4. Two Golden Yellow Horizontal Bars inside the Cart
    val barHeight = h * 0.09f
    val barCorner = CornerRadius(barHeight / 2, barHeight / 2)

    // Upper bar (longer)
    drawRoundRect(
        color = goldColor,
        topLeft = Offset(w * 0.42f, h * 0.44f),
        size = Size(w * 0.40f, barHeight),
        cornerRadius = barCorner
    )

    // Lower bar (slightly shorter to match tapered cart)
    drawRoundRect(
        color = goldColor,
        topLeft = Offset(w * 0.46f, h * 0.60f),
        size = Size(w * 0.34f, barHeight),
        cornerRadius = barCorner
    )

    // 5. Golden Circular Badge with White Plus Sign on Top-Right
    val badgeRadius = w * 0.17f
    val badgeCenter = Offset(w * 0.88f, h * 0.28f)

    drawCircle(
        color = goldColor,
        radius = badgeRadius,
        center = badgeCenter
    )

    // Plus symbol in white
    val plusArm = badgeRadius * 0.58f
    val plusStroke = strokeWidth * 0.85f

    // Horizontal arm
    drawLine(
        color = Color.White,
        start = Offset(badgeCenter.x - plusArm, badgeCenter.y),
        end = Offset(badgeCenter.x + plusArm, badgeCenter.y),
        strokeWidth = plusStroke,
        cap = StrokeCap.Round
    )
    // Vertical arm
    drawLine(
        color = Color.White,
        start = Offset(badgeCenter.x, badgeCenter.y - plusArm),
        end = Offset(badgeCenter.x, badgeCenter.y + plusArm),
        strokeWidth = plusStroke,
        cap = StrokeCap.Round
    )
}

/**
 * Compact brand lockup with official logo mark and Sudhanihub typography
 */
@Composable
fun SudhanihubBrandBadge(
    modifier: Modifier = Modifier,
    logoSize: Dp = 34.dp,
    showSubline: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SudhanihubLogo(
            size = logoSize,
            showText = false,
            elevation = 1.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sudhani",
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    color = SudhaniNavyPrimary
                )
                Text(
                    text = "hub",
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    color = SudhaniGoldDark
                )
            }
            if (showSubline) {
                Text(
                    text = "10-Min Grocery Delivery",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensitySlate500,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}
