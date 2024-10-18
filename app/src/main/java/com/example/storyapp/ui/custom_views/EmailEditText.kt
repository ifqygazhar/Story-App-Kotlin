package com.example.storyapp.ui.custom_views

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewParent
import com.example.storyapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EmailEditTextMaterial @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputEditText(context, attrs, defStyleAttr) {

    private var isInitialized = false

    init {
        setupView()
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val parentTextInputLayout = findParentTextInputLayout()
                if (!isInitialized) {
                    return
                }

                if (s.isNullOrEmpty()) {
                    parentTextInputLayout?.error = context.getString(R.string.error_empty_email)
                } else if (!isValidEmail(s)) {
                    parentTextInputLayout?.error = context.getString(R.string.validationEmail)
                } else {
                    parentTextInputLayout?.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupView() {
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isEnabled = true
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun findParentTextInputLayout(): TextInputLayout? {
        var parentView: ViewParent? = parent
        while (parentView != null) {
            if (parentView is TextInputLayout) {
                return parentView
            }
            parentView = (parentView as? View)?.parent
        }
        return null
    }

    private fun isValidEmail(email: CharSequence?): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInitialized = true
    }
}
