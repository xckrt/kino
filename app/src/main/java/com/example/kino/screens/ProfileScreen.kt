package com.example.kino.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kino.components.AchievementCard
import com.example.kino.components.Badge
import com.example.kino.components.LoyaltyBottomSheet
import com.example.kino.components.ProfileReviewItem
import com.example.kino.dialogs.AllAchievementsDialog
import com.example.kino.dialogs.ChangeNameDialog
import com.example.kino.utils.AchievementSystem
import com.example.kino.utils.LoyaltySystem
import com.example.kino.viewmodel.CinemaViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    viewModel: CinemaViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val topUsers by viewModel.topUsers.collectAsState()
    val myReviews by viewModel.currentUserReviews.collectAsState()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showLoyaltySheet by remember { mutableStateOf(false) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val currentUser by viewModel.currentUserProfile.collectAsState()
    var showAllAchievements by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.loadUserReviews()
    }
    val displayName = remember(currentUser) {
        if (currentUser?.username.isNullOrEmpty()) {
            currentUser?.email?.substringBefore("@") ?: "User"
        } else {
            currentUser!!.username
        }
    }
    if (showEditNameDialog) {
        ChangeNameDialog(
            currentName = displayName,
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                viewModel.updateUsername(newName)
                showEditNameDialog = false
            }
        )
    }
    if (showAllAchievements && currentUser != null) {
        AllAchievementsDialog(
            unlockedIds = currentUser!!.unlockedAchievements,
            onDismiss = { showAllAchievements = false }
        )
    }
    if (showLoyaltySheet && currentUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showLoyaltySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LoyaltyBottomSheet(
                currentRating = currentUser!!.rating,
                currentPoints = currentUser!!.points,
                onDismiss = { showLoyaltySheet = false }
            )
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Мой Профиль", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (currentUser == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser!!.email.take(1).uppercase(),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showEditNameDialog = true }
                        ) {
                            Text(
                                text = displayName,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Изменить имя",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = currentUser!!.email,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showLoyaltySheet = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(text = LoyaltySystem.getStatusLabel(currentUser!!.rating))
                            Spacer(Modifier.width(8.dp))
                            Badge(text = "${currentUser!!.points} баллов", color = Color(0xFF4CAF50))

                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Достижения",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        TextButton(onClick = { showAllAchievements = true }) {
                            Text("Все", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(AchievementSystem.list) { achievement ->
                            val isUnlocked = currentUser!!.unlockedAchievements.contains(achievement.id)
                            AchievementCard(achievement, isUnlocked)
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Мои рецензии (${myReviews.size})",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                if (myReviews.isEmpty()) {
                    item {
                        Text(
                            "Вы еще не оставили ни одной рецензии",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    items(myReviews) { review ->
                        ProfileReviewItem(review)
                    }
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Зал Славы",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                itemsIndexed(topUsers) { index, user ->
                    val isMe = user.uid == currentUid
                    val bgColor = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "#${index + 1}",
                            color = if (index < 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(30.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMe) {
                                    val name = user.username.ifEmpty { user.email }
                                    "$name (Вы)"
                                } else user.username.ifEmpty {
                                    user.email.substringBefore("@") + "***"
                                },
                                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", user.rating),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surface)
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Настройки",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    val isDark by viewModel.isDarkTheme.collectAsState()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Темная тема",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp
                        )

                        Switch(
                            checked = isDark,
                            onCheckedChange = { newValue ->
                                viewModel.toggleTheme(newValue)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(32.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Выйти")
                    }
                }
            }
        }
    }
}

