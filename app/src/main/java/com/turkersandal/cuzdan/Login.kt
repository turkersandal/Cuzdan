package com.turkersandal.cuzdan

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.create_user.*
import kotlinx.android.synthetic.main.create_user.view.*
import kotlinx.android.synthetic.main.dialog_forget.view.*
import kotlinx.android.synthetic.main.login_activity.*
import kotlin.math.ceil

class Login:AppCompatActivity() {
    var mAuth= FirebaseAuth.getInstance()
    internal lateinit var progressBar: ProgressDialog
    var context=this
    var cm :ConnectivityManager? = null
    var netInfo:NetworkInfo?=null
    var mInterstitialAd: InterstitialAd?=null


    private  fun checkConnection (context: Context):Boolean {
        cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        netInfo= cm!!.activeNetworkInfo

        return netInfo != null && netInfo!!.isConnected

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        val mypref = getSharedPreferences("mypref", Context.MODE_PRIVATE)
        val edit = mypref.edit()

        var kontrol = mypref.getString("check","")
        if(kontrol == "true"){checkBox.isChecked = true}

        if(checkBox.isChecked) {


            val email = mypref.getString("email", "")
            val password = mypref.getString("password", "")

            edtEmail.setText(email)
            edtPassword.setText(password)
        }

        if(checkConnection(this)){
            Toast.makeText(baseContext,"*HOŞGELDİNİZ*",Toast.LENGTH_SHORT).show()

        }else{
            var check = LayoutInflater.from(this).inflate(R.layout.check_connection,null)
            var builder = AlertDialog.Builder(context).setView(check)
            builder.setNegativeButton("Kapat"){dialog, which ->  }
            var alert = builder.show()
            
        }


        progressBar = ProgressDialog(this)
        progressBar.setMessage("Giris Yapılıyor...")
        progressBar.setTitle("Cuzdan")
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER)



        btnGiris.setOnClickListener {


            if(checkConnection(this)) {

                var mAuth = FirebaseAuth.getInstance()
                progressBar.show()
                if (!edtEmail.text.isEmpty() && !edtPassword.text.isEmpty()) {

                    if(checkBox.isChecked) {

                        edit.putString("check","true")
                        edit.apply()
                        val mypref = getSharedPreferences("mypref", Context.MODE_PRIVATE)
                        val editor = mypref.edit()
                        editor.putString("email", edtEmail.text.trim().toString())
                        editor.putString("password", edtPassword.text.trim().toString())
                        editor.apply()
                    }else{

                        edit.putString("check","false")
                        edit.apply()
                    }


                    mAuth.signInWithEmailAndPassword(edtEmail.text.trim().toString(), edtPassword.text.trim().toString())
                        .addOnSuccessListener {


                            Toast.makeText(this, "Giris Yapıldı", Toast.LENGTH_SHORT).show()
                            var intent = Intent(baseContext, MainActivity::class.java)
                            startActivity(intent)
                            progressBar.dismiss()
                            //GEÇİS REKLAMI
                            mInterstitialAd = InterstitialAd(this)
                            mInterstitialAd?.adUnitId = "ca-app-pub-3880919772436636/5445761359"
                            mInterstitialAd?.loadAd(AdRequest.Builder().build())
                            mInterstitialAd?.adListener=object: AdListener(){
                                override fun onAdLoaded() {
                                    mInterstitialAd?.show()
                                }
                            }


                        }.addOnFailureListener {
                        progressBar.dismiss()
                        Toast.makeText(this, "Email Adresi veya Şifre Yanlış", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressBar.dismiss()
                    Toast.makeText(baseContext, "Lütfen Alanları Doldurunuz.", Toast.LENGTH_SHORT).show()

                }
            }else{
                var check = LayoutInflater.from(this).inflate(R.layout.check_connection,null)
                var builder = AlertDialog.Builder(context).setView(check)
                builder.setNegativeButton("Kapat"){dialog, which ->  }
                var alert = builder.show()


            }
        }
        txtCreate.setOnClickListener {

            if(checkConnection(this)) {
                val alertCreate = LayoutInflater.from(this).inflate(R.layout.create_user, null)
                val builder = AlertDialog.Builder(this).setTitle("Kullanıcı Oluştur")
                builder.setView(alertCreate)
                val mAlert = builder.show()
                alertCreate.button.setOnClickListener {
                    if (!alertCreate.editText3.text.isEmpty() && !alertCreate.editText4.text.isEmpty() && !alertCreate.editText5.text.isEmpty()) {

                        if (alertCreate.editText3.text.length >= 6 && alertCreate.editText5.text.length >= 6) {


                            if (alertCreate.editText3.text.toString() == alertCreate.editText5.text.toString()) {

                                mAuth.createUserWithEmailAndPassword(
                                    alertCreate.editText4.text.trim().toString(),
                                    alertCreate.editText5.text.trim().toString()
                                ).addOnSuccessListener {

                                    Toast.makeText(this, "Kayıt Başarılı.", Toast.LENGTH_SHORT).show()
                                    mAlert.cancel()

                                }.addOnFailureListener {
                                    Toast.makeText(this, "Lütfen Email Adresinizi Kontrol Ediniz", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {

                                Toast.makeText(this, "Parola Alanları Aynı Olmalıdır.", Toast.LENGTH_SHORT).show()

                            }

                        } else {
                            Toast.makeText(this, "Parola En Az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
                        }


                    } else {
                        Toast.makeText(this, "Tüm Alanları Doldurunuz.", Toast.LENGTH_SHORT).show()

                    }

                }
            }else{

                var check = LayoutInflater.from(this).inflate(R.layout.check_connection,null)
                var builder = AlertDialog.Builder(context).setView(check)
                builder.setNegativeButton("Kapat"){dialog, which ->  }
                var alert = builder.show()
            }





        }
        txtForgot.setOnClickListener {


            if (checkConnection(this)) {
                val alertForgetTarget = LayoutInflater.from(this).inflate(R.layout.dialog_forget, null)
                val builder = AlertDialog.Builder(this).setTitle("Parolamı Unuttum").setView(alertForgetTarget)
                val mAlert = builder.show()
                alertForgetTarget.button2.setOnClickListener {
                    if (!alertForgetTarget.editText.text.isEmpty()) {
                        mAuth.sendPasswordResetEmail(alertForgetTarget.editText.text.trim().toString()).addOnSuccessListener {
                            Toast.makeText(this, "Parola Sıfırlama Maili Gönderildi", Toast.LENGTH_SHORT).show()
                            mAlert.cancel()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Lütfen Email Adresinizi kontrol ediniz.", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this, "Lütfen Alnları Doldurunuz.", Toast.LENGTH_SHORT).show()

                    }
                }
            }else{

                var check = LayoutInflater.from(this).inflate(R.layout.check_connection,null)
                var builder = AlertDialog.Builder(context).setView(check)
                builder.setNegativeButton("Kapat"){dialog, which ->  }
                var alert = builder.show()

            }
        }
    }
}