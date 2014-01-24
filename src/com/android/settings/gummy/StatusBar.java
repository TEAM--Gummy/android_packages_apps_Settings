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

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.util.gummy.DeviceUtils;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarSettings";

    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String KEY_NOTIFICATIONS_CATAGORY = "notifications";
    private static final String KEY_STYLE_CATAGORY = "status_bar_style";
    private static final String KEY_MISSED_CALL_BREATH = "missed_call_breath";
    private static final String KEY_MMS_BREATH = "mms_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";
    private static final String STATUSBAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUSBAR_SIGNAL_TEXT_COLOR = "status_bar_signal_color";

    private PreferenceScreen mClockStyle;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private PreferenceCategory mNotificationsCatagory;
    private PreferenceCategory mStyleCatagory;
    private CheckBoxPreference mMissedCallBreath;
    private CheckBoxPreference mMMSBreath;
    private CheckBoxPreference mVoicemailBreath;
    private CheckBoxPreference mStatusBarNotifCount;
    private ListPreference mStatusBarSignal;
    private ColorPickerPreference mStatusBarSignalColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mNotificationsCatagory = (PreferenceCategory) prefSet.findPreference(KEY_NOTIFICATIONS_CATAGORY);
        mStyleCatagory = (PreferenceCategory) prefSet.findPreference(KEY_STYLE_CATAGORY);

        // Start observing for changes on auto brightness
        StatusBarBrightnessChangedObserver statusBarBrightnessChangedObserver =
            new StatusBarBrightnessChangedObserver(new Handler());
        statusBarBrightnessChangedObserver.startObserving();

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);
        if (mClockStyle != null) {
            updateClockStyleDescription();
        }

        mStatusBarBrightnessControl =
            (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        mMissedCallBreath = (CheckBoxPreference) findPreference(KEY_MISSED_CALL_BREATH);
            mMissedCallBreath.setOnPreferenceChangeListener(this);

        mMMSBreath = (CheckBoxPreference) findPreference(KEY_MMS_BREATH);
            mMMSBreath.setOnPreferenceChangeListener(this);

        mVoicemailBreath = (CheckBoxPreference) findPreference(VOICEMAIL_BREATH);
            mVoicemailBreath.setOnPreferenceChangeListener(this);

        mStatusBarNotifCount = (CheckBoxPreference) findPreference(STATUSBAR_NOTIF_COUNT);
            mStatusBarNotifCount.setOnPreferenceChangeListener(this);

        mStatusBarSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);
        int signalStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUSBAR_SIGNAL_TEXT, 0);
        mStatusBarSignal.setValue(String.valueOf(signalStyle));
        mStatusBarSignal.setSummary(mStatusBarSignal.getEntry());
        mStatusBarSignal.setOnPreferenceChangeListener(this);

        mStatusBarSignalColor = (ColorPickerPreference) findPreference(STATUSBAR_SIGNAL_TEXT_COLOR);
        mStatusBarSignalColor.setOnPreferenceChangeListener(this);
        int defaultColor = getResources().getColor(
                com.android.internal.R.color.white);
        int intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, defaultColor);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarSignalColor.setSummary(hexColor);

        isTelephony();

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarBrightnessControl) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mMissedCallBreath) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.MISSED_CALL_BREATH,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;
        } else if (preference == mMMSBreath) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.MMS_BREATH,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;
        } else if (preference == mVoicemailBreath) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.VOICEMAIL_BREATH,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;
        } else if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.STATUSBAR_NOTIF_COUNT,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;
        } else if (preference == mStatusBarSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT, signalStyle);
            mStatusBarSignal.setSummary(mStatusBarSignal.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarSignalColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
        updateStatusBarBrightnessControl();
    }

    private void updateStatusBarBrightnessControl() {
        try {
            if (mStatusBarBrightnessControl != null) {
                int mode = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    mStatusBarBrightnessControl.setEnabled(false);
                    mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
                } else {
                    mStatusBarBrightnessControl.setEnabled(true);
                    mStatusBarBrightnessControl.setSummary(
                        R.string.status_bar_toggle_brightness_summary);
                }
            }
        } catch (SettingNotFoundException e) {
        }
    }

    private void updateClockStyleDescription() {
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.enabled));
        } else {
            mClockStyle.setSummary(getString(R.string.disabled));
         }
    }

    private class StatusBarBrightnessChangedObserver extends ContentObserver {
        public StatusBarBrightnessChangedObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateStatusBarBrightnessControl();
        }

        public void startObserving() {
            getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                    false, this);
        }
    }

    private void isTelephony() {
        // Determine options based on device telephony support
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            // No telephony, remove dependent options
            mNotificationsCatagory.removePreference(findPreference(KEY_MISSED_CALL_BREATH));
            mNotificationsCatagory.removePreference(findPreference(KEY_MMS_BREATH));
            mStyleCatagory.removePreference(findPreference(STATUS_BAR_SIGNAL));
            mStyleCatagory.removePreference(findPreference(STATUSBAR_SIGNAL_TEXT_COLOR));
        }
    }

}
