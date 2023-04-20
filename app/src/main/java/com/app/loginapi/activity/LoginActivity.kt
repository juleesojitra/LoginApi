package com.app.loginapi.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.app.loginapi.MediaUploadResponse
import com.app.loginapi.R
import com.app.loginapi.request.SocialLoginRequest
import com.app.loginapi.apiCalling.RetrofitClient
import com.app.loginapi.databinding.ActivityLoginBinding
import com.app.loginapi.instagramAuth.ApplicationData
import com.app.loginapi.instagramAuth.InstagramApp
import com.app.loginapi.response.LoginResponse
import com.app.loginapi.utils.Const
import com.app.loginapi.utils.PreferenceManager
import com.app.loginapi.utils.Utils
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Progress
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    val b: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    //  google
    var loginKey = ""
    var authProvider = ""
    var fbEmail = ""
    var profile_pic: URL? = null
    lateinit var utils: Utils
    var deviceId = ""
    lateinit var pref: PreferenceManager
    var fileImage: File? = null
    var imageName = ""

    // Google
    private var googleSignIn: SignInButton? = null
    private var googleApiClient: GoogleApiClient? = null
    private var googleSignInClient: GoogleSignInClient? = null
    var RC_SIGN_IN = 4732
    var imageProfileUrl = ""

    //Facebook
    private val EMAIL = "email"
    var isLoggedIn = true
    var email = ""
    var id = ""
    var name = ""
    var authId = ""
    private lateinit var callbackManager: CallbackManager
    private var mApp: InstagramApp? = null
    private var userInfoHashmap = HashMap<String, String>()
    var handler = Handler { msg ->
        if (msg.what == InstagramApp.WHAT_FINALIZE) {
            userInfoHashmap = mApp!!.userInfo
        } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
            Toast.makeText(this, "Check your network.", Toast.LENGTH_SHORT).show()
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        initView()
        clickEvent()
    }

    private fun initView() {
        googleLogin()
        instagramAuth()
        facebookLogin()
    }

    private fun clickEvent() {
        b.ivGoogle.setOnClickListener {
            signInGoogle()
        }
        b.ivFb.setOnClickListener {
            b.fbLoginButton.performClick()
        }
        b.ivInstra.setOnClickListener {
            mApp!!.authorize()
        }
    }

    private fun googleLogin() {
        googleSignIn = b.googleButton
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.GOOGLE_CLIENT_ID))
            .requestProfile()
            .requestId()
            .requestEmail()
            .build()
        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient!!.signOut()
    }

    private fun signInGoogle() {
        utils.showProgress(this)
        val signInIntent = googleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CANCELED) {
            return
        }

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result!!.isSuccess) {
                // Signed in successfully, show authenticated UI.
                val acct = result.signInAccount
                handleSignInResult(acct)
            } else {
                utils.dismissProgress()
                Log.e("googleSign", "onActivityResult: ${result.status}")
            }
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount?) {
        utils.dismissProgress()
        try {
            authId = account!!.id!!
            //deviceToken = account.getIdToken();
            name = account.displayName!!
            loginKey = account.email.toString()
            if (account.photoUrl != null) {
                if (!TextUtils.isEmpty(account.photoUrl.toString())) {
                    imageProfileUrl = account.photoUrl.toString()
                }
            }
            pref.setString(Const.socialName, name)
            pref.setString(Const.socialEmail, loginKey)
            pref.setString(Const.socialAuthID, authId)
            pref.setString(Const.socialAuthProvider, "google")

            if (imageProfileUrl.equals("", ignoreCase = true)) {
                var firstName = ""
                var lastName = ""
                if (name.trim { it <= ' ' }.contains(" ")) {
                    val names = name.split(" ".toRegex()).toTypedArray()
                    firstName = names[0]
                    lastName = names[1]
                } else {
                    firstName = name
                }
                socialLoginAPI(loginKey, firstName, authId, "", "google")
            } else {
                googleImageDownload(imageProfileUrl)
            }
            Log.e(
                "LoginGoogle",
                "Name: $name, email: $loginKey, Image: $imageProfileUrl"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun googleImageDownload(image: String) {
        val fname = "Locationtracker/App"
        val myfolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + fname).toString()
        val file = File(myfolder)
        if (!file.exists()) {
            file.mkdirs()
        }
        val downloadId =
            PRDownloader.download(image + "", file.absolutePath, "googleLocationtracker.jpg")
                .build()
                .setOnStartOrResumeListener {}
                .setOnPauseListener {}
                .setOnCancelListener {}
                .setOnProgressListener { progress: Progress? -> }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        Log.e("TAG", "onDownloadComplete: " + "TAG")
                        fileImage = File("$file/googleLocationtracker.jpg")
//                        val bmOptions = BitmapFactory.Options()
//                        val photo = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
//                        val tempUri = getImageUri(applicationContext, photo)
//                        fileImage = File(FileUtils.getPath(activity, tempUri))
                        uploadMedia()
                    }

                    override fun onError(error: com.downloader.Error?) {
                        loginKey = pref.getString(Const.socialEmail).toString()
                        val firstName = pref.getString(Const.socialFName).toString()
                        val lastName = pref.getString(Const.socialLName).toString()
                        authId = pref.getString(Const.socialAuthID).toString()
                        authProvider = pref.getString(Const.socialAuthProvider).toString()

                        socialLoginAPI(
                            loginKey,
                            firstName,
                            authId,
                            "",
                            authProvider
                        )
                    }
                })
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                inContext.contentResolver,
                inImage,
                "googleLocationtracke${Calendar.getInstance().time}",
                null
            )
        return Uri.parse(path)
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logOut()
        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()
        b.fbLoginButton.setReadPermissions(listOf(EMAIL))
        try {
            val info = packageManager.getPackageInfo(
                "com.app.locationtracker", PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
        // Register a callback for the Facebook Login button
        b.fbLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                utils.showToast("error")
                Log.d("TAG", "onError:$exception ")
            }

            override fun onSuccess(result: LoginResult?) {
                val request = GraphRequest.newMeRequest(
                    result!!.accessToken
                ) { `object`, response ->
                    authId = `object`!!.getString("id")
                    profile_pic = URL(
                        "http://graph.facebook.com/$authId/picture?type=large"
                    )
                    Log.i("profile_pic", profile_pic.toString() + "")
                    Log.v("Main", response.toString())
                    name = `object`.getString("name")
                    email = `object`.getString("email")
                    pref.setString(Const.socialName, name)
                    pref.setString(Const.socialEmail, email)
                    pref.setString(Const.authId, authId)
                    pref.setString(Const.socialAuthProvider, "facebook")


                    socialLoginAPI(
                        email, name, authId, "", "facebook"
                    )
                }
                val parameters = Bundle()
                parameters.putString(
                    "fields", "id,name,email,gender, birthday,cover,picture,first_name,last_name"
                )
                request.parameters = parameters
                request.executeAsync()
            }
        })
    }

    @SuppressLint("LongLogTag")
    fun getUserProfile(token: AccessToken?, userId: String?) {

        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id, first_name, middle_name, last_name, name, picture, email"
        )
        GraphRequest(token,
            "/$userId/",
            parameters,
            HttpMethod.GET,
            GraphRequest.Callback { response ->
                val jsonObject = response.jsonObject ?: return@Callback

                // Facebook Access Token
                // You can't see it in Logcat using Log.d, Facebook did that to avoid leaking user's access token.
                if (BuildConfig.DEBUG) {
                    FacebookSdk.setIsDebugEnabled(true)
                    FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS)
                }

                // Facebook Id
                if (jsonObject.has("id")) {
                    id = jsonObject.getString("id")
                    authId = id
                    Log.e("Facebook Id: ", authId)

                } else {
                    Log.e("Facebook Id: ", "Not exists")
                }

                // Facebook First Name
                if (jsonObject.has("first_name")) {
                    val facebookFirstName = jsonObject.getString("first_name")
                    Log.e("Facebook First Name: ", facebookFirstName)
                } else {
                    Log.e("Facebook First Name: ", "Not exists")
                }

                // Facebook Middle Name
                if (jsonObject.has("middle_name")) {
                    val facebookMiddleName = jsonObject.getString("middle_name")
                    Log.e("Facebook Middle Name: ", facebookMiddleName)
                } else {
                    Log.e("Facebook Middle Name: ", "Not exists")
                }

                // Facebook Last Name
                if (jsonObject.has("last_name")) {
                    val facebookLastName = jsonObject.getString("last_name")
                    Log.e("Facebook Last Name: ", facebookLastName)
                } else {
                    Log.e("Facebook Last Name: ", "Not exists")
                }

                // Facebook Name
                if (jsonObject.has("name")) {
                    name = jsonObject.getString("name")
                    Log.e("Facebook Name: ", jsonObject.getString("name"))
                } else {
                    Log.e("Facebook Name: ", "Not exists")
                }

                // Facebook Profile Pic URL
                if (jsonObject.has("picture")) {
                    val facebookPictureObject = jsonObject.getJSONObject("picture")
                    if (facebookPictureObject.has("data")) {
                        val facebookDataObject = facebookPictureObject.getJSONObject("data")
                        if (facebookDataObject.has("url")) {
                            val facebookProfilePicURL = facebookDataObject.getString("url")
                            Log.e("Facebook Profile Pic URL: ", facebookProfilePicURL)
                        }
                    }
                } else {
                    Log.e("Facebook Profile Pic URL: ", "Not exists")
                }

                // Facebook Email
                if (jsonObject.has("email")) {
                    val facebookEmail = jsonObject.getString("email")
                    fbEmail = facebookEmail
                    Log.e("Facebook Email: ", fbEmail)
                } else {
                    Log.e("Facebook Email: ", "Not exists")
                }

                pref.setString(Const.socialName, name)
                pref.setString(Const.socialEmail, email)
                pref.setString(Const.socialId, authId)
                pref.setString(Const.socialImage, imageName)
                pref.setString(Const.socialAuthProvider, "facebook")


                socialLoginAPI(
                    fbEmail, name, authId, imageName, "facebook"
                )
            }).executeAsync()
    }

    private fun instagramAuth() {
        mApp = InstagramApp(
            this, ApplicationData.CLIENT_ID,
            ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL
        )
        mApp!!.setListener(object : InstagramApp.OAuthAuthenticationListener {
            override fun onSuccess() {
                try {
                    mApp!!.fetchUserName(handler)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    Log.e(
                        "onSuccess",
                        "onSuccess: " + mApp!!.id.toString() + "   " + mApp!!.userName
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (mApp!!.id.isNotEmpty()) {
                    authId = mApp!!.id

                    name = mApp!!.userName
                    pref.setString(Const.socialAuthProvider, "instagram")
                    pref.setString(Const.authId, authId)
                    pref.setString(Const.socialName, name)


                    socialLoginAPI(email, name, authId, "", "instagram")

                }
            }

            override fun onFail(error: String?) {
                Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    //api
    private fun socialLoginAPI(
        email: String,
        firstname: String,
        authID: String,
        imageName: String,
        authProvider: String,
    ) {
        pref.setString(Const.authId, authId)
        if (utils.isNetworkAvailable()) {
            utils.showProgress(this)

            val socialLoginRequest = SocialLoginRequest(
                SocialLoginRequest.Data(
                    authID,
                    authProvider,
                    deviceId,
                    Const.deviceType,
                    if (email.isEmpty()) "1" else "0",
                    Const.langType,
                    firstname,
                    email,
                    Const.timezone,
                    imageName
                )
            )
            val socialLogin = RetrofitClient.getClient.socialLogin(socialLoginRequest)
            socialLogin.enqueue(object : Callback<LoginResponse?> {
                override fun onResponse(
                    call: Call<LoginResponse?>, response: Response<LoginResponse?>
                ) {
                    utils.dismissProgress()
                    if (response.isSuccessful) {
                        val loginResponse = response.body()!!
                        when (loginResponse.status) {
                            "1" -> {
                                pref.setString(Const.userData, Gson().toJson(response.body()))
                                pref.setString(Const.token, loginResponse.data.token)
                                pref.setString(Const.id, loginResponse.data.id)
                                pref.setString(
                                    Const.profileStatus,
                                    loginResponse.data.profileStatus
                                )
//                                checkProfileStatus()
                            }
                            "3" -> {
//                                startActivity(
//                                    Intent(this, VerificationActivity::class.java).putExtra(
//                                        VerificationActivity.IS_FORGOT, true
//                                    ).putExtra(
//                                        VerificationActivity.EMAIL, loginResponse.data.email
//                                    )
//                                )
                            }
                            "4" -> {
//                                startActivity(
//                                    Intent(
//                                        this, EmailActivity::class.java
//                                    ).putExtra(EmailActivity.IS_BLANK, true)
//                                )
                            }
                            else -> {
                                checkStatus(loginResponse.status, loginResponse.message)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                    utils.dismissProgress()
                    utils.showToast(getString(R.string.serverNotResponding))
                }
            })
        }
    }

    fun uploadMedia() {
        if (fileImage != null) {
            val requestBody = RequestBody.create("*/*".toMediaTypeOrNull(), fileImage!!)
            Log.e("file name", "uploadMedia: $fileImage")
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("files", fileImage!!.name, requestBody)
            val langType = RequestBody.create("text/plain".toMediaTypeOrNull(), "1")
            try {
                utils.showProgress(this)
                val call: Call<MediaUploadResponse?>? =
                    RetrofitClient.getClient.mediaUpload(langType, body)
                call?.enqueue(object : Callback<MediaUploadResponse?> {
                    override fun onResponse(
                        call: Call<MediaUploadResponse?>,
                        response: Response<MediaUploadResponse?>
                    ) {
                        utils.dismissProgress()
                        try {
                            when (response.code()) {
                                200 -> {
                                    val data = Gson().toJson(response.body())
                                    val jsonObject = JSONObject(data)
                                    val status = jsonObject.getString("status")
                                    if (status.equals("1", ignoreCase = true)) {
                                        utils.dismissProgress()
                                        val jsonArray = jsonObject.getJSONArray("data")
                                        val jsonObject1 = JSONObject(jsonArray[0].toString())
                                        jsonObject1.getString("mediaName")
                                        jsonObject1.getString("mediaBaseUrl")
                                        jsonObject1.getString("medialThumUrl")
                                        jsonObject1.getString("videoThumbImgName")
                                        val imagePath = jsonObject.getString("base_url")
                                        val imageName = jsonObject1.getString("mediaName")
                                        Log.e("imageName:", imageName + "")
                                        pref.setString(Const.socialImage, imageName)
                                        var firstName = ""
                                        var lastName = ""
                                        name = pref.getString(Const.socialName).toString()
                                        if (!TextUtils.isEmpty(name)) {
                                            val names = name.split(" ".toRegex()).toTypedArray()
                                            firstName = names[0]
                                            lastName = names[1]
                                        }
                                        loginKey = pref.getString(Const.socialEmail).toString()
                                        authId = pref.getString(Const.socialAuthID).toString()
                                        authProvider =
                                            pref.getString(Const.socialAuthProvider).toString()

                                        socialLoginAPI(
                                            loginKey,
                                            firstName,
                                            authId,
                                            imageName,
                                            authProvider
                                        )
                                    } else if (status.equals("0", ignoreCase = true)) {
                                        utils.dismissProgress()
                                        utils.showToast(jsonObject.getString("message"))
                                    }
                                }
                                404, 500 -> {
                                    utils.dismissProgress()
                                    utils.showToast("Server not responding!")
                                }
                                else -> {}
                            }
                        } catch (e: Exception) {
                            utils.dismissProgress()
                            e.message?.let { utils.showToast(it) }
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<MediaUploadResponse?>, t: Throwable) {
                        utils.dismissProgress()
                        utils.showToast(getString(R.string.serverNotResponding))
                    }
                })
            } catch (e: Exception) {
                utils.dismissProgress()
                e.printStackTrace()
            }
        }
    }

    fun checkStatus(status: String, msg: String) {
        if (status == "0") {
            utils.showToast(msg)
        } else if (status == "2" || status == "5") {
//            utils.logOut(activity, msg)
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }


}