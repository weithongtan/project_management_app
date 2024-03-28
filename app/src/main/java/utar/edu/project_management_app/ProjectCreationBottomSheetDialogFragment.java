package utar.edu.project_management_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectCreationBottomSheetDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectCreationBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_project_creation, container, false);
    }
}