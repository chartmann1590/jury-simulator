package com.charles.jurysim.ui.screens.simulation

import com.charles.jurysim.data.model.AIJuror
import com.charles.jurysim.data.model.JurorLeaning

internal object JurorParsing {
    const val EXPECTED_JUROR_COUNT = 11

    private val jurorPattern = Regex(
        """JUROR_(\d+):\s*NAME:\s*(.+?)\s*AGE:\s*(\d+)\s*OCCUPATION:\s*(.+?)\s*PERSONALITY:\s*(.+?)\s*HIDDEN_BIAS:\s*(.+?)\s*LEANING:\s*(\w+)""",
        RegexOption.DOT_MATCHES_ALL
    )

    fun parseJurors(response: String): List<AIJuror> {
        val jurors = mutableListOf<AIJuror>()

        jurorPattern.findAll(response).forEach { match ->
            val id = match.groupValues[1].toIntOrNull() ?: (jurors.size + 1)
            val name = match.groupValues[2].trim()
            val age = match.groupValues[3].toIntOrNull() ?: 30
            val occupation = match.groupValues[4].trim()
            val personality = match.groupValues[5].trim()
            val hiddenBias = match.groupValues[6].trim()
            val leaning = parseLeaning(match.groupValues[7])
            val avatarId = (id % 10) + 1

            jurors.add(AIJuror(id, name, occupation, personality, leaning, age, hiddenBias, avatarId))
        }

        while (jurors.size < EXPECTED_JUROR_COUNT) {
            jurors.addAll(defaultJurors().take(EXPECTED_JUROR_COUNT - jurors.size))
        }

        return jurors.take(EXPECTED_JUROR_COUNT)
    }

    private fun parseLeaning(raw: String): JurorLeaning {
        val s = raw.trim().uppercase()
        return when {
            s.contains("NOT_GUILTY") || s.contains("NOT GUILTY") -> JurorLeaning.LEANING_NOT_GUILTY
            s.contains("GUILTY") -> JurorLeaning.LEANING_GUILTY
            else -> JurorLeaning.UNDECIDED
        }
    }

    fun defaultJurors(): List<AIJuror> = listOf(
        AIJuror(1, "Sarah Mitchell", "Elementary School Teacher", "Empathetic and thoughtful", JurorLeaning.UNDECIDED, 34, "Brother was incarcerated for a minor offense", 1),
        AIJuror(2, "Marcus Johnson", "Accountant", "Analytical and detail-oriented", JurorLeaning.LEANING_GUILTY, 45, "Trusts authority figures implicitly", 2),
        AIJuror(3, "Linda Chen", "Nurse", "Compassionate but pragmatic", JurorLeaning.LEANING_NOT_GUILTY, 29, "Has seen false accusations in hospital work", 3),
        AIJuror(4, "Robert Williams", "Retired Police Officer", "Law-and-order minded", JurorLeaning.LEANING_GUILTY, 62, "Believes police rarely make mistakes", 4),
        AIJuror(5, "Maria Garcia", "Social Worker", "Understanding of difficult circumstances", JurorLeaning.LEANING_NOT_GUILTY, 38, "Systemic bias concerns", 5),
        AIJuror(6, "James Thompson", "Construction Foreman", "Practical and straightforward", JurorLeaning.UNDECIDED, 50, "Dislikes lawyers generally", 6),
        AIJuror(7, "Patricia Brown", "Librarian", "Methodical and fair-minded", JurorLeaning.UNDECIDED, 55, "Obsessed with details", 7),
        AIJuror(8, "David Kim", "Software Engineer", "Logical and evidence-focused", JurorLeaning.LEANING_GUILTY, 27, "Believes technology (DNA) over witness testimony", 8),
        AIJuror(9, "Nancy Davis", "Restaurant Owner", "Business-minded and skeptical", JurorLeaning.LEANING_NOT_GUILTY, 48, "Was sued frivolously once", 9),
        AIJuror(10, "Michael Anderson", "College Professor", "Intellectual and questioning", JurorLeaning.UNDECIDED, 60, "Overanalyzes semantic details", 10),
        AIJuror(11, "Jennifer Martinez", "Stay-at-home Parent", "Protective and community-focused", JurorLeaning.LEANING_GUILTY, 33, "Worried about neighborhood safety", 1)
    )
}
