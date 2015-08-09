package mobi.tattu.utils.views.preference;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;



public class ExtendedListPreference extends ListPreference {
	
	private ProPreferenceListener mProListener;
	
	public ExtendedListPreference(Context context) {
		this(context, null);
	}
	
	public ExtendedListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference pref, Object val) {
				CharSequence[] entryValues = getEntryValues();
				CharSequence[] entries = getEntries();
				for (int i = 0; i < entryValues.length; i++) {
					if (entryValues[i].equals(val)) {
						pref.setSummary(entries[i]);
					}
				}
				return true;
			}
		});
	}

	public void setProPreferenceListener(ProPreferenceListener listener) {
		this.mProListener = listener;
	}
	
	/* (non-Javadoc)
	 * @see android.preference.DialogPreference#showDialog(android.os.Bundle)
	 */
	@Override
	protected void showDialog(Bundle state) {
		if (mProListener == null || mProListener.isPreferenceEnabled(this)) {
			super.showDialog(state);
		}
	}
	
}
