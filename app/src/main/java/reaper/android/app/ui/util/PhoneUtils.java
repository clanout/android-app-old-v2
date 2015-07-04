package reaper.android.app.ui.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import reaper.android.app.config.AppConstants;

public class PhoneUtils {
    private static PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public static String sanitize(String number, String defaultCountryCode) {
        String phone = parsePhone(number, defaultCountryCode);
        if (phone == null) {
            return number;
        } else {
            return phone;
        }
    }

    public static String parsePhone(String number, String countryCode) {
        try {
            String regionCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parseAndKeepRawInput(number, regionCode);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("+");
                stringBuilder.append(phoneNumber.getCountryCode());
                stringBuilder.append(phoneNumber.getNationalNumber());
                return stringBuilder.toString();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String temp(String number, String countryCode) {
        number = number.replaceAll(" ", "");
        number = number.replaceAll("-", "");
        if (number.charAt(0) == '+') {
            return number;
        } else if (number.charAt(0) == '0') {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("+");
            stringBuilder.append(countryCode);
            stringBuilder.append(number.substring(1));
            return stringBuilder.toString();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("+");
            stringBuilder.append(countryCode);
            stringBuilder.append(number);
            return stringBuilder.toString();
        }
    }

    public static String getCountryCode(String number) {
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parseAndKeepRawInput(number, phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(AppConstants.DEFAULT_COUNTRY_CODE)));
            if (phoneNumber.getCountryCodeSource() == Phonenumber.PhoneNumber.CountryCodeSource.FROM_DEFAULT_COUNTRY) {
                return null;
            } else {
                return String.valueOf(phoneNumber.getCountryCode());
            }
        } catch (NumberParseException e) {

        }
        return null;
    }

    public static String getNumber(String number) {
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parseAndKeepRawInput(number, "IN");
            return String.valueOf(phoneNumber.getNationalNumber());
        } catch (NumberParseException e) {

        }
        return null;
    }
}