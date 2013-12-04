/* 
 * Copyright (C) 2012 Paul Burke
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

package com.ipaulpro.afilechooser;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment that displays a list of Files in a given path.
 * 
 * @version 2012-10-28
 * 
 * @author paulburke (ipaulpro)
 * 
 */
public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<File>>, OnBackClickListener {

	private static final int LOADER_ID = 0;

	private FileListAdapter mAdapter;
	private String mPath;

	public ListView mList;
	private boolean mListShown;
	private View mProgressContainer;
	private View mListContainer;

	private TextView currentFilePath;

	private static final String PATH_ARG = "PATH_ARG";

	private static final String SELECT_FILE_ARG = "SELECT_FILE_ARG";

	private static final String LOCK_DIRECTORY_ARG = "LOCK_DIRECTORY_ARG";

	private static final String ADD_TRANPARENT_ARG = "ADD_TRANPARENT_ARG";

	private static final String SLASH_STRING = "/";

	private static final String ROOT_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath();

	private OnFileSelectedListener onFileSelectedListener;
	private OnFolderSelectedListener onFolderSelectedListener;

	/**
	 * Create a new instance with the given file path.
	 * 
	 * @param path
	 *            The absolute path of the file (directory) to display.
	 * @return A new Fragment with the given file path.
	 */
	public static FileListFragment newInstance(String path, boolean selectFile, boolean lockDirectory, boolean addTransparent) {

		FileListFragment fragment = new FileListFragment();
		Bundle args = new Bundle();
		args.putString(PATH_ARG, path);
		args.putBoolean(SELECT_FILE_ARG, selectFile);
		args.putBoolean(LOCK_DIRECTORY_ARG, lockDirectory);
		args.putBoolean(ADD_TRANPARENT_ARG, addTransparent);
		fragment.setArguments(args);

		return fragment;
	}

	public static interface OnFileSelectedListener {
		void onFileSelected(String tag, String filePath);

	}

	public static interface OnFolderSelectedListener {
		void onFolderSelected(String tag, String folderPath);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mAdapter = new FileListAdapter(getActivity(), !getArguments().getBoolean(LOCK_DIRECTORY_ARG));

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (getArguments().getBoolean(LOCK_DIRECTORY_ARG)) {
			setEmptyText(getString(R.string.empty_content));
		} else {
			setEmptyText(getString(R.string.empty_directory));
		}
		setListAdapter(mAdapter);
		setListShown(false);

		super.onActivityCreated(savedInstanceState);

		currentFilePath = (TextView) getView().findViewById(R.id.currentPath);
		String path = getArguments().getString(PATH_ARG) != null ? getArguments().getString(PATH_ARG) : ROOT_FOLDER;
		setPath(path);

		View currentDirectoryButton = getView().findViewById(R.id.currentDirectoryButton);

		if (!getArguments().getBoolean(SELECT_FILE_ARG, true)) {
			currentDirectoryButton.setVisibility(View.VISIBLE);
			currentDirectoryButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (onFolderSelectedListener != null) {
						onFolderSelectedListener.onFolderSelected(getTag(), mPath);
					}
				}
			});
		}

		View backButton = getView().findViewById(R.id.backButton);

		if (getArguments().getBoolean(LOCK_DIRECTORY_ARG, false)) {
			backButton.setVisibility(View.GONE);
			currentFilePath.setVisibility(View.GONE);

		} else {

			backButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onBackClick();
				}
			});
		}

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int position, long id) {
				onListItemLongClick(paramView, position);
				return false;
			}
		});

		getLoaderManager().initLoader(LOADER_ID, null, this);

	}

	@Override
	public void setListShown(boolean shown) {
		setListShown(shown, true);
	}

	@Override
	public void setListShownNoAnimation(boolean shown) {
		setListShown(shown, false);
	}

	public void setListShown(boolean shown, boolean animate) {
		if (mListShown == shown) {
			return;
		}
		mListShown = shown;
		if (shown) {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
			}
			mProgressContainer.setVisibility(View.GONE);
			mListContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
			}
			mProgressContainer.setVisibility(View.VISIBLE);
			mListContainer.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		FileListAdapter adapter = (FileListAdapter) l.getAdapter();
		if (adapter != null) {
			File file = (File) adapter.getItem(position);
			if (file != null && file.isDirectory()) {
				setPath(file.getAbsolutePath());
				getLoaderManager().restartLoader(0, null, this);
			} else {
				if (onFileSelectedListener != null) {
					onFileSelectedListener.onFileSelected(getTag(), "file://" + file.getAbsoluteFile().toString());
				}
			}
		}
	}

	public void onListItemLongClick(View view, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.select_action));
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
		arrayAdapter.add(getString(R.string.delete));
		builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, int which) {
				String strName = arrayAdapter.getItem(which);
				if (strName.equals(getString(R.string.delete))) {
					AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
					final File selectedFile = (File) getListView().getAdapter().getItem(position);
					builderInner.setMessage(String.format(getString(R.string.file_would_deleted), selectedFile.getName()));
					builderInner.setTitle(getString(R.string.delete_alarm_message));
					builderInner.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							selectedFile.delete();
							dialog.dismiss();
						}
					});
					builderInner.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface paramDialogInterface, int paramInt) {
							dialog.dismiss();
						}
					});
					builderInner.show();
				}
			}
		});
		builder.show();
	}

	@Override
	public Loader<List<File>> onCreateLoader(int id, Bundle args) {

		return new FileLoader(getActivity(), mPath);
	}

	@Override
	public void onLoadFinished(Loader<List<File>> loader, List<File> data) {

		mAdapter.setListItems(data);

		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}

	}

	@Override
	public void onLoaderReset(Loader<List<File>> loader) {

		mAdapter.clear();
	}

	private void setPath(String path) {
		mPath = path;
		currentFilePath.setText(path);
	}

	@Override
	public void onBackClick() {
		String path = getUpLevelPath();
		if (path != null) {
			setPath(path);
			getLoaderManager().restartLoader(0, null, this);
		} else if (!getArguments().getBoolean(SELECT_FILE_ARG, true)) {
			getActivity().finish();
		}
	}

	private String getUpLevelPath() {
		String result = mPath;
		if (result.equals(ROOT_FOLDER)) {
			return null;
		}
		int index = result.lastIndexOf(SLASH_STRING);
		if (index != -1) {
			return result.substring(0, index);
		}

		return null;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int INTERNAL_EMPTY_ID = 0x00ff0001;
		View root = inflater.inflate(R.layout.chooser, container, false);
		(root.findViewById(R.id.internalEmpty)).setId(INTERNAL_EMPTY_ID);
		mList = (ListView) root.findViewById(android.R.id.list);
		mListContainer = root.findViewById(R.id.listContainer);
		mProgressContainer = root.findViewById(R.id.progressContainer);
		mListShown = true;
		if (getArguments().getBoolean(ADD_TRANPARENT_ARG, true)) {
			root.setBackgroundColor(getResources().getColor(R.color.transparent));
		}
		return root;
	}

	public OnFileSelectedListener getOnFileSelectedListener() {
		return onFileSelectedListener;
	}

	public void setOnFileSelectedListener(OnFileSelectedListener fileSelectedListener) {
		this.onFileSelectedListener = fileSelectedListener;
	}

	public OnFolderSelectedListener getOnFolderSelectedListener() {
		return onFolderSelectedListener;
	}

	public void setOnFolderSelectedListener(OnFolderSelectedListener folderSelectedListener) {
		this.onFolderSelectedListener = folderSelectedListener;
	}

}