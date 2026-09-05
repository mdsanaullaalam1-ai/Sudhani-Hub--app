package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserProfile
import com.example.ui.theme.SudhaniGoldDark
import com.example.ui.theme.SudhaniGoldLight
import com.example.ui.theme.SudhaniGoldPrimary
import com.example.ui.theme.SudhaniNavyDark
import com.example.ui.theme.SudhaniNavyPrimary
import java.io.File

/**
 * Reusable Circular User Avatar component.
 * Displays:
 * 1. The custom user profile picture (if uploaded & exists)
 * 2. Elegant fallback with user's initials & Sudhanihub navy/gold styling
 * 3. Optional small pencil/edit icon badge on the bottom right
 */
@Composable
fun UserAvatar(
    user: UserProfile,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showEditBadge: Boolean = false,
    useCameraIconForBadge: Boolean = false,
    onEditClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val hasValidPhoto = !user.profilePicturePath.isNullOrBlank() && File(user.profilePicturePath).exists()
    val badgeSize = (size.value * 0.34f).coerceIn(20f, 32f).dp
    val borderWidth = if (size >= 80.dp) 2.5.dp else 2.dp
    val textSize = (size.value * 0.38f).coerceIn(14f, 36f).sp

    Box(
        modifier = modifier
            .size(size)
            .testTag("user_avatar_container")
    ) {
        // Main Avatar Circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SudhaniNavyPrimary, SudhaniNavyDark)
                    )
                )
                .border(borderWidth, SudhaniGoldPrimary, CircleShape)
                .testTag("user_avatar_circle"),
            contentAlignment = Alignment.Center
        ) {
            if (hasValidPhoto) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(user.profilePicturePath!!))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture of ${user.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .testTag("user_avatar_image")
                )
            } else {
                // Initials Fallback with clean Gold typography
                Text(
                    text = user.initials,
                    fontSize = textSize,
                    fontWeight = FontWeight.Black,
                    color = SudhaniGoldLight,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.testTag("user_avatar_initials")
                )
            }
        }

        // Small Edit/Pencil Badge (Optional)
        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(SudhaniGoldPrimary)
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable(enabled = onEditClick != null) { onEditClick?.invoke() }
                    .testTag("user_avatar_edit_badge"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (useCameraIconForBadge) Icons.Default.CameraAlt else Icons.Default.Edit,
                    contentDescription = "Edit Profile Picture",
                    tint = SudhaniNavyDark,
                    modifier = Modifier.size(badgeSize * 0.58f)
                )
            }
        }
    }
}
