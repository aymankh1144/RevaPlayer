package com.revaplayer.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.revaplayer.ui.activities.BookmarksFragment;
import com.revaplayer.ui.activities.HistoryFragment;
import com.revaplayer.ui.activities.HomeFragment;
import com.revaplayer.ui.activities.SettingsFragment;

public class HomeTabAdapter extends FragmentStateAdapter {

    public HomeTabAdapter(FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new HistoryFragment();
            case 2: return new BookmarksFragment();
            case 3: return new SettingsFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() { return 4; }
}
