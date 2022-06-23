package com.example.cdio8_solitaire_app

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.*
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.cdio8_solitaire_app.databinding.FragmentSecondBinding
import java.io.File
import java.io.File.createTempFile
import java.util.jar.Manifest
import kotlin.math.log

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val REQUEST_CODE = 1
    private lateinit var photoFile : File
    private val FILE_NAME = "photo.jpg"
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.trK.visibility = View.GONE
        binding.trK2.visibility = View.GONE
        binding.buttonSecond.setOnClickListener {
            //The taken photo have be saved as a file, because otherwise we will
            //only see the thumbnail, which is bas quality
            //only see the thumbnail, which is bad quality
            photoFile = getPhotoFile(FILE_NAME)

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val fileProvider = FileProvider.getUriForFile(this.requireContext(),"com.example.cdio8_solitaire_app.fileprovider",photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

//           Log.i("this is test", getPythonHelloWorld())

            try {
                startActivityForResult(takePictureIntent,REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context,"camera not working",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return createTempFile(fileName, ".jpg",storageDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Automatically used the image intent is used
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            binding.trK.visibility = View.VISIBLE
            binding.trK2.visibility = View.VISIBLE
            binding.infoText.visibility = View.GONE
            binding.imageView2.setRotation(90F)

            val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.imageView2.setImageBitmap(imageBitmap)


        }
        else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}