package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.ProfileImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun CropAndAdjustImageDialog(
    sourceBitmap: Bitmap,
    onImageSaved: (savedPath: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var isProcessing by remember { mutableStateOf(false) }

    val viewportSizeDp = 260.dp
    val viewportSizePx = with(density) { viewportSizeDp.toPx() }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .testTag("crop_dialog_surface"),
            shape = RoundedCornerShape(20.dp),
            color = SudhaniTheme.colors.cardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Adjust & Crop Avatar",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = SudhaniTheme.colors.textPrimary
                        )
                        Text(
                            text = "Drag to reposition • Pinch or slide to zoom",
                            fontSize = 12.sp,
                            color = SudhaniTheme.colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = { if (!isProcessing) onDismiss() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SudhaniTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Crop Viewport
                Box(
                    modifier = Modifier
                        .size(viewportSizeDp)
                        .clip(CircleShape)
                        .background(SudhaniNavyDark)
                        .border(2.5.dp, SudhaniGoldPrimary, CircleShape)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1.0f, 3.5f)
                                val maxPan = (viewportSizePx * (scale - 1f) / 2f).coerceAtLeast(viewportSizePx * 0.5f)
                                offsetX = (offsetX + pan.x).coerceIn(-maxPan, maxPan)
                                offsetY = (offsetY + pan.y).coerceIn(-maxPan, maxPan)
                            }
                        }
                        .testTag("crop_viewport"),
                    contentAlignment = Alignment.Center
                ) {
                    // Transformed Image
                    val imageBitmap = remember(sourceBitmap) { sourceBitmap.asImageBitmap() }
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap,
                        contentDescription = "Crop Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                                rotationZ = rotationDegrees
                            }
                    )

                    // Subtle alignment grid overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 0.8.dp.toPx()
                        val gridColor = SudhaniGoldLight.copy(alpha = 0.25f)
                        val oneThirdW = size.width / 3f
                        val twoThirdW = 2 * size.width / 3f
                        val oneThirdH = size.height / 3f
                        val twoThirdH = 2 * size.height / 3f

                        // Vertical guidelines
                        drawLine(gridColor, Offset(oneThirdW, 0f), Offset(oneThirdW, size.height), strokeWidth)
                        drawLine(gridColor, Offset(twoThirdW, 0f), Offset(twoThirdW, size.height), strokeWidth)
                        // Horizontal guidelines
                        drawLine(gridColor, Offset(0f, oneThirdH), Offset(size.width, oneThirdH), strokeWidth)
                        drawLine(gridColor, Offset(0f, twoThirdH), Offset(size.width, twoThirdH), strokeWidth)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Zoom Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = SudhaniTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 1.0f..3.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = SudhaniGoldPrimary,
                            activeTrackColor = SudhaniGoldDark,
                            inactiveTrackColor = SudhaniTheme.colors.chipBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("crop_zoom_slider")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = SudhaniTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Tool Controls (Rotate & Reset)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            rotationDegrees = (rotationDegrees + 90f) % 360f
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SudhaniTheme.colors.textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder),
                        modifier = Modifier.testTag("crop_rotate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "Rotate 90 degrees",
                            modifier = Modifier.size(16.dp),
                            tint = SudhaniGoldDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotate 90°", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = {
                            scale = 1.0f
                            offsetX = 0f
                            offsetY = 0f
                            rotationDegrees = 0f
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SudhaniTheme.colors.textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder),
                        modifier = Modifier.testTag("crop_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Crop",
                            modifier = Modifier.size(16.dp),
                            tint = SudhaniTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("crop_cancel_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder)
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            color = SudhaniTheme.colors.textSecondary
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val savedPath = withContext(Dispatchers.IO) {
                                    ProfileImageManager.saveCroppedProfilePicture(
                                        context = context,
                                        sourceBitmap = sourceBitmap,
                                        zoomScale = scale,
                                        panOffsetX = offsetX,
                                        panOffsetY = offsetY,
                                        rotationDegrees = rotationDegrees,
                                        viewportSizePx = viewportSizePx
                                    )
                                }
                                isProcessing = false
                                if (savedPath != null) {
                                    onImageSaved(savedPath)
                                } else {
                                    // Fallback: save sourceBitmap directly
                                    val fallbackPath = withContext(Dispatchers.IO) {
                                        ProfileImageManager.saveBitmapDirectly(context, sourceBitmap)
                                    }
                                    if (fallbackPath != null) {
                                        onImageSaved(fallbackPath)
                                    }
                                }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("crop_apply_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SudhaniGoldPrimary,
                            contentColor = SudhaniNavyDark
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SudhaniNavyDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SudhaniNavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply & Save", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                        }
                    }
                }
            }
        }
    }
}
