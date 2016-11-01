package net.samclarke.android.habittracker.ui.pickers;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.CheckBox;

import net.samclarke.android.habittracker.R;

class DayButton extends CheckBox {
    private int mSelectedTextColor;
    private int mTextColor;

    public DayButton(Context context) {
        this(context, null);
    }

    public DayButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int padding = (int) getResources().getDimension(R.dimen.day_button_padding);
        int size = (int) getResources().getDimension(R.dimen.day_button_size);
//
//        mSelectedTextColor = ContextCompat.getColor(context, R.color.day_button_size);
//        mTextColor = ContextCompat.getColor(context, R.color.);

//        setTextColor(getContext().getColor(android.R.color.primary_text_dark));
//        TextViewCompat.setTextAppearance(this, android.R.style.TextAppearance_Material_Body1);
        setWidth(size);
        setHeight(size);
        setGravity(Gravity.CENTER);
        setClickable(true);
        setButtonDrawable(null);
        setBackgroundResource(R.drawable.day_selector_button);
        setPadding(padding, padding, padding, padding);
    }
}
