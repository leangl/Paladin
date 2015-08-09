package mobi.tattu.utils.views.preference;


import mobi.tattu.utils.Tattu;

public class MultiStateSeekBarPrefListener extends SeekBarPreference.SeekBarPreferenceListener {

	private String[] summaryValues;
	private int strResId;

	/**
	 * Construct a change listener for the specified widget.
	 */
	public MultiStateSeekBarPrefListener(String[] values) {
		this(0, values);
	}

	/**
	 * Construct a change listener for the specified widget.
	 * 
	 *            SeekBarPreference object
	 * @param strResId
	 *            string resource id that takes a integer argument
	 */
	public MultiStateSeekBarPrefListener(int strResId, String[] values) {
		this.summaryValues = values;
		this.strResId = strResId;
	}

	/**
	 * Convert integer progress to summary string.
	 * 
	 * @param newValue
	 *            should be an Integer instance
	 */
	@Override
	public String toSummary(SeekBarPreference pref, int newValue, int newProgress) {
		int idx = (int) (newProgress / Math.ceil((float) pref.getLength() / summaryValues.length));
		if (idx >= summaryValues.length)
			idx--;
		return strResId != 0 ? Tattu.context.getString(strResId, summaryValues[idx]) : summaryValues[idx];
	}

	/**
	 * @return the summaryValues
	 */
	public String[] getSummaryValues() {
		return summaryValues;
	}

	/**
	 * @param summaryValues
	 *            the summaryValues to set
	 */
	public void setSummaryValues(String[] summaryValues) {
		this.summaryValues = summaryValues;
	}

}
