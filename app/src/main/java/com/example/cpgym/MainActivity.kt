package com.example.cpgym

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cpgym.ui.theme.CpGymTheme


// ================================
// CORES DO APLICATIVO
// ================================

val VermelhoGym = Color(0xFFD32F2F)
val VermelhoEscuro = Color(0xFFB71C1C)

val CinzaFundo = Color(0xFFF2F2F2)
val CinzaCard = Color(0xFFE0E0E0)
val CinzaTexto = Color(0xFF424242)
val CinzaEscuro = Color(0xFF212121)


// ================================
// MODELO DE TREINO
// ================================

data class Treino(
    val titulo: String,
    val foto: Bitmap
)


// ================================
// ARMAZENAMENTO LOCAL (AULA 08)
// Camada de Dados & Nativo: só esta
// classe conhece o SharedPreferences.
// ================================

class TreinoStore(context: Context) {

    // abre (ou cria) a gaveta "GymRatsPrefs",
    // visível só para este app
    private val prefs =
        context.getSharedPreferences(
            "GymRatsPrefs",
            Context.MODE_PRIVATE
        )

    // lê o total de treinos já registrados;
    // se nunca foi gravado, devolve 0 (plano B,
    // nunca crash)
    fun lerTotalTreinos(): Int =
        prefs.getInt("total_treinos", 0)

    // edit() abre a transação, putInt grava o
    // valor, apply() persiste em background
    // sem travar a tela
    fun salvarTotalTreinos(v: Int) {
        prefs.edit()
            .putInt("total_treinos", v)
            .apply()
    }
}


// ================================
// MAIN ACTIVITY
// ================================

class MainActivity : ComponentActivity() {

    // Foto tirada pela câmera
    private var cameraBitmap by mutableStateOf<Bitmap?>(null)


    // Launcher responsável por abrir a câmera
    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val bitmap =
                    result.data?.extras?.get("data") as? Bitmap

                cameraBitmap = bitmap
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            CpGymTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CinzaFundo
                ) { innerPadding ->

                    GymRatsScreen(

                        modifier = Modifier.padding(innerPadding),

                        cameraBitmap = cameraBitmap,

                        // Abre a câmera
                        onOpenCamera = {

                            val intent = Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE
                            )

                            cameraLauncher.launch(intent)
                        },

                        // Limpa a foto atual
                        onClearCamera = {

                            cameraBitmap = null
                        }
                    )
                }
            }
        }
    }
}


// ================================
// TELA PRINCIPAL
// ================================

