package com.charles.jurysim.ui.screens.simulation

import com.charles.jurysim.data.model.VoteChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerdictResolverTest {

    @Test
    fun `unanimous guilty returns GUILTY`() {
        val votes = (0..11).associateWith { VoteChoice.GUILTY }

        val decision = VerdictResolver.resolve(votes, currentRound = 1, maxRounds = 5)

        assertTrue(decision is VerdictDecision.Unanimous)
        assertEquals("GUILTY", (decision as VerdictDecision.Unanimous).verdict)
    }

    @Test
    fun `unanimous not guilty returns NOT GUILTY`() {
        val votes = (0..11).associateWith { VoteChoice.NOT_GUILTY }

        val decision = VerdictResolver.resolve(votes, currentRound = 1, maxRounds = 5)

        assertTrue(decision is VerdictDecision.Unanimous)
        assertEquals("NOT GUILTY", (decision as VerdictDecision.Unanimous).verdict)
    }

    @Test
    fun `split vote before final round is Hung`() {
        val votes = mapOf(
            0 to VoteChoice.GUILTY,
            1 to VoteChoice.GUILTY,
            2 to VoteChoice.NOT_GUILTY,
            3 to VoteChoice.NOT_GUILTY
        )

        val decision = VerdictResolver.resolve(votes, currentRound = 2, maxRounds = 5)

        assertTrue(decision is VerdictDecision.Hung)
        val hung = decision as VerdictDecision.Hung
        assertEquals(2, hung.guilty)
        assertEquals(2, hung.notGuilty)
    }

    @Test
    fun `split vote at max round is Mistrial`() {
        val votes = mapOf(
            0 to VoteChoice.GUILTY,
            1 to VoteChoice.NOT_GUILTY
        )

        val decision = VerdictResolver.resolve(votes, currentRound = 5, maxRounds = 5)

        assertTrue(decision is VerdictDecision.Mistrial)
    }

    @Test
    fun `single-vote majority never produces a verdict when minority disagrees`() {
        // 11 guilty, 1 not-guilty — not unanimous, before maxRounds → Hung.
        val votes = mutableMapOf<Int, VoteChoice>()
        for (i in 0..10) votes[i] = VoteChoice.GUILTY
        votes[11] = VoteChoice.NOT_GUILTY

        val decision = VerdictResolver.resolve(votes, currentRound = 3, maxRounds = 5)

        assertTrue(decision is VerdictDecision.Hung)
    }

    @Test
    fun `empty vote map is Hung not a unanimous GUILTY`() {
        // Guards against the latent edge case where 0==0 would report GUILTY.
        val decision = VerdictResolver.resolve(emptyMap(), currentRound = 1, maxRounds = 5)

        assertTrue(decision is VerdictDecision.Hung)
    }
}
