package com.daycounter.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.daycounter.R
import com.daycounter.dataclass.Counter
import com.daycounter.databinding.FragmentEditCountersBinding
import com.daycounter.other.enum.Constants
import com.daycounter.other.enum.Strings
import com.daycounter.other.enum.TranslationType
import com.daycounter.service.data.SaveUserDataService
import com.daycounter.service.date.DateDifferenceService
import com.daycounter.service.validation.InputDateValidationService
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class EditMainCountersFragment : Fragment() {

    private var _binding: FragmentEditCountersBinding? = null
    private val binding get() = _binding!!
    private val validateDate = InputDateValidationService()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentEditCountersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createDateInputBindings()
        if (Constants.MAIN_COUNTER != null)
            fillInData(Constants.MAIN_COUNTER!!)
    }

    private fun createDateInputBindings() {

        binding.submitButton.isEnabled = false
        binding.submitButton.setOnClickListener {
            if (checkEmptyFields()) {
                saveNewMainCounter(binding.inputPersonOne.text.toString(), binding.inputPersonTwo.text.toString(), generateDate())
                findNavController().navigate(R.id.action_editcounter_to_start)
            } else {
                Snackbar.make(view!!, Strings.FILL_IN_ALL_FIELDS, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }

        binding.inputPersonOne.doOnTextChanged {
            _,_,_,_ ->
            if (checkEmptyFields())
                binding.submitButton.isEnabled = true
        }

        binding.inputPersonTwo.doOnTextChanged {
                _,_,_,_ ->
            if (checkEmptyFields())
                binding.submitButton.isEnabled = true
        }

        binding.inputDay.doOnTextChanged {
            text, _, _, _ ->
                if (!validateDate.validate(0, text.toString())) {
                    binding.inputDay.error = Strings.INVALID_INPUT_DAY
                    binding.submitButton.isEnabled = false
                } else {
                    if (checkEmptyFields())
                        binding.submitButton.isEnabled = true
                    if (text?.length == 2) binding.inputMonth.requestFocus()
                }
        }

        binding.inputMonth.doOnTextChanged {
            text, _, _, _ ->
                if (!validateDate.validate(1, text.toString())) {
                    binding.inputMonth.error = Strings.INVALID_INPUT_MONTH
                    binding.submitButton.isEnabled = false
                } else {
                    if (checkEmptyFields())
                        binding.submitButton.isEnabled = true
                    if (text?.length == 2) binding.inputYear.requestFocus()
                }
        }

        binding.inputYear.doOnTextChanged {
                text, _, _, _ ->
            if (!validateDate.validate(2, text.toString())) {
                binding.inputYear.error = Strings.INVALID_INPUT_YEAR
                binding.submitButton.isEnabled = false
            } else {
                if (checkEmptyFields())
                    binding.submitButton.isEnabled = true
            }
        }
    }

    private fun fillInData(counter: Counter) {
        binding.inputPersonOne.setText(counter.personOne)
        binding.inputPersonTwo.setText(counter.personTwo)
        binding.inputDay.setText(String.format("%s", Constants.SDF.format(counter.startDate)).substring(0, 2))
        binding.inputMonth.setText(String.format("%s", Constants.SDF.format(counter.startDate)).substring(3, 5))
        binding.inputYear.setText(String.format("%s", Constants.SDF.format(counter.startDate)).substring(6, 10))
    }

    private fun generateDate(): String {
        val finalDate = String.format("%s-%s-%s",
            binding.inputDay.text.toString(),
            binding.inputMonth.text.toString(),
            binding.inputYear.text.toString())

        return if (validateDate.validate(3, finalDate)) finalDate else ""
    }

    private fun checkEmptyFields(): Boolean {
        return binding.inputDay.text.toString().trim().isNotEmpty()
                && binding.inputMonth.text.toString().trim().isNotEmpty()
                && binding.inputYear.text.toString().trim().isNotEmpty()
                && binding.inputPersonOne.text.toString().trim().isNotEmpty()
                && binding.inputPersonTwo.text.toString().trim().isNotEmpty()
    }

    private fun saveNewMainCounter(personOne: String?, personTwo: String?, date: String?) {

        val handler = SaveUserDataService()
        val dateDiff = DateDifferenceService()

        Constants.MAIN_COUNTER = try {
            if (personOne != "") {
                val date = Constants.SDF.parse(date!!)

                Counter(personOne, personTwo, date,
                    dateDiff.getDateDifference(date, TranslationType.DAYS))
            } else {
                val testDate = Constants.SDF.parse("27-12-2019-00-00-00")

                Counter("Person One", "Person Two", testDate,
                    dateDiff.getDateDifference(testDate, TranslationType.DAYS))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        handler.saveUserData(this.context!!
            .getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
