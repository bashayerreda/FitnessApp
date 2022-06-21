package com.example.fitnessapp.ui.Fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fitnessapp.R
import com.example.fitnessapp.others.Constants.KEY_NAME
import com.example.fitnessapp.others.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.fragment_setup.etName
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDataFromSharedPreferences()
        btnApplyChanges.setOnClickListener {
            val success = editDataInSharedPreference()
            if(success){
                Toast.makeText(context,"data updated", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(context,"please enter all data you want to update",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun loadDataFromSharedPreferences(){
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 60f)
        etName.setText(name)
        Weight.setText(weight.toString())
    }

    private fun editDataInSharedPreference():Boolean{
        val nameText = etName.text.toString()
        val weight = Weight.text.toString()
        if (nameText.isEmpty() || weight.isEmpty()){
            return false
        }
      sharedPreferences.edit()
          .putString(KEY_NAME,nameText)
          .putFloat(KEY_WEIGHT,weight.toFloat())
          .apply()
        val toolbarText = "Let's go $nameText"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true

    }
}