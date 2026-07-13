package com.example.insightself.util

import com.example.insightself.data.model.AssessmentResultDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssessmentSnapshotUtilsTest {

    @Test
    fun latestAssessmentSnapshots_returnsLatestPerType() {
        val results = listOf(
            AssessmentResultDto(id = 1, type = "BFI10", resultLabel = "Openness", createdAt = "2026-01-01T10:00:00"),
            AssessmentResultDto(id = 2, type = "BFI10", resultLabel = "Extraversion", createdAt = "2026-02-01T10:00:00"),
            AssessmentResultDto(id = 3, type = "MBTI", resultLabel = "INTJ", createdAt = "2026-01-15T10:00:00")
        )

        val snapshots = latestAssessmentSnapshots(results)
        assertEquals(4, snapshots.size)

        val bfi = snapshots.first { it.config.type == "BFI10" }
        assertEquals(2L, bfi.latestResult?.id)
        assertEquals("Extraversion", bfi.latestResult?.resultLabel)

        val mbti = snapshots.first { it.config.type == "MBTI" }
        assertEquals(3L, mbti.latestResult?.id)

        val career = snapshots.first { it.config.type == "CAREER" }
        assertNull(career.latestResult)
    }
}
