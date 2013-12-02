package by.gravity.doublexplayer.installer.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import by.gravity.doublexplayer.installer.R;
import by.gravity.doublexplayer.installer.adapter.PlayerAdapter;
import by.gravity.doublexplayer.installer.model.DoublePlayerModel;

public class InstallerFragment extends Fragment {

	private static InstallerFragment instance;

	private static List<DoublePlayerModel> list;

	public static InstallerFragment newInstance(List<DoublePlayerModel> list) {
		if (instance == null) {
			instance = new InstallerFragment();
		}

		InstallerFragment.list = list;

		return instance;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView listView = (ListView) getView().findViewById(R.id.list);
		PlayerAdapter adapter = new PlayerAdapter(getActivity(), R.layout.item, list);
		listView.setAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.f_installer, null);
	}

}
