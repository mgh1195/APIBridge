package com.wrapper.apibridge.service;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import org.springframework.stereotype.Service;

@Service
public class PersianUtilsService {
    public String convertTextNumbersToPersian(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                chars[i] = (char) ('۰' + (chars[i] - '0'));
            }
        }
        return new String(chars);
    }

    public String getCurrentPersianDateString(String pattern) {
        Calendar persianCal = Calendar.getInstance(new ULocale("fa_IR@calendar=persian"));
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, new ULocale("fa_IR@calendar=persian"));
        return sdf.format(persianCal.getTime());
    }

    public String formatPersianRials(long amount) {
        NumberFormat nf = NumberFormat.getInstance(new ULocale("fa_IR"));
        return nf.format(amount);
    }
}
