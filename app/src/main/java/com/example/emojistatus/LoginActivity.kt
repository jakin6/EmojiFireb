package com.example.emojistatus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.emojistatus.databinding.LoginActivityBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private lateinit var  mGoogleSignInClient: GoogleSignInClient

    private companion object {
        private const val TAG = "LoginActivity"
        const val RC_SIGN_IN = 9001
    }

    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=Firebase.auth

        val gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso)
        binding.signInButton.setOnClickListener {
            signIn()
        }
    }
    private fun signIn(){
        val signInIntent=mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
    }
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user==null){
            Log.w(TAG,"User is null,not going to navigate")
            return
        }
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Google Sign In was successful,authenticate with Firebase
                val account=task.getResult(ApiException::class.java)!!
                Log.d(TAG,"firebaseAuthWithGoogle:"+account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e:ApiException){
                //Google Sign In failed,update UI appropriately
                Log.w(TAG,"Google sign in failed",e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken:String){
        val credential=GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task->
                if (task.isSuccessful){
                    //Sing   in success ,update UI the signed-in user's infromation
                    Log.d(TAG,"signInWithCredential:success")
                    val user=auth.currentUser
                    updateUI(user)
                }else{
                    //If sign in fails,display a message to the user.
                    Log.w(TAG,"signInWithCredential:failure",task.exception)
                    Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

            }
    }

}