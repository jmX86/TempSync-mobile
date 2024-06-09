package hr.com.in.tempsync.ui.connections;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConnectionsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ConnectionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}