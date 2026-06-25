package com.revaplayer.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.revaplayer.R;
import com.revaplayer.domain.model.Bookmark;

public class BookmarkDialog extends Dialog {

    public interface OnSave { void onSave(Bookmark bookmark); }

    private final String source;
    private final String videoTitle;
    private final double positionSeconds;
    private final OnSave onSave;

    public BookmarkDialog(Context context, String source, String videoTitle,
                          double positionSeconds, OnSave onSave) {
        super(context, R.style.RevaDialog);
        this.source = source;
        this.videoTitle = videoTitle;
        this.positionSeconds = positionSeconds;
        this.onSave = onSave;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_bookmark);

        TextView tvPosition = findViewById(R.id.tvBookmarkDialogPosition);
        EditText etTitle = findViewById(R.id.etBookmarkTitle);
        EditText etNote = findViewById(R.id.etBookmarkNote);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        Button btnSave = findViewById(R.id.btnSaveBookmark);
        Button btnCancel = findViewById(R.id.btnCancelBookmark);

        // عرض الوقت الحالي
        tvPosition.setText("⏱ " + formatTime((long) positionSeconds));

        // الفئات
        String[] categories = {"عام", "مهم", "للمراجعة", "مفضل"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) title = videoTitle;
            String note = etNote.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            Bookmark b = new Bookmark(source, title, positionSeconds, note, category);
            onSave.onSave(b);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private String formatTime(long s) {
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, sec);
        return String.format("%d:%02d", m, sec);
    }
}
