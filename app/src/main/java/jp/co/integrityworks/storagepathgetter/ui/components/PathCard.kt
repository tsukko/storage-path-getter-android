package jp.co.integrityworks.storagepathgetter.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.co.integrityworks.storagepathgetter.R

@Composable
fun PathCard(title: String, path: String, onCopy: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { onCopy(path) }) {
                    Text("コピー")
                }
            }
            OutlinedTextField(
                value = path.ifEmpty { stringResource(id = R.string.text_none) },
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
        }
    }
}
