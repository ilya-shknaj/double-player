package by.gravity.doublexplayer.installer;

import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import by.gravity.common.utils.FileUtil;
import by.gravity.doublexplayer.installer.fragment.InstallerFragment;
import by.gravity.doublexplayer.installer.model.DoublePlayerModel;

import com.ipaulpro.afilechooser.FileListFragment;
import com.ipaulpro.afilechooser.FileListFragment.OnFileSelectedListener;

public class MainActivity extends TrackingActivity {

	private List<DoublePlayerModel> modelList;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);

		modelList = getApplicationList();
		initInstallerFragment();

	}

	public void onInstallButtonClick(final DoublePlayerModel playerModel) {
		showProgress();
		final ZipFile zipFile = createZipFile(playerModel.getContentInputPath());
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				if (zipFile == null) {
					return false;
				}
				try {
					zipFile.extractAll(playerModel.getContentOutputPath());
				} catch (ZipException e) {
					e.printStackTrace();
					return false;
				}

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				hideProgress();
				String text = result ? getString(R.string.success) : getString(R.string.error);
				Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
			}

		}.execute();

	}

	private ZipFile createZipFile(String zipPath) {
		try {
			return new ZipFile(zipPath);
		} catch (ZipException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void onChooseArhiveFileButtonClick(DoublePlayerModel playerModel) {
		initFileChooserFragment(playerModel);
	}

	private void showProgress() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(getString(R.string.progress_title));
		progressDialog.setMessage(getString(R.string.progress_message));
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	private void hideProgress() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.hide();
		}
	}

	private void initInstallerFragment() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.content, InstallerFragment.newInstance(modelList));
		transaction.commit();
	}

	private void initFileChooserFragment(final DoublePlayerModel playerModel) {
		FileListFragment fileListFragment = FileListFragment.newInstance(Environment.getExternalStorageDirectory().getAbsolutePath(), true, false,
				false);
		fileListFragment.setOnFileSelectedListener(new OnFileSelectedListener() {

			@Override
			public void onFileSelected(String tag, String filePath) {
				filePath = FileUtil.removeFileFromStartPath(filePath);
				playerModel.setContentInputPath(filePath);
				initInstallerFragment();
			}
		});
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.content, fileListFragment);
		transaction.commit();
	}

	private List<DoublePlayerModel> getApplicationList() {
		List<DoublePlayerModel> list = new ArrayList<DoublePlayerModel>();

		addModel(list, R.string.basketball, R.drawable.ic_basletball, "basketball.zip", "by.gravity.doublexplayer.basketball");
		addModel(list, R.string.volleyball, R.drawable.ic_volleyball, "volleyball.zip", "by.gravity.doublexplayer.volleyball");
		addModel(list, R.string.football, R.drawable.ic_football, "football.zip", "by.gravity.doublexplayer.football");
		addModel(list, R.string.athletics, R.drawable.ic_athletics, "athletics.zip", "by.gravity.doublexplayer.athletics");
		addModel(list, R.string.aerobics, R.drawable.ic_aerobics, "aerobics.zip", "by.gravity.doublexplayer.aerobics");
		addModel(list, R.string.gymnastics, R.drawable.ic_gymnastics, "gymnastics.zip", "by.gravity.doublexplayer.gymnastics");
		addModel(list, R.string.tourism, R.drawable.ic_tourism, "tourism.zip", "by.gravity.doublexplayer.tourism");
		addModel(list, R.string.obstacle, R.drawable.ic_obstacle, "obstacle.zip", "by.gravity.doublexplayer.obstacle");

		return list;
	}

	private void addModel(List<DoublePlayerModel> list, int appName, int recourse, String inputPath, String outputPath) {
		DoublePlayerModel model = new DoublePlayerModel();
		model.setAppName(getString(appName));
		model.setIconRecourse(recourse);
		model.setContentInputPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + inputPath);
		model.setContentOutputPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + outputPath + "/files");
		list.add(model);
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().findFragmentById(R.id.content) instanceof FileListFragment) {
			initInstallerFragment();
		} else {
			super.onBackPressed();
		}

	}

}
