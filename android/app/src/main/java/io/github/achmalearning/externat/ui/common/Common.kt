package io.github.achmalearning.externat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Highlight
import io.github.achmalearning.externat.data.Progress

@Composable
fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            painterResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border),
            contentDescription = stringResource(if (isFavorite) R.string.favorite_remove else R.string.favorite_add),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Marges du Scaffold sauf le bas (gauche/droite comptent en paysage avec la barre système sur le côté) ;
 * le bas est passé en contentPadding des listes pour défiler sous la barre de navigation.
 */
@Composable
fun PaddingValues.exceptBottom(): PaddingValues {
    val dir = LocalLayoutDirection.current
    return PaddingValues(start = calculateStartPadding(dir), top = calculateTopPadding(), end = calculateEndPadding(dir))
}

/** « 12 / 20 » + barre de progression. */
@Composable
fun ProgressRow(progress: Progress, modifier: Modifier = Modifier) {
    val description = pluralStringResource(R.plurals.progress_description, progress.done, progress.done, progress.total)
    Column(modifier.semantics(mergeDescendants = true) { contentDescription = description }) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.progress_label), style = MaterialTheme.typography.labelMedium)
            Text(progress.label, style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { progress.fraction },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

/** Texte avec les correspondances de recherche en gras et colorées. */
@Composable
fun highlighted(text: String, ranges: List<Highlight>): AnnotatedString {
    val style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    return buildAnnotatedString {
        append(text)
        ranges.forEach { addStyle(style, it.start.coerceIn(0, text.length), it.end.coerceIn(0, text.length)) }
    }
}
