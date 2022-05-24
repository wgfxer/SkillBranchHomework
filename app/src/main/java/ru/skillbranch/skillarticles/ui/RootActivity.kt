package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProviders
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.ui.custom.CheckableImageView
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class RootActivity : AppCompatActivity() {

    private lateinit var viewModel: ArticleViewModel

    private lateinit var submenu: ArticleSubmenu
    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnLike: CheckableImageView
    private lateinit var btnBookmark: CheckableImageView
    private lateinit var btnShare: ImageView
    private lateinit var btnSettings: CheckableImageView
    private lateinit var switchMode: SwitchMaterial
    private lateinit var tvTextContent: TextView
    private lateinit var btnTextUp: CheckableImageView
    private lateinit var btnTextDown: CheckableImageView
    private lateinit var bottombar: Bottombar
    private lateinit var coordinatorContainer: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        findViews()
        setupToolbar()
        setupBottombar()
        setupSubmenu()
        initViewModel()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.observeState(this) {
            renderUi(it)
        }

        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    private fun initViewModel() {
        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
    }

    private fun findViews() {
        btnLike = findViewById(R.id.btn_like)
        btnBookmark = findViewById(R.id.btn_bookmark)
        btnShare = findViewById(R.id.btn_share)
        btnSettings = findViewById(R.id.btn_settings)
        toolbar = findViewById(R.id.toolbar)
        submenu = findViewById(R.id.submenu)
        bottombar = findViewById(R.id.bottombar)
        coordinatorContainer = findViewById(R.id.coordinator_container)
        tvTextContent = findViewById(R.id.tv_text_content)
        switchMode = findViewById(R.id.switch_mode)
        btnTextUp = findViewById(R.id.btn_text_up)
        btnTextDown = findViewById(R.id.btn_text_down)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        //check toolbar imports
        (logo?.layoutParams as? Toolbar.LayoutParams)?.let {
            it.width = dpToIntPx(40)
            it.height = dpToIntPx(40)
            it.marginEnd = dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.root_menu, menu)
        menu?.findItem(R.id.action_search)?.apply {
            val searchView = this.actionView as SearchView
            if (viewModel.currentState.isSearch) {
                if (!isActionViewExpanded) expandActionView()
                searchView.setQuery(viewModel.currentState.searchQuery, false)
            }
            searchView.setOnQueryTextListener(object : OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.handleSearch(newText)
                    return true
                }

            })
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    viewModel.handleSearchMode(true)
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    viewModel.handleSearchMode(false)
                    return true
                }
            })

        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun renderUi(data: ArticleState) {
        // bind submenu state
        btnSettings.isChecked = data.isShowMenu
        if(data.isShowMenu) submenu.open() else submenu.close()

        // bind article person data
        btnLike.isChecked = data.isLike
        btnBookmark.isChecked = data.isBookmark

        // bind submenu views
        switchMode.isChecked = data.isDarkMode
        delegate.localNightMode = if(data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if(data.isBigText) {
            tvTextContent.textSize = 18f
            btnTextUp.isChecked = true
            btnTextDown.isChecked = false
        } else {
            tvTextContent.textSize = 14f
            btnTextUp.isChecked = false
            btnTextDown.isChecked = true
        }

        // bind content
        tvTextContent.text = if (data.isLoadingContent) "loading" else data.content.first() as String

        // bind toolbar
        toolbar.title = data.title ?: "Skill Articles"
        toolbar.subtitle = data.category ?: "loading..."
        if (data.categoryIcon != null) toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)

        when(notify) {
            is Notify.TextMessage -> { /* nothing */}
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }

    private fun setupBottombar() {
        btnLike.setOnClickListener { viewModel.handleLike() }
        btnBookmark.setOnClickListener { viewModel.handleBookmark() }
        btnShare.setOnClickListener { viewModel.handleShare() }
        btnSettings.setOnClickListener { viewModel.handleToggleMenu() }
    }

    private fun setupSubmenu() {
        btnTextUp.setOnClickListener { viewModel.handleUpText() }
        btnTextDown.setOnClickListener{ viewModel.handleDownText() }
        switchMode.setOnClickListener { viewModel.handleNightMode() }
    }

}