@Composable
fun GymRatsScreen(

    modifier: Modifier = Modifier,

    cameraBitmap: Bitmap?,

    onOpenCamera: () -> Unit,

    onClearCamera: () -> Unit
) {

    // =================================
    // ARMAZENAMENTO LOCAL (AULA 08)
    // o store é criado uma vez, junto com a tela
    // =================================

    val context = LocalContext.current

    val treinoStore = remember {
        TreinoStore(context)
    }


    // =================================
    // ESTADO DO TÍTULO
    // =================================

    var tituloTreino by remember {

        mutableStateOf("")
    }


    // =================================
    // LISTA DE TREINOS
    // =================================

    val treinos = remember {

        mutableStateListOf<Treino>()
    }


    // =================================
    // TOTAL DE TREINOS (PERSISTIDO)
    // rememberSaveable segura a rotação;
    // o valor inicial vem do disco (ou 0,
    // se nunca foi gravado) — é o que
    // segura o fechamento do app.
    // =================================

    var totalTreinos by rememberSaveable {

        mutableStateOf(treinoStore.lerTotalTreinos())
    }


    Column(

        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        // ================================
        // CABEÇALHO
        // ================================

        Text(

            text = "GYMRATS",

            fontSize = 30.sp,

            fontWeight = FontWeight.Bold,

            color = VermelhoGym
        )


        Spacer(
            modifier = Modifier.height(6.dp)
        )


        Text(

            text = "Registre seu treino",

            fontSize = 15.sp,

            color = CinzaTexto
        )


        Spacer(
            modifier = Modifier.height(6.dp)
        )


        // ================================
        // TOTAL PERSISTIDO (SharedPreferences)
        // sobrevive à rotação e ao fechamento
        // do app
        // ================================

        Text(

            text = "Total de treinos registrados: $totalTreinos",

            fontSize = 13.sp,

            fontWeight = FontWeight.Medium,

            color = VermelhoGym
        )


        Spacer(
            modifier = Modifier.height(26.dp)
        )


        // ================================
        // ÁREA DA FOTO
        // ================================

        Box(

            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(CinzaCard),

            contentAlignment = Alignment.Center
        ) {

            if (cameraBitmap != null) {

                Image(

                    bitmap = cameraBitmap.asImageBitmap(),

                    contentDescription = "Foto do treino",

                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(16.dp)
                        ),

                    contentScale = ContentScale.Crop
                )

            } else {

                Column(

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(

                        text = "📷",

                        fontSize = 42.sp
                    )


                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    Text(

                        text =
                            "Nenhuma foto registrada",

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.Medium,

                        color = CinzaTexto,

                        textAlign =
                            TextAlign.Center
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ================================
        // BOTÃO DA CÂMERA
        // ================================

        Button(

            onClick = onOpenCamera,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape =
                RoundedCornerShape(12.dp),

            colors =
                ButtonDefaults.buttonColors(

                    containerColor =
                        VermelhoGym,

                    contentColor =
                        Color.White
                )
        ) {

            Text(

                text = "Tirar foto",

                fontSize = 16.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ================================
        // TÍTULO DO TREINO
        // ================================

        OutlinedTextField(

            value = tituloTreino,

            onValueChange = {

                tituloTreino = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {

                Text(
                    text = "Título do treino"
                )
            },

            placeholder = {

                Text(
                    text =
                        "Ex.: Peito e Tríceps"
                )
            },

            singleLine = true,

            shape =
                RoundedCornerShape(12.dp),

            colors =
                OutlinedTextFieldDefaults.colors(

                    focusedBorderColor =
                        VermelhoGym,

                    focusedLabelColor =
                        VermelhoGym,

                    cursorColor =
                        VermelhoGym,

                    unfocusedBorderColor =
                        Color.Gray
                )
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ================================
        // BOTÃO REGISTRAR
        // ================================

        Button(

            onClick = {

                // Só registra se tiver
                // foto e título

                if (
                    cameraBitmap != null &&
                    tituloTreino.isNotBlank()
                ) {

                    // Adiciona o treino
                    // na lista

                    treinos.add(

                        Treino(

                            titulo =
                                tituloTreino,

                            foto =
                                cameraBitmap
                        )
                    )


                    // Limpa o campo
                    // para o próximo treino

                    tituloTreino = ""


                    // Pede para a Activity
                    // limpar a foto atual

                    onClearCamera()


                    // Novo total na memória
                    // e no disco, na mesma ação
                    // (mesmo padrão do Contador
                    // da Aula 08)

                    totalTreinos = totalTreinos + 1

                    treinoStore.salvarTotalTreinos(
                        totalTreinos
                    )
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape =
                RoundedCornerShape(12.dp),

            colors =
                ButtonDefaults.buttonColors(

                    containerColor =
                        CinzaEscuro,

                    contentColor =
                        Color.White
                )
        ) {

            Text(

                text = "Registrar treino",

                fontSize = 16.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ================================
        // LISTA DE TREINOS
        // ================================

        if (treinos.isNotEmpty()) {

            Text(

                text = "MEUS TREINOS",

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    VermelhoGym
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // Lista que permite
            // vários treinos

            LazyColumn(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(treinos) { treino ->

                    TreinoCard(
                        treino = treino
                    )
                }
            }
        }
    }
}


// ================================
// CARD DO TREINO
// ================================

@Composable
fun TreinoCard(

    treino: Treino
) {

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .padding(16.dp)
    ) {


        // FOTO

        Image(

            bitmap =
                treino.foto.asImageBitmap(),

            contentDescription =
                "Foto do treino",

            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(
                    RoundedCornerShape(12.dp)
                ),

            contentScale =
                ContentScale.Crop
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // TÍTULO

        Text(

            text = treino.titulo,

            fontSize = 20.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                CinzaEscuro
        )


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        // DESCRIÇÃO

        Text(

            text = "Treino registrado",

            fontSize = 14.sp,

            color =
                CinzaTexto
        )
    }
}


// ================================
// PREVIEW
// ================================

@Preview(showBackground = true)
@Composable
fun GymRatsPreview() {

    CpGymTheme {

        GymRatsScreen(

            cameraBitmap = null,

            onOpenCamera = {},

            onClearCamera = {}
        )
    }
}