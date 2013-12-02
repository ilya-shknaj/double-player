package by.gravity.doublexplayer.installer.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import by.gravity.doublexplayer.installer.MainActivity;
import by.gravity.doublexplayer.installer.R;
import by.gravity.doublexplayer.installer.model.DoublePlayerModel;

public class PlayerAdapter extends ArrayAdapter<DoublePlayerModel> {

	private File file;

	public PlayerAdapter(Context context, int resource, List<DoublePlayerModel> list) {
		super(context, resource, 0, list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final DoublePlayerModel model = getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, null);
		}
		ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
		imageView.setImageDrawable(getContext().getResources().getDrawable(model.getIconRecourse()));

		Button installButton = (Button) convertView.findViewById(R.id.intallButton);
		installButton.setText(model.getAppName());
		installButton.setEnabled(fileFounded(model.getContentInputPath()));

		installButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getContext()).onInstallButtonClick(model);
			}
		});
		
		Button chooseFileButton = (Button) convertView.findViewById(R.id.changePathButton);
		chooseFileButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((MainActivity) getContext()).onChooseArhiveFileButtonClick(model);
			}
		});

		return convertView;
	}

	private boolean fileFounded(String path) {
		file = new File(path);
		return file.exists();
	}

}
