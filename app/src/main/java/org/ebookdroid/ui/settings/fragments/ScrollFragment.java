package org.ebookdroid.ui.settings.fragments;

import nb.cblink.readbook.R;

import android.annotation.TargetApi;

@TargetApi(11)
public class ScrollFragment extends BasePreferenceFragment {

    public ScrollFragment() {
        super(R.xml.fragment_scroll);
    }

    @Override
    public void decorate() {
        super.decorate();
        decorator.decorateScrollSettings();
    }

}
