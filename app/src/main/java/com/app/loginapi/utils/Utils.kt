package com.app.loginapi.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.loginapi.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.regex.Pattern

class Utils(val context: Context) {
    var customProgress: CustomProgress? = null


    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    //hide keyboard
    fun hideKeyBoardFromView(context: Context) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        // Find the currently focused view, so we can grab the correct window
        // token from it.
        var view = (context as Activity).currentFocus
        // If no view currently has focus, create a new one, just so we can grab
        // a window token from it
        if (view == null) {
            view = View(context)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    fun statusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color
        }
    }

    fun hideKeyBoardFromView() {
        val activity = context as AppCompatActivity
        val view = activity.currentFocus
        if (view != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showAlert(msg: String?) {
        MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setTitle("Alert")
            .setMessage(msg)
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .show();
    }
    fun isMyServiceRunning(serviceClass: Class<*>, mActivity: Activity): Boolean {
        val manager: ActivityManager =
            mActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }
 fun getScreenSizeDialog(context: Activity, resourceId: Int): View? {
        val displayRectangle = Rect()
        val window = context.window
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(resourceId, null)
        layout.minimumWidth = (displayRectangle.width() * 1.0f).toInt()
        layout.minimumHeight = (displayRectangle.height() * 1.0f).toInt()
        return layout
    }

    fun isValidEmail(email: String?): Boolean {
        return email != null && Pattern.compile(emailPattern).matcher(email).matches()
    }
    companion object {
        private const val emailPattern = ("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
    }
    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        val pattern = Pattern.compile(passwordPattern)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }


    fun checkDigit(number: Int): String {
        // less than 10 add 0
        return if (number <= 9) "0$number" else number.toString()
    }

    fun addDecimal(number: String): String {
        // add decimal
        return if (!number.contains(".")) "$number.00" else number
    }


    fun showProgress(activity: Activity) {
        try {
            if (customProgress != null && customProgress!!.isShowing) customProgress!!.dismiss()
            if (customProgress == null) customProgress =
                CustomProgress(
                    context
                )
            customProgress!!.setCancelable(false)
            customProgress!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissProgress() {
        if (customProgress != null && customProgress!!.isShowing) customProgress?.dismiss()
        customProgress == null
    }

    fun showToast(msg: String) {
        val toast = Toast(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
        val textView: TextView = view.findViewById(R.id.custom_toast_text)
        textView.text = msg
        toast.view = view
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER, 0, 70)
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }
//    fun logOut(activity: Activity, msg: String?) {
//        if (msg != null) {
//            showToast(msg)
//        }
//        WebSocketManager.close()
//        val preferenceManager = PreferenceManager(context)
//        preferenceManager.clearPreferences()
//        val intent = Intent(activity, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        activity.startActivity(intent)
//        activity.finish() // call this to finish the current activity
//
//    }


}