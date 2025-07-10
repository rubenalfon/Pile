package com.ganadoro.pile.ui.compostables

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.R
import com.ganadoro.pile.util.renderFirstPdfPage
import java.io.File

@Composable
fun Document(
    documentModel: DocumentModel,
    modifier: Modifier = Modifier,
    onClick: (id: String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick(documentModel.id)
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current

        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(key1 = documentModel) {
            bitmap = renderFirstPdfPage(File(context.filesDir, documentModel.id))
        }

        val imageModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.outline)
            .padding(1.dp)
            .clip(RoundedCornerShape(8.dp))


        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.test_image), // TODO: Coger la imagen del documento
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        }

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = documentModel.title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}