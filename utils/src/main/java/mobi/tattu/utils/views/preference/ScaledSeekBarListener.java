package mobi.tattu.utils.views.preference;


import mobi.tattu.utils.Tattu;

public class ScaledSeekBarListener extends SeekBarPreference.SeekBarPreferenceListener {
	
	private final int mStrResId;
	private final int mMinResId;
	private final int mMaxResId;
	private final int mScale;
	
	public ScaledSeekBarListener() {
		this(0, 0, 0, 1);
	}
	
	public ScaledSeekBarListener(int strResId, int minResId, int scale) {
		this(strResId, minResId, 0, scale);
	}
	
	public ScaledSeekBarListener(int strResId, int minResId, int maxResId, int scale) {
		mStrResId = strResId;
		mMinResId = minResId;
		mMaxResId = maxResId;
		mScale = scale;
	}
	
	@Override
	public String toSummary(SeekBarPreference pref, int newValue, int newProgress) {
		if (newValue == pref.getMin() && mMinResId != 0) {
			return Tattu.context.getString(mMinResId);
		} else if (newValue == pref.getMax() && mMaxResId != 0) {
			return Tattu.context.getString(mMaxResId);
		}
		int scaledValue = newValue * mScale;
		return mStrResId != 0 ? Tattu.context.getString(mStrResId, scaledValue) : String.valueOf(scaledValue);
	}
}
