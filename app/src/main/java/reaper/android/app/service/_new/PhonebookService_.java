package reaper.android.app.service._new;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.app.config.AppConstants;
import reaper.android.app.model.PhonebookContact;
import reaper.android.app.model.util.PhonebookContactComparator;
import reaper.android.app.ui.util.PhoneUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PhonebookService_
{
    private static PhonebookService_ instance;

    public static void init(Context context)
    {
        instance = new PhonebookService_(context);
    }

    public static PhonebookService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[PhonebookService Not Initialized]");
        }

        return instance;
    }

    private Context context;

    private PhonebookService_(Context context)
    {
        this.context = context;
    }

    public boolean isReadContactsPermissionGranted()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat
                .checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public Observable<List<String>> fetchAllNumbers()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<String>>()
                {
                    @Override
                    public void call(Subscriber<? super List<String>> subscriber)
                    {
                        try
                        {
                            List<String> allContacts = new ArrayList<>();

                            String defaultCountryCode = AppConstants.DEFAULT_COUNTRY_CODE;
                            String[] PROJECTION = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                            Cursor cur = context
                                    .getContentResolver()
                                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);

                            if (cur.moveToFirst())
                            {
                                do
                                {
                                    String phone = PhoneUtils
                                            .sanitize(cur.getString(0), defaultCountryCode);
                                    allContacts.add(phone);
                                } while (cur.moveToNext());
                            }
                            cur.close();

                            subscriber.onNext(allContacts);
                            subscriber.onCompleted();
                        }
                        catch (Exception e)
                        {
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<PhonebookContact>> fetchAllContacts()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<PhonebookContact>>()
                {
                    @Override
                    public void call(Subscriber<? super List<PhonebookContact>> subscriber)
                    {
                        try
                        {
                            List<PhonebookContact> contacts = new ArrayList<>();
                            Cursor cursor = context
                                    .getContentResolver()
                                    .query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

                            String id = null;
                            String name = null;
                            String phone = null;
                            String sanitizedPhone = null;

                            if (cursor.getCount() > 0)
                            {
                                while (cursor.moveToNext())
                                {
                                    id = cursor.getString(cursor
                                            .getColumnIndex(ContactsContract.Contacts._ID));
                                    name = cursor.getString(cursor
                                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                    if (Integer.parseInt(cursor.getString(cursor
                                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                                    {

                                        Cursor smallCursor =
                                                context.getContentResolver()
                                                       .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                                               ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                                               new String[]{id}, null);


                                        while (smallCursor.moveToNext())
                                        {
                                            phone = smallCursor.getString(smallCursor
                                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                            sanitizedPhone = PhoneUtils
                                                    .sanitize(phone, AppConstants.DEFAULT_COUNTRY_CODE);

                                            PhonebookContact contact = new PhonebookContact();
                                            contact.setName(name);
                                            contact.setPhone(sanitizedPhone);
                                            if (!contacts.contains(contact))
                                            {
                                                contacts.add(contact);
                                            }
                                        }
                                        smallCursor.close();
                                    }
                                }
                                cursor.close();

                                subscriber.onNext(contacts);
                                subscriber.onCompleted();
                            }
                        }
                        catch (Exception e)
                        {
                            subscriber.onError(e);
                        }
                    }
                })
                .map(new Func1<List<PhonebookContact>, List<PhonebookContact>>()
                {
                    @Override
                    public List<PhonebookContact> call(List<PhonebookContact> phonebookContacts)
                    {
                        List<PhonebookContact> filtered = new ArrayList<PhonebookContact>();
                        for(PhonebookContact phonebookContact:phonebookContacts)
                        {
                            if(!phonebookContact.getName().equalsIgnoreCase("Identified As Spam"))
                            {
                                filtered.add(phonebookContact);
                            }
                        }

                        Collections.sort(filtered, new PhonebookContactComparator());
                        return filtered;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }
}