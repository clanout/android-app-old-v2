package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.List;
import java.util.concurrent.TimeUnit;

import reaper.android.R;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.factory.CreateEventSuggestionFactory;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class DummyActivity extends AppCompatActivity
{
    List<CreateEventModel> models = CreateEventSuggestionFactory.getEventSuggestions();

    TextSwitcher textSwitcher;

    ViewGroup createBox;
    ViewGroup createOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        textSwitcher = (TextSwitcher) findViewById(R.id.tsDummy);
        createBox = (ViewGroup) findViewById(R.id.llCreateEventContainer);
        createOverlay = (ViewGroup) findViewById(R.id.rlCreateOverlay);

        textSwitcher.setFactory(new ViewSwitcher.ViewFactory()
        {

            public View makeView()
            {
                TextView myText = new TextView(DummyActivity.this);
                myText.setTextSize(24);
                myText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                myText.setGravity(Gravity.CENTER_HORIZONTAL);
                myText.setTextColor(ContextCompat
                        .getColor(DummyActivity.this, android.R.color.white));
                return myText;
            }
        });

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        textSwitcher.setInAnimation(in);
        textSwitcher.setOutAnimation(out);

        createOverlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                VisibilityAnimationUtil.collapse(createOverlay, 200);
            }
        });

        final int[] i = {0};
        final int size = models.size();
        Observable.interval(3, TimeUnit.SECONDS)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Subscriber<Long>()
                  {
                      @Override
                      public void onCompleted()
                      {
                          Timber.v("HERE");
                      }

                      @Override
                      public void onError(Throwable e)
                      {
                          e.printStackTrace();
                      }

                      @Override
                      public void onNext(Long aLong)
                      {
                          String title = models.get(i[0]++).getTitle();
                          Timber.v(title);
                          textSwitcher.setText(title);
                          textSwitcher.requestLayout();
                          if (i[0] == size)
                          {
                              i[0] = 0;
                          }
                      }
                  });
    }
}
