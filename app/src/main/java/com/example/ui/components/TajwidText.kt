package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.data.quran.Verse

@Composable
fun TajwidArabicText(
    verse: Verse,
    fontSizeSp: Float,
    isTajwidEnabled: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val annotated = if (!isTajwidEnabled || verse.tajwidSpans.isEmpty()) {
        AnnotatedString(verse.arabicText)
    } else {
        buildAnnotatedString {
            append(verse.arabicText)
            for (span in verse.tajwidSpans) {
                if (span.startIndex in 0..verse.arabicText.length &&
                    span.endIndex in 0..verse.arabicText.length &&
                    span.startIndex < span.endIndex
                ) {
                    addStyle(
                        style = SpanStyle(
                            color = span.rule.color,
                            fontWeight = FontWeight.Bold
                        ),
                        start = span.startIndex,
                        end = span.endIndex
                    )
                }
            }
        }
    }

    Text(
        text = annotated,
        color = textColor,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.85f).sp,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}
