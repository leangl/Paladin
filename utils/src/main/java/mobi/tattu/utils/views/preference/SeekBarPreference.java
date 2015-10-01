package mobi.tattu.utils.views.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import mobi.tattu.utils.R;
import mobi.tattu.utils.Utils;
import mobi.tattu.utils.log.Logger;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

	private SeekBar seekbar;
	private TextView summary;
	private int max = 100;
	private int min = 0;
	private boolean inverted;
	private int value;
	private boolean discard;

	/**
	 * Perform inflation from XML and apply a class-specific base style.
	 * 
	 * @param context
	 *            The Context this is associated with, through which it can access the current theme, resources,
	 *            {@link android.content.SharedPreferences}, etc.
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the preference.
	 * @param defStyle
	 *            The default style to apply to this preference. If 0, no style will be applied (beyond what is included in the theme). This
	 *            may either be an attribute resource, whose value will be retrieved from the current theme, or an explicit style resource.
	 * @see #SeekBarPreference(Context, AttributeSet)
	 */
	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Constructor that is called when inflating a Preference from XML.
	 * 
	 * @param context
	 *            The Context this is associated with, through which it can access the current theme, resources,
	 *            {@link android.content.SharedPreferences}, etc.
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the preference.
	 * @see #SeekBarPreference(Context, AttributeSet, int)
	 */
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructor to create a Preference.
	 * 
	 * @param context
	 *            The Context in which to store Preference values.
	 */
	public SeekBarPreference(Context context) {
		super(context);
	}

	/**
	 * Create progress bar and other view contents.
	 */
	protected View onCreateView(ViewGroup p) {

		final Context ctx = getContext();

		LinearLayout layout = new LinearLayout(ctx);
		layout.setId(android.R.id.widget_frame);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(15, 10, 15, 10);

		TextView title = new TextView(ctx);
		int textColor = ctx.getResources().getColor(R.color.seekbar_preference_title_color);
		title.setId(android.R.id.title);
		title.setSingleLine();
		title.setTextAppearance(ctx, android.R.style.TextAppearance_Medium);
		title.setTextColor(textColor);
		layout.addView(title);

		seekbar = new SeekBar(ctx);
		seekbar.setId(android.R.id.progress);
		recalcSeekBarMax();
		seekbar.setOnSeekBarChangeListener(this);
		layout.addView(seekbar);

		summary = new TextView(ctx);
		summary.setId(android.R.id.summary);
		summary.setTextAppearance(ctx, android.R.style.TextAppearance_Small);
		summary.setTextColor(R.color.seekbar_preference_summary_color);
		layout.addView(summary);

		return layout;
	}

	/**
	 * Binds the created View to the data for this Preference.
	 */
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		if (seekbar != null) {
			seekbar.setProgress(getProgressFromValue(this.value));
		}
	}

	public void persistValue(int progress) {
		int newValue = getValueFromProgress(progress);
		if (value != newValue) {
			value = newValue;
			if (Utils.isDebug()) {
				Logger.d(this, "Seekbar progress: " + progress);
				Logger.d(this, "Seekbar persisted value: " + value);
			}
			persistInt(value);

			notifyDependencyChange(shouldDisableDependents());
			notifyChanged();
		}
	}

	private int getProgressFromValue(int value) {
		if (inverted) {
			return max - value;
		} else {
			return value - min;
		}
	}

	private int getValueFromProgress(int progress) {
		int value = progress + min;
		if (inverted) {
			value = max - value + min;
		}
		return value;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	public int getProgress() {
		return getProgressFromValue(getValue());
	}

	/**
	 * @return the min
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Set the max value for the <code>SeekBar</code> object.
	 * 
	 * @param max
	 *            max value
	 */
	public void setMax(int max) {
		this.max = max;
		recalcSeekBarMax();
	}

	public void recalcSeekBarMax() {
		if (seekbar != null) {
			seekbar.setMax(this.max - this.min);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		persistValue(getProgressFromValue(restoreValue ? getPersistedInt(value) : (Integer) defaultValue));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldDisableDependents() {
		return getProgressFromValue(value) == 0 || super.shouldDisableDependents();
	}

	/**
	 * Set the progress of the preference.
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		discard = !callChangeListener(getValueFromProgress(progress));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		discard = false;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (discard) {
			seekBar.setProgress(getProgressFromValue(this.value));
		} else {
			persistValue(seekBar.getProgress());
			OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
			if (listener instanceof SeekBarPreferenceListener) {
				setSummary(((SeekBarPreferenceListener) listener).toSummary(this, getValue(), getProgress()));
			}
		}
	}

	/**
	 * @return the min
	 */
	public int getMin() {
		return min;
	}

	/**
	 * @param min
	 *            the min to set
	 */
	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * @return the inverted
	 */
	public boolean isInverted() {
		return inverted;
	}

	/**
	 * @param inverted
	 *            the inverted to set
	 */
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	/**
	 * 
	 * @return
	 */
	public int getLength() {
		return max - min;
	}

	public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
		super.setOnPreferenceChangeListener(onPreferenceChangeListener);
		setSummary(((SeekBarPreferenceListener) onPreferenceChangeListener).toSummary(this, getValue(), getProgress()));
	}

	/**
	 * Abstract seek bar summary updater.
	 * 
	 */
	public static abstract class SeekBarPreferenceListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference instanceof SeekBarPreference) {
				SeekBarPreference pref = (SeekBarPreference) preference;
				if (newValue instanceof Integer) {
					pref.summary.setText(toSummary(pref, (Integer) newValue, pref.getProgressFromValue((Integer) newValue)));
				} else {
					Logger.e(this, "SeekBar value not integer: " + newValue);
				}
			} else {
				Logger.e(this, "SeekBar value not a SeekBarPreference: " + preference);
			}
			return true;
		}

		/**
		 * Convert integer value to summary string.
		 * 
		 * @param newValue
		 *            should be an Integer instance
		 */
		public abstract String toSummary(SeekBarPreference pref, int newValue, int newProgress);

	}

}
