package com.edoreczenia.feature.inbox.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edoreczenia.feature.inbox.domain.model.InboxMessage

@Composable
fun InboxMessageItem(
    message: InboxMessage,
    onToggleStar: (String) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadIndicatorColor = Color(0xFFfd8b00)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(message.id) }
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left unread bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(80.dp)
                .background(
                    if (!message.isRead) MaterialTheme.colorScheme.secondaryContainer
                    else Color.Transparent
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Envelope icon
        Icon(
            imageVector = if (!message.isRead) Icons.Filled.Mail else Icons.Outlined.Drafts,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (!message.isRead) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Unread dot indicator
        if (!message.isRead) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = unreadIndicatorColor)
            }
        } else {
            Spacer(modifier = Modifier.size(8.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Message content
        Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Case number badge
                Text(
                    text = message.caseNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                // Date
                Text(
                    text = message.displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!message.isRead) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Sender
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (!message.isRead) FontWeight.Bold else FontWeight.Normal,
                color = if (!message.isRead) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Subject
            Text(
                text = message.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!message.isRead) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Preview
            Text(
                text = message.preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Star icon
        IconButton(onClick = { onToggleStar(message.id) }) {
            Icon(
                imageVector = if (message.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (message.isStarred) MaterialTheme.colorScheme.secondaryContainer
                       else MaterialTheme.colorScheme.outline
            )
        }
    }
}

