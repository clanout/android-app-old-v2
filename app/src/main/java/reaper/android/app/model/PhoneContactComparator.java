package reaper.android.app.model;

import java.util.Comparator;

/**
 * Created by harsh on 26/09/15.
 */
public class PhoneContactComparator implements Comparator<PhoneContact> {
    @Override
    public int compare(PhoneContact lhs, PhoneContact rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }
}