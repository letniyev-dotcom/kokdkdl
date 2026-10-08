package dev.test.blurnav

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// Палитра из прототипа (тема «Венеция»)
private val Bg = Color(0xFFF7F4FC)
private val Container = Color(0xFFE8E0F3)
private val NavInd = Color(0xFFDDD2F1)
private val OnInd = Color(0xFF25164A)
private val TextMain = Color(0xFF1C1A22)
private val Text2 = Color(0xFF6C6779)
private val NavColor = lerp(Bg, Container, 0.38f)      // bg 62% + container 38%
private val PillColor = lerp(Container, NavInd, 0.78f)  // nav-ind 78% + container 22%

private enum class Mode(val label: String) { Progressive("Progressive"), Mask("Mask"), Off("Off") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(background = Bg, surface = Bg)) { App() }
        }
    }
}

@Composable
private fun App() {
    val hazeState = rememberHazeState()
    var mode by remember { mutableStateOf(Mode.Progressive) }
    var radius by remember { mutableFloatStateOf(20f) }
    var extra by remember { mutableFloatStateOf(2f) }
    var tint by remember { mutableFloatStateOf(0.8f) }
    var tab by remember { mutableIntStateOf(1) }

    val inset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navContent = 72.dp
    val navH = navContent + inset
    val zoneH = navH + extra.dp

    Box(Modifier.fillMaxSize().background(Bg)) {
        // Контент, который размывается под навбаром
        LazyColumn(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState),
            contentPadding = PaddingValues(bottom = navH + 16.dp),
        ) {
            item {
                Controls(
                    mode, { mode = it },
                    radius, { radius = it },
                    extra, { extra = it },
                    tint, { tint = it },
                )
            }
            itemsIndexed(CHATS) { i, c -> ChatRow(c, ACCENTS[i % ACCENTS.size]) }
        }

        // Слой 1: блюр
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(zoneH)
                .hazeEffect(state = hazeState) {
                    backgroundColor = Bg
                    blurRadius = radius.dp
                    noiseFactor = 0f
                    blurEnabled = mode != Mode.Off
                    progressive = if (mode == Mode.Progressive) {
                        HazeProgressive.verticalGradient(startIntensity = 0f, endIntensity = 1f)
                    } else null
                    mask = if (mode == Mode.Mask) {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
                    } else null
                },
        )
        // Слой 2: лёгкий цвет навбара прямо в блюре (80% внизу, 50% на 55% высоты, 0 вверху)
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(zoneH)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to NavColor.copy(alpha = tint * 0.625f),
                        1f to NavColor.copy(alpha = tint),
                    )
                ),
        )
        // Слой 3: сам навбар (иконки и пилюля, размеры как в прототипе: 72x38, иконка 28, подпись 14)
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = inset)
                .height(navContent)
                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
        ) {
            NavItem("Всякое", Icons.Outlined.Star, tab == 0, Modifier.weight(1f)) { tab = 0 }
            NavItem("Чаты", Icons.Outlined.Home, tab == 1, Modifier.weight(1f)) { tab = 1 }
            NavItem("Профиль", Icons.Outlined.Person, tab == 2, Modifier.weight(1f)) { tab = 2 }
        }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val pill by animateColorAsState(if (selected) PillColor else PillColor.copy(alpha = 0f), label = "pill")
    val fg by animateColorAsState(if (selected) OnInd else Text2, label = "fg")
    val tx by animateColorAsState(if (selected) TextMain else Text2, label = "tx")
    Column(
        modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            Modifier.size(72.dp, 38.dp).clip(RoundedCornerShape(19.dp)).background(pill),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(28.dp)) }
        Text(label, color = tx, fontSize = 14.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun Controls(
    mode: Mode, onMode: (Mode) -> Unit,
    radius: Float, onRadius: (Float) -> Unit,
    extra: Float, onExtra: (Float) -> Unit,
    tint: Float, onTint: (Float) -> Unit,
) {
    Column(Modifier.statusBarsPadding().padding(16.dp)) {
        Text("Blur test (Haze 1.7.2)", fontSize = 22.sp, color = TextMain)
        Text("Android API ${Build.VERSION.SDK_INT}. Крути список под навбаром.", fontSize = 13.sp, color = Text2)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Mode.entries.forEach { m ->
                FilterChip(selected = mode == m, onClick = { onMode(m) }, label = { Text(m.label) })
            }
        }
        Text("Радиус блюра: ${radius.toInt()} dp", fontSize = 13.sp, color = Text2)
        Slider(radius, onRadius, valueRange = 0f..40f)
        Text("Блюр выше навбара: ${extra.toInt()} dp", fontSize = 13.sp, color = Text2)
        Slider(extra, onExtra, valueRange = 0f..60f)
        Text("Цвет навбара в блюре: ${(tint * 100).toInt()}%", fontSize = 13.sp, color = Text2)
        Slider(tint, onTint, valueRange = 0f..1f)
    }
}

@Composable
private fun ChatRow(c: Pair<String, String>, accent: Color) {
    Row(
        Modifier.fillMaxWidth().height(88.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
            Text(c.first.take(2).uppercase(), color = Color.White, fontSize = 17.sp)
        }
        Column {
            Text(c.first, fontSize = 17.sp, color = TextMain)
            Text(c.second, fontSize = 15.sp, color = Text2, maxLines = 1)
        }
    }
}

private val ACCENTS = listOf(
    Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFFBA68C8),
    Color(0xFFFFB74D), Color(0xFF4DB6AC), Color(0xFFF06292),
)
private val CHATS = listOf(
    "Полина" to "Голосовое сообщение", "Артур С" to "Привет, увидимся позже?",
    "Мама" to "смотри, что нашла", "Тимбилдинг" to "классика, ха-ха",
    "Моника" to "Привет! Нашла классную тему", "Джессика" to "Привет, ты тут?",
    "Алисия" to "Эй, ты пропал", "Дмитрий" to "Здарова",
).let { base -> List(32) { base[it % base.size].let { (n, m) -> "$n ${it / base.size + 1}" to m } } }
