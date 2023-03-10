package com.example.appback4testeii

import android.accounts.AuthenticatorDescription
import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.parse.ParseObject
import com.parse.ParseQuery

class MainActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    private var popupInputDialogView: View? = null
    private var titleInput: EditText? = null
    private var descriptionInput: EditText? = null
    private var saveTodoButton: Button? = null
    private var cancelUserDataButton: Button? = null


    private var openInputPopupDialogButton: FloatingActionButton? = null
    private var recyclerView: RecyclerView? = null
    private var empty_text: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialog = ProgressDialog(this@MainActivity)
        initMainActivityControls()
        getTodoList()

        openInputPopupDialogButton?.setOnClickListener{ fabButtonView ->
            val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
            alertDialogBuilder.setTitle("Create a TODO")
            alertDialogBuilder.setCancelable(true)
            initPopupViewControls()

            alertDialogBuilder.setView(popupInputDialogView)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
            saveTodoButton?.setOnClickListener{ saveButtonView ->
                saveData(alertDialog)
            }
            cancelUserDataButton?.setOnClickListener { cancelButtonView ->
                alertDialog.cancel()
            }
        }
    }

    private fun saveData(alertDialog: AlertDialog) {
        val todo = ParseObject("Todo")
        if (titleInput?.text.toString().isNotEmpty() && descriptionInput?.text.toString().isNotEmpty()){
            alertDialog.cancel()
            progressDialog?.show()
            todo.put("title", titleInput?.text.toString())
            todo.put("description", descriptionInput?.text.toString())
            todo.saveInBackground { e ->
                progressDialog?.dismiss()
                if(e == null){
                    getTodoList()
                } else {
                    showAlert("Error", e.message!!)
                }
            }
        } else {
            showAlert("Error", "Please enter a title and description")
        }
    }

    private fun initMainActivityControls() {
        recyclerView = findViewById(R.id.recyclerView)
        empty_text = findViewById(R.id.empty_text)
        openInputPopupDialogButton = findViewById(R.id.fab)
    }

    private fun getTodoList() {
        progressDialog?.show()
        val query = ParseQuery.getQuery<ParseObject>("Todo")
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            progressDialog?.dismiss()
            if(e == null){
                initTodoList(objects)
            } else {
                showAlert("Error", "Please enter a title and description")
            }
        }
    }

    private fun initTodoList(list: List<ParseObject>?){
        if(list == null || list.isEmpty()){
            empty_text!!.visibility = View.VISIBLE
            return
        }
        empty_text?.visibility = View.GONE

        val adapter = TodoAdapter(list as ArrayList<ParseObject>, this)

        adapter.onDeleteListener.observe(this@MainActivity, { parseObject ->
            progressDialog?.show()
            parseObject.deleteInBackground{ e ->
                progressDialog?.dismiss()
                if(e == null){
                    getTodoList()
                }else{
                    showAlert("Error", e.message!!)
                }
            }
        })

        adapter.clickListenerToEdit.observe(this@MainActivity, { parseObject ->
            val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
            alertDialogBuilder.setTitle("Update a TODO")
            alertDialogBuilder.setCancelable(true)

            initPopupViewControls(
                parseObject.getString("title")!!,
                parseObject.getString("description")!!
            )
            alertDialogBuilder.setView(popupInputDialogView)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

            saveTodoButton?.setOnClickListener{ saveButtonView ->
                if (titleInput?.text.toString().isNotEmpty() && descriptionInput?.text.toString().isNotEmpty()) {
                    alertDialog.cancel()
                    progressDialog?.show()
                    parseObject.put("title", titleInput?.text.toString())
                    parseObject.put("description", descriptionInput?.text.toString())
                    parseObject.saveInBackground{ el ->
                        progressDialog?.dismiss()
                        if(el == null){
                            getTodoList()
                        } else {
                            showAlert("Error", el.message!!)
                        }
                    }
                } else {
                    showAlert("Error", "Please enter a title and description")
                }
            }
        })

        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.setHasFixedSize(false)
        recyclerView?.adapter = adapter
    }

    private fun initPopupViewControls(){
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        popupInputDialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        titleInput = popupInputDialogView?.findViewById(R.id.titleInput)
        descriptionInput = popupInputDialogView?.findViewById(R.id.descriptionInput)
        saveTodoButton = popupInputDialogView?.findViewById(R.id.button_save_todo)
        cancelUserDataButton = popupInputDialogView?.findViewById(R.id.button_cancel_user_data)
    }

    private fun initPopupViewControls(title: String, description: String){
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        popupInputDialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        titleInput = popupInputDialogView?.findViewById(R.id.titleInput)
        descriptionInput = popupInputDialogView?.findViewById(R.id.descriptionInput)
        saveTodoButton = popupInputDialogView?.findViewById(R.id.button_save_todo)
        cancelUserDataButton = popupInputDialogView?.findViewById(R.id.button_cancel_user_data)

        titleInput?.setText(title)
        descriptionInput?.setText(description)
    }

    private fun showAlert(title: String, message: String){
        val builder = AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, which ->
                dialog.cancel()
            }
        val ok = builder.create()
        ok.show()
    }
}