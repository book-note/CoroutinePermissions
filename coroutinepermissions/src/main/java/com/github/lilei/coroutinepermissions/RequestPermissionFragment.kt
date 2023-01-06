package com.github.lilei.coroutinepermissions

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment

class RequestPermissionFragment : Fragment() {
    private lateinit var permissions: Array<String>
    private var listener: RequestPermissionsListener? = null

    init {
        retainInstance = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            permissions = it.getStringArray(ARG_PERMISSIONS) as Array<String>
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        request()
    }

    fun setListener(listener: RequestPermissionsListener): RequestPermissionFragment {
        this.listener = listener
        return this
    }

    fun request() {
        if (permissions.isNotEmpty()) {
            initPermission()
        } else {
            removeFragment()
        }
    }

    private fun removeFragment() {
        fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
    }

    interface RequestPermissionsListener {
        fun onRequestPermissions(hasPermissions: Boolean, permissions: Array<out String>)
    }

    private fun initPermission() {
        if (hasPermissions(*permissions)) {
            listener?.onRequestPermissions(true, permissions)
            removeFragment()
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun hasPermissions(vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (context == null) {
            throw IllegalArgumentException("Can't check permissions for null context")
        }
        return permissions.toMutableList().all {
            context!!.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        listener?.onRequestPermissions(true, permissions)
        removeFragment()
    }

    private fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        listener?.onRequestPermissions(false, permissions)
        removeFragment()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    onPermissionsGranted(requestCode, permissions.toList())
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    onPermissionsDenied(requestCode, permissions.toList())
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }
            else -> {
                // ignore
            }
        }
    }


    companion object {
        private const val ARG_PERMISSIONS = "arg_permission"
        private const val PERMISSION_REQUEST_CODE = 115
        fun newInstance(
            vararg permissions: String
        ): RequestPermissionFragment {
            val bundle = Bundle().apply {
                putStringArray(ARG_PERMISSIONS, permissions)
            }
            return RequestPermissionFragment().apply {
                arguments = bundle
            }
        }
    }
}