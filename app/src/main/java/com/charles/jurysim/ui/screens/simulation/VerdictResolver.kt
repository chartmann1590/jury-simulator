package com.charles.jurysim.ui.screens.simulation

import com.charles.jurysim.data.model.VoteChoice

internal sealed class VerdictDecision {
    data class Unanimous(val verdict: String) : VerdictDecision()
    data object Mistrial : VerdictDecision()
    data class Hung(val guilty: Int, val notGuilty: Int) : VerdictDecision()
}

internal object VerdictResolver {
    fun resolve(
        votes: Map<Int, VoteChoice>,
        currentRound: Int,
        maxRounds: Int
    ): VerdictDecision {
        val guilty = votes.values.count { it == VoteChoice.GUILTY }
        val notGuilty = votes.values.count { it == VoteChoice.NOT_GUILTY }
        val total = guilty + notGuilty

        if (total > 0 && guilty == total) return VerdictDecision.Unanimous("GUILTY")
        if (total > 0 && notGuilty == total) return VerdictDecision.Unanimous("NOT GUILTY")
        if (currentRound >= maxRounds) return VerdictDecision.Mistrial
        return VerdictDecision.Hung(guilty, notGuilty)
    }
}
