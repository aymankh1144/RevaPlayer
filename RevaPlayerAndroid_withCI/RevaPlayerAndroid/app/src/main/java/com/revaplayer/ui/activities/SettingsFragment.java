package com.revaplayer.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.revaplayer.R;
import com.revaplayer.data.database.RevaDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private RevaDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = RevaDatabase.getInstance(requireContext());

        // Auto Resume
        SwitchCompat switchAutoResume = view.findViewById(R.id.switchAutoResume);
        boolean autoResume = db.getSetting("auto_resume", "true").equals("true");
        switchAutoResume.setChecked(autoResume);
        switchAutoResume.setOnCheckedChangeListener((btn, checked) ->
                db.setSetting("auto_resume", checked ? "true" : "false"));

        // PiP
        SwitchCompat switchPip = view.findViewById(R.id.switchPip);
        boolean pip = db.getSetting("pip_enabled", "true").equals("true");
        switchPip.setChecked(pip);
        switchPip.setOnCheckedChangeListener((btn, checked) ->
                db.setSetting("pip_enabled", checked ? "true" : "false"));

        // Reset all data
        View btnReset = view.findViewById(R.id.btnResetData);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> showResetDialog());
        }

        // App version
        TextView tvVersion = view.findViewById(R.id.tvVersion);
        if (tvVersion != null) {
            tvVersion.setText("Reva Player v1.0.0 · Android");
        }
    }

    private void showResetDialog() {
        new AlertDialog.Builder(requireContext(), R.style.RevaDialog)
                .setTitle("إعادة تعيين البيانات")
                .setMessage("سيتم حذف كل السجل والعلامات والإعدادات. هذا الإجراء لا يمكن التراجع عنه.")
                .setPositiveButton("إعادة تعيين", (d, w) -> {
                    executor.execute(() -> {
                        db.resetAll();
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "تم مسح جميع البيانات", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
