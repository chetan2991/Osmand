package net.osmand.plus.wikivoyage.explore;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import net.osmand.PicassoUtils;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.WikivoyageShowImages;
import net.osmand.plus.R;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithDescription;
import net.osmand.plus.base.bottomsheetmenu.SimpleBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.DividerHalfItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.wikivoyage.data.TravelDbHelper;
import net.osmand.plus.wikivoyage.data.TravelLocalDataHelper;

import java.io.File;
import java.util.List;

public class WikivoyageOptionsBottomSheetDialogFragment extends MenuBottomSheetDialogFragment {

	public final static String TAG = "WikivoyageOptionsBottomSheetDialogFragment";

	public static final int REQUEST_CODE = 0;
	public static final int DOWNLOAD_IMAGES_CHANGED = 1;
	public static final int CACHE_CLEARED = 2;

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		final OsmandApplication app = getMyApplication();
		final OsmandSettings.CommonPreference<WikivoyageShowImages> showImagesPref = app.getSettings().WIKIVOYAGE_SHOW_IMAGES;
		final TravelDbHelper dbHelper = app.getTravelDbHelper();

		items.add(new TitleItem(getString(R.string.shared_string_options)));

		if (dbHelper.getExistingTravelBooks().size() > 1) {
			BaseBottomSheetItem selectTravelBook = new BottomSheetItemWithDescription.Builder()
					.setDescription(dbHelper.formatTravelBookName(dbHelper.getSelectedTravelBook()))
					.setDescriptionColorId(nightMode ? R.color.wikivoyage_active_dark : R.color.wikivoyage_active_light)
					.setTitle(getString(R.string.shared_string_travel_book))
					.setLayoutId(R.layout.bottom_sheet_item_with_right_descr)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							selectTravelBookDialog();
							dismiss();
						}
					})
					.create();
			items.add(selectTravelBook);
		}

		BaseBottomSheetItem showImagesItem = new BottomSheetItemWithDescription.Builder()
				.setDescription(getString(showImagesPref.get().name))
				.setDescriptionColorId(nightMode ? R.color.wikivoyage_active_dark : R.color.wikivoyage_active_light)
				.setIcon(getContentIcon(R.drawable.ic_type_img))
				.setTitle(getString(R.string.download_images))
				.setLayoutId(R.layout.bottom_sheet_item_with_right_descr)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.END);
						for (final WikivoyageShowImages showImages : WikivoyageShowImages.values()) {
							MenuItem item = popup.getMenu().add(getString(showImages.name));
							item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
								@Override
								public boolean onMenuItemClick(MenuItem item) {
									showImagesPref.set(showImages);
									sendResult(DOWNLOAD_IMAGES_CHANGED);
									dismiss();
									return true;
								}
							});
						}
						popup.show();
					}
				})
				.create();
		items.add(showImagesItem);

		BaseBottomSheetItem clearCacheItem = new BottomSheetItemWithDescription.Builder()
				.setDescription(getString(R.string.shared_string_clear))
				.setDescriptionColorId(nightMode ? R.color.wikivoyage_active_dark : R.color.wikivoyage_active_light)
				.setTitle(getString(R.string.images_cache))
				.setLayoutId(R.layout.bottom_sheet_item_with_right_descr)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new WebView(getContext()).clearCache(true);
						PicassoUtils.clearAllPicassoCache();
						sendResult(CACHE_CLEARED);
						dismiss();
					}
				})
				.create();
		items.add(clearCacheItem);

		items.add(new DividerHalfItem(getContext()));

		BaseBottomSheetItem clearHistoryItem = new SimpleBottomSheetItem.Builder()
				.setIcon(getContentIcon(R.drawable.ic_action_history))
				.setTitle(getString(R.string.delete_search_history))
				.setLayoutId(R.layout.bottom_sheet_item_simple)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						TravelLocalDataHelper ldh = getMyApplication().getTravelDbHelper().getLocalDataHelper();
						ldh.clearHistory();
						dismiss();
					}
				})
				.create();
		items.add(clearHistoryItem);
	}

	private void sendResult(int resultCode) {
		Fragment fragment = getTargetFragment();
		if (fragment != null) {
			fragment.onActivityResult(getTargetRequestCode(), resultCode, null);
		}
	}

	private void selectTravelBookDialog() {
		Context ctx = getContext();
		if (ctx == null) {
			return;
		}

		final TravelDbHelper dbHelper = getMyApplication().getTravelDbHelper();
		final List<File> list = dbHelper.getExistingTravelBooks();
		String[] ls = new String[list.size()];
		for (int i = 0; i < ls.length; i++) {
			ls[i] = dbHelper.formatTravelBookName(list.get(i));
		}

		new AlertDialog.Builder(ctx)
				.setTitle(R.string.select_travel_book)
				.setItems(ls, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dbHelper.selectTravelBook(list.get(which));
					}
				})
				.setNegativeButton(R.string.shared_string_dismiss, null)
				.show();
	}
}
