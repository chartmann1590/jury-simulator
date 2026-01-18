package com.jurysim.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        val SERVER_URL_KEY = stringPreferencesKey("server_url")
        val SELECTED_MODEL_KEY = stringPreferencesKey("selected_model")
        val JUROR_NAME_KEY = stringPreferencesKey("juror_name")
        val JUROR_AGE_KEY = stringPreferencesKey("juror_age")
        val JUROR_OCCUPATION_KEY = stringPreferencesKey("juror_occupation")
        val JUROR_EDUCATION_KEY = stringPreferencesKey("juror_education")
        val JUROR_LEGAL_EXP_KEY = stringPreferencesKey("juror_legal_exp")
        val JUROR_VICTIM_KEY = stringPreferencesKey("juror_victim")
        val JUROR_POLITICAL_KEY = stringPreferencesKey("juror_political")
        val JUROR_INFO_KEY = stringPreferencesKey("juror_info")
    }

    val serverUrl: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SERVER_URL_KEY]
    }

    val selectedModel: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL_KEY]
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL_KEY] = model
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
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

    suspend fun getJurorProfile(): com.jurysim.data.model.JurorProfile {
        val prefs = context.dataStore.data.first()
        return com.jurysim.data.model.JurorProfile(
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
