package com.app.loginapi.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.app.loginapi.apiCalling.RetrofitClient
import com.app.loginapi.utils.Const
import com.app.loginapi.utils.PreferenceManager
import com.app.loginapi.databinding.ActivityMainBinding
import com.app.loginapi.request.LoginRequest
import com.app.loginapi.response.LoginResponse
import com.app.loginapi.utils.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    lateinit var utils: Utils
    var deviceId = ""
    lateinit var pref: PreferenceManager
    lateinit var loginResponse: LoginResponse


    val b: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        initView()
        clickEvent()
    }

    private fun initView() {
        utils = Utils(this)
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        pref = PreferenceManager(this)
    }
    private fun clickEvent() {
        b.btnSign.setOnClickListener {
                loginApi()
        }
        b.checkPswdHide.setOnClickListener {
            if (b.checkPswdHide.isChecked) {
                b.etPass.transformationMethod = null
            } else {
                b.etPass.transformationMethod = PasswordTransformationMethod()
            }
            b.etPass.setSelection(b.etPass.length())
        }
    }
    fun successToast(msg: String) {
//Add library in settings.gradle -  maven{url 'https://jitpack.io'}
        Toasty.success(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun errorToast(msg: String) {
        Toasty.error(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun loginApi() {
        if (utils.isNetworkAvailable()) {
            utils.showProgress(this)
            val loginRequest = LoginRequest(
                LoginRequest.Data(
                    Const.langType,
                    deviceId,
                    Const.deviceType,
                    b.etEmail.text.toString().trim(),
                    b.etPass.text.toString().trim(),
                    Const.timezone
                )
            )
            val login: Call<LoginResponse?> = RetrofitClient.getClient.login(loginRequest)
            login.enqueue(object : Callback<LoginResponse?> {
                override fun onResponse(
                    call: Call<LoginResponse?>,
                    response: Response<LoginResponse?>
                ) {
                    utils.dismissProgress()
                    if (response.isSuccessful) {
                        val loginResponse = response.body()!!
                        when (loginResponse.status) {
                            "1"-> {
                                pref.setString(Const.userData, Gson().toJson(response.body()))
                                pref.setString(Const.token, loginResponse.data.token)
                                pref.setString(Const.id, loginResponse.data.id)
                                pref.setString(Const.profileStatus, loginResponse.data.profileStatus)
                                successToast(loginResponse.message)
                                checkProfileStatus()
                            }
                            else -> {
                                errorToast(loginResponse.message)
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                    utils.dismissProgress()
                    utils.showToast(t.message.toString())
                }

            })
        }
    }

    fun checkProfileStatus() {
        if (pref.getString(Const.userData)?.isNotEmpty()!!) {
            loginResponse =
                Gson().fromJson(pref.getString(Const.userData), LoginResponse::class.java)
        }
        finish()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
//        val handler = Handler()
//        handler.postDelayed({
//            if (pref.getString(Const.token).equals("")) {
//               startActivity(Intent(this, QuickActivity::class.java))
//                finish()
//            else {
//            finish()
//            startActivity(Intent(this, LoginActivity::class.java))
//           }
//        }, 3000)
    }
}