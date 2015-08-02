package mobi.tattu.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;


import mobi.tattu.utils.Utils;
import mobi.tattu.utils.log.Logger;

public class ImageFileSystemFetcher extends ImageResizer {

	/**
	 * Initialize providing a target image width and height for the processing images.
	 * 
	 * @param context
	 * @param imageWidth
	 * @param imageHeight
	 */
	public ImageFileSystemFetcher(Context context, int imageWidth, int imageHeight) {
		super(context, imageWidth, imageHeight);
		init(context);
	}

	/**
	 * Initialize providing a single target image size (used for both width and height);
	 * 
	 * @param context
	 * @param imageSize
	 */
	public ImageFileSystemFetcher(Context context, int imageSize) {
		super(context, imageSize);
		init(context);
	}

	private void init(Context context) {
	}

	@Override
	protected void flushCacheInternal() {
		super.flushCacheInternal();
	}

	@Override
	protected void closeCacheInternal() {
		super.closeCacheInternal();
	}

	@Override
	protected Bitmap processBitmap(Object data) {
		if (Utils.isDebug()) Logger.d(this, "processBitmap - " + data);
		File file = (File) data;
		FileInputStream fileInputStream = null;
		FileDescriptor fileDescriptor = null;
		try {
			fileInputStream = new FileInputStream(file);
			fileDescriptor = fileInputStream.getFD();
		} catch (Exception e) {
			Logger.e(this, e.getMessage(), e);
		}

		Bitmap bitmap = null;
		if (fileDescriptor != null) {
			if (file != null && file.exists()) {
				try {
					bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth,
							mImageHeight, getImageCache());
				} catch (Exception ex) {
					Logger.w(this, ex.getMessage(), ex);
				}
			}
		}
		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (IOException e) {
			}
		}

		return bitmap;

	}

	/**
	 * Get the orientation in degrees from file EXIF information. Idea and code from http://stackoverflow.com/a/11081918/527759
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	@Override
	protected int getOrientationInDegrees(Object data) throws IOException {
		File file = (File) data;
		ExifInterface exif = new ExifInterface(file.getAbsolutePath());
		int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		int rotationInDegrees = exifToDegrees(rotation);
		return rotationInDegrees;
	}

	/**
	 * Convert exif orientation information to degrees. Idea and code taken from http://stackoverflow.com/a/11081918/527759
	 * 
	 * @param exifOrientation
	 * @return
	 */
	private static int exifToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

}
