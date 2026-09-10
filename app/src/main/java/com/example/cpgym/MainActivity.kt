package com.example.cpgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cpgym.ui.theme.CpGymTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CpGymTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    GymRatsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GymRatsScreen(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "GymRats"
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Nenhuma foto registrada"
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                // Vamos colocar a câmera aqui depois
            }
        ) {
            Text(
                text = "Tirar foto"
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = "",
            onValueChange = {
                // Vamos colocar o remember aqui depois
            },
            label = {
                Text(
                    text = "Título do treino"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                // Vamos implementar depois
            }
        ) {
            Text(
                text = "Registrar treino"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GymRatsPreview() {

    CpGymTheme {
        GymRatsScreen()
    }
}