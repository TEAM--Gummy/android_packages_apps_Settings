/*
 * Copyright (C) 2013 Gummy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.gummy;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Collections;

public class About extends SettingsPreferenceFragment {

    public static final String TAG = "About";

    private static final String GUMMY_REVIEW = "http://review.gummyrom.com/#/q/status:open,n,z";
    private static final String JENKINS = "http://jenkins.gummyrom.com/";

    Preference mReviewUrl;
    Preference mJenkins;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.about_Gummy);
        addPreferencesFromResource(R.xml.prefs_about);

        mReviewUrl = findPreference("gummy_gerrit");
        mJenkins = findPreference("jenkins");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mReviewUrl) {
            launchUrl(GUMMY_REVIEW);
            return true;
        } else if (preference == mJenkins) {
            launchUrl(JENKINS);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent whatever = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(whatever);
    }
}
