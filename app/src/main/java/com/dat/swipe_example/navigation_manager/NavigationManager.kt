package com.dat.swipe_example.navigation_manager

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.dat.swipe_example.R

class NavigationManager() :
    FragmentManager.OnBackStackChangedListener {
    protected lateinit var mActivity: Activity
    protected lateinit var mFragmentManager: FragmentManager
    private var mContentId: Int? = null
    var navigateAble = true
    val handlerNavigate = Handler(Looper.getMainLooper())

    companion object {
        fun getInstance(): NavigationManager = NavigationManagerHolder.navigationManagerHolder
    }

    private object NavigationManagerHolder {
        @SuppressLint("StaticFieldLeak")
        val navigationManagerHolder = NavigationManager()
    }

    fun init(activity: Activity, fragmentManager: FragmentManager, @IdRes contentId: Int) {
        mActivity = activity
        mFragmentManager = fragmentManager
        mContentId = contentId
        mFragmentManager.addOnBackStackChangedListener(this)
    }


    /**
     * flag mark the Navigation is created on Fragment class
     */
    fun isBackStackEmpty() = mFragmentManager.backStackEntryCount == 0
    fun isRoot() = mFragmentManager.backStackEntryCount <= 1

    fun popBackStack() {
        mActivity.onBackPressed()
    }


    fun openFragment(
        fragment: Fragment,
        @AnimRes enter: Int,
        @AnimRes exit: Int,
        @AnimRes popEnter: Int,
        @AnimRes popExit: Int
    ) {
        try {
            if (!navigateAble)
                return
            mFragmentManager.commit {
                if (enter != 0 || exit != 0 || popEnter != 0 || popExit != 0) {
                    setCustomAnimations(enter, exit, popEnter, popExit)
                }
                mContentId?.let {
                    add(it, fragment, fragment::class.simpleName)
                }
                addToBackStack((2147483646.0 * Math.random()).toInt().toString())
                navigateAble = false
                handlerNavigate.postDelayed(
                    { navigateAble = true }, 100
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openFragment(
        fragment: Fragment,
    ) {
        openFragment(
            fragment,
            R.anim.slide_in_left,
            R.anim.opacity_1_to_0,
            0,
            R.anim.slide_out_right
        )
    }

    override fun onBackStackChanged() {

    }
}