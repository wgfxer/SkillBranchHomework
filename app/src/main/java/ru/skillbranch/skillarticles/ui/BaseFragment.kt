package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import ru.skillbranch.skillarticles.viewmodels.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.VMState

/**
 * @author Valeriy Minnulin
 */
abstract class BaseFragment<S, T : BaseViewModel<S>, B: ViewBinding>(@LayoutRes layout: Int): Fragment(layout), LifecycleObserver where S : VMState {
    protected val root
        get() = requireActivity() as RootActivity

    abstract val viewModel: T
    abstract val viewBinding: B

    abstract fun renderUi(data: S)
    abstract fun setupViews()

    open fun setupActivityViews() {

    }

    open fun observeViewModelData() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        requireActivity().lifecycle.addObserver(this)
        viewModel.observeNotifications(viewLifecycleOwner, root::renderNotification)
        viewModel.observeNavigation(viewLifecycleOwner, root::handleNavigation)
        viewModel.observeState(viewLifecycleOwner, ::renderUi)
        observeViewModelData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().lifecycle.removeObserver(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun activityInflated() {
        root.viewBinding.appbar.setExpanded(true, true)
        root.viewBinding.toolbar.logo = null
        root.viewBinding.toolbar.subtitle = null
        setupActivityViews()
    }
}