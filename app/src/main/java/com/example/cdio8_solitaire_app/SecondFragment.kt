package com.example.cdio8_solitaire_app
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Environment.*
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.cdio8_solitaire_app.databinding.FragmentSecondBinding
import com.example.cdio_solitaire.Model.Columns
import com.example.cdio_solitaire.controller.SolitaireSolver
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.File.createTempFile
import java.io.FileOutputStream


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val REQUEST_CODE = 1
    private lateinit var photoFile : File
    private val FILE_NAME = "photo"
    val solitaireSolver = SolitaireSolver()

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
    /***
     * @author s201729, s183925
     */
    private fun sendPythonPicture(path: String): String  {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this.requireContext()))
        }
        val python = Python.getInstance()
        val pythonFile = python.getModule("sendImage")
        return pythonFile.callAttr("sendPicture", path).toString()
    }


    private fun getPhotoFile(fileName: String): File {
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        print(storageDir)
        val PATH = createTempFile(fileName, ".jpg",storageDir).absolutePath
        print(PATH)
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
            //save picture
            val result = sendPythonPicture(photoFile.absolutePath)
            print(result)
            Log.i("result", result)
            parseScriptOutput(result)
            solitaireSolver.printContestSolutionAndDo()
            val solution = solitaireSolver.getContestSolution()
            binding.trK2.text = solution
            val baos = ByteArrayOutputStream()
            val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            binding.imageView2.setImageBitmap(imageBitmap)

        }
        else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /***
     * @author s201729
     * parses output from python script and inserts them into columns structure
     */
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
                var column = mutableListOf<String>()

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
                if (column.isNotEmpty()) {
                    column = column.reversed() as MutableList<String>
                }
                // add column to all columns
                allColumns.add(column)
            }
        }

        // further parsing to add data in columns object
        val backside = 'b'
        val gap = ' '
        var i = 0
        for (column in allColumns) {
            for (card in column) {
                val cardIterator = card.toCharArray().iterator()
                var next = cardIterator.next()

                // card faces down
                if (next == backside) {
                    solitaireSolver.addBottomCard(null, null, true, i)
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
                        solitaireSolver.addBottomCard(stringRank.toInt(), suit, false, i)
                    }
                    // if card is top card
                    else if (i <= 10) {
                        solitaireSolver.addTopCard(stringRank.toInt(), suit, false, i-7)
                    } else {
                        solitaireSolver.updateTalon(stringRank.toInt(), suit)
                    }
                }
            }
            i += 1
        }
    }
}