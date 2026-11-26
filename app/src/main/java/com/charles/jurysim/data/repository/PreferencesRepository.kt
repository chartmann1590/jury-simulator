package com.charles.jurysim.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        val MODEL_READY_KEY = booleanPreferencesKey("model_ready")
        val TTS_ENABLED_KEY = booleanPreferencesKey("tts_enabled")
        val JUROR_NAME_KEY = stringPreferencesKey("juror_name")
        val JUROR_AGE_KEY = stringPreferencesKey("juror_age")
        val JUROR_OCCUPATION_KEY = stringPreferencesKey("juror_occupation")
        val JUROR_EDUCATION_KEY = stringPreferencesKey("juror_education")
        val JUROR_LEGAL_EXP_KEY = stringPreferencesKey("juror_legal_exp")
        val JUROR_VICTIM_KEY = stringPreferencesKey("juror_victim")
        val JUROR_POLITICAL_KEY = stringPreferencesKey("juror_political")
        val JUROR_INFO_KEY = stringPreferencesKey("juror_info")
        val CONTENT_REPORTS_KEY = stringSetPreferencesKey("content_reports")
    }

    /**
     * True once the on-device LiteRT-LM model has been fully downloaded,
     * verified, and successfully loaded once. Drives the start destination
     * (Onboarding vs Home).
     */
    val modelReady: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MODEL_READY_KEY] ?: false
    }

    val ttsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TTS_ENABLED_KEY] ?: true
    }

    suspend fun setModelReady(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MODEL_READY_KEY] = value
        }
    }

    suspend fun setTtsEnabled(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TTS_ENABLED_KEY] = value
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Locally flags an AI-generated message the user reported via
     * long-press "Report this content". Entries are stored ONLY on the
     * device (timestamp | speaker | excerpt, newest 50 kept); the app
     * never transmits them. The report UI additionally offers a
     * pre-filled email so the user can choose to send a copy to the
     * developer.
     */
    suspend fun recordContentReport(speaker: String, content: String) {
        val entry = buildString {
            append(System.currentTimeMillis())
            append('|')
            append(speaker.replace('|', ' ').take(48))
            append('|')
            append(content.replace('\n', ' ').take(280))
        }
        context.dataStore.edit { prefs ->
            val existing = prefs[CONTENT_REPORTS_KEY] ?: emptySet()
            prefs[CONTENT_REPORTS_KEY] = (existing + entry).toList().takeLast(50).toSet()
        }
    }

    /** All locally stored content reports, oldest first. */
    val contentReports: Flow<List<String>> = context.dataStore.data.map { prefs ->
        (prefs[CONTENT_REPORTS_KEY] ?: emptySet()).sorted()
    }

    suspend fun saveJurorProfile(
        name: String,
        age: Int,
        occupation: String,
        education: String,
        hasLegalExperience: Boolean,
        hasBeenVictim: Boolean,
        politicalLeaning: String,
        additionalInfo: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[JUROR_NAME_KEY] = name
            preferences[JUROR_AGE_KEY] = age.toString()
            preferences[JUROR_OCCUPATION_KEY] = occupation
            preferences[JUROR_EDUCATION_KEY] = education
            preferences[JUROR_LEGAL_EXP_KEY] = hasLegalExperience.toString()
            preferences[JUROR_VICTIM_KEY] = hasBeenVictim.toString()
            preferences[JUROR_POLITICAL_KEY] = politicalLeaning
            preferences[JUROR_INFO_KEY] = additionalInfo
        }
    }

    suspend fun getJurorProfile(): com.charles.jurysim.data.model.JurorProfile {
        val prefs = context.dataStore.data.first()
        return com.charles.jurysim.data.model.JurorProfile(
            name = prefs[JUROR_NAME_KEY] ?: "",
            age = prefs[JUROR_AGE_KEY]?.toIntOrNull() ?: 30,
            occupation = prefs[JUROR_OCCUPATION_KEY] ?: "",
            education = prefs[JUROR_EDUCATION_KEY] ?: "",
            hasLegalExperience = prefs[JUROR_LEGAL_EXP_KEY]?.toBoolean() ?: false,
            hasBeenVictim = prefs[JUROR_VICTIM_KEY]?.toBoolean() ?: false,
            politicalLeaning = prefs[JUROR_POLITICAL_KEY] ?: "Moderate",
            additionalInfo = prefs[JUROR_INFO_KEY] ?: ""
        )
    }
}
