package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.data.repository.SudhaniRepository
import com.example.ui.components.CropAndAdjustImageDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import com.example.util.ProfileImageManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: UserProfile,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var email by remember { mutableStateOf(user.email) }
    var profilePicturePath by remember { mutableStateOf(user.profilePicturePath) }

    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var pendingCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Gallery Picker (Supports JPG, JPEG, PNG via PickVisualMedia.ImageOnly)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        showPhotoOptionsDialog = false
        if (uri != null) {
            val bitmap = ProfileImageManager.decodeBitmapFromUri(context, uri)
            if (bitmap != null) {
                pendingCropBitmap = bitmap
            } else {
                Toast.makeText(context, "Could not load selected photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        showPhotoOptionsDialog = false
        if (success && tempCameraUri != null) {
            val bitmap = ProfileImageManager.decodeBitmapFromUri(context, tempCameraUri!!)
            if (bitmap != null) {
                pendingCropBitmap = bitmap
            } else {
                Toast.makeText(context, "Could not load camera photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val (uri, _) = ProfileImageManager.createTempCameraUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    val launchCameraFlow: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val (uri, _) = ProfileImageManager.createTempCameraUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SudhaniTheme.colors.chipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = SudhaniGoldDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Text(
                        text = "Update avatar & personal details",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Profile Avatar with camera edit badge
                val previewUser = user.copy(
                    name = name.ifBlank { "User" },
                    profilePicturePath = profilePicturePath
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserAvatar(
                        user = previewUser,
                        size = 88.dp,
                        showEditBadge = true,
                        useCameraIconForBadge = true,
                        onEditClick = { showPhotoOptionsDialog = true },
                        modifier = Modifier
                            .clickable { showPhotoOptionsDialog = true }
                            .testTag("edit_profile_avatar_preview")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { showPhotoOptionsDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SudhaniTheme.colors.chipBackground,
                                contentColor = SudhaniTheme.colors.textPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("change_profile_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = SudhaniGoldDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Change Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!profilePicturePath.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    SudhaniRepository.removeProfilePicture()
                                    profilePicturePath = null
                                    Toast.makeText(context, "Profile picture removed", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("remove_profile_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Remove",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 0.8.dp)

                // Editable Fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SudhaniGoldDark) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SudhaniGoldDark) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SudhaniGoldDark) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        SudhaniRepository.updateUserProfile(
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            profilePicturePath = profilePicturePath
                        )
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SudhaniTheme.colors.textSecondary)
            }
        },
        shape = RoundedCornerShape(18.dp)
    )

    // Photo Selection Options Dialog
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            containerColor = SudhaniTheme.colors.surface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = SudhaniGoldDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Change Profile Picture",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Supported formats: JPG, JPEG, PNG",
                        fontSize = 12.sp,
                        color = SudhaniTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Gallery
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("option_choose_gallery"),
                        shape = RoundedCornerShape(12.dp),
                        color = SudhaniTheme.colors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SudhaniTheme.colors.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    tint = SudhaniGoldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Choose from Gallery",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SudhaniTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Select a photo from your device",
                                    fontSize = 11.sp,
                                    color = SudhaniTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // Option 2: Camera
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                launchCameraFlow()
                            }
                            .testTag("option_take_photo"),
                        shape = RoundedCornerShape(12.dp),
                        color = SudhaniTheme.colors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SudhaniTheme.colors.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = SudhaniNavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Take photo using Camera",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SudhaniTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Capture a new avatar now",
                                    fontSize = 11.sp,
                                    color = SudhaniTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // Option 3: Remove
                    if (!profilePicturePath.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SudhaniRepository.removeProfilePicture()
                                    profilePicturePath = null
                                    showPhotoOptionsDialog = false
                                    Toast.makeText(context, "Profile picture removed", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("option_remove_photo"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFCDD2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Remove Profile Picture",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFC62828)
                                    )
                                    Text(
                                        text = "Revert to initials avatar",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("Cancel", color = SudhaniTheme.colors.textSecondary)
                }
            }
        )
    }

    // Crop, Zoom, and Adjust Dialog
    if (pendingCropBitmap != null) {
        CropAndAdjustImageDialog(
            sourceBitmap = pendingCropBitmap!!,
            onImageSaved = { savedPath ->
                profilePicturePath = savedPath
                SudhaniRepository.updateProfilePicture(savedPath)
                pendingCropBitmap = null
                Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                pendingCropBitmap = null
            }
        )
    }
}

@Composable
fun WalletDialog(
    onDismiss: () -> Unit
) {
    val walletBalance by SudhaniRepository.walletBalance.collectAsState()
    val transactions by SudhaniRepository.walletTransactions.collectAsState()
    var customAmount by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SudhaniGoldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Sudhani Wallet", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                        Text("Instant refunds & 1-tap checkout", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Balance Banner
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniNavyPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("CURRENT BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "₹${"%.2f".format(walletBalance)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = SudhaniGoldPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = SudhaniGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Usable on any grocery delivery without OTP",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Add Money Section
                item {
                    Text("Add Money to Wallet", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SudhaniTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100.0, 250.0, 500.0, 1000.0).forEach { amt ->
                            OutlinedButton(
                                onClick = {
                                    SudhaniRepository.addMoneyToWallet(amt)
                                    Toast.makeText(context, "Added ₹${amt.toInt()} to wallet!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniGoldPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SudhaniGoldDark),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("+₹${amt.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SudhaniGoldDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { customAmount = it },
                            placeholder = { Text("Enter amount (₹)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val amt = customAmount.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    SudhaniRepository.addMoneyToWallet(amt)
                                    Toast.makeText(context, "Added ₹${amt.toInt()} to wallet!", Toast.LENGTH_SHORT).show()
                                    customAmount = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("Top Up", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                        }
                    }
                }

                // Transactions
                item {
                    Text("Recent Wallet Transactions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SudhaniTheme.colors.textPrimary)
                }

                items(transactions) { txn ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (txn.isCredit) SudhaniTheme.colors.chipBackground else SudhaniTheme.colors.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (txn.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (txn.isCredit) SudhaniGoldDark else SudhaniTheme.colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(txn.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
                                Text(txn.description, fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                                Text(txn.date, fontSize = 10.sp, color = SudhaniTheme.colors.textMuted)
                            }
                            Text(
                                text = "${if (txn.isCredit) "+" else "-"}₹${txn.amount.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (txn.isCredit) SudhaniGoldDark else SudhaniTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
            ) {
                Text("Done", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun ShoppingListsDialog(
    onDismiss: () -> Unit
) {
    val lists by SudhaniRepository.shoppingLists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = SudhaniGoldDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("My Shopping Lists", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                        Text("Order your recurring groceries in 1 tap", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SudhaniNavyDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create New Shopping List", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                    }
                }

                if (lists.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📝", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No shopping lists yet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SudhaniTheme.colors.textPrimary)
                                Text("Create lists like 'Weekly Milk & Veggies' for faster orders", fontSize = 12.sp, color = SudhaniTheme.colors.textSecondary)
                            }
                        }
                    }
                }

                items(lists) { list ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(list.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(list.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SudhaniTheme.colors.textPrimary)
                                        Text(list.description, fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                                    }
                                }
                                IconButton(onClick = { SudhaniRepository.deleteShoppingList(list.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = SudhaniTheme.colors.textMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Items pill display
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                list.itemNames.forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SudhaniGoldDark, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(item, fontSize = 12.sp, color = SudhaniTheme.colors.textSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Est. Total: ₹${list.estimatedTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = SudhaniGoldDark)
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Added ${list.itemNames.size} items to Cart!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SudhaniTheme.colors.chipBackground, contentColor = SudhaniGoldDark),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = SudhaniGoldDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add All to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SudhaniGoldDark)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )

    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        var rawItems by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SudhaniTheme.colors.surface,
            title = { Text("New Shopping List", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("List Name (e.g. Sunday Breakfast)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Short Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = rawItems,
                        onValueChange = { rawItems = it },
                        label = { Text("Items (comma separated)") },
                        placeholder = { Text("Milk, Eggs, Bread, Butter") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val itemsList = rawItems.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            SudhaniRepository.addShoppingList(
                                name = newName.trim(),
                                description = newDesc.ifBlank { "Custom shopping list" },
                                items = if (itemsList.isEmpty()) listOf("Fresh Milk", "Eggs") else itemsList,
                                estimatedTotal = (150..450).random().toDouble()
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
                ) {
                    Text("Create", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = SudhaniTheme.colors.textSecondary) }
            }
        )
    }
}

@Composable
fun SavedPaymentsDialog(
    onDismiss: () -> Unit
) {
    val paymentMethods by SudhaniRepository.savedPaymentMethods.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.82f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = SudhaniGoldDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Saved Payment Methods", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                        Text("Manage 1-click checkout accounts", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddCard, contentDescription = null, tint = SudhaniNavyDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New UPI ID or Card", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                    }
                }

                items(paymentMethods) { pm ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (pm.isDefault) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SudhaniTheme.colors.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (pm.type == "UPI") Icons.Default.QrCode else Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = SudhaniGoldDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pm.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SudhaniTheme.colors.textPrimary)
                                    if (pm.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = SudhaniTheme.colors.chipBackground,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "DEFAULT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SudhaniGoldDark,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(pm.subtitle, fontSize = 12.sp, color = SudhaniTheme.colors.textSecondary)
                            }

                            Row {
                                if (!pm.isDefault) {
                                    TextButton(onClick = { SudhaniRepository.setDefaultPaymentMethod(pm.id) }) {
                                        Text("Set Default", fontSize = 11.sp, color = SudhaniGoldDark)
                                    }
                                }
                                IconButton(onClick = { SudhaniRepository.deleteSavedPaymentMethod(pm.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = SudhaniTheme.colors.textMuted)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var details by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("UPI") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SudhaniTheme.colors.surface,
            title = { Text("Add Payment Method", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == "UPI",
                            onClick = { type = "UPI" },
                            label = { Text("UPI ID") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == "CARD",
                            onClick = { type = "CARD" },
                            label = { Text("Card") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (type == "UPI") "App Name (e.g. Paytm / GPay)" else "Bank Name (e.g. SBI Debit)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text(if (type == "UPI") "UPI ID (e.g. user@oksbi)" else "Card Number ending in 4 digits") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && details.isNotBlank()) {
                            SudhaniRepository.addSavedPaymentMethod(
                                SavedPaymentMethod(
                                    id = "PM_${System.currentTimeMillis()}",
                                    title = title.trim(),
                                    subtitle = details.trim(),
                                    type = type,
                                    isDefault = false
                                )
                            )
                            Toast.makeText(context, "Payment method saved!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
                ) {
                    Text("Save", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = SudhaniTheme.colors.textSecondary) }
            }
        )
    }
}

@Composable
fun ReviewsDialog(
    onDismiss: () -> Unit
) {
    val reviews by SudhaniRepository.userReviews.collectAsState()
    var showWriteReview by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SudhaniGoldDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("My Ratings & Reviews", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                        Text("Your verified feedback on fresh items", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Button(
                        onClick = { showWriteReview = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, tint = SudhaniNavyDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Write a Grocery Review", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                    }
                }

                items(reviews) { rev ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(rev.productEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(rev.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
                                }
                                Text(rev.date, fontSize = 10.sp, color = SudhaniTheme.colors.textMuted)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < rev.rating) SudhaniGoldDark else SudhaniTheme.colors.cardBorder,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${rev.comment}\"",
                                fontSize = 12.sp,
                                color = SudhaniTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )

    if (showWriteReview) {
        var prodName by remember { mutableStateOf("") }
        var selectedStars by remember { mutableStateOf(5) }
        var comment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showWriteReview = false },
            containerColor = SudhaniTheme.colors.surface,
            title = { Text("Rate a Grocery Product", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Product Name") },
                        placeholder = { Text("e.g. Fresh Tomatoes / Paneer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Rating:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { selectedStars = star }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star star",
                                    tint = if (star <= selectedStars) SudhaniGoldDark else SudhaniTheme.colors.cardBorder,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your Review / Feedback") },
                        placeholder = { Text("Freshness, delivery speed, packaging...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (prodName.isNotBlank() && comment.isNotBlank()) {
                            SudhaniRepository.addUserReview(
                                ProductReview(
                                    id = "REV_${System.currentTimeMillis()}",
                                    productName = prodName.trim(),
                                    productEmoji = "🥑",
                                    rating = selectedStars,
                                    date = "Just now",
                                    comment = comment.trim()
                                )
                            )
                            Toast.makeText(context, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
                            showWriteReview = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
                ) {
                    Text("Submit", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWriteReview = false }) { Text("Cancel", color = SudhaniTheme.colors.textSecondary) }
            }
        )
    }
}

@Composable
fun NotificationsDialog(
    onDismiss: () -> Unit
) {
    val notifs by SudhaniRepository.notifications.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.82f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = SudhaniGoldDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                        Text("${notifs.count { !it.isRead }} unread alerts", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { SudhaniRepository.markAllNotificationsAsRead() }) {
                            Text("Mark All as Read", fontSize = 12.sp, color = SudhaniGoldDark)
                        }
                        TextButton(onClick = { SudhaniRepository.clearAllNotifications() }) {
                            Text("Clear All", fontSize = 12.sp, color = HighDensityRed)
                        }
                    }
                }

                if (notifs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔔", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No notifications yet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SudhaniTheme.colors.textPrimary)
                                Text("We will notify you when your orders are dispatched", fontSize = 12.sp, color = SudhaniTheme.colors.textSecondary)
                            }
                        }
                    }
                }

                items(notifs) { notif ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) SudhaniTheme.colors.cardBackground else SudhaniTheme.colors.chipBackground
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (notif.isRead) SudhaniTheme.colors.cardBorder else SudhaniGoldPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { SudhaniRepository.markNotificationAsRead(notif.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SudhaniTheme.colors.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (notif.type) {
                                        "ORDER" -> Icons.Default.DeliveryDining
                                        "WALLET" -> Icons.Default.AccountBalanceWallet
                                        else -> Icons.Default.LocalOffer
                                    },
                                    contentDescription = null,
                                    tint = SudhaniGoldDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
                                    Text(notif.timeAgo, fontSize = 10.sp, color = SudhaniTheme.colors.textMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(notif.message, fontSize = 12.sp, color = SudhaniTheme.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun OffersDialog(
    onDismiss: () -> Unit
) {
    val coupons by SudhaniRepository.availableCoupons.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.80f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = SudhaniGoldDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sudhani Hub Offers", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(coupons) { coupon ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = SudhaniTheme.colors.chipBackground,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        coupon.code,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = SudhaniGoldDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(coupon.description, fontSize = 12.sp, color = SudhaniTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Min order: ₹${coupon.minimumOrder.toInt()}", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                            }
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString(coupon.code))
                                    Toast.makeText(context, "Copied code: ${coupon.code}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun GroceryDealsDialog(
    onDismiss: () -> Unit,
    onNavigateToProduct: (String) -> Unit
) {
    val products by SudhaniRepository.products.collectAsState()
    val dealProducts = products.filter { it.discountPercent > 0 || it.isDeal }.take(8)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = SudhaniGoldDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Daily Grocery Deals", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dealProducts) { prod ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onNavigateToProduct(prod.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prod.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
                                Text("Unit: ${prod.unit}", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${prod.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SudhaniGoldDark)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (prod.mrp > prod.price) {
                                        Text(
                                            "₹${prod.mrp.toInt()}",
                                            fontSize = 11.sp,
                                            color = SudhaniTheme.colors.textMuted,
                                            style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = HighDensityRed.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                "${prod.discountPercent}% OFF",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityRed,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { SudhaniRepository.addToCart(prod) }) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = "Add", tint = SudhaniGoldDark)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun ReferAndEarnDialog(
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val referralCode = "SANAULLA50"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SudhaniTheme.colors.chipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = SudhaniGoldDark)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Refer & Earn", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                    Text("Give ₹100, Get ₹100 grocery cash", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎁", fontSize = 48.sp)
                Text(
                    "Invite your friends and neighbors to Sudhani Hub. When they place their first delivery order, both of you get ₹100 added directly to your Sudhani Wallet!",
                    fontSize = 13.sp,
                    color = SudhaniTheme.colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.chipBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniGoldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("YOUR REFERRAL CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SudhaniGoldDark)
                            Text(referralCode, fontSize = 18.sp, fontWeight = FontWeight.Black, color = SudhaniTheme.colors.textPrimary)
                        }
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(referralCode))
                                Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
                        ) {
                            Text("Copy", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString("Use code $referralCode on Sudhani Hub to get ₹100 off on fresh groceries delivered in 10 minutes!"))
                    Toast.makeText(context, "Share link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = SudhaniNavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Invite", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun SupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SudhaniTheme.colors.chipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = SudhaniGoldDark)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Help & 24/7 Support", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                    Text("Instant resolution in under 2 minutes", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.chipBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("100% Quality & Refund Guarantee", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniGoldDark)
                            Text("Damaged or missing item? Instant wallet refund with no return questions asked.", fontSize = 11.sp, color = SudhaniTheme.colors.textSecondary)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Calling Toll-Free 1800-SUDHANI...", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = SudhaniGoldDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Toll-Free Customer Care", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
                            Text("1800-SUDHANI (1800-783-4264)", fontSize = 12.sp, color = SudhaniGoldDark)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Email copied: support@sudhanihub.com", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = SudhaniGoldDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Email Support", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
                            Text("support@sudhanihub.com", fontSize = 12.sp, color = SudhaniGoldDark)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
            ) {
                Text("Got It", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun FaqDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.80f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SudhaniTheme.colors.chipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = SudhaniGoldDark)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Frequently Asked Questions", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FaqItem(
                        question = "How does 10-minute grocery delivery work?",
                        answer = "Sudhani Hub operates localized micro dark-stores within 2-3 kms of your neighborhood. When you place an order, our store pickers pack in 2 minutes and delivery partners reach your door within 10-15 minutes."
                    )
                }
                item {
                    FaqItem(
                        question = "What if an item is rotten or defective?",
                        answer = "We offer a 100% freshness guarantee. You can request a refund directly from the My Orders screen or via Help & Support. The refund is credited immediately to your Sudhani Wallet."
                    )
                }
                item {
                    FaqItem(
                        question = "Is there a minimum order requirement?",
                        answer = "No minimum order requirement! You can order even a single packet of milk. Free delivery is available on orders above ₹199."
                    )
                }
                item {
                    FaqItem(
                        question = "Which payment modes are accepted?",
                        answer = "We support UPI (GPay, PhonePe, Paytm), Credit/Debit Cards, Net Banking, Sudhani Wallet, and Cash on Delivery (COD)."
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(question, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(answer, fontSize = 12.sp, color = SudhaniTheme.colors.textSecondary, lineHeight = 16.sp)
        }
    }
}

@Composable
fun TermsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
        text = {
            Text(
                "Sudhani Hub operates hyper-local high-speed delivery adhering strictly to consumer protection laws, food safety standards (FSSAI), and transparent pricing. All products undergo rigorous batch inspection prior to dispatch. Users agree to provide genuine delivery addresses and receive timely deliveries.",
                fontSize = 13.sp,
                color = SudhaniTheme.colors.textSecondary,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
            ) {
                Text("I Understand", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
        text = {
            Text(
                "At Sudhani Hub, we respect your privacy. Your phone number, email address, and precise GPS delivery coordinates are encrypted and used solely for delivering your groceries quickly and notifying you of order milestones. We never sell or share your personal data with third-party advertisers.",
                fontSize = 13.sp,
                color = SudhaniTheme.colors.textSecondary,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
            ) {
                Text("Got It", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HighDensityRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = HighDensityRed)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Log Out of Sudhani Hub?", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
            }
        },
        text = {
            Text(
                "Are you sure you want to log out? You can easily sign back in anytime using your registered mobile number.",
                fontSize = 13.sp,
                color = SudhaniTheme.colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityRed),
                modifier = Modifier.testTag("confirm_logout_button")
            ) {
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SudhaniTheme.colors.textSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun WishlistDialog(
    onDismiss: () -> Unit,
    onNavigateToProduct: (String) -> Unit
) {
    val wishlist by SudhaniRepository.wishlist.collectAsState()
    val products by SudhaniRepository.products.collectAsState()
    val wishlistedProducts = products.filter { wishlist.contains(it.id) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HighDensityRed.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = HighDensityRed)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("My Wishlist (${wishlistedProducts.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SudhaniTheme.colors.textPrimary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            if (wishlistedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❤️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your wishlist is empty", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SudhaniTheme.colors.textPrimary)
                        Text("Tap the heart icon on any product to save it here", fontSize = 12.sp, color = SudhaniTheme.colors.textSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(wishlistedProducts) { prod ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    onNavigateToProduct(prod.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prod.emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniTheme.colors.textPrimary)
                                    Text("₹${prod.price.toInt()} • ${prod.unit}", fontSize = 12.sp, color = SudhaniGoldDark, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = {
                                        SudhaniRepository.addToCart(prod)
                                        Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Add", tint = SudhaniGoldDark)
                                }
                                IconButton(
                                    onClick = { SudhaniRepository.toggleWishlist(prod.id) }
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Remove", tint = HighDensityRed)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SudhaniTheme.colors.textSecondary) }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

