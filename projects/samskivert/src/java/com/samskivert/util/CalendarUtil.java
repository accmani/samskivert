//
// $Id: CalendarUtil.java,v 1.2 2004/06/09 09:40:04 mdb Exp $

package com.samskivert.util;

import java.util.Calendar;

/**
 * Contains some useful calendar related functions.
 */
public class CalendarUtil
{
    /**
     * Set all the time components of the passed in calendar to zero.
     */
    public static void zeroTime (Calendar cal)
    {
        cal.clear(Calendar.HOUR);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
    }

    /**
     * Returns the difference between the dates represented by the two
     * calendars in days, properly accounting for daylight savings time,
     * leap seconds, etc. The order of the two dates in time does not
     * matter, the absolute number of days between them will be returned.
     *
     * @return the number of days between d1 and d2, 0 if they are the
     * same day.
     *
     * @author http://www.jguru.com/forums/view.jsp?EID=489372
     */
    public static int getDaysBetween (Calendar d1, Calendar d2)
    {
        if (d1.after(d2)) {  // swap dates so that d1 is start and d2 is end
            Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }

        int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
        int y2 = d2.get(Calendar.YEAR);
        if (d1.get(Calendar.YEAR) != y2) {
            d1 = (Calendar)d1.clone();
            do {
                days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
                d1.add(Calendar.YEAR, 1);
            } while (d1.get(Calendar.YEAR) != y2);
        }
        return days;
    }
}
