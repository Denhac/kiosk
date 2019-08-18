package org.denhac.kiosk;

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
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class MeetupRepository {
    private final MeetupService service;
    private final ReplaySubject<List<Event>> newEvents;
    private final SparseArray<Disposable> yearToGetEvents;
    private final SparseArray<Long> yearToExpiredTime;

    public MeetupRepository() {
        newEvents = ReplaySubject.createWithSize(1);
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
        if(currentDisposable != null) {
            currentDisposable.dispose();
        }

        Disposable disposable = service
                .events("denhac-hackerspace",
                        currentYear + "-01-01T00:00:00.000",
                        (currentYear + 1) + "-01-01T00:00:00.000")
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Event>>() {
                    @Override
                    public void accept(List<Event> events) {
                        newEvents.onNext(events);
                    }
                });

        yearToGetEvents.put(currentYear, disposable);
        yearToExpiredTime.put(currentYear, SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(1));
    }

    public Observable<List<Event>> fetchEvents(Calendar timestamp) {
        fetchEventsForYear(timestamp.get(Calendar.YEAR));

        SimpleDateFormat yearMonthDay = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final String currentDayAsString = yearMonthDay.format(timestamp.getTime());

        return newEvents
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
}

interface MeetupService {
    @GET("{group}/events?status=past,upcoming")
    Single<List<Event>> events(@Path("group") String groupName,
                               @Query("no_earlier_than") String noEarlierThan,
                               @Query("no_later_than") String noLaterThan);
}

