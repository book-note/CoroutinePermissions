package com.github.lilei.coroutinepermissions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.lilei.coroutinepermissions.callbacks.FailCallback
import com.github.lilei.coroutinepermissions.callbacks.RequestResultListener
import com.github.lilei.coroutinepermissions.callbacks.SuccessCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class InlinePermissionResult {
    private val TAG = "ACTIVITY_RESULT_FRAGMENT_WEEEEE"
    private var activityReference: Reference<FragmentActivity>
    private val successCallbacks = ArrayList<SuccessCallback>()
    private val failCallbacks = ArrayList<FailCallback>()
    private val responseListeners = ArrayList<RequestResultListener>()
    private var listener = object : RequestPermissionFragment.RequestPermissionsListener {
        override fun onRequestPermissions(hasPermissions: Boolean, permissions: Array<out String>) {
            if (hasPermissions) {
                for (callback in successCallbacks) {
                    callback.onSuccess()
                }
                for (listener in responseListeners) {
                    listener.onSuccess()
                }
            } else {
                for (callback in failCallbacks) {
                    callback.onFailed()
                }
                for (listener in responseListeners) {
                    listener.onFailed()
                }
            }

        }
    }

    constructor(activity: FragmentActivity) {
        activityReference = WeakReference(activity)
    }

    constructor(fragment: Fragment) {
        activityReference = WeakReference(fragment.requireActivity())
    }

    fun onSuccess(callback: SuccessCallback): InlinePermissionResult {
        successCallbacks.add(callback)
        return this
    }

    fun onFail(callback: FailCallback): InlinePermissionResult {
        failCallbacks.add(callback)
        return this
    }

    fun requestPermissions(vararg permissions: String) {
        val activity = activityReference.get()
        if (activity == null || activity.isFinishing) return

        val oldFragment =
            activity.supportFragmentManager.findFragmentByTag(TAG) as RequestPermissionFragment?
        if (oldFragment != null) {
            oldFragment.setListener(listener)
            oldFragment.request()
        } else {
            val newFragment = RequestPermissionFragment.newInstance(
                permissions = *permissions
            )
            newFragment.setListener(listener)
            CoroutineScope(Dispatchers.Main).launch {
                activity.supportFragmentManager
                    .beginTransaction()
                    .add(newFragment, TAG)
                    .commitNowAllowingStateLoss()
            }
        }
    }
}