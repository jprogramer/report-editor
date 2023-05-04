// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        val viewModel = remember { MainViewModel() }
        Window(onCloseRequest = ::exitApplication) {
            MaterialTheme {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.workDir.value,
                        onValueChange = {
                            viewModel.updateWorkDir(it)
                        },
                        label = { Text("Work Directory") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row {
                        OutlinedTextField(
                            value = viewModel.htmlFileName.value,
                            onValueChange = {
                                viewModel.updateHtmlFileName(it)
                            },
                            label = { Text("HTML File") },
                        )

                        Button(onClick = {
                            viewModel.openWorkDirChooser(window)
                        }) {
                            Text("Browse")
                        }

                        Button(onClick = {
                            viewModel.refresh()
                        }) {
                            Text("Refresh")
                        }
                    }
                    Text(text = viewModel.status.value, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}