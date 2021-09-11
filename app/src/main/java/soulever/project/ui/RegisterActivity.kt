package soulever.project.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import soulever.project.databinding.ActivityRegisterBinding
import soulever.project.entity.User
import soulever.project.entity.Users

class RegisterActivity : AppCompatActivity() {
    private lateinit var activityRegisterBinding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    var databaseReference: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(activityRegisterBinding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("Profile")

        activityRegisterBinding.ayologin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }

        activityRegisterBinding.LoginButton.setOnClickListener {
            regUser()
        }

    }

    override fun onDestroy() {
        auth.signOut()
        finish()
        super.onDestroy()
    }

    private fun regUser()
    {
        val username = activityRegisterBinding.username.text.toString()
        val email = activityRegisterBinding.email.text.toString()
        val password = activityRegisterBinding.Password.text.toString()

        if (username.isEmpty())
        {
            activityRegisterBinding.username.setError("Please fill username")
            activityRegisterBinding.username.requestFocus()
            return
        }
        if (email.isEmpty()){
            activityRegisterBinding.email.setError("Please fill email")
            activityRegisterBinding.email.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            activityRegisterBinding.email.setError("Use email format")
            activityRegisterBinding.email.requestFocus()
            return
        }
        if (password.isEmpty())
        {
            activityRegisterBinding.Password.setError("Please fill password")
            activityRegisterBinding.Password.requestFocus()
            return
        }
        if (password.length <6)
        {
            activityRegisterBinding.Password.setError("Password length must larger than 6 character")
            activityRegisterBinding.Password.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
            if (it.isSuccessful)
            {
                val registeredUser = Users(username,email,password)
                auth.currentUser?.let {
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(it.uid)
                        .setValue(registeredUser).addOnCompleteListener{
                            if (it.isSuccessful)
                            {
                                Toast.makeText(this,"Register Succesfull",Toast.LENGTH_LONG).show()
                                startActivity(Intent(this,LoginActivity::class.java))
                            }
                            else{
                                Toast.makeText(this,"Register Failed",Toast.LENGTH_LONG).show()
                                Log.d("RegisGagal",it.exception.toString())
                            }
                        }
                }
            }
            else {
                // If sign in fails, display a message to the user.
                Log.d("RegisGagal", "createUserWithEmail:failure", it.exception)
                Toast.makeText(
                    this, "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
//                        updateUI(null)
            }
        }
    }

    private fun register() {
        activityRegisterBinding.LoginButton.setOnClickListener {
            if (TextUtils.isEmpty(activityRegisterBinding.username.text.toString())) {
                activityRegisterBinding.username.setError("Please Enter Username")
                return@setOnClickListener
            } else if (TextUtils.isEmpty(activityRegisterBinding.email.text.toString())) {
                activityRegisterBinding.email.setError("Please Enter your Email")
                return@setOnClickListener
            } else if (TextUtils.isEmpty(activityRegisterBinding.Password.text.toString())) {
                activityRegisterBinding.Password.setError("Please Enter Your Password")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(
                activityRegisterBinding.email.text.toString(),
                activityRegisterBinding.Password.text.toString()
            )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val currentUser = auth.currentUser
                        val currentUserdb = databaseReference?.child((currentUser?.uid!!))
                        currentUserdb?.child("Email")
                            ?.setValue(activityRegisterBinding.email.text.toString())
                        currentUserdb?.child("Password")
                            ?.setValue(activityRegisterBinding.Password.text.toString())
                        currentUserdb?.child("Username")
                            ?.setValue(activityRegisterBinding.username.text.toString())
                        currentUserdb?.child("Name")?.setValue("")
                        currentUserdb?.child("Address")?.setValue("")
                        currentUserdb?.child("Telephone")?.setValue("")

                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration Success",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration Failed, please try again!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

        }
    }
}