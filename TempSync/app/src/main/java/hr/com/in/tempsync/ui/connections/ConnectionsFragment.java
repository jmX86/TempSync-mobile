package hr.com.in.tempsync.ui.connections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import hr.com.in.tempsync.databinding.FragmentConnectionsBinding;

public class ConnectionsFragment extends Fragment {

    private FragmentConnectionsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConnectionsViewModel connectionsViewModel =
                new ViewModelProvider(this).get(ConnectionsViewModel.class);

        binding = FragmentConnectionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;

        textView.setText("Coming soon");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}