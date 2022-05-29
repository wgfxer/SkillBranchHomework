package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefDelegate<T>(private val defaultValue: T, private val customKey: String? = null) {
    operator fun provideDelegate(
        thisRef: PrefManager,
        prop: KProperty<*>
    ): ReadWriteProperty<PrefManager, T> {

        val key = createKey(customKey ?: prop.name, defaultValue)

        return object : ReadWriteProperty<PrefManager, T> {
            private var _storedValue: T? = null
            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T) {
                _storedValue = value
                thisRef.scope.launch {
                    thisRef.dataStore.edit { prefs ->
                        prefs[key] = value
                    }
                }
            }

            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T {
                if (_storedValue == null) {
                    val flowValue = thisRef.dataStore.data.map { prefs -> prefs[key] ?: defaultValue }
                    _storedValue = runBlocking(Dispatchers.IO) { flowValue.first() }
                }
                return _storedValue!!
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createKey(name: String, value: T): Preferences.Key<T> {
        return when(value) {
            is Int -> intPreferencesKey(name)
            is Long -> longPreferencesKey(name)
            is Double -> doublePreferencesKey(name)
            is Float -> floatPreferencesKey(name)
            is String -> stringPreferencesKey(name)
            is Boolean -> booleanPreferencesKey(name)
            else -> error("This type cannot be stored in preferences")
        }.run { this as Preferences.Key<T> }
    }
}