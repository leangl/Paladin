package mobi.tattu.utils.views.preference;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class ExtendedEditTextPreference extends EditTextPreference {
	
	private ProPreferenceListener mProListener;
	
	public ExtendedEditTextPreference(Context context) {
		this(context, null);
	}
	
	public ExtendedEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
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
