package com.example.cdio8_solitaire_app

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.cdio8_solitaire_app.databinding.ActivityMainBinding
import com.example.cdio_solitaire.Model.Columns
import com.example.cdio_solitaire.controller.SolitaireSolver

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val CAMERA_PERSISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        this.getSupportActionBar()?.hide();
//        val res = imageRecognition()
        parseScriptOutput(imageRecognition())



//        Log.i("result", res.toString())



        val solitaireSolver = SolitaireSolver()
        solitaireSolver.addBottomCard(1,"C",false,3)
        //solitaireSolver.addTopCard()
//        solitaireSolver.printContestSolution(solitaireSolver.solve())

        //solitaireSolver.updateTalon()
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        checkPermission(android.Manifest.permission.CAMERA,CAMERA_PERSISSION_CODE)
        /*
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

         */
    }

    private fun imageRecognition(): String {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val value = pythonFile.callAttr("recognizeImage").toString()
//        for (letter in letters)
//            Log.i("letter",letters.next().toString())
//        val res = value.toString()
//        Log.i("res list", res)
        return value
    }

    private fun parseScriptOutput(stringData: String) {

        val charData = stringData.toCharArray()
        val charIterator = charData.iterator()
        val allColumns: MutableList<MutableList<String>> = mutableListOf()
        val columnStart = '['
        val columnEnd = ']'
        val objectStartEnd = '"'

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

        val columns = Columns()
        val backside = 'b'
        val gap = ' '
        var i = 0
        for (column in allColumns) {
            for (card in column) {
                val cardIterator = card.toCharArray().iterator()
                var next = cardIterator.next()

                // card is backcard
                if (next == backside) {
                    columns.addToBottomList(null, null, true, i)
                }
                // card is front card
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




    private fun getPythonHelloWorld(): String {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val python = Python.getInstance()
        val pythonFile = python.getModule("helloworldscript")
        return pythonFile.callAttr("helloworld").toString()
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun checkPermission(permission: String, requestCode: Int){
        if (ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else{
            Toast.makeText(this,"Permission granted already", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERSISSION_CODE)
            if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Camera Permission Grantend", Toast.LENGTH_SHORT).show()}
            else{
                Toast.makeText(this, "Camera Permission Not Grantend", Toast.LENGTH_SHORT).show()}
    }


}