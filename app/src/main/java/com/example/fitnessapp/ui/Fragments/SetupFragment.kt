package  com.example.fitnessapp.ui.Fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.others.Constants.KEY_FIRST_TIME
import com.example.fitnessapp.others.Constants.KEY_NAME
import com.example.fitnessapp.others.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @set:Inject
    var firstTime = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!firstTime){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        tvContinue.setOnClickListener {
            val success = saveNameAndWeight()
            if (success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
           else {
               Toast.makeText(context,"please enter all data",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveNameAndWeight(): Boolean {
        val name_txt = etName.text.toString()
        val weight = etWeight.text.toString()
        if (name_txt.isEmpty() || weight.isEmpty()) {
            return false
        }
    sharedPreferences.edit()
        .putString(KEY_NAME,name_txt)
        .putFloat(KEY_WEIGHT, weight.toFloat())
        .putBoolean(KEY_FIRST_TIME, false)
        .apply()
        val toolbarText = "Let's go, $name_txt!"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true

    }



}