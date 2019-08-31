package org.denhac.kiosk.meetup;

import android.os.SystemClock;
import android.util.SparseArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MeetupRepository {
    public static final String DENHAC_HACKERSPACE = "denhac-hackerspace";
    private final MeetupService service;
    private final SparseArray<ReplaySubject<List<Event>>> yearToNewEvents;
    private final SparseArray<Disposable> yearToGetEvents;
    private final SparseArray<Long> yearToExpiredTime;
    private final NetworkStatus networkStatus;

    public MeetupRepository(NetworkStatus networkStatus) {
        this.networkStatus = networkStatus;
        yearToNewEvents = new SparseArray<>();
        yearToGetEvents = new SparseArray<>();
        yearToExpiredTime = new SparseArray<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.meetup.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(MeetupService.class);

        Calendar instance = Calendar.getInstance();
        int currentYear = instance.get(Calendar.YEAR);

        fetchEventsForYear(currentYear);
    }

    public void fetchEventsForYear(int currentYear) {
        Disposable currentDisposable = yearToGetEvents.get(currentYear);
        if (yearToExpiredTime.get(currentYear, 0L) > SystemClock.elapsedRealtime()) {
            return;
        }
        if (currentDisposable != null) {
            currentDisposable.dispose();
        }

        if (yearToNewEvents.get(currentYear) == null) {
            ReplaySubject<List<Event>> subject = ReplaySubject.createWithSize(1);
            yearToNewEvents.put(currentYear, subject);
        }

        final ReplaySubject<List<Event>> replaySubject = yearToNewEvents.get(currentYear);

        Disposable disposable = service
                .events(DENHAC_HACKERSPACE,
                        currentYear + "-01-01T00:00:00.000",
                        (currentYear + 1) + "-01-01T00:00:00.000")
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Event>>() {
                    @Override
                    public void accept(List<Event> events) {
                        networkStatus.notifyStatus(true);
                        replaySubject.onNext(events);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        networkStatus.notifyStatus(false);
                    }
                });

        yearToGetEvents.put(currentYear, disposable);
        yearToExpiredTime.put(currentYear, SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(1));
    }

    public Observable<List<Event>> fetchEvents(Calendar timestamp) {
        fetchEventsForYear(timestamp.get(Calendar.YEAR));

        SimpleDateFormat yearMonthDay = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final String currentDayAsString = yearMonthDay.format(timestamp.getTime());

        return yearToNewEvents.get(timestamp.get(Calendar.YEAR))
                .subscribeOn(Schedulers.io())
                .map(new Function<List<Event>, List<Event>>() {
                    @Override
                    public List<Event> apply(List<Event> events) {
                        List<Event> result = new LinkedList<>();

                        for (Event event : events) {
                            if (event.getLocalDate().equals(currentDayAsString)) {
                                result.add(event);
                            }
                        }

                        return result;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<EventAttendee>> fetchAttendees(Event event) {
        return service.eventAttendees(DENHAC_HACKERSPACE, event.getId())
                .subscribeOn(Schedulers.io());
    }

    public interface NetworkStatus {
        void notifyStatus(boolean online);
    }
}
