package com.example.cpgym

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cpgym.data.UserPreferences
import com.example.cpgym.data.WorkoutDatabase
import com.example.cpgym.data.WorkoutEntity
import com.example.cpgym.ui.theme.CpGymTheme
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

private val RedGym = Color(0xFFD32F2F)
private val DarkGym = Color(0xFF212121)
private val ScreenGray = Color(0xFFF6F6F6)

/** SharedPreferences stores this small aggregate independently of the database. */
class TrainingCounter(context: Context) {
    private val preferences = context.getSharedPreferences("GymRatsPrefs", Context.MODE_PRIVATE)
    fun get(): Int = preferences.getInt("total_treinos", 0)
    fun increment(): Int = get().plus(1).also { preferences.edit().putInt("total_treinos", it).apply() }
}

class MainActivity : ComponentActivity() {
    private var photo by mutableStateOf<Bitmap?>(null)
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo = it }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CpGymTheme { GymRatsScreen(photo, { cameraLauncher.launch(null) }, { photo = null }) } }
    }
}

@Composable
private fun GymRatsScreen(photo: Bitmap?, onTakePhoto: () -> Unit, onClearPhoto: () -> Unit) {
    val context = LocalContext.current
    val database = remember { WorkoutDatabase.get(context) }
    val workouts by database.workoutDao().observeAll().collectAsState(initial = emptyList())
    val counter = remember { TrainingCounter(context) }
    val preferences = remember { UserPreferences(context) }
    val savedName by preferences.userName.collectAsState(initial = "")
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var total by rememberSaveable { mutableStateOf(counter.get()) }
    var error by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(savedName) { if (name.isBlank()) name = savedName }

    Scaffold(containerColor = ScreenGray) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 28.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GYMRATS", color = RedGym, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                    Text("Seu diário de treinos", color = Color.DarkGray)
                    Spacer(Modifier.height(12.dp))
                    Text("Total registrado: $total", color = RedGym, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nome do aluno (DataStore)") }, singleLine = true, colors = fieldColors())
            }
            item {
                Button({ scope.launch { preferences.saveUserName(name) } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DarkGym)) { Text("Salvar perfil") }
            }
            item { Text(if (savedName.isBlank()) "Olá! Identifique-se para personalizar o app." else "Olá, $savedName!", fontWeight = FontWeight.Medium) }
            item { PhotoArea(photo) }
            item {
                Button(onTakePhoto, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = RedGym)) { Text("Tirar foto do treino") }
            }
            item {
                OutlinedTextField(title, { title = it; error = "" }, Modifier.fillMaxWidth(), label = { Text("Título do treino") }, placeholder = { Text("Ex.: Peito e tríceps") }, singleLine = true, colors = fieldColors())
            }
            if (error.isNotBlank()) item { Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
            item {
                Button(
                    onClick = {
                        val image = photo
                        if (title.isBlank() || image == null) error = "Informe o título e tire uma foto antes de registrar."
                        else scope.launch {
                            database.workoutDao().insert(WorkoutEntity(title = title.trim(), createdAt = System.currentTimeMillis(), photo = image.toBytes()))
                            total = counter.increment()
                            title = ""
                            onClearPhoto()
                        }
                    }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = DarkGym)
                ) { Text("Registrar treino", fontWeight = FontWeight.Bold) }
            }
            if (workouts.isNotEmpty()) {
                item { Text("MEUS TREINOS (Room / SQLite)", color = RedGym, fontWeight = FontWeight.Bold) }
                items(workouts, key = { it.id }) { WorkoutCard(it) }
            }
        }
    }
}

@Composable private fun PhotoArea(photo: Bitmap?) {
    Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
        if (photo != null) Image(photo.asImageBitmap(), "Foto atual do treino", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text("📷\nNenhuma foto registrada", textAlign = TextAlign.Center, fontSize = 16.sp, color = Color.DarkGray)
    }
}

@Composable private fun WorkoutCard(workout: WorkoutEntity) {
    val bitmap = remember(workout.id) { BitmapFactory.decodeByteArray(workout.photo, 0, workout.photo.size) }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(14.dp)) {
            Image(bitmap.asImageBitmap(), "Foto de ${workout.title}", Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.height(8.dp))
            Text(workout.title, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(workout.createdAt)), color = Color.DarkGray, fontSize = 13.sp)
        }
    }
}

private fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
    compress(Bitmap.CompressFormat.JPEG, 85, stream)
    stream.toByteArray()
}

@Composable private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedGym, focusedLabelColor = RedGym, cursorColor = RedGym)
