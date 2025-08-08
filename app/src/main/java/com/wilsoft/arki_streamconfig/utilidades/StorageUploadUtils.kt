// StorageUploadUtils.kt
package com.wilsoft.arki_streamconfig.utilidades

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

fun subirLogoAFirebaseStorage(uri: Uri, context: Context, onUploadComplete: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val logoRef = storageRef.child("logos/${System.currentTimeMillis()}.png")

    logoRef.putFile(uri)
        .addOnSuccessListener {
            logoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                // Guardar URL en la base de datos
                FirebaseDatabase.getInstance()
                    .getReference("CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS")
                    .child("urlLogo")
                    .setValue(downloadUrl.toString())

                onUploadComplete(downloadUrl.toString())
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al subir logo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}


