package com.edoreczenia.feature.inbox.data.repository

import com.edoreczenia.feature.inbox.domain.model.InboxMessage
import com.edoreczenia.feature.inbox.domain.repository.InboxRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeInboxRepository(
    val shouldSimulateError: Boolean = false
) : InboxRepository {

    private val _messages = MutableStateFlow(MOCK_MESSAGES)

    override fun getMessages(): Flow<List<InboxMessage>> {
        return _messages.asStateFlow()
    }

    override suspend fun refresh(): Result<Unit> {
        delay((500..1000).random().toLong())
        return if (shouldSimulateError) {
            Result.failure(Exception("Błąd połączenia z serwerem"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun toggleStar(id: String): Result<Unit> {
        _messages.update { list ->
            list.map { msg ->
                if (msg.id == id) msg.copy(isStarred = !msg.isStarred) else msg
            }
        }
        return Result.success(Unit)
    }

    companion object {
        val MOCK_MESSAGES = listOf(
            InboxMessage(
                id = "msg-001",
                caseNumber = "KM 123/2025",
                senderName = "Sąd Rejonowy w Warszawie",
                subject = "Zawiadomienie o wszczęciu postępowania",
                preview = "Informujemy o wszczęciu postępowania egzekucyjnego w sprawie KM 123/2025.",
                displayDate = "14 kwi",
                isRead = false,
                isStarred = false
            ),
            InboxMessage(
                id = "msg-002",
                caseNumber = "KM 456/2025",
                senderName = "Komornik Sądowy Jan Kowalski",
                subject = "Wezwanie do zapłaty",
                preview = "Wzywamy do uregulowania zaległości w terminie 7 dni od daty doręczenia.",
                displayDate = "10 kwi",
                isRead = true,
                isStarred = true
            ),
            InboxMessage(
                id = "msg-003",
                caseNumber = "KM 789/2025",
                senderName = "Sąd Okręgowy w Krakowie",
                subject = "Zawiadomienie o terminie licytacji",
                preview = "Zawiadamiamy o terminie licytacji nieruchomości wyznaczonym na dzień 20.05.2025.",
                displayDate = "7 kwi",
                isRead = false,
                isStarred = false
            ),
            InboxMessage(
                id = "msg-004",
                caseNumber = "KM 012/2025",
                senderName = "Urząd Skarbowy Warszawa-Mokotów",
                subject = "Decyzja podatkowa",
                preview = "Przekazujemy decyzję podatkową nr DEC/2025/012 dotyczącą rozliczenia za rok 2024.",
                displayDate = "2 kwi",
                isRead = true,
                isStarred = false
            )
        )
    }
}



