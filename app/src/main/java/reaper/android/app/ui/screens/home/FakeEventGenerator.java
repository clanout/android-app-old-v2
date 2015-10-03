package reaper.android.app.ui.screens.home;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import rx.Observable;

public class FakeEventGenerator
{
    public static Observable<List<Event>> getEvents()
    {
        List<Event> events = new ArrayList<>();

        Event event1 = new Event();
        event1.setId("1");
        event1.setCategory("GENERAL");
        event1.setChatId("1");
        event1.setEndTime(DateTime.now().plusDays(2));
        event1.setFriendCount(3);
        event1.setInviterCount(2);
        event1.setIsChatUpdated(true);
        event1.setIsFinalized(false);
        event1.setIsUpdated(true);
        event1.setLastUpdated(DateTime.now());
        event1.setOrganizerId("2");
        event1.setRsvp(Event.RSVP.YES);
        event1.setStartTime(DateTime.now());
        event1.setTitle("Sutta");
        event1.setType(Event.Type.PUBLIC);

        Location location = new Location();
        location.setZone("Bengaluru");

        event1.setLocation(location);


        Event event2 = new Event();
        event2.setId("2");
        event2.setCategory("EAT_OUT");
        event2.setChatId("2");
        event2.setEndTime(DateTime.now().plusDays(2));
        event2.setFriendCount(3);
        event2.setInviterCount(2);
        event2.setIsChatUpdated(true);
        event2.setIsFinalized(false);
        event2.setIsUpdated(true);
        event2.setLastUpdated(DateTime.now());
        event2.setOrganizerId("2");
        event2.setRsvp(Event.RSVP.NO);
        event2.setStartTime(DateTime.now());
        event2.setTitle("Dinner");
        event2.setType(Event.Type.PUBLIC);

        Location location2 = new Location();
        location2.setZone("Bengaluru");

        event2.setLocation(location2);

        Event event3 = new Event();
        event3.setId("3");
        event3.setCategory("DRINKS");
        event3.setChatId("3");
        event3.setEndTime(DateTime.now().plusDays(2));
        event3.setFriendCount(3);
        event3.setInviterCount(2);
        event3.setIsChatUpdated(true);
        event3.setIsFinalized(false);
        event3.setIsUpdated(true);
        event3.setLastUpdated(DateTime.now());
        event3.setOrganizerId("2");
        event3.setRsvp(Event.RSVP.MAYBE);
        event3.setStartTime(DateTime.now().plusDays(1));
        event3.setTitle("Beer Party");
        event3.setType(Event.Type.PUBLIC);

        Location location3 = new Location();
        location3.setZone("Bengaluru");

        event3.setLocation(location3);

        Event event4 = new Event();
        event4.setId("4");
        event4.setCategory("CAFE");
        event4.setChatId("4");
        event4.setEndTime(DateTime.now().plusDays(1));
        event4.setFriendCount(3);
        event4.setInviterCount(2);
        event4.setIsChatUpdated(true);
        event4.setIsFinalized(false);
        event4.setIsUpdated(true);
        event4.setLastUpdated(DateTime.now());
        event4.setOrganizerId("2");
        event4.setRsvp(Event.RSVP.YES);
        event4.setStartTime(DateTime.now().plusDays(1));
        event4.setTitle("Coffee");
        event4.setType(Event.Type.PUBLIC);

        Location location4 = new Location();
        location4.setZone("Bengaluru");

        event4.setLocation(location4);


        Event event5 = new Event();
        event5.setId("5");
        event5.setCategory("MOVIES");
        event5.setChatId("5");
        event5.setEndTime(DateTime.now().plusDays(2));
        event5.setFriendCount(3);
        event5.setInviterCount(2);
        event5.setIsChatUpdated(true);
        event5.setIsFinalized(false);
        event5.setIsUpdated(true);
        event5.setLastUpdated(DateTime.now());
        event5.setOrganizerId("2");
        event5.setRsvp(Event.RSVP.YES);
        event5.setStartTime(DateTime.now().plusDays(2));
        event5.setTitle("Star Wars");
        event5.setType(Event.Type.PUBLIC);

        Location location5 = new Location();
        location5.setZone("Bengaluru");

        event5.setLocation(location5);


        Event event6 = new Event();
        event6.setId("6");
        event6.setCategory("OUTDOORS");
        event6.setChatId("6");
        event6.setEndTime(DateTime.now().plusDays(2));
        event6.setFriendCount(3);
        event6.setInviterCount(2);
        event6.setIsChatUpdated(true);
        event6.setIsFinalized(false);
        event6.setIsUpdated(true);
        event6.setLastUpdated(DateTime.now());
        event6.setOrganizerId("2");
        event6.setRsvp(Event.RSVP.MAYBE);
        event6.setStartTime(DateTime.now().plusDays(2));
        event6.setTitle("Long Drive");
        event6.setType(Event.Type.PUBLIC);

        Location location6 = new Location();
        location6.setZone("Bengaluru");

        event6.setLocation(location6);


        Event event7 = new Event();
        event7.setId("7");
        event7.setCategory("PARTY");
        event7.setChatId("7");
        event7.setEndTime(DateTime.now().plusDays(2));
        event7.setFriendCount(3);
        event7.setInviterCount(2);
        event7.setIsChatUpdated(true);
        event7.setIsFinalized(false);
        event7.setIsUpdated(true);
        event7.setLastUpdated(DateTime.now());
        event7.setOrganizerId("2");
        event7.setRsvp(Event.RSVP.MAYBE);
        event7.setStartTime(DateTime.now().plusDays(3));
        event7.setTitle("House Party");
        event7.setType(Event.Type.PUBLIC);

        Location location7 = new Location();
        location7.setZone("Bengaluru");

        event7.setLocation(location7);


        Event event8 = new Event();
        event8.setId("8");
        event8.setCategory("LOCAL_EVENTS");
        event8.setChatId("8");
        event8.setEndTime(DateTime.now().plusDays(2));
        event8.setFriendCount(3);
        event8.setInviterCount(2);
        event8.setIsChatUpdated(true);
        event8.setIsFinalized(false);
        event8.setIsUpdated(true);
        event8.setLastUpdated(DateTime.now());
        event8.setOrganizerId("2");
        event8.setRsvp(Event.RSVP.NO);
        event8.setStartTime(DateTime.now().plusMinutes(5));
        event8.setTitle("Concert");
        event8.setType(Event.Type.PUBLIC);

        Location location8 = new Location();
        location8.setZone("Bengaluru");

        event8.setLocation(location8);


        Event event9 = new Event();
        event9.setId("9");
        event9.setCategory("SHOPPING");
        event9.setChatId("9");
        event9.setEndTime(DateTime.now().plusDays(2));
        event9.setFriendCount(3);
        event9.setInviterCount(2);
        event9.setIsChatUpdated(true);
        event9.setIsFinalized(false);
        event9.setIsUpdated(true);
        event9.setLastUpdated(DateTime.now());
        event9.setOrganizerId("2");
        event9.setRsvp(Event.RSVP.YES);
        event9.setStartTime(DateTime.now().plusDays(4));
        event9.setTitle("Window Shopping with me at Lifestyle Mall in Sony Signal");
        event9.setType(Event.Type.PUBLIC);

        Location location9 = new Location();
        location9.setZone("Bengaluru");

        event9.setLocation(location9);

        events.add(event1);
        events.add(event2);
        events.add(event3);
        events.add(event4);
        events.add(event5);
        events.add(event6);
        events.add(event7);
        events.add(event8);
        events.add(event9);

        return Observable.just(events);
    }
}
