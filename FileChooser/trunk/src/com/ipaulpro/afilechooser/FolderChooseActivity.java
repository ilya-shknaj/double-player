package com.ipaulpro.afilechooser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;

public class FolderChooseActivity extends FragmentActivity implements FileListFragment.OnFolderSelectedListener {

	private static final String EXTERNAL_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

	public static final String EXTRA_START_PATH = "EXTRA_START_PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		String startPath = getIntent().getStringExtra(EXTRA_START_PATH);
		if (startPath == null) {
			startPath = EXTERNAL_BASE_PATH;
		}

		addFragment(startPath);
		setTitle(startPath);

	}

	private void addFragment(String path) {
		FileListFragment explorerFragment = FileListFragment.newInstance(path, false, false,false);
		explorerFragment.setOnFolderSelectedListener(this);
		getSupportFragmentManager().beginTransaction().add(R.id.explorer_fragment, explorerFragment).commit();
	}

	@Override
	public void onFolderSelected(String tag, String folderPath) {
		setResult(RESULT_OK, new Intent().setData(Uri.parse(folderPath)));
		finish();
	}

}
