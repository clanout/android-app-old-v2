package reaper.android.app.model.util;

import java.util.Comparator;

import reaper.android.app.model.PhonebookContact;

public class PhonebookContactComparator implements Comparator<PhonebookContact>
{
    @Override
    public int compare(PhonebookContact lhs, PhonebookContact rhs)
    {
        return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
    }
}
