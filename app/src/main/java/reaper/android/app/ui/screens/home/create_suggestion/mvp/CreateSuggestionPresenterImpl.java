package reaper.android.app.ui.screens.home.create_suggestion.mvp;

import java.util.List;

import reaper.android.app.model.CreateEventSuggestion;
import reaper.android.app.model.EventCategory;
import reaper.android.app.service.EventService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CreateSuggestionPresenterImpl implements CreateSuggestionPresenter
{
    private EventService eventService;
    private CreateSuggestionView view;

    private List<CreateEventSuggestion> suggestions;

    public CreateSuggestionPresenterImpl(EventService eventService)
    {
        this.eventService = eventService;
    }

    @Override
    public void attachView(CreateSuggestionView view)
    {
        this.view = view;
        fetchSuggestions();
    }

    @Override
    public void detachView()
    {
        view = null;
    }

    @Override
    public void select(EventCategory category)
    {
        view.navigateToCreate(category);
    }

    /* Helper Methods */
    private void fetchSuggestions()
    {
        getObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<CreateEventSuggestion>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(List<CreateEventSuggestion> suggestions)
                    {
                        view.init(suggestions);
                    }
                });
    }

    private Observable<List<CreateEventSuggestion>> getObservable()
    {
        if (suggestions != null && !suggestions.isEmpty())
        {
            return Observable.just(suggestions);
        }
        else
        {
            return eventService._getCreateSuggestions();
        }
    }
}
