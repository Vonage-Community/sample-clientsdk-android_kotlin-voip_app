package com.example.vonage.voicesampleapp.activities.fragments

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.R
import com.example.vonage.voicesampleapp.databinding.FragmentDialerBinding
import com.example.vonage.voicesampleapp.utils.Constants
import com.example.vonage.voicesampleapp.utils.DialerType

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TYPE = "dialer_type"

/**
 * A [Fragment] subclass to render a Dialer.
 * Use the [DialerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialerFragment : DialogFragment() {
    private val clientManager = App.coreContext.clientManager
    private var type: DialerType = DialerType.PHONE_NUMBER
    private lateinit var binding: FragmentDialerBinding
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_DTMF, DTMF_VOLUME)
    private var dialedNumber : String get() {
        return binding.dialedNumberTextView.text.toString()
    } set(value) {
        binding.dialedNumberTextView.text = value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = DialerType.valueOf(it.getString(ARG_TYPE)!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDialerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBindings()
    }

    private fun setBindings() = binding.run {
        buttonDismiss.setOnClickListener{ dismiss() }
        buttonCall.visibility = if(type == DialerType.PHONE_NUMBER) View.VISIBLE else View.GONE
        buttonCall.setOnClickListener { makeCall() }
        listOf(button1, button2, button3, button4, button5, button6, button7, button8, button9, buttonStar, button0, buttonPound)
            .forEach { button ->
                button.setOnClickListener {
                    appendDigit(button.text.toString())
                    if(type == DialerType.DTMF)
                        sendDtmf(button.text.toString())
                }
            }
        button0.setOnLongClickListener { appendDigit("+"); true }
        buttonBackspace.apply {
            setOnClickListener{ backspace() }
            setOnLongClickListener{ clearNumber(); true }
        }
    }

    private fun appendDigit(digit: String){
        dialedNumber += digit
    }

    private fun backspace(){
        dialedNumber = dialedNumber.dropLast(1)
    }

    private fun clearNumber(){
        dialedNumber = ""
    }

    private fun makeCall(){
        val callContext = dialedNumber.takeUnless { it.isEmpty() }?.let {
            mapOf(
                Constants.CONTEXT_KEY_RECIPIENT to it,
                Constants.CONTEXT_KEY_TYPE to Constants.PSTN_TYPE
            )
        }
        clientManager.startOutboundCall(callContext)
    }

    private fun sendDtmf(digit: String){
        val toneType = when(digit){
            getString(R.string.dialer_btn_zero) -> ToneGenerator.TONE_DTMF_0
            getString(R.string.dialer_btn_one) -> ToneGenerator.TONE_DTMF_1
            getString(R.string.dialer_btn_two) -> ToneGenerator.TONE_DTMF_2
            getString(R.string.dialer_btn_three) -> ToneGenerator.TONE_DTMF_3
            getString(R.string.dialer_btn_four) -> ToneGenerator.TONE_DTMF_4
            getString(R.string.dialer_btn_five) -> ToneGenerator.TONE_DTMF_5
            getString(R.string.dialer_btn_six) -> ToneGenerator.TONE_DTMF_6
            getString(R.string.dialer_btn_seven) -> ToneGenerator.TONE_DTMF_7
            getString(R.string.dialer_btn_eight) -> ToneGenerator.TONE_DTMF_8
            getString(R.string.dialer_btn_nine) -> ToneGenerator.TONE_DTMF_9
            getString(R.string.dialer_btn_pound) -> ToneGenerator.TONE_DTMF_P
            getString(R.string.dialer_btn_star) -> ToneGenerator.TONE_DTMF_S
            else -> null
        } ?: return
        toneGenerator.startTone(toneType, DTMF_DURATION)
        App.coreContext.activeCall?.let {
            clientManager.sendDtmf(it, digit)
        }
    }

    companion object {
        private const val DTMF_VOLUME = 100
        private const val DTMF_DURATION = 100
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param type Dialer Type.
         * @return A new instance of fragment DialerFragment.
         */
        @JvmStatic
        fun newInstance(type: DialerType) =
            DialerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type.name)
                }
            }
    }
}
