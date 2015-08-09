package mobi.tattu.utils.views.preference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.TypedPref;
import mobi.tattu.utils.Utils;

public class MultiSelectPreference extends ExtendedListPreference {
	
	private boolean[] mClickedDialogEntryIndex;
	private MultiSelectPreferenceListener mMultiSelectPreferenceListener;
	
	public MultiSelectPreference(Context context) {
		this(context, null);
	}
	
	public MultiSelectPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mClickedDialogEntryIndex = new boolean[getEntries().length];

	}
	@Override
	public void setEntries(CharSequence[] entries) {
		super.setEntries(entries);
		mClickedDialogEntryIndex = new boolean[entries.length];
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		CharSequence[] entries = getEntries();
		CharSequence[] entryValues = getEntryValues();
		if (entries == null || entryValues == null || entries.length != entryValues.length) {
			throw new IllegalStateException(
					"MultiSelectPreference requires an entries array and an entryValues array which are both the same length");
		}
		mClickedDialogEntryIndex = new boolean[entries.length];
		restoreCheckedEntries();
		builder.setMultiChoiceItems(entries, mClickedDialogEntryIndex,
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						boolean callbackResult = isChecked;
						if (mMultiSelectPreferenceListener != null) {
							callbackResult = mMultiSelectPreferenceListener.onClick(MultiSelectPreference.this, getKey(), getEntryValues()[which],
									isChecked);
						}
						((AlertDialog) dialog).getListView().setItemChecked(which, callbackResult);
						mClickedDialogEntryIndex[which] = callbackResult;
					}
				});
	}
	
	private String[] parseStoredValue(CharSequence val) {
		if ("".equals(val)) {
			return null;
		} else {
			return splitMultiValue(val.toString());
		}
	}

	private void restoreCheckedEntries() {
		CharSequence[] entryValues = getEntryValues();
		
		String[] vals = parseStoredValue(getValue());
		
		if (vals != null) {
			List<String> valuesList = Arrays.asList(vals);
			for (int i = 0; i < entryValues.length; i++) {
				CharSequence entry = entryValues[i];
				if (valuesList.contains(entry)) {
					mClickedDialogEntryIndex[i] = true;
				}
			}
		}
	}



	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(mMultiSelectPreferenceListener != null){
			mMultiSelectPreferenceListener.onDialogClosedd(positiveResult,mClickedDialogEntryIndex);
		}
			ArrayList<String> values = new ArrayList<String>();
			CharSequence[] entryValues = getEntryValues();
			if (positiveResult && entryValues != null) {
				for (int i = 0; i < entryValues.length; i++) {
					if (mClickedDialogEntryIndex[i] == true) {
						values.add((String) entryValues[i]);
					}
				}
				if (callChangeListener(values)) {
					setValue(Utils.joinMultiValue(values));
					setSummary(Utils.joinMultiValueByComma(values));
				}
			}

	}
	
	public void setOnMultiSelectPreferenceListener(MultiSelectPreferenceListener listener) {
		super.setProPreferenceListener(listener);
		this.mMultiSelectPreferenceListener = listener;
	}
	
	public static interface MultiSelectPreferenceListener extends ProPreferenceListener {
		public boolean onClick(MultiSelectPreference preference, String preferenceKey, CharSequence multiPreferenceKey, boolean isChecked);
		/**
		 *
		 * @param positiveResult Whether the positive button was clicked (true), or
		 *            the negative button was clicked or the dialog was canceled (false).
		 * @param clickedDialogEntryIndex tiene la posicion de la opcion seleccionada
		 */
		public void onDialogClosedd(boolean positiveResult,boolean [] clickedDialogEntryIndex );
	}
	
	public static String[] splitMultiValue(String values) {
		return values.split("\\" + Utils.MULTIVALUE_PREFERENCE_SEPARATOR);
	}
	
	public static boolean containsPreference(int resId, TypedPref<String> preference) {
		return containsPreference(Tattu.context.getString(resId), preference);
	}
	
	public static boolean containsPreference(String value, TypedPref<String> preference) {
		String multiValuePreference = preference.getValue();
		String[] vals = splitMultiValue(multiValuePreference);
		for (int i = 0; i < vals.length; i++) {
			if (vals[i].equals(value)) {
				return true;
			}
		}
		return false;
	}
	

	
}
