package reaper.android.app.model;

import java.util.Comparator;

public class EventComparator
{
    public static class Relevance implements Comparator<Event>
    {
        private static final double FRIEND_COUNT_COEFF = 0.4;
        private static final double INVITER_COUNT_COEFF = 0.6;
        private static final double ORGANISER_CONSTANT = 10;
        private static final double GOING_COEFF = 2;
        private static final double MAYBE_COEFF = 1;

        @Override
        public int compare(Event event, Event event2)
        {
            Double importanceEvent1 = ((FRIEND_COUNT_COEFF * event.getFriendCount()) + (INVITER_COUNT_COEFF * event.getInviterCount())) + getConstant(event);
            Double importanceEvent2 = ((FRIEND_COUNT_COEFF * event2.getFriendCount()) + (INVITER_COUNT_COEFF * event2.getInviterCount())) + getConstant(event2);
            return importanceEvent2.compareTo(importanceEvent1);
        }

        private double getConstant(Event event)
        {
            if (event.getRsvp() == Event.RSVP.YES)
            {
                return GOING_COEFF;
            }
            else if (event.getRsvp() == Event.RSVP.MAYBE)
            {
                return MAYBE_COEFF;
            }
            else
            {
                return 0;
            }
        }
    }

    public static class DateTime implements Comparator<Event>
    {
        @Override
        public int compare(Event event, Event event2)
        {
            return event.getStartTime().compareTo(event2.getStartTime());
        }
    }

    public static class Distance implements Comparator<Event>
    {
        private double longitude;
        private double latitude;

        public Distance(double longitude, double latitude)
        {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        @Override
        public int compare(Event event, Event event2)
        {
            Location location = event.getLocation();
            Double distance = Math.sqrt(Math.pow(location.getLongitude() - longitude, 2) + Math.pow(location.getLatitude() - latitude, 2));

            Location location2 = event2.getLocation();
            Double distance2 = Math.sqrt(Math.pow(location2.getLongitude() - longitude, 2) + Math.pow(location2.getLatitude() - latitude, 2));

            return distance.compareTo(distance2);
        }
    }
}
