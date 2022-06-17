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
import android.util.Log
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
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.chaquo.python.PyObject
import com.example.cdio8_solitaire_app.databinding.FragmentSecondBinding
import com.example.cdio_solitaire.Model.Columns
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


        binding.buttonThird.visibility = View.GONE
        binding.trK.visibility = View.GONE
        binding.trK2.visibility = View.GONE
        binding.buttonSecond.setOnClickListener {

            //The taken photo have be saved as a file, because otherwise we will
            //only see the thumbnail, which is bas quality

            photoFile = getPhotoFile(FILE_NAME)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val fileProvider = FileProvider.getUriForFile(this.requireContext(),"com.example.cdio8_solitaire_app.fileprovider",photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

//            Log.i("this is test", getPythonHelloWorld())

            try {
                startActivityForResult(takePictureIntent,REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context,"camera not working",Toast.LENGTH_SHORT).show()
            }
        }
    }

    // recognizes cards in given image and produces output for solution algorithm (NOT YET FULLY IMPLEMENTED)
    private fun imageRecognition(arg: String): String {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this.requireContext()))
        }
        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val value = pythonFile.callAttr("recognizeImage").toString()
        return value
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
            binding.buttonThird.visibility = View.VISIBLE
            binding.buttonThird.setOnClickListener{
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    startActivityForResult(takePictureIntent,REQUEST_CODE)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context,"camera not working",Toast.LENGTH_SHORT).show()
                }
            }
            binding.trK.visibility = View.VISIBLE
            binding.trK2.visibility = View.VISIBLE
            binding.buttonSecond.visibility = View.GONE
            binding.infoText.visibility = View.GONE
            binding.imageView2.setRotation(90F)

            val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.imageView2.setImageBitmap(imageBitmap)


        }
        else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    //parses output from python script and inserts them into columns structure
    private fun parseScriptOutput(stringData: String) {
        val charIterator = stringData.toCharArray().iterator()
        val allColumns: MutableList<MutableList<String>> = mutableListOf()
        val columnStart = '['
        val columnEnd = ']'
        val objectStartEnd = '"'

        // below outer while loop parses initial script output organising each column into string lists
        // ignore first start bracket
        charIterator.next()
        while(charIterator.hasNext()) {
            var next = charIterator.next()

            // if new column
            if (next == columnStart) {
                next = charIterator.next()
                val column = mutableListOf<String>()

                // in column
                while (next != columnEnd) {

                    // start of object
                    if(next == objectStartEnd) {
                        next = charIterator.next()
                        var cardval = String()

                        // in object string
                        while (next != objectStartEnd) {
                            val sString = next.toString()
                            cardval += sString
                            next = charIterator.next()
                        }
                        // add object to column
                        column.add(cardval)
                    }
                    next = charIterator.next()
                }
                // add column to all columns
                allColumns.add(column)
            }
        }

        // further parsing to add data in columns object
        val columns = Columns()
        val backside = 'b'
        val gap = ' '
        var i = 0
        for (column in allColumns) {
            for (card in column) {
                val cardIterator = card.toCharArray().iterator()
                var next = cardIterator.next()

                // card faces down
                if (next == backside) {
                    columns.addToBottomList(null, null, true, i)
                }
                // card faces up
                else {
                    var stringRank = ""
                    var suit = ""
                    while (cardIterator.hasNext()) {

                        // when part of value is rank
                        if (next != gap) {
                            stringRank += next.toString()
                            next = cardIterator.next()
                        }
                        // when part of value is suit
                        else {
                            suit = cardIterator.next().toString()
                        }
                    }
                    // if card is bottomcard
                    if (i <= 6) {
                        columns.addToBottomList(stringRank.toInt(), suit, false, i)
                    }
                    // if card is top card
                    else {
                        columns.addToTopList(stringRank.toInt(), suit, false, i)
                    }
                }
            }
            i += 1
        }
    }

    // test method for identifying issues with kotlin to python script communication
    private fun getPythonHelloWorld(): String {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this.requireContext()))
        }

        val python = Python.getInstance()
        val pythonFile = python.getModule("helloworldscript")
        return pythonFile.callAttr("helloworld").toString()
    }
}