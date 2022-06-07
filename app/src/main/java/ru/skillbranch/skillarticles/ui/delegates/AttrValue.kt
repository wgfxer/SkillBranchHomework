package ru.skillbranch.skillarticles.ui.delegates

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import ru.skillbranch.skillarticles.extensions.attrValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class AttrValue(@AttrRes private val res: Int): ReadOnlyProperty<Context, Int> {
    private var _value: Int? = null

    override fun getValue(thisRef: Context, property: KProperty<*>): Int {
        if (_value == null) {
            _value = thisRef.attrValue(res)
        }
        return _value!!
    }
}