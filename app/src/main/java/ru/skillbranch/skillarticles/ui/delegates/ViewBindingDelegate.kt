package ru.skillbranch.skillarticles.ui.delegates

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBindingDelegate<T: ViewBinding>(
    private val activity: AppCompatActivity,
    private val initializer: (LayoutInflater) -> T
): ReadOnlyProperty<AppCompatActivity, T>, LifecycleObserver {

    private var _value: T? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        if (_value == null) {
            _value = initializer(activity.layoutInflater)
        }

        activity.setContentView(_value!!.root)
        activity.lifecycle.removeObserver(this)
    }

    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        if (_value == null) {
            _value = initializer(activity.layoutInflater)
        }
        return _value!!
    }
}

inline fun <reified T: ViewBinding> AppCompatActivity.viewBinding(noinline initializer: (LayoutInflater) -> T) =
    ViewBindingDelegate(this, initializer)

class ViewBindingFragmentDelegate<T: ViewBinding>(
    private val fragment: Fragment,
    private val initializer: (View) -> T
): ReadOnlyProperty<Fragment, T>, LifecycleObserver {

    private var _value: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) {
            it.lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        if (_value == null) {
            _value = initializer(fragment.requireView())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        _value = null
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = _value
        if (binding != null) return binding
        val lifecycle = thisRef.viewLifecycleOwner.lifecycle

        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get binding when Fragment views are destroyed")
        }

        return initializer(thisRef.requireView()).also { this._value = it }
    }
}

inline fun <reified T: ViewBinding> Fragment.viewBinding(noinline initializer: (View) -> T) =
    ViewBindingFragmentDelegate(this, initializer)

