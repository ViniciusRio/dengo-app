package com.viniciusrio.dengo.ui.history

import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val now = Instant.parse("2026-09-30T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialStateIsEmpty() = runTest {
        val viewModel = HistoryViewModel(FakeCoupleRepository(clock), clock)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.days.isEmpty())
    }

    @Test
    fun requestEventsResolveTheirOwnRequestByIdWithoutUsingCurrentStatus() {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createRequest(CareRequestType.DENGO)
        val second = repository.createRequest(CareRequestType.MEDICINE)
        repository.acceptRequest(first.id)
        repository.declineRequest(second.id)

        val items = repository.state.value.toHistoryUiState(clock.zone).days.single().items

        assertEquals(
            listOf(
                HistoryEvent.RequestDeclined::class,
                HistoryEvent.RequestAccepted::class,
                HistoryEvent.RequestCreated::class,
                HistoryEvent.RequestCreated::class,
            ),
            items.map { it.event::class },
        )
        assertEquals(second.id, (items[0].event as HistoryEvent.RequestDeclined).requestId)
        assertEquals(CareRequestType.MEDICINE, items[0].requestType)
        assertEquals(first.id, (items[1].event as HistoryEvent.RequestAccepted).requestId)
        assertEquals(CareRequestType.DENGO, items[1].requestType)
        assertEquals(CareRequestType.DENGO, items[3].requestType)
        assertEquals(listOf(3, 2, 1, 0), items.map { it.originalIndex })
    }

    @Test
    fun allRequestTypesAndOtherOriginalTextReachTheTimeline() {
        val repository = FakeCoupleRepository(clock)
        CareRequestType.entries.filterNot { it == CareRequestType.OTHER }.forEach { repository.createRequest(it) }
        val other = repository.createRequest(CareRequestType.OTHER, "Pode trazer água?")
        repository.acceptRequest(other.id)

        val items = repository.state.value.toHistoryUiState(clock.zone).days.single().items

        assertEquals(CareRequestType.entries.toSet(), items.mapNotNull { it.requestType }.toSet())
        assertEquals("Pode trazer água?", items[0].requestMessage)
        assertEquals("Pode trazer água?", items[1].requestMessage)
        assertTrue(items.drop(2).all { it.requestMessage == null })
    }

    @Test
    fun missingRequestKeepsCreatedAcceptedAndDeclinedEventsWithGenericContext() {
        val state = CoupleState(
            partners = emptyList(),
            history = listOf(
                HistoryEvent.RequestCreated(42, PartnerId.LIDIANNE, now),
                HistoryEvent.RequestAccepted(42, PartnerId.VINICIUS, now),
                HistoryEvent.RequestDeclined(43, PartnerId.VINICIUS, now),
            ),
        )

        val items = state.toHistoryUiState(clock.zone).days.single().items

        assertEquals(3, items.size)
        assertEquals(listOf(2, 1, 0), items.map { it.originalIndex })
        items.forEach {
            assertNull(it.requestType)
            assertNull(it.requestMessage)
        }
    }

    @Test
    fun moodChangesAndBothPersonalSpaceEventsStayIndependent() {
        val repository = FakeCoupleRepository(clock)
        repository.setMood(MoodOption.TIRED)
        repository.setMood(MoodOption.HAPPY)
        repository.activatePersonalSpace()
        repository.endPersonalSpace()

        val events = repository.state.value.toHistoryUiState(clock.zone).days.single().items.map { it.event }

        assertEquals(4, events.size)
        assertTrue(events[0] is HistoryEvent.PersonalSpaceEnded)
        assertTrue(events[1] is HistoryEvent.PersonalSpaceActivated)
        assertEquals(MoodOption.HAPPY, (events[2] as HistoryEvent.MoodChanged).option)
        assertEquals(MoodOption.TIRED, (events[3] as HistoryEvent.MoodChanged).option)
    }

    @Test
    fun sortsByTimestampThenByLaterInsertionIndex() {
        val earlier = now.minusSeconds(60)
        val later = now.plusSeconds(60)
        val state = CoupleState(
            partners = emptyList(),
            history = listOf(
                HistoryEvent.MoodChanged(MoodOption.TIRED, PartnerId.LIDIANNE, now),
                HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, earlier),
                HistoryEvent.MoodChanged(MoodOption.HAPPY, PartnerId.LIDIANNE, now),
                HistoryEvent.PersonalSpaceEnded(PartnerId.LIDIANNE, later),
            ),
        )

        assertEquals(
            listOf(3, 2, 0, 1),
            state.toHistoryUiState(clock.zone).days.single().items.map { it.originalIndex },
        )
    }

    @Test
    fun groupsByLocalDayAndKeepsLocalTime() {
        val zone = ZoneId.of("America/Fortaleza")
        val state = CoupleState(
            partners = emptyList(),
            history = listOf(
                HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, Instant.parse("2026-09-30T02:30:00Z")),
                HistoryEvent.PersonalSpaceEnded(PartnerId.LIDIANNE, Instant.parse("2026-09-30T03:15:00Z")),
                HistoryEvent.MoodChanged(MoodOption.OKAY, PartnerId.LIDIANNE, Instant.parse("2026-09-28T13:00:00Z")),
            ),
        )

        val days = state.toHistoryUiState(zone).days

        assertEquals(listOf(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 29), LocalDate.of(2026, 9, 28)), days.map { it.date })
        assertEquals(listOf(LocalTime.of(0, 15), LocalTime.of(23, 30), LocalTime.of(10, 0)), days.flatMap { it.items }.map { it.time })
    }

    @Test
    fun classifiesTodayYesterdayAndOlderDateFromConsultationDay() {
        val today = LocalDate.of(2026, 9, 30)

        assertEquals(HistoryDayLabel.TODAY, historyDayLabel(today, today))
        assertEquals(HistoryDayLabel.YESTERDAY, historyDayLabel(today.minusDays(1), today))
        assertEquals(HistoryDayLabel.DATE, historyDayLabel(today.minusDays(2), today))
        assertEquals(HistoryDayLabel.DATE, historyDayLabel(today.plusDays(1), today))
        assertEquals("28/09/2026", formatHistoryDate(today.minusDays(2)))
        assertEquals("09:05", formatHistoryTime(LocalTime.of(9, 5)))
    }

    @Test
    fun bothHomePerspectivesFeedTheSameHistoryState() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)
        val history = HistoryViewModel(repository, clock)

        lidianne.createQuickRequest(CareRequestType.DENGO)
        val requestId = repository.state.value.requests.single().id
        vinicius.acceptRequest(requestId)
        lidianne.setMood(MoodOption.LOVING)
        advanceUntilIdle()

        assertEquals(repository.state.value.history.size, history.state.value.days.single().items.size)
        assertEquals(
            listOf(HistoryEvent.MoodChanged::class, HistoryEvent.RequestAccepted::class, HistoryEvent.RequestCreated::class),
            history.state.value.days.single().items.map { it.event::class },
        )
    }
}
