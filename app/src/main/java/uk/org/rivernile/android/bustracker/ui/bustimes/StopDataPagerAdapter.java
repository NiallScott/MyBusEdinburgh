package uk.org.rivernile.android.bustracker.ui.bustimes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment;
import uk.org.rivernile.android.bustracker.ui.bustimes.times.BusTimesFragment;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link FragmentPagerAdapter} provides the pages and tabs for
 * {@link DisplayStopDataActivity2}.
 *
 * @author Niall Scott
 */
class StopDataPagerAdapter extends FragmentPagerAdapter {

    private final Context context;
    private final String stopCode;

    /**
     * Create a new {@code StopDataPagerAdapter}.
     *
     * @param context A {@link Context} instance.
     * @param fragmentManager The {@link FragmentManager}.
     * @param stopCode The stop code for this bus stop.
     */
    StopDataPagerAdapter(@NonNull final Context context,
            @NonNull final FragmentManager fragmentManager, @NonNull final String stopCode) {
        super(fragmentManager);

        this.context = context;
        this.stopCode = stopCode;
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 0:
                return BusTimesFragment.newInstance(stopCode);
            case 1:
                return StopDetailsFragment.newInstance(stopCode);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.displaystopdata_tab_times);
            case 1:
                return context.getString(R.string.displaystopdata_tab_details);
            default:
                return null;
        }
    }
